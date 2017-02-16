package com.globalsight.machineTranslation.tildemt;

import com.globalsight.everest.projecthandler.MachineTranslationProfile;
import com.globalsight.everest.webapp.pagehandler.administration.mtprofile.MTProfileConstants;
import com.globalsight.machineTranslation.AbstractTranslator;
import com.globalsight.machineTranslation.MTHelper;
import com.globalsight.machineTranslation.MachineTranslationException;
import com.globalsight.machineTranslation.MachineTranslator;
import com.globalsight.util.gxml.GxmlElement;
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
import java.net.URISyntaxException;
import java.util.*;

/**
 * The implementation of the Tilde MT translation engine. This class: <br>
 * 1) Manages parsing of the Tilde MT system selection web components state JSON. The web component implements the
 *    UI for selecting and configuring Tilde MT translation systems for different source and target language pairs.
 *    This class merely retrieves the configuration for the intended translation direction and passes it to
 *    {@link TildeMTService}. <br>
 * 2) Uses {@link TildeMTService} to make translation requests. <br>
 * 3) Handles tag processing to convert GXML to a more Tilde MT friendly format, and back. <br>
 * <p>
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

    /**
     * Converts source and target locale to a key to be used to retrieve system's configuration from the Tilde MT system
     * selection web component's state JSON.
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @return a key in the form <em>src_trg</em> where <em>src</em> and <em>trg</em> are the two letter country codes
     * of the respective locales.
     */
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

    /**
     * Translates the given string from source language to target. The string can be either
     * plaintext or GXML
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @param p_string plaintext or GXML to translate
     * @return translated plaintext or GXML
     * @throws MachineTranslationException the translation request can fail for various reasons related to the current
     * status of the translation system or wrong request parameters. In such an event the JSON response from the
     * Tilde MT API contains the error code and message which is then wrapped in an exception. This should be shown
     * to the user as it often indicates the further actions for her to take, i.e., wait for the system to wake up, go
     * to the Tilde MT web platform to turn the system on, etc.
     */
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

        // If Quality Estimation is available for the system and the threshold is configured
        // drop segments that whose QE score is bellow the threshold. Intended to not show low
        // quality results the user.
        return qeThreshold == null || res.QeScore >= qeThreshold ? translation : null;
    }

    /**
     * Replaces the tags in text to support translating with Tilde MT. All &lt;bpt&gt; and &lt;ept&gt;
     * tags are restructured to form a single tag, i.e.,
     * &lt;bpt i="12"&gt;some-encoded-tag&lt;/bpt&gt;Text to translate&lt;ept i="12&gt;some-encoded-tag&lt;/ept&gt;
     * is replaced with &lt;span id="12"&gt;Text to translate&lt;/span&gt;. &lt;ph/&gt; tags are processed similarly.
     * This is done to restore the original XML structure of the document which has been obfuscated by some earlier
     * process that wraps the original tags found in text.
     * <p>
     * This is the inverse of {@link #restoreTags(String, String)} method.
     *
     * @param original text to process
     * @return {@code original} if it's plaintext. Else text with matching &lt;bpt&gt; and &lt;ept&gt; tags replaced
     * with a &lt;span&gt; tag.
     * @throws MachineTranslationException
     */
    private String preprocessTags(String original)
            throws MachineTranslationException {
        GxmlElement gxmlRoot = MTHelper.getGxmlElement(original);
        if (gxmlRoot == null){
            // this is a plaintext. no tag processing needed
            return original;
        }
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
                default: { // to catch <ph/> or other tags
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

    /**
     * A helper method that wraps the GxmlElement.getAttribute() method to throw an exception instead of returning null
     * when the attribute doesn't exist.
     *
     * @param element the element to probe
     * @param attribute the attribute to get
     * @return the attribute's value
     * @throws MachineTranslationException when the attribute does not exist
     */
    private String getElementAttribute(GxmlElement element, String attribute) throws MachineTranslationException{
        String id = element.getAttribute(attribute);
        if (id == null) {
            throw new MachineTranslationException(
                    String.format("The GXML node doesn't contain an attribute called '%1$s' - %2$s",
                            attribute, element.toString()));
        }
        return id;
    }

    /**
     * Restore the tags to their original form after translating a text with Tilde MT. All &lt;span&gt; tags are
     * replaced with the original &lt;bpt&gt; and &lt;ept&gt; tags, i.e.,
     * &lt;span id="12"&gt;Translated text&lt;/span&gt; is replaced with
     * &lt;bpt i="12"&gt;original-encoded-tag&lt;/bpt&gt;Translated text&lt;ept i="12&gt;original-encoded-tag&lt;/ept&gt;.
     * <p>
     * This method is the inverse of {@link #preprocessTags(String)} method.
     *
     * @param original the original GXML string to get the original tags from.
     * @param translation the translation returned by Lets MT. Either HTML or plaintext.
     * @return unmodified {@code translation} if it's plaintext. Else text with &lt;span&gt; tags replaced with
     * mathching &lt;bpt&gt; and &lt;ept&gt; tags.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws MachineTranslationException
     */
    private String restoreTags(String original, String translation)
            throws ParserConfigurationException,
            IOException,
            SAXException,
            MachineTranslationException{
        GxmlElement gxmlRoot = MTHelper.getGxmlElement(original);
        if (gxmlRoot == null){
            // the original was a plaintext. no tag processing was done. no restoring needed
            return translation;
        }
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

    /**
     * Convert a HTML node to text with restored original GXML tags.
     * <p>
     * This is a helper method used by {@link #restoreTags(String, String)} to do the recursive traversal of a HTML tree
     * to restore the corresponding tags found in the original text. Recursively interacts with
     * {@link #restoreChildrenTags(GxmlElement, Node)}.
     *
     * @param gxmlRoot GXML of the original segment to extract the original tags from
     * @param current the current root node of a HTML tree to traverse
     * @return text with tags restored
     */
    private String _restoreTags(GxmlElement gxmlRoot, Node current){
        // the method contains branching logic for different tag types and does the actual tag restoring.
        // it calls the restoreChildrenTags() method to manage recursion.
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

    /**
     * Convert a HTML node's child elements to text with restored original GXML tags.
     * <p>
     * Recursively interacts with {@link #_restoreTags(GxmlElement, Node)}.
     *
     * @param gxmlRoot GXML of the original segment to extract the original tags from
     * @param current the node whose children to traverse
     * @return
     */
    private String restoreChildrenTags(GxmlElement gxmlRoot, Node current){
        // the method manages recursion for subtree traversal during tag restoration.
        // the actual tag processing is done in the _restoreTags() method.
        StringBuilder restored = new StringBuilder();
        NodeList childNodes = current.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            restored.append(_restoreTags(gxmlRoot, child));
        }
        return restored.toString();
    }

    /**
     * Gets the Tilde MT API request parameters for a source and target language combination. The information is
     * extracted from the Tilde MT system selection web component's state JSON containing system configuration for one
     * or more language pairs.
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @param obj the Tilde MT system selection web components state JSON
     * @return object containing parameters to be used for Tilde MT API requests.
     * @throws JSONException
     */
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

    /**
     * Gets the Quality Estimation threshold value for a translation system. It is a user specified threshold to be used
     * for  filtering translation results received from Tilde MT. The value is available only if the translation system
     * supports QE and the user has specified a threshold for that system.
     *
     * @param p_sourceLocale indicates source language of the system
     * @param p_targetLocale indicates target language of the system
     * @param obj the Tilde MT system selection web components state JSON
     * @return a threshold value in the interval 0-1 or {@code null} if the value does not exist
     * @throws JSONException
     */
    private Double getQeThresholdFromJson(Locale p_sourceLocale, Locale p_targetLocale, JSONObject obj)
            throws JSONException
    {
        JSONObject systems = obj.getJSONObject("systems");

        String langKey = createLangKey(p_sourceLocale, p_targetLocale);
        JSONObject system = systems.getJSONObject(langKey);
        return system.getDouble("qeThreshold");
    }

    /**
     * Retrieves all key value pairs of a {@code JSONObject} in a list.
     *
     * @param jso JSON to convert
     * @return a list of key (String) and value (converted to String) pairs.
     */
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

    /**
     * Maps {@link #doTranslation(Locale, Locale, String)} to each element of an array of translation segments.
     *
     * @param p_sourceLocale source
     * @param p_targetLocale target
     * @param p_segments an array of plaintext or GXML segments to translate
     * @return an array of respective plaintext or GXML translations
     * @throws MachineTranslationException the translation request can fail for various reasons related to the current
     * status of the translation system or wrong request parameters. In such an event the JSON response from the
     * Tilde MT API contains the error code and message which is then wrapped in an exception. This should be shown
     * to the user as it often indicates the further actions for her to take, i.e., wait for the system to wake up, go
     * to the Tilde MT web platform to turn the system on, etc.
     */
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
