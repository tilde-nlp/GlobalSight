package com.globalsight.machineTranslation.tildemt;

import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.NameValuePair;
import org.apache.http.HttpResponse;
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
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rihards.krislauks@tilde.lv on 26.09.2016.
 */
public class TildeMTService {
    private static final Logger logger =
            Logger.getLogger(TildeMTService.class);
    private String clientVersion;
    private String client;
    private String appId;
    private URIBuilder uriBuilder;
    private String key;

    public TildeMTService(String url, String key, String appId, String client, String clientVersion) throws URISyntaxException {
        this.uriBuilder = new URIBuilder(url);
        this.key = key;
        this.appId = appId;
        this.client = client;
        this.clientVersion = clientVersion;
    }

    private String readFromBuffer(BufferedReader reader) throws IOException{
        StringBuilder result = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private Boolean isNullOrEmpty(String str){
        return str == null || str.isEmpty();
    }

    private String getOptions(String termsCorporaId, Boolean useQE)
    {
        List<String> options = new LinkedList<>();
        if (!isNullOrEmpty(this.client) && !isNullOrEmpty(this.clientVersion))
        {
            options.add(String.format("client=%1$s", client));
            options.add(String.format("version=%1$s", clientVersion));
        }
        if (!isNullOrEmpty(termsCorporaId))
            options.add(String.format("termCorpusId=%1$s", termsCorporaId));
        if (useQE)
            options.add("qe");

        return String.join(",", options);
    }

    private List<NameValuePair> getParams(String text, String systemId, String termsCorporaId, Boolean useQE){
        List<NameValuePair> params = new LinkedList<>();
        params.add(new BasicNameValuePair("systemId", systemId));
        params.add(new BasicNameValuePair("appID", this.appId));
        params.add(new BasicNameValuePair("text", text));
        String options = getOptions(termsCorporaId, useQE);
        params.add(new BasicNameValuePair("options", options));
        return params;
    }

    private TranslateResult jsonToTranslateResult(String jsonText) throws JSONException{
        JSONObject json = new JSONObject(jsonText);
        TranslateResult res = new TranslateResult();
        res.Translation = json.getString("translation");
        res.QeScore = json.getDouble("qualityEstimate");
        return res;
    }

    public TranslateResult Translate(String text, String systemId, String termsCorporaId, Boolean useQE)
    {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(getMaxWaitTime() * 1000).build();
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()){
            List<NameValuePair> params = getParams(text, systemId, termsCorporaId, useQE);
            URI uri = this.uriBuilder
                    .addParameters(params)
                    .build();
            HttpGet request = new HttpGet(uri);
            request.setHeader("client-id", this.key);
            HttpResponse response = httpClient.execute(request);
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String json = readFromBuffer(rd);
            return jsonToTranslateResult(json);
        } catch (IOException | URISyntaxException | JSONException e){

        }
        throw new NotImplementedException();
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
