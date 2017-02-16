package com.globalsight.machineTranslation.tildemt;

/**
 * A class to encapsulate all of the relevant information to be retrieved from the Tilde MT API's translate request.
 * <p>
 * Created by rihards.krislauks on 30.09.2016.
 */
public class TranslateResult {
    /**
     * Translation returned by the Tilde MT API
     */
    public String Translation;
    /**
     * The Quality Estimation score of the translation. A value in the interval 0-1 or {@code null} if the translation
     * system doesn't support QE or it's turned off.
     */
    public Double QeScore;
}
