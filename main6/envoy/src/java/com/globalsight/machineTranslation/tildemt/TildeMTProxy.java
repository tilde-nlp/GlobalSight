package com.globalsight.machineTranslation.tildemt;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
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

    protected String createLangKey(Locale p_sourceLocale, Locale p_targetLocale)
    {
        return String.format("%1$s-%2$s",
                p_sourceLocale.getLanguage(), p_targetLocale.getLanguage());
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
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale) {
        Map paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap
                .get(MachineTranslator.MT_PROFILE);
        String json = mtProfile.getJsonInfo();
        try
        {
            JSONObject obj = new JSONObject(json);
            // if the params don't exist json exception will be thrown
            getParamsFromJson(p_sourceLocale, p_targetLocale, obj);
            return true;
        } catch (JSONException e)
        {
            return false;
        }
    }

    @Override
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
            throws MachineTranslationException {
        Map paramMap = getMtParameterMap();
        String json = (String) paramMap.get(MTProfileConstants.MT_TILDEMT_STATE_JSON);
        JSONObject obj;
        ServiceParams params;
        try {
            obj = new JSONObject(json);
            params = getParamsFromJson(p_sourceLocale, p_targetLocale, obj);
        } catch (JSONException e) {
            throw new MachineTranslationException(e);
        }

        TildeMTService service;
        try {
            service = new TildeMTService(
                    null,
                    params.clientId
            );
        } catch (URISyntaxException e){
            throw new MachineTranslationException(e);
        }
        TranslateResult res = service.Translate(p_string, params.requestParams);

        return res.Translation;
    }

    protected ServiceParams getParamsFromJson(Locale p_sourceLocale, Locale p_targetLocale, JSONObject obj)
            throws JSONException
    {
        String clientId = obj.getString("client-id");
        JSONObject systems = obj.getJSONObject("systems");

        String langKey = createLangKey(p_sourceLocale, p_targetLocale);
        JSONObject system = systems.getJSONObject(langKey);
        JSONObject systemParams = system.getJSONObject("params");
        List<NameValuePair> requestParams = jsonObjectToList(systemParams);
        return new ServiceParams(clientId, requestParams);
    }

    protected List<NameValuePair> jsonObjectToList(JSONObject jso)
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
            params.add(new BasicNameValuePair(key, item.toString()));
        }
        return params;
    }

    @Override
    protected String[] doBatchTranslation(Locale p_sourceLocale,
                                          Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        String[] translations = new String[p_segments.length];
        for (int i = 0; i < p_segments.length; i++) {
            String segment = p_segments[i];
            translations[i] = doTranslation(p_sourceLocale, p_targetLocale, segment);
        }
        return translations;
    }
}
