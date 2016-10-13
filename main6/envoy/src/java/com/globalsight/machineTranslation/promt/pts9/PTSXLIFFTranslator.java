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
package com.globalsight.machineTranslation.promt.pts9;

public interface PTSXLIFFTranslator extends javax.xml.rpc.Service
{
    public java.lang.String getPTSXLIFFTranslatorSoap12Address();

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap12()
            throws javax.xml.rpc.ServiceException;

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap12(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException;

    public java.lang.String getPTSXLIFFTranslatorSoapAddress();

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap()
            throws javax.xml.rpc.ServiceException;

    public PTSXLIFFTranslatorSoap getPTSXLIFFTranslatorSoap(
            java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
