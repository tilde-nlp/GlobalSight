package com.globalsight.machineTranslation.tildemt;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by rihards.krislauks@tilde.lv on 26.09.2016.
 */
public class TildeMTProxy extends AbstractTranslator {
    /**
     * Returns the MT engine name.
     *
     * @return name
     */
    @Override
    public String getEngineName() {
        return ENGINE_TILDEMT;
    }

    /**
     * Returns true if the given locale pair is supported for MT.
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @return true | false
     * @throws MachineTranslationException
     */
    @Override
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale)
            throws MachineTranslationException {
        return true;
    }

    @Override
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
            throws MachineTranslationException {
        Map paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap
                .get(MachineTranslator.MT_PROFILE);
        String json = mtProfile.getJsonInfo();
        JSONObject obj;
        String clientId;
        List<NameValuePair> requestParams;
        try
        {
            obj = new JSONObject(json);
            clientId = obj.getString("client-id");
            JSONObject systems = obj.getJSONObject("systems");
            String langKey = String.format("%1$s-%2$s",
                    p_sourceLocale.getLanguage(), p_targetLocale.getLanguage());
            // There might be no system selected for this language pair
            // but it is expected that supportsLocalePair() has returned
            // false for it and doTranslation does not have to worry about it.
            JSONObject system = systems.getJSONObject(langKey);
            JSONObject ssytemParams = system.getJSONObject("params");
            requestParams = jsonObjectToList(ssytemParams);
        } catch (JSONException e)
        {
            throw new MachineTranslationException(e);
        }

        TildeMTService service;
        try {
            service = new TildeMTService(
                    mtProfile.getUrl(),
                    clientId
            );
        } catch (URISyntaxException e){
            throw new MachineTranslationException(e);
        }
        TranslateResult res = service.Translate(p_string, requestParams);

        return res.Translation;
    }

    private List<NameValuePair> jsonObjectToList(JSONObject jso)
    {
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        Iterator<?> keys = jso.keys();
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            Object item;
            try {
                item = jso.get(key);
            } catch (JSONException e) { throw new Error(e); }  // This should newer happen
            params.add(new BasicNameValuePair("systemId", item.toString()));
        }
        return params;
    }

    public boolean TestHost() {
        try {
            doTranslation(null, null, "hello");
        } catch (Exception e){
            return false;
        }
        return true;
    }
}
