/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.cxe.util.company;

import java.util.Map;

import com.globalsight.everest.company.CompanyThreadLocal;

public class CreateCompanyRunnable implements Runnable
{
    private Map<String, String> m_data;

    public CreateCompanyRunnable(Map<String, String> data)
    {
        m_data = data;
    }

    @Override
    public void run()
    {
        Map<String, String> data = getData();
        if (data != null)
        {
            String companyId = data.get("companyId");
            if (companyId != null)
            {
                CompanyThreadLocal.getInstance().setIdValue(companyId);
            }
            CreateCompanyUtil.createCompany(data);
        }
    }

    private Map<String, String> getData()
    {
        return m_data;
    }
}
