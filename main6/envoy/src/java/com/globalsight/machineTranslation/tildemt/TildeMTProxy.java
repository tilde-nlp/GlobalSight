package com.globalsight.machineTranslation.tildemt;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;

import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Map;

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
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale) throws MachineTranslationException {
        return true;
    }

    @Override
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string) throws MachineTranslationException {
        @SuppressWarnings("rawtypes")
        Map paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap
                .get(MachineTranslator.MT_PROFILE);
        TildeMTService service;
        try {
            service = new TildeMTService(
                    mtProfile.getUrl(),
                    mtProfile.getPassword(),
                    "GlobalSight",
                    "Client", // TODO
                    "ClientVersion"  // TODO
            );
        } catch (URISyntaxException e){
            throw new MachineTranslationException(e);
        }
        TranslateResult res = service.Translate(
                        p_string,
                        mtProfile.getCategory(),
                        null,
                        false
        );

        return res.Translation;
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
