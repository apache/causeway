package org.apache.isis.viewer.json.applib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.Serializer;
import nu.xom.ValidityException;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Charsets;

/**
 * A wrapper around {@link JsonNode} that provides some additional helper
 * methods.
 */
public class JsonRepresentation {

    private final JsonNode jsonNode;

    public JsonRepresentation(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public int size() {
        if (!jsonNode.isArray()) {
            throw new IllegalStateException("Is an array");
        }
        return jsonNode.size();
    }

    public String getString(String key) {
        JsonNode subNode = jsonNode.get(key);
        if (subNode == null) {
            return null;
        }
        ensureValue(key, subNode, "string");
        if (!subNode.isTextual()) {
            throw new IllegalArgumentException("'" + key + "' (" + subNode.toString() + ") is not a string");
        }
        return subNode.getTextValue();
    }

    private void ensureValue(String key, JsonNode subNode, String requiredType) {
        if (subNode.isValueNode()) {
            return;
        }
        if (subNode.isArray()) {
            throw new IllegalArgumentException("'" + key + "' (a list) is not a " + requiredType);
        } else {
            throw new IllegalArgumentException("'" + key + "' (a map) is not a " + requiredType);
        }
    }

    public Link getLink(String key) throws JsonMappingException {
        JsonNode subNode = jsonNode.get(key);
        // TODO: extra checking here required

        try {
            // TODO: review, rather heavyweight
            return JsonMapper.instance().read(subNode.toString(), Link.class);
        } catch (JsonParseException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        } catch (IOException e) {
            // shouldn't happen
            throw new RuntimeException(e);
        }
    }

    /**
     * Requires XOM to be added as a dependency.
     */
    public String toXml() {
        XMLSerializer serializer = new XMLSerializer();
        JSON json = JSONSerializer.toJSON(jsonNode.toString());
        String xml = serializer.write(json);
        return xml;
    }

    /**
     * Requires XOM to be added as a dependency.
     */
    public JsonRepresentation xpath(String xpathExpression) throws ValidityException, ParsingException, IOException {
        String xml = toXml();
        Builder builder = new nu.xom.Builder();
        Document document = builder.build(xml, ".");
        // toXml() puts structure under a <o> root.
        Nodes matchingNodes = document.query("/o" + xpathExpression);
        Document doc;
        switch (matchingNodes.size()) {
        case 0:
            return null;
        case 1:
            Node node = matchingNodes.get(0);
            if (!(node instanceof Element)) {
                return null;
            }
            Element el = (Element) node;
            el.detach();
            doc = new nu.xom.Document(el);
            return asJsonRepresentation(doc);
        default:
            doc = new Document(new nu.xom.Element("o"));
            for (int i = 0; i < matchingNodes.size(); i++) {
                Node matchingNode = matchingNodes.get(i);
                matchingNode.detach();
                doc.getRootElement().appendChild(matchingNode);
            }
            return asJsonRepresentation(doc);
        }
    }

    private JsonRepresentation asJsonRepresentation(nu.xom.Document document2) throws IOException, JsonParseException, JsonMappingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer serializer = new nu.xom.Serializer(baos);
        serializer.write(document2);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.readFromStream(bais);
        String jsonStr = json.toString();

        return JsonMapper.instance().read(jsonStr, JsonRepresentation.class);
    }

    public boolean isArray() {
        return jsonNode.isArray();
    }

    
    
    @Override
    public String toString() {
        return jsonNode.toString();
    }
    

}
