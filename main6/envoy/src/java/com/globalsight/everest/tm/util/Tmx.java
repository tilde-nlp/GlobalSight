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

package com.globalsight.everest.tm.util;

import com.globalsight.util.edit.EditUtil;
import com.globalsight.util.UTC;

import org.dom4j.Attribute;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents the TMX header of a TMX file for both import and export.
 * 
 * @see http://www.lisa.org/tmx/tmx.htm
 * @see http://www.w3.org/TR/1998/NOTE-datetime-19980827
 */
public class Tmx
{
    //
    // TMX version constants
    //

    static public final String TMX_11 = "1.1";
    static public final String TMX_12 = "1.2";
    static public final String TMX_13 = "1.3";
    static public final String TMX_14 = "1.4";
    static public final String TMX_DTD_11 = "tmx11.dtd";
    static public final String TMX_DTD_12 = "tmx12.dtd";
    static public final String TMX_DTD_13 = "tmx13.dtd";
    static public final String TMX_DTD_14 = "tmx14.dtd";
    static public final int TMX_VERSION_11 = 110;
    static public final int TMX_VERSION_12 = 120;
    static public final int TMX_VERSION_13 = 130;
    static public final int TMX_VERSION_14 = 140;

    // GlobalSight's version, used as backup format
    static public final String TMX_GS = "1.0 GS";
    static public final String TMX_DTD_GS = "tmx-gs.dtd";
    static public final int TMX_VERSION_GS = 42;

    //
    // Constants for TMX element and attribute names.
    //
    static final public String ADMINLANG = "adminlang";
    static final public String CHANGEDATE = "changedate";
    static final public String CHANGEID = "changeid";
    static final public String CREATIONDATE = "creationdate";
    static final public String CREATIONID = "creationid";
    static final public String CREATIONTOOL = "creationtool";
    static final public String CREATIONTOOLVERSION = "creationtoolversion";
    static final public String DATATYPE = "datatype";
    static final public String LASTUSAGEDATE = "lastusagedate";
    static final public String O_ENCODING = "o-encoding";
    static final public String O_TMF = "o-tmf";
    static final public String SEGTYPE = "segtype";
    static final public String SRCLANG = "srclang";
    static final public String TUID = "tuid";
    static final public String USAGECOUNT = "usagecount";
    static final public String VERSION = "version";

    // careful: must write "xml:lang" but read "lang"
    static final public String LANG = "lang";

    //
    // User-defined constants we use in <prop> when writing out GXML
    // in TMX (aka, G-TMX)
    //
    static final public String PROP_SEGMENTTYPE = "gs-segment-type";

    static final public String PROP_TUTYPE = "gs-tu-type";
    static final public String VAL_TU_LOCALIZABLE = "localizable";
    static final public String VAL_TU_TRANSLATABLE = "translatable";

    static final public String PROP_SOURCE_TM_NAME = "x-sourcetmname";
    static final public String PROP_UPDATED_BY_PROJECT = "x-updatedbyproject";
    static final public String PROP_CREATION_PROJECT = "x-creationproject";
    static final public String PROP_PREVIOUS_HASH = "x-hash-prev";
    static final public String PROP_NEXT_HASH = "x-hash-next";
    static final public String PROP_JOB_ID = "x-creationjobid";
    static final public String PROP_JOB_NAME = "x-creationjobname";
    static final public String PROP_MT_NAME = "x-mt";

    static final public String PROP_TM_UDA_SID = "x-idiom-tm-uda-SID";
    //
    // System-defined constants when writing out the TMX header
    //
    static final public String GLOBALSIGHT = "Welocalize GlobalSight";
    static final public String GLOBALSIGHTVERSION = "6.0";

    // segtype values
    static final public String SEGMENTATION_BLOCK = "block";
    static final public String SEGMENTATION_PARAGRAPH = "paragraph";
    static final public String SEGMENTATION_SENTENCE = "sentence";
    static final public String SEGMENTATION_PHRASE = "phrase";

    // o-tmf values: our pivot is GXML, so this is our original TM format
    static final public String TMF_GXML = "gxml";

    // adminlang values: en_US by default
    static final public String DEFAULT_ADMINLANG = "EN-US";

