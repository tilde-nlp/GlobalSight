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
package galign.helpers.util;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.StringReader;


/**
 */
public class GapEntityResolver
    implements EntityResolver
{
    public InputSource resolveEntity(String publicId, String systemId)
    {
        if(systemId.equals("gap.dtd"))
        {
            // returns an empty entity
            return new InputSource(new StringReader(new String()));
        }
        else
        {
          // use the default behaviour
            return null;
        }
    }
}
