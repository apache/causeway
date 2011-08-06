package org.apache.isis.viewer.json.applib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

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

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * A wrapper around {@link JsonNode} that provides some additional helper
 * methods, including searching using xpath (requires optional XOM dependency).
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


    public boolean isArray() {
        return jsonNode.isArray();
    }

    public boolean isValue() {
        return jsonNode.isValueNode();
    }

    public boolean isMap() {
        return !isArray() && !isValue();
    }

    public Link getLink(String path) throws JsonMappingException {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }

        if (node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' (a list) does not represent a link");
        }
        if (node.isValueNode()) {
            throw new IllegalArgumentException("'" + path + "' (a value) does not represent a link");
        }

        Link link = JsonNodeUtils.convert(node, Link.class);
        if(link.getHref() == null || link.getRel() == null) {
            throw new IllegalArgumentException("'" + path + "' (a map) does not fully represent a link");
        }
        return link;
    }

    public JsonRepresentation getRepresentation(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }

        return new JsonRepresentation(node);
    }



    public Integer getInt(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        checkValue(path, node, "an int");
        if (!node.isInt()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not an int");
        }
        return node.getIntValue();
    }

    public Long getLong(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        checkValue(path, node, "a long");
        if (!node.isLong()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not a long");
        }
        return node.getLongValue();
    }
    
    public Double getDouble(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        checkValue(path, node, "a double");
        if (!node.isDouble()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not a double");
        }
        return node.getDoubleValue();
    }

    public String getString(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        checkValue(path, node, "a string");
        if (!node.isTextual()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not a string");
        }
        return node.getTextValue();
    }


    private static void checkValue(String path, JsonNode node, String requiredType) {
        if (node.isValueNode()) {
            return;
        }
        if (node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' (a list) is not " + requiredType);
        } else {
            throw new IllegalArgumentException("'" + path + "' (a map) is not " + requiredType);
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

    private static JsonRepresentation asJsonRepresentation(nu.xom.Document xmlDoc) throws IOException, JsonParseException, JsonMappingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer serializer = new nu.xom.Serializer(baos);
        serializer.write(xmlDoc);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.readFromStream(bais);
        String jsonStr = json.toString();

        return JsonMapper.instance().read(jsonStr, JsonRepresentation.class);
    }

    private JsonNode getNode(String path) {
        String[] keys = path.split("\\.");
        JsonNode node = jsonNode;
        for(String key: keys) {
            node = node.path(key);
        }
        return node;
    }

    @Override
    public String toString() {
        return jsonNode.toString();
    }

}
