package com.globalsight.machineTranslation.tildemt;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.StringUtil;
import com.globalsight.util.edit.GxmlUtil;
import com.globalsight.util.gxml.GxmlElement;
import com.globalsight.util.gxml.GxmlReader;
import com.globalsight.util.gxml.TextNode;
import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Created by rihards.krislauks@tilde.lv on 26.09.2016.
 */
public class TildeMTProxy extends AbstractTranslator {
    /**
     * Returns the MT engine name.
     *
     * @return name
     */
    @Override
    public String getEngineName() {
        return ENGINE_TILDEMT;
    }

    private String createLangKey(Locale p_sourceLocale, Locale p_targetLocale)
    {
        return String.format("%1$s-%2$s",
                p_sourceLocale.getLanguage(), p_targetLocale.getLanguage());
    }

    /**
     * Returns true if the given locale pair is supported for MT.
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @return true | false
     * @throws MachineTranslationException
     */
    @Override
    public boolean supportsLocalePair(Locale p_sourceLocale, Locale p_targetLocale) {
        Map paramMap = getMtParameterMap();
        MachineTranslationProfile mtProfile = (MachineTranslationProfile) paramMap
                .get(MachineTranslator.MT_PROFILE);
        String json = mtProfile.getJsonInfo();
        try
        {
            JSONObject obj = new JSONObject(json);
            // if the params don't exist json exception will be thrown
            getParamsFromJson(p_sourceLocale, p_targetLocale, obj);
            return true;
        } catch (JSONException e)
        {
            return false;
        }
    }

    @Override
    protected String doTranslation(Locale p_sourceLocale, Locale p_targetLocale, String p_string)
            throws MachineTranslationException {
        String processed = preprocessTags(p_string);
        Map paramMap = getMtParameterMap();
        String json = (String) paramMap.get(MTProfileConstants.MT_TILDEMT_STATE_JSON);
        JSONObject obj;
        ServiceParams params;
        try {
            obj = new JSONObject(json);
            params = getParamsFromJson(p_sourceLocale, p_targetLocale, obj);
        } catch (JSONException e) {
            throw new MachineTranslationException(e);
        }

        TildeMTService service;
        try {
            service = new TildeMTService(
                    null,
                    params.clientId
            );
        } catch (URISyntaxException e){
            throw new MachineTranslationException(e);
        }
        TranslateResult res = service.Translate(processed, params.requestParams);

        Double qeThreshold;
        try {
            qeThreshold = getQeThresholdFromJson(p_sourceLocale, p_targetLocale, obj);
        } catch (JSONException e) {
            qeThreshold = null;
        }

        String translation;
        try {
             translation = restoreTags(p_string, res.Translation);
        } catch (SAXException | ParserConfigurationException | IOException e){
            translation = res.Translation;
        }

        return qeThreshold == null || res.QeScore >= qeThreshold ? translation : null;
    }

    private String preprocessTags(String original)
            throws MachineTranslationException {
        GxmlElement gxmlRoot = MTHelper.getGxmlElement(original);
        List childElements = gxmlRoot.getChildElements();
        StringBuilder preprocessed = new StringBuilder();
        for (Object item :
                childElements) {
            GxmlElement element = (GxmlElement)item;
            switch (element.getType()){
                case GxmlElement.TEXT_NODE:
                    preprocessed.append(element.getTextNodeValue());
                    break;
                case GxmlElement.BPT: {
                    String i = getElementAttribute(element, "i");  // functions similar to "id" for BPT and EPT
                    // we assume that there always will be a corresponding EPT tag
                    // that will provide a closing </span> tag; and that BPT does
                    // not have any translatable content inside it
                    preprocessed.append(String.format("<span id=\"%1$s\" type=\"%2$s\">",
                            i, GxmlElement.BPT));
                    break;
                }
                case GxmlElement.EPT:
                    // we assume that always when EPT is encountered there has been a
                    // corresponding BPT before that for which an opening <span> tag
                    // has already been placed; and that EPT does not have any
                    // translatable content inside it
                    preprocessed.append("</span>");
                    break;
                default: {
                    String id = getElementAttribute(element, "id");
                    // we assume that the element doesn't have any translatable content
                    preprocessed.append(String.format("<span id=\"%1$s\" type=\"%2$s\"/>",
                            id, element.getType()));
                    break;
                }

            }
        }
        return "<html>" + preprocessed.toString() + "</html>";
    }

    private String getElementAttribute(GxmlElement element, String attribute) throws MachineTranslationException{
        String id = element.getAttribute(attribute);
        if (id == null) {
            throw new MachineTranslationException(
                    String.format("The GXML node doesn't contain an attribute called '%1$s' - %2$s",
                            attribute, element.toString()));
        }
        return id;
    }

