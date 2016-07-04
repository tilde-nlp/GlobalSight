/**
 * Copyright 2009 Welocalize, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.globalsight.restful.version1_0.tmprofile;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import com.globalsight.restful.RestfulApiTestHelper;
import com.globalsight.restful.login.LoginResourceTester;

public class TmProfileResourceTester extends RestfulApiTestHelper
{
    private String accessToken = null;

    public TmProfileResourceTester(String accessToken)
    {
        this.accessToken = accessToken;
    }

    /**
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/tmprofiles/{id}
     */
    public void testGetTMProfile()
    {
        String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/York/tmprofiles/1";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpGet httpGet = getHttpGet(url, accessToken);

            httpResponse = httpClient.execute(httpGet);

            printHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            consumeQuietly(httpResponse);
        }

    }

    /**
     * http://localhost:8080/globalsight/restfulServices/1.0/companies/{companyName}/tmprofiles
     */
    public void testGetAllTmProfiles()
    {
        String url = "http://localhost:8080/globalsight/restfulServices/1.0/companies/York/tmprofiles";

        CloseableHttpClient httpClient = getHttpClient();
        HttpResponse httpResponse = null;
        try
        {
            HttpGet httpGet = getHttpGet(url, accessToken);

            httpResponse = httpClient.execute(httpGet);

            printHttpResponse(httpResponse);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            consumeQuietly(httpResponse);
        }
    }

    public static void main(String[] args)
    {
        TmProfileResourceTester tester = null;

        try
        {
            LoginResourceTester loginTester = new LoginResourceTester();
            String accessToken = loginTester.testLogin("york", "password");
            System.out.println("access token: " + accessToken);

            tester = new TmProfileResourceTester(accessToken);

            tester.testGetTMProfile();

            tester.testGetAllTmProfiles();
        }
        finally
        {
            tester.shutdownHttpClient();
        }
    }
}