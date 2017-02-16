package com.globalsight.machineTranslation.tildemt;

import org.apache.http.NameValuePair;
import java.util.List;

/**
 * A class to encapsulate all of the parameters necessary to make requests to the Tilde MT API
 * <p>
 * Created by rihards.krislauks on 16.12.2016.
 */
public class ServiceParams {

    /**
     * The ClientID string on the Tilde MT platform to authenticate the API requests.
     */
    protected String clientId;

    /**
     * The GET query parameters for the request indicating the translation system, terminology
     * selection, etc., as prepared by the Tilde MT system selection web component (frontend).
     */
    protected List<NameValuePair> requestParams;

    /**
     * Creates a new instance of the {@code ServiceParams} class initializing all of its fields.
     * @param clientId The ClientID string on the Tilde MT platform to authenticate the API requests.
     * @param requestParams The GET query parameters for the request indicating the translation system, terminology
     *                      selection, etc., as prepared by the Tilde MT system selection web component (frontend).
     */
    public ServiceParams(String clientId, List<NameValuePair> requestParams) {
        this.clientId = clientId;
        this.requestParams = requestParams;
    }
}