    private String restoreTags(String original, String translation)
            throws ParserConfigurationException,
            IOException,
            SAXException,
            MachineTranslationException{
        GxmlElement gxmlRoot = MTHelper.getGxmlElement(original);
        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        ByteArrayInputStream input =  new ByteArrayInputStream(
                translation.getBytes("UTF-8"));
        Document doc = builder.parse(input);
        NodeList rootNodes = doc.getChildNodes();
        if (rootNodes.getLength() != 1){
            throw new MachineTranslationException("Invalid XML received from LetsMT");
        }
        String restored = _restoreTags(gxmlRoot, rootNodes.item(0));
        int index = original.indexOf(">");
        String head = original.substring(0, index + 1);
        String tail = "</segment>";

        return head + restored + tail;
    }

    private String _restoreTags(GxmlElement gxmlRoot, Node current){
        switch (current.getNodeType()) {
            case Node.TEXT_NODE:
                return current.getTextContent();
            case Node.ELEMENT_NODE:
                Element element = (Element) current;
                if ("html".equals(element.getTagName())){
                    return restoreChildrenTags(gxmlRoot, current);
                }
                String id = element.getAttribute("id");
                String type = element.getAttribute("type");
                if (type.equals(Integer.toString(GxmlElement.BPT))) {
                    // means that a <bpt> and an <ept> tag was replaced
                    // with a single <span> tag with the same id

                    StringBuilder restored = new StringBuilder();
                    // for BPT and EPT "i" functions similar to "id"
                    GxmlElement bpt = gxmlRoot.getDescendantByAttributeValue("i", id, GxmlElement.BPT);
                    GxmlElement ept = gxmlRoot.getDescendantByAttributeValue("i", id, GxmlElement.EPT);
                    restored.append(bpt.toGxml());
                    restored.append(restoreChildrenTags(gxmlRoot, current));
                    restored.append(ept.toGxml());
                    return restored.toString();
                } else {
                    // means that a <ph/> or some other tag was replaced with a
                    // <span> tag with the same id
                    // we assume that there hasn't been any translatable content
                    // inside the tag
                    Integer typeInt = Integer.parseInt(type);
                    GxmlElement node = gxmlRoot.getDescendantByAttributeValue("id", id, typeInt);
                    return node.toGxml();
                }
            default:
                // this should not happen
                return "";
        }
    }

    private String restoreChildrenTags(GxmlElement gxmlRoot, Node current){
        StringBuilder restored = new StringBuilder();
        NodeList childNodes = current.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            restored.append(_restoreTags(gxmlRoot, child));
        }
        return restored.toString();
    }

    private ServiceParams getParamsFromJson(Locale p_sourceLocale, Locale p_targetLocale, JSONObject obj)
            throws JSONException
    {
        String clientId = obj.getString("client-id");
        JSONObject systems = obj.getJSONObject("systems");

        String langKey = createLangKey(p_sourceLocale, p_targetLocale);
        JSONObject system = systems.getJSONObject(langKey);
        JSONObject systemParams = system.getJSONObject("params");
        List<NameValuePair> requestParams = jsonObjectToList(systemParams);
        return new ServiceParams(clientId, requestParams);
    }

    private Double getQeThresholdFromJson(Locale p_sourceLocale, Locale p_targetLocale, JSONObject obj)
            throws JSONException
    {
        JSONObject systems = obj.getJSONObject("systems");

        String langKey = createLangKey(p_sourceLocale, p_targetLocale);
        JSONObject system = systems.getJSONObject(langKey);
        return system.getDouble("qeThreshold");
    }

    private List<NameValuePair> jsonObjectToList(JSONObject jso)
    {
        List<NameValuePair> params = new LinkedList<NameValuePair>();
        Iterator<?> keys = jso.keys();
        while(keys.hasNext())
        {
            String key = (String)keys.next();
            Object item;
            try {
                item = jso.get(key);
            } catch (JSONException e) { throw new Error(e); }  // This should newer happen
            params.add(new BasicNameValuePair(key, item.toString()));
        }
        return params;
    }

    @Override
    protected String[] doBatchTranslation(Locale p_sourceLocale,
                                          Locale p_targetLocale, String[] p_segments)
            throws MachineTranslationException
    {
        String[] translations = new String[p_segments.length];
        for (int i = 0; i < p_segments.length; i++) {
            String segment = p_segments[i];
            translations[i] = doTranslation(p_sourceLocale, p_targetLocale, segment);
        }
        return translations;
    }
}
