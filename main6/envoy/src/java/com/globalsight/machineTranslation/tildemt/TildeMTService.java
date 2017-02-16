package com.globalsight.machineTranslation.tildemt;

import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import org.apache.commons.lang.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * A class to manage interaction with the Tilde MT JSON API. Only the translation request is implemented since all the
 * other functionality, like translation system list retrieval, system's terminology list retrieval, etc., is managed
 * in the frontend via the Tilde MT system selection web component. This class then uses only the authentication
 * credentials and a list of API request parameters, both retrieved by the web component. The request parameter list
 * contains all of the necessary information identifying a translation system and its configuration (terms, quality
 * estimation, etc.).
 * <p>
 * Created by rihards.krislauks@tilde.lv on 26.09.2016.
 */
public class TildeMTService {
    private static final Logger logger =
            Logger.getLogger(TildeMTService.class);
    private String baseUrl;
    private String translatePath = "/TranslateEx";
    private String key;

    /**
     * Creates a TildeMTService instance to make translation requests to the Tilde MT API
     *
     * @param url The JSON API endpoint to use. If {@code url == null} then the default endpoint at
     *            https://www.letsmt.eu/ws/service.svc/json is used.
     * @param key The ClientID string on the Tilde MT platform to authenticate the API requests.
     * @throws URISyntaxException when an invalid {@code url} is supplied
     */
    public TildeMTService(String url, String key) throws URISyntaxException
    {
        if (url == null || url.isEmpty())
        {
            url = "https://www.letsmt.eu/ws/service.svc/json";
        }
        this.baseUrl = StringUtils.stripEnd(url.trim(), "/").trim();
        this.key = key;
    }

    /**
     * Reads line by line from a {@link BufferedReader} and returns the concatenated result.
     *
     * @param reader from where to read
     * @return the concatenated lines retrieved from the {@code reader}
     * @throws IOException
     */
    private String readFromBuffer(BufferedReader reader) throws IOException{
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    /**
     * Makes a translation request to the Tilde MT JSON API endpoint.
     *
     * @param text to translate
     * @param requestParams the GET query parameters for the request indicating the translation system, terminology
     *                      selection, etc., as prepared by the Tilde MT system selection web component (frontend).
     * @return the translated text and it's Quality Estimation score (can be null if QE not available/disabled).
     * @throws MachineTranslationException
     */
    public TranslateResult Translate(String text, List<NameValuePair> requestParams)
            throws MachineTranslationException
    {
        requestParams.add(new BasicNameValuePair("text", text));
        URI uri;
        try {
            uri = new URIBuilder(this.baseUrl + this.translatePath)
                    .addParameters(requestParams)
                    .build();
        } catch (URISyntaxException e) {
            String message = String.format("Invalid webservice URL format - \"%1s\", params: %2s",
                    this.baseUrl + this.translatePath, requestParams.toString());
            throw new MachineTranslationException(message, e);
        }
        HttpGet request = new HttpGet(uri);
        request.setHeader("client-id", this.key);
        return makeRequest(request);
    }

    /**
     * Performs a Http GET request, parses the response JSON, returns the translation.
     *
     * @param request a pre-prepared GET request to perform
     * @return translation
     * @throws MachineTranslationException the translation request can fail for various reasons related to the current
     * status of the translation system or wrong request parameters. In such an event the JSON response from the
     * Tilde MT API contains the error code and message which is then wrapped in an exception. This should be shown
     * to the user as it often indicates the further actions for her to take, i.e., wait for the system to wake up, go
     * to the Tilde MT web platform to turn the system on, etc.
     */
    private TranslateResult makeRequest(HttpGet request) throws MachineTranslationException
    {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(getMaxWaitTime() * 1000).build();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()){
            HttpResponse response = httpClient.execute(request);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() != HttpStatus.SC_OK) {
                String message;
                if (status.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    message = String.format("Invalid Client ID (%1s)", status.getReasonPhrase());
                } else {
                    BufferedReader rd = new BufferedReader(
                            new InputStreamReader(response.getEntity().getContent()));
                    String json = readFromBuffer(rd);
                    try {
                        message = jsonToErrorMessage(json);
                    } catch (JSONException e){
                        message = String.format("Translation request failed (%1s)", status.getReasonPhrase());
                    }
                }
                throw new MachineTranslationException(message);
            }
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String json = readFromBuffer(rd);
            return jsonToTranslateResult(json);
        } catch (IOException | JSONException e){
            throw new MachineTranslationException(e);
        }
    }

    /**
     * Parse the response JSON form Tilde MT to retrieve the translation.
     *
     * @param jsonText translation request response JSON
     * @return the translated text and it's Quality Estimation score (can be null if QE not available/disabled).
     * @throws JSONException
     * @throws MachineTranslationException
     */
    private TranslateResult jsonToTranslateResult(String jsonText)
            throws JSONException, MachineTranslationException {
        JSONObject json = new JSONObject(jsonText);
        TranslateResult res = new TranslateResult();
        res.Translation = json.getString("translation");
        try {
            res.QeScore = json.getDouble("qualityEstimate");
        } catch(JSONException e) {
            res.QeScore = null;
        }
        return res;
    }

    /**
     * Parse the response JSON from Tilde MT to retrieve the error message.
     *
     * @param jsonText translation request response JSON
     * @return the extracted error message
     * @throws JSONException
     */
    private String jsonToErrorMessage(String jsonText)
            throws JSONException{
        JSONObject json = new JSONObject(jsonText);
        return String.format("%1s (%2s)",
                json.getString("ErrorMessage"), json.getInt("ErrorCode"));
    }

    /**
     * Gets the maximum time to wait for the translation request to finish
     *
     * @return time in seconds
     */
    private static int getMaxWaitTime()
    {
        // Default 15 minutes(900 seconds).
        int maxWaitTime = 900;
        try
        {
            String param = MTHelper.getMTConfig("tildemt.max.wait.timeout");
            if (param != null)
            {
                maxWaitTime = Integer.parseInt(param);
            }
        }
        catch (Exception ignore)
        {

        }
        return maxWaitTime;
    }
}
