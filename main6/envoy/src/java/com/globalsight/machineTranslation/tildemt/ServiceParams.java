package com.globalsight.machineTranslation.tildemt;

import org.apache.http.NameValuePair;
import java.util.List;

/**
 * Created by rihards.krislauks on 16.12.2016.
 */
public class ServiceParams {

    protected String clientId;

    protected List<NameValuePair> requestParams;

    public ServiceParams(String clientId, List<NameValuePair> requestParams) {
        this.clientId = clientId;
        this.requestParams = requestParams;
    }
}
