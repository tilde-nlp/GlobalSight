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

import java.util.*;
import java.io.*;

/** Represents a parsed Document Type Definition
 *
 * @author Mark Wutka
 * @version $Revision: 1.1 $ $Date: 2009/04/14 15:09:38 $ by $Author: yorkjin $
 */


public class DTD implements DTDOutput
{
/** Contains all the elements defined in the DTD */
    public Hashtable elements;

/** Contains all the entities defined in the DTD */
    public Hashtable entities;

/** Contains all the notations defined in the DTD */
    public Hashtable notations;

/** Contains parsed DTD's for any external entity DTD declarations */
    public Hashtable externalDTDs;

/** Contains all the items defined in the DTD in their original order */
    public Vector items;

/** Contains the element that is most likely the root element  or null
    if the root element can't be determined.  */
    public DTDElement rootElement;

/** Creates a new DTD */
    public DTD()
    {
        elements = new Hashtable();
        entities = new Hashtable();
        notations = new Hashtable();
        externalDTDs = new Hashtable();
        items = new Vector();
    }

/** Writes the DTD to an output writer in standard DTD format (the format
 *  the parser normally reads).
 *  @param outWriter The writer where the DTD will be written
 */
    public void write(PrintWriter outWriter)
        throws IOException
    {
        Enumeration e = items.elements();

        while (e.hasMoreElements())
        {
            DTDOutput item = (DTDOutput) e.nextElement();

            item.write(outWriter);
        }
    }

/** Returns true if this object is equal to another */
    public boolean equals(Object ob)
    {
        if (this == ob) return true;

        if (!(ob instanceof DTD)) return false;

        DTD otherDTD = (DTD) ob;

        return items.equals(otherDTD.items);
    }

/** Stores an array of items in the items array */
    public void setItems(Object[] newItems)
    {
        items = new Vector(newItems.length);
        for (int i=0; i < newItems.length; i++)
        {
            items.addElement(newItems[i]);
        }
    }

/** Returns the items as an array */
    public Object[] getItems()
    {
        return items.toArray();
    }

/** Stores an item in the items array */
    public void setItem(Object item, int i)
    {
        items.setElementAt(item, i);
    }

/** Retrieves an item from the items array */
    public Object getItem(int i)
    {
        return items.elementAt(i);
    }

/** Retrieves a list of items of a particular type */
    public Vector getItemsByType(Class itemType)
    {
        Vector results = new Vector();

        Enumeration e = items.elements();

        while (e.hasMoreElements())
        {
            Object ob = e.nextElement();

            if (itemType.isAssignableFrom(ob.getClass()))
            {
                results.addElement(ob);
            }
        }

        return results;
    }
}
