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
 * Created by rihards.krislauks@tilde.lv on 26.09.2016.
 */
public class TildeMTService {
    private static final Logger logger =
            Logger.getLogger(TildeMTService.class);
    private String baseUrl;
    private String translatePath = "/TranslateEx";
    private String key;

    public TildeMTService(String url, String key) throws URISyntaxException
    {
        if (url == null || url.isEmpty())
        {
            url = "https://www.letsmt.eu/ws/service.svc/json";
        }
        this.baseUrl = StringUtils.stripEnd(url.trim(), "/").trim();
        this.key = key;
    }

    private String readFromBuffer(BufferedReader reader) throws IOException{
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

//    private Boolean isNullOrEmpty(String str){
//        return str == null || str.isEmpty();
//    }

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

    private String jsonToErrorMessage(String jsonText)
            throws JSONException{
        JSONObject json = new JSONObject(jsonText);
        return String.format("%1s (%2s)",
                json.getString("ErrorMessage"), json.getInt("ErrorCode"));
    }


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
