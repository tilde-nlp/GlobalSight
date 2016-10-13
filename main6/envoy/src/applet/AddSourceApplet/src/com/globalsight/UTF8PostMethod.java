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

package com.globalsight;

import org.apache.commons.httpclient.methods.PostMethod;

public class UTF8PostMethod extends PostMethod
{
    public UTF8PostMethod(String url)
    {
        super(url);
    }

    @Override
    public String getRequestCharSet()
    {
        return "UTF-8";
    }
    
    public String getResponseCharSet()
    {
        return "UTF-8";
    }
}