    // datatype values: pivot is HTML, so all segments are HTML by default
    static final public String DATATYPE_HTML = "html";

    // srclang values: en_US by default
    static final public String DEFAULT_SOURCELANG = "EN-US";

    // default user that created a TU/TUV if information is missing.
    static final public String DEFAULT_USER = "system";

    //
    // Private members
    //

    // <tmx>
    private String m_tmxVersion;

    // <header> mandatory
    private String m_creationtool;
    private String m_creationtoolversion;
    private String m_segtype;
    // original tm format
    private String m_o_tmf;
    private String m_adminlang;
    private String m_srclang;
    private String m_datatype;

    // <header> optional
    private String m_o_encoding; // an IANA encoding
    private Date m_creationdate; // a ISO 8601 date
    private String m_creationid;
    private Date m_changedate;
    private String m_changeid;

    // elements inside header
    private ArrayList m_notes = new ArrayList();
    private ArrayList m_props = new ArrayList();

    // TODO: user-defined encodings
    // private ArrayList m_udes = new ArrayList();

    static public class Note
    {
        // optional attrs
        private String m_lang = null;
        private String m_o_encoding = null;

        // the note value
        private String m_value;

        public Note()
        {
        }

        public Note(String p_value)
        {
            m_value = p_value;
        }

        public Note(Element p_element)
        {
            m_lang = p_element.attributeValue("lang");
            m_o_encoding = p_element.attributeValue(O_ENCODING);

            m_value = p_element.getText();
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer();

            result.append("<note");

            if (isSet(m_lang))
            {
                result.append(" xml:lang=\"");
                result.append(m_lang);
                result.append("\"");
            }
            if (isSet(m_o_encoding))
            {
                result.append(" ");
                result.append(O_ENCODING);
                result.append("=\"");
                result.append(m_o_encoding);
                result.append("\"");
            }

            result.append(">");
            result.append(EditUtil.encodeXmlEntities(m_value));
            result.append("</note>\r\n");

            return result.toString();
        }
    }

    static public class Prop
    {
        // required attrs
        private String m_type;
        // optional attrs
        private String m_lang = null;
        private String m_o_encoding = null;

        // the property value
        private String m_value;

        public Prop()
        {
        }

        public Prop(String p_type, String p_value)
        {
            m_type = p_type;
            m_value = p_value;
        }

        public Prop(Element p_element)
        {
            m_type = p_element.attributeValue("type");
            m_lang = p_element.attributeValue("lang");
            m_o_encoding = p_element.attributeValue(O_ENCODING);

            m_value = p_element.getText();
        }

        public String asXML()
        {
            StringBuffer result = new StringBuffer();

            result.append("<prop type=\"");
            result.append(EditUtil.encodeXmlEntities(m_type));
            result.append("\"");

            if (isSet(m_lang))
            {
                result.append(" xml:lang=\"");
                result.append(m_lang);
                result.append("\"");
            }
            if (isSet(m_o_encoding))
            {
                result.append(" ");
                result.append(O_ENCODING);
                result.append("=\"");
                result.append(m_o_encoding);
                result.append("\"");
            }

            result.append(">");
            result.append(EditUtil.encodeXmlEntities(m_value));
            result.append("</prop>\r\n");

            return result.toString();
        }
    }

    /** User-defined encodings of extended Unicode characters. */
    static public class Ude
    {
        // required attrs
        private String m_name;
        // optional attrs
        private String m_base;

        // TODO: MAP elements.
    }

    //
    // Constructor
    //
    public Tmx()
    {
    }

    public Tmx(Element p_header)
    {
        init(p_header);
    }

    //
    // Public Methods
    //
    public String getTmxVersion()
    {
        return m_tmxVersion;
    }

    public void setTmxVersion(String p_arg)
    {
        m_tmxVersion = p_arg;
    }

    public void setCreationTool(String p_arg)
    {
        m_creationtool = p_arg;
    }

    public void setCreationToolVersion(String p_arg)
    {
        m_creationtoolversion = p_arg;
    }

    public void setSegmentationType(String p_arg)
    {
        m_segtype = p_arg;
    }

    public String getOriginalFormat()
    {
        return m_o_tmf;
    }

    public void setOriginalFormat(String p_arg)
    {
        m_o_tmf = p_arg;
    }

