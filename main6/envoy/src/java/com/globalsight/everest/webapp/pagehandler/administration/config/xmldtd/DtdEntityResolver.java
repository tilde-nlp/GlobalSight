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

package com.globalsight.everest.webapp.pagehandler.administration.config.xmldtd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.globalsight.util.Assert;

public class DtdEntityResolver implements EntityResolver
{
    private File file = null;

    public DtdEntityResolver(File file)
    {      
        Assert.assertFileExist(file);
        this.file = file;
    }

    @Override
    public InputSource resolveEntity(String arg0, String arg1)
            throws SAXException, IOException
    {
        return new InputSource(new FileInputStream(file));
    }
}
