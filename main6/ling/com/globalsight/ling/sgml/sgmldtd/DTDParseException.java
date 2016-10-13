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
package com.globalsight.ling.sgml.sgmldtd;

import java.text.MessageFormat;

public class DTDParseException extends java.io.IOException
{
    public String uriID = "";
    public int lineNumber;
    public int column;

    public DTDParseException()
    {
        lineNumber=-1;
        column=-1;
    }

    public DTDParseException(String message)
    {
        super(message);
        lineNumber=-1;
        column=-1;
    }

    public DTDParseException(String message, int line, int col)
    {
        super(MessageFormat.format(Messages.getString("DTDParseException.1"),
                new Object[] { new Integer(line), new Integer(col), message }));
        lineNumber = line;
        column = col;
    }

    public DTDParseException(String id, String message, int line, int col)
    {
        super(MessageFormat.format(Messages.getString("DTDParseException.2"),
                new Object[] { ((null != id && id.length() > 0) ? "URI " + id + " " : ""),
                    new Integer(line), new Integer(col), message }));
        if (null != id)
            uriID = id;

        lineNumber = line;
        column = col;
    }

    public String getId() { return(uriID); }
    public int getLineNumber() { return lineNumber; }
    public int getColumn() { return column; }
}
