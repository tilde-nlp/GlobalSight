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
package test.globalsight.ling.common;

// Imports
import com.globalsight.ling.common.Text;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
*/
public class TextTest
    extends TestCase
{   
    
    public static Test suite()
    {
        return new TestSuite(TextTest.class);
    }

    public TextTest(String p_Name)
    {
        super(p_Name);
    }

    public void setUp()
    {               
    }

    public void test1()
    {
        String ret = Text.normalizeWhiteSpaces("xx\nxxx\r\nxx\rxx xx");        
        int x = 1;
    }
}
