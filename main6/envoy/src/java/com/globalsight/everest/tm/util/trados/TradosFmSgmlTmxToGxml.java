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

package com.globalsight.everest.tm.util.trados;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.globalsight.everest.tm.util.DtdResolver;
import com.globalsight.everest.tm.util.Tmx;
import com.globalsight.ling.common.HtmlEntities;
import com.globalsight.ling.docproc.DiplomatAPI;
import com.globalsight.ling.docproc.DocumentElement;
import com.globalsight.ling.docproc.IFormatNames;
import com.globalsight.ling.docproc.Output;
import com.globalsight.ling.docproc.SegmentNode;
import com.globalsight.ling.docproc.TranslatableElement;

/**
 * <p>
 * Trados-TMX Converter for S-Tagger for FrameMaker+SGML.
 * </p>
 *
 * <p>
 * This tool reads a Trados TMX file containing segments created with Trados'
 * S-Tagger for FrameMaker+SGML filter, and writes it out as GXML.
 * </p>
 */
public class TradosFmSgmlTmxToGxml
{
    static public final String s_TOOLNAME = "GlobalSight TradosFmSgmlTmxToGxml";
    static public final String s_TOOLVERSION = "1.0";

    private Logger m_logger = null;

    private int m_entryCount = 0;
    private int m_errorCount = 0;
    private int m_fileCount = 0;
    private String m_filename;

    private PrintWriter m_writer;

    private String m_version = "";
    private Tmx m_header = null;
    private Tmx m_newHeader = null;

    private boolean m_tuError;

    private HtmlEntities m_htmlDecoder = new HtmlEntities();
    private DiplomatAPI m_diplomat = new DiplomatAPI();

    private SAXReader m_segmentReader = null;

    private Pattern m_pattern = Pattern.compile("<ut>|</ut>");

    /*
     * Old way using jakarta-regexp private static final REProgram
     * UT_SEARCH_PATTERN =
     * //createSearchPattern("<ut>(.*?)</ut>(.*?)<ut>(.*?)</ut>");
     * createSearchPattern("<ut>|</ut>");
     * 
     * private static REProgram createSearchPattern(String p_pattern) {
     * REProgram pattern = null;
     * 
     * try { RECompiler compiler = new RECompiler(); pattern =
     * compiler.compile(p_pattern); } catch (RESyntaxException e) { // Pattern
     * syntax error. Stop the application. throw new
     * RuntimeException(e.getMessage()); } return pattern; }
     */

    //
    // Constructors
    //

    public TradosFmSgmlTmxToGxml()
    {
    }

    public TradosFmSgmlTmxToGxml(Logger p_logger)
    {
        m_logger = p_logger;
    }

    // *************************************************

    public void debug(String p_message)
    {
        if (m_logger != null)
        {
            if (m_logger.isDebugEnabled())
            {
                m_logger.debug(p_message);
            }
        }
        else
        {
            System.err.println(p_message);
        }
    }

    public void info(String p_message)
    {
        if (m_logger != null)
        {
            m_logger.info(p_message);
        }
        else
        {
            System.err.println(p_message);
        }
    }

    public void info(String p_message, Throwable t)
    {
        if (m_logger != null)
        {
            m_logger.info(p_message, t);
        }
        else
        {
            System.err.println(p_message);
            t.printStackTrace(System.err);
        }
    }

    // *************************************************

    public String getBaseName(String p_name)
    {
        return p_name.substring(0, p_name.lastIndexOf("."));
    }

    public String getExtension(String p_name)
    {
        return p_name.substring(p_name.lastIndexOf(".") + 1);
    }