    public void setAdminLang(String p_arg)
    {
        m_adminlang = p_arg;
    }

    public String getSourceLang()
    {
        return m_srclang;
    }

    public void setSourceLang(String p_arg)
    {
        m_srclang = p_arg;
    }

    public String getDatatype()
    {
        return m_datatype;
    }

    public void setDatatype(String p_arg)
    {
        m_datatype = p_arg;
    }

    public void setOriginalEncoding(String p_arg)
    {
        m_o_encoding = p_arg;
    }

    // Arg must be in ISO 8601 format
    public void setCreationDate(String p_arg)
    {
        Date date = UTC.parseNoSeparators(p_arg);
        if (date == null)
        {
            date = UTC.parse(p_arg);
        }

        m_creationdate = date;
    }

    public void setCreationDate(Date p_arg)
    {
        m_creationdate = p_arg;
    }

    public void setCreationId(String p_arg)
    {
        m_creationid = p_arg;
    }

    // Arg must be in ISO 8601 format
    public void setChangeDate(String p_arg)
    {
        Date date = UTC.parseNoSeparators(p_arg);
        if (date == null)
        {
            date = UTC.parse(p_arg);
        }

        m_changedate = date;
    }

    public void setChangeDate(Date p_arg)
    {
        m_changedate = p_arg;
    }

    public void setChangeId(String p_arg)
    {
        m_changeid = p_arg;
    }

    public void addNote(String p_value)
    {
        m_notes.add(new Note(p_value));
    }

    public void addProp(String p_type, String p_value)
    {
        m_props.add(new Prop(p_type, p_value));
    }

    /** Returns the DTD devlaration &lt;!DOCTYPE tmx SYSTEM "tmx12.dtd"&gt; */
    public String getTmxDeclaration()
    {
        if (m_tmxVersion.equals(TMX_GS))
        {
            return "<!DOCTYPE tmx SYSTEM \""
                    + getTmxDtdFromVersion(m_tmxVersion) + "\" >";
        }
        else
        {
            // Change "DOCTYPE" reference path
            // from:
            // <!DOCTYPE tmx SYSTEM "http://www.lisa.org/tmx/tmx14.dtd" >
            // to:
            // <!DOCTYPE tmx SYSTEM "tmx14.dtd" >
            return "<!DOCTYPE tmx SYSTEM \""
                    + getTmxDtdFromVersion(m_tmxVersion) + "\" >";
        }
    }

    /** Returns the TMX start element &lt;tmx version="1.4"&gt;. */
    public String getTmxXml()
    {
        return "<tmx version=\"" + m_tmxVersion + "\">";
    }

