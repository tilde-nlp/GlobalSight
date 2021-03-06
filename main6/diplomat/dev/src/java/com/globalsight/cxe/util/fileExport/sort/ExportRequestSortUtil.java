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
package com.globalsight.cxe.util.fileExport.sort;

public class ExportRequestSortUtil
{

    public static void upRequest(String[] keys)
    {
        UpRequestSorter sorter = new UpRequestSorter();
        sorter.sortWaitingPriority(keys);
    }
    
    public static void downRequest(String[] keys)
    {
        DownRequestSorter sorter = new DownRequestSorter();
        sorter.sortWaitingPriority(keys);
    }
    
    public static void topRequest(String[] keys)
    {
        TopRequestSorter sorter = new TopRequestSorter();
        sorter.sortWaitingPriority(keys);
    }
    
    public static void bottomRequest(String[] keys)
    {
        BottomRequestSorter sorter = new BottomRequestSorter();
        sorter.sortWaitingPriority(keys);
    }
}