    public void startOutputFile(String p_base) throws Exception
    {
        m_fileCount++;

        m_filename = p_base + "-gxml.tmx";

        debug("Writing to file " + m_filename);

        m_writer = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(m_filename)),
                "UTF8"));

        m_writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
    }

    public void closeOutputFile() throws Exception
    {
        m_writer.println("</body>");
        m_writer.println("</tmx>");
        m_writer.close();
    }

    public void writeEntry(String p_message)
    {
        m_writer.println(p_message);
    }

    // ******************************************

    public String handleTuv(Element p_element) throws Throwable
    {
        try
        {
            String segment = removeUtElements(p_element);

            // debug("Segment=" + segment);

            Output output = extractSegment(segment);
            String gxml = getFirstSegment(output);

            // debug("GXML=" + gxml);

            return gxml;
        }
        catch (Throwable ex)
        {
            info("Segment could not be converted:\n" + p_element.asXML(), ex);

            throw ex;
        }
    }

    public Output extractSegment(String p_segment) throws Exception
    {
        m_diplomat.reset();
        m_diplomat.setSourceString(p_segment);
        m_diplomat.setInputFormat(IFormatNames.FORMAT_WORD_HTML);
        m_diplomat.setEncoding("unicode");
        m_diplomat.setLocale(Locale.US);
        m_diplomat.setSentenceSegmentation(false);

        m_diplomat.extract();

        return m_diplomat.getOutput();
    }

    /**
     * After System4 paragraph segmentation, return the first translatable
     * segment.
     */
    public String getFirstSegment(Output p_output)
    {
        for (Iterator it = p_output.documentElementIterator(); it.hasNext();)
        {
            DocumentElement de = (DocumentElement) it.next();

            switch (de.type())
            {
                case DocumentElement.TRANSLATABLE:
                {
                    TranslatableElement elem = (TranslatableElement) de;

                    if (elem.hasSegments())
                    {
                        SegmentNode node = (SegmentNode) elem.getSegments()
                                .get(0);

                        return node.getSegment();
                    }

                    break;
                }
                case DocumentElement.LOCALIZABLE:
                {
                    // LocalizableElement elem = (LocalizableElement)de;
                    // return elem.getChunk();

                    break;
                }
                default:
                    // skip all others
                    break;
            }
        }

        return "";
    }

    /**
     * Removes all TMX 1.4 &lt;ut&gt; elements from a segment and converts the
     * tag content to Unicode characters or HTML markup, if possible.
     */
    private String removeUtElements(Element p_seg)
    {
        StringBuffer result = new StringBuffer();
        String xml = p_seg.asXML();

        /*
         * Old way using jakarta-regexp RE re = new RE(UT_SEARCH_PATTERN);
         * String[] parts = re.split(xml);
         */

        // Split string at <ut> boundaries
        String[] parts = m_pattern.split(xml);

        int i = 0, max = parts.length;

        if (max == 1)
        {
            result.append(parts[0]);
        }
        else
        {
            while (i < max - 1)
            {
                boolean b_isFrameTag = false;

                String pre = parts[i++];
                String rtfStart = parts[i++].trim();

                result.append(pre);

                if (rtfStart.startsWith("{"))
                {
                    b_isFrameTag = true;
                }

                if (b_isFrameTag)
                {
                    String tag = parts[i++].trim();
                    String rtfEnd = parts[i++];

                    result.append(mapFrameTag(tag));
                }
                else
                {
                    result.append(mapRtfCode(rtfStart));
                }
            }

            if (i == max - 1)
            {
                result.append(parts[max - 1]);
            }
        }

        return result.toString();
    }

    /**
     * Maps an STagger tag to Unicode/G-TMX.
     */
    private String mapFrameTag(String p_tag)
    {
        // SGML tags are by default unknown to us. Discard.

        return "";
    }

    /**
     * Maps RTF character codes inside <UT>.
     *
     * TODO: map multiple codes.
     */
    private String mapRtfCode(String rtfStart)
    {
        StringBuffer result = new StringBuffer();

        if (rtfStart.startsWith("\\emdash"))
        {
            result.append('\u2014'); // EM DASH
        }
        else if (rtfStart.startsWith("\\endash"))
        {
            result.append('\u2013'); // EN DASH
        }
        else if (rtfStart.startsWith("\\lquote"))
        {
            result.append('\u2018'); // LEFT SINGLE QUOTATION MARK
        }
        else if (rtfStart.startsWith("\\rquote"))
        {
            result.append('\u2019'); // RIGHT SINGLE QUOTATION MARK
        }
        else if (rtfStart.startsWith("\\ldblquote"))
        {
            result.append('\u201C'); // LEFT DOUBLE QUOTATION MARK
        }
        else if (rtfStart.startsWith("\\rdblquote"))
        {
            result.append('\u201D'); // RIGHT DOUBLE QUOTATION MARK
        }
        else if (rtfStart.startsWith("\\bullet"))
        {
            result.append('\u2022'); // BULLET
        }
        else if (rtfStart.startsWith("\\~"))
        {
            result.append('\u00A0'); // NO-BREAK SPACE
        }
        else if (rtfStart.startsWith("\\-"))
        {
            result.append('\u00AD'); // SOFT HYPHEN
        }
        else if (rtfStart.startsWith("\\_"))
        {
            result.append('\u2011'); // NON-BREAKING HYPHEN
        }
        else if (rtfStart.startsWith("\\\\"))
        {
            result.append('\\');
        }
        else if (rtfStart.startsWith("\\{"))
        {
            result.append('{');
        }
        else if (rtfStart.startsWith("\\}"))
        {
            result.append('}');
        }
        else if (rtfStart.startsWith("\\emspace"))
        {
            result.append('\u2003'); // EM SPACE
        }
        else if (rtfStart.startsWith("\\enspace"))
        {
            result.append('\u2002'); // EN SPACE
        }
        else if (rtfStart.startsWith("\\tab"))
        {
            result.append('\t'); // TAB
        }

        // Discard any other tags.

        return result.toString();
    }

    public void setOldHeader(Element p_element)
    {
        m_header = new Tmx(p_element);
    }

    public void createNewHeader()
    {
        m_newHeader = new Tmx();
        m_newHeader.setTmxVersion(Tmx.TMX_GS);
        m_newHeader.setCreationTool(s_TOOLNAME);
        m_newHeader.setCreationToolVersion(s_TOOLVERSION);
        m_newHeader.setSegmentationType("sentence");
        m_newHeader.setOriginalFormat(m_header.getOriginalFormat());
        m_newHeader.setAdminLang(Tmx.DEFAULT_ADMINLANG);
        m_newHeader.setSourceLang(m_header.getSourceLang());
        // TODO: Datatype should be a constant in Tmx.java. The data
        // type should probably be a native identifier ("g-tmx"?)
        // because now we have native data and don't care about where
        // it came from.
        m_newHeader.setDatatype("fm+sgml");

        m_writer.println(m_newHeader.getTmxDeclaration());
        m_writer.println(m_newHeader.getTmxXml());
        m_writer.println(m_newHeader.getHeaderXml());
        m_writer.println("<body>");
    }

    /**
     * Parses an XML string into a document.
     */
    public Document parse(String p_xml)
    {
        try
        {
            // Don't validate here since main parser has already validated.
            if (m_segmentReader == null)
            {
                m_segmentReader = new SAXReader();
                m_segmentReader
                        .setXMLReaderClassName("org.dom4j.io.aelfred.SAXDriver");
                m_segmentReader.setValidation(false);
            }

            return m_segmentReader.read(new StringReader(p_xml));
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Main method to call, returns the new filename of the result.
     */
    public String convertToGxml(String p_url) throws Exception
    {
        final String baseName = getBaseName(p_url);
        final String extension = getExtension(p_url);

        info("Converting TMX file to GXML: `" + p_url + "'");
        startOutputFile(baseName);

        m_entryCount = 0;

        // Reading from a file, need to use Xerces.
        SAXReader reader = new SAXReader();
        reader.setXMLReaderClassName("org.apache.xerces.parsers.SAXParser");
        reader.setEntityResolver(DtdResolver.getInstance());
        reader.setValidation(true);

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx", new ElementHandler()
        {
            final public void onStart(ElementPath path)
            {
                Element element = path.getCurrent();

                m_version = element.attributeValue("version");
            }

            final public void onEnd(ElementPath path)
            {
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/header", new ElementHandler()
        {
            final public void onStart(ElementPath path)
            {
            }

            final public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                setOldHeader(element);
                createNewHeader();

                // prune the current element to reduce memory
                element.detach();

                element = null;
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu", new ElementHandler()
        {
            final public void onStart(ElementPath path)
            {
                ++m_entryCount;
                m_tuError = false;
            }

            final public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                if (m_tuError)
                {
                    m_errorCount++;
                }
                else
                {
                    writeEntry(element.asXML());
                }

                // prune the current element to reduce memory
                element.detach();

                element = null;

                if (m_entryCount % 1000 == 0)
                {
                    debug("Entry " + m_entryCount);
                }
            }
        });

        // enable element complete notifications to conserve memory
        reader.addHandler("/tmx/body/tu/tuv/seg", new ElementHandler()
        {
            final public void onStart(ElementPath path)
            {
            }

            final public void onEnd(ElementPath path)
            {
                Element element = path.getCurrent();

                try
                {
                    String gxml = handleTuv(element);
                    Document doc = parse("<root>" + gxml + "</root>");

                    // Remove old content of seg
                    List content = element.content();
                    for (int i = content.size() - 1; i >= 0; --i)
                    {
                        ((Node) content.get(i)).detach();
                    }

                    // Add new GXML content (backwards)
                    content = doc.getRootElement().content();
                    Collections.reverse(content);
                    for (int i = content.size() - 1; i >= 0; --i)
                    {
                        Node node = (Node) content.get(i);
                        element.add(node.detach());
                    }
                }
                catch (Throwable ex)
                {
                    m_tuError = true;
                }
            }
        });

        Document document = reader.read(p_url);

        closeOutputFile();

        info("Processed " + m_entryCount + " TUs " + "into file `" + m_filename
                + "', " + m_errorCount + " errors.");

        return m_filename;
    }

    static public void main(String[] argv) throws Exception
    {
        TradosFmSgmlTmxToGxml a = new TradosFmSgmlTmxToGxml();

        if (argv.length != 1)
        {
            System.err.println("Usage: TradosFmSgmlTmxToGxml FILE\n");
            System.err.println("Converts Trados TMX containing "
                    + "FrameMaker+SGML data to GXML.\n");
            System.exit(1);
        }

        a.convertToGxml(argv[0]);
    }
}