    /**
     * Returns the TMX header element as XML: <header [all
     * attributes]><note><prop></header>
     */
    public String getHeaderXml()
    {
        StringBuffer result = new StringBuffer();

        result.append("<header ");
        result.append(CREATIONTOOL);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_creationtool));
        result.append("\" ");
        result.append(CREATIONTOOLVERSION);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_creationtoolversion));
        result.append("\" ");
        result.append(SEGTYPE);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_segtype));
        result.append("\" ");
        result.append(O_TMF);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_o_tmf));
        result.append("\" ");
        result.append(ADMINLANG);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_adminlang));
        result.append("\" ");
        result.append(SRCLANG);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_srclang == null ? m_srclang
                : m_srclang.replace("_", "-")));
        result.append("\" ");
        result.append(DATATYPE);
        result.append("=\"");
        result.append(EditUtil.encodeXmlEntities(m_datatype));
        result.append("\" ");

        if (isSet(m_o_encoding))
        {
            result.append(O_ENCODING);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_o_encoding));
            result.append("\" ");
        }
        if (m_creationdate != null)
        {
            result.append(CREATIONDATE);
            result.append("=\"");
            result.append(UTC.valueOfNoSeparators(m_creationdate));
            result.append("\" ");
        }
        if (isSet(m_creationid))
        {
            result.append(CREATIONID);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_creationid));
            result.append("\" ");
        }
        if (m_changedate != null)
        {
            result.append(CHANGEDATE);
            result.append("=\"");
            result.append(UTC.valueOfNoSeparators(m_changedate));
            result.append("\" ");
        }
        if (isSet(m_changeid))
        {
            result.append(CHANGEID);
            result.append("=\"");
            result.append(EditUtil.encodeXmlEntities(m_changeid));
            result.append("\" ");
        }

        result.append(">\r\n");

        for (int i = 0; i < m_notes.size(); i++)
        {
            Note note = (Note) m_notes.get(i);
            result.append(note.asXML());
        }

        for (int i = 0; i < m_props.size(); i++)
        {
            Prop prop = (Prop) m_props.get(i);
            result.append(prop.asXML());
        }

        /*
         * TODO for (int i = 0; i < m_udes.size(); i++) { Ude ude =
         * (Ude)m_udes.get(i); result.append(ude.asXML()); }
         */

        result.append("</header>\r\n");

        return result.toString();
    }

    /**
     * Returns an internal version number (integer) based on the TMX DTD.
     */
    static public int getTmxDtdVersion(String p_dtd)
    {
        if (p_dtd.equals(TMX_DTD_11))
        {
            return TMX_VERSION_11;
        }
        else if (p_dtd.equals(TMX_DTD_12))
        {
            return TMX_VERSION_12;
        }
        else if (p_dtd.equals(TMX_DTD_13))
        {
            return TMX_VERSION_13;
        }
        else if (p_dtd.equals(TMX_DTD_14))
        {
            return TMX_VERSION_14;
        }
        else if (p_dtd.equals(TMX_DTD_GS))
        {
            return TMX_VERSION_GS;
        }

        return TMX_VERSION_14;
    }

    /**
     * Returns the DTD for the given TMX version string.
     */
    static public String getTmxDtdFromVersion(String p_version)
    {
        if (p_version.equals(TMX_11))
        {
            return TMX_DTD_11;
        }
        else if (p_version.equals(TMX_12))
        {
            return TMX_DTD_12;
        }
        else if (p_version.equals(TMX_13))
        {
            return TMX_DTD_13;
        }
        else if (p_version.equals(TMX_14))
        {
            return TMX_DTD_14;
        }
        else if (p_version.equals(TMX_GS))
        {
            return TMX_DTD_GS;
        }

        return TMX_DTD_14;
    }

    //
    // Private Methods
    //
    static private boolean isSet(String s)
    {
        if (s != null && s.length() > 0)
        {
            return true;
        }

        return false;
    }

    private void init(Element p_element)
    {
        Attribute attr;
        List nodes;
        Date date;

        // mandatory

        m_creationtoolversion = p_element.attributeValue(CREATIONTOOLVERSION);
        m_creationtool = p_element.attributeValue(CREATIONTOOL);
        m_segtype = p_element.attributeValue(SEGTYPE);
        m_o_tmf = p_element.attributeValue(O_TMF);
        m_adminlang = p_element.attributeValue(ADMINLANG);
        m_srclang = p_element.attributeValue(SRCLANG);
        m_datatype = p_element.attributeValue(DATATYPE);

        // optional

        m_o_encoding = p_element.attributeValue(O_ENCODING);
        m_creationid = p_element.attributeValue(CREATIONID);
        m_changeid = p_element.attributeValue(CHANGEID);

        attr = p_element.attribute(CREATIONDATE);
        if (attr == null)
        {
            date = null;
        }
        else
        {
            date = UTC.parseNoSeparators(attr.getValue());
            if (date == null)
            {
                date = UTC.parse(attr.getValue());
            }
        }
        m_creationdate = date;

        attr = p_element.attribute(CHANGEDATE);
        if (attr == null)
        {
            date = null;
        }
        else
        {
            date = UTC.parseNoSeparators(attr.getValue());
            if (date == null)
            {
                date = UTC.parse(attr.getValue());
            }
        }
        m_changedate = date;

        // elements

        nodes = p_element.selectNodes("note");
        for (int i = 0; i < nodes.size(); i++)
        {
            Element node = (Element) nodes.get(i);

            m_notes.add(new Note(node));
        }

        nodes = p_element.selectNodes("prop");
        for (int i = 0; i < nodes.size(); i++)
        {
            Element node = (Element) nodes.get(i);

            m_props.add(new Prop(node));
        }

        // TODO: UDE
    }
}
