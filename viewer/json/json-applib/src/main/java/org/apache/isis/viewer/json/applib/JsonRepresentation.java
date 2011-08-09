package org.apache.isis.viewer.json.applib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.Serializer;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;


/**
 * A wrapper around {@link JsonNode} that provides some additional helper
 * methods, including searching using xpath (requires optional XOM dependency).
 */
public class JsonRepresentation {

    public static JsonRepresentation newObject() {
        return new JsonRepresentation(new ObjectNode(JsonNodeFactory.instance));
    }

    public static JsonRepresentation newArray() {
        return newArray(0);
    }

    public static JsonRepresentation newArray(int initialSize) {
        ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
        for(int i=0; i<initialSize; i++) {
            arrayNode.addNull();
        }
        return new JsonRepresentation(arrayNode);
    }

    protected final JsonNode jsonNode;

    public JsonRepresentation(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public int arraySize() {
        if (!isArray()) {
            throw new IllegalStateException("not an array");
        }
        return jsonNode.size();
    }
    
    public int mapSize() {
        if (!isMap()) {
            throw new IllegalStateException("not a map");
        }
        return jsonNode.size();
    }


    /////////////////////////////////////////////////////////////////////////
    // isArray, isMap, isValue, isNull
    /////////////////////////////////////////////////////////////////////////

    public boolean isArray() {
        return jsonNode.isArray();
    }

    /**
     * Node is a value (nb: could be {@link #isNull() null}).
     */
    public boolean isValue() {
        return jsonNode.isValueNode();
    }

    public boolean isNull() {
        return jsonNode.isNull();
    }

    public boolean isMap() {
        return !isArray() && !isValue();
    }


    /////////////////////////////////////////////////////////////////////////
    // elementAt, setElementAt
    /////////////////////////////////////////////////////////////////////////

    public JsonRepresentation elementAt(int i) {
        ensureIsAnArrayNoLargerThan(i);
        return new JsonRepresentation(jsonNode.get(i));
    }

    public void setElementAt(int i, JsonRepresentation objectRepr) {
        ensureIsAnArrayNoLargerThan(i);
        if(objectRepr.isArray()) {
            throw new IllegalArgumentException("Representation being set cannot be an array");
        }
        // can safely downcast because *this* representation is an array
        ArrayNode arrayNode = (ArrayNode)jsonNode;
        arrayNode.set(i, objectRepr.getJsonNode());
    }

    private void ensureIsAnArrayNoLargerThan(int i) {
        if (!jsonNode.isArray()) {
            throw new IllegalStateException("Is not an array");
        }
        if(i >= arraySize()) {
            throw new IndexOutOfBoundsException("array has " + arraySize() + " elements"); 
        }
    }


    /////////////////////////////////////////////////////////////////////////
    // getInt, getLong, getDouble, getString
    /////////////////////////////////////////////////////////////////////////


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

    /**
     * Either returns a {@link JsonRepresentation} that indicates that the wrapped node has <tt>null</tt> value
     * (ie {@link JsonRepresentation#isNull()}), or returns <tt>null</tt> if there was no node with the
     * provided path.
     */
    public JsonRepresentation getNull(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        checkValue(path, node, "the null value");
        if (!node.isNull()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not the null value");
        }
        return new JsonRepresentation(node);
    }

    /**
     * Indicates that the wrapped node has <tt>null</tt> value
     * (ie {@link JsonRepresentation#isNull()}), or returns <tt>null</tt> if there was no node with the
     * provided path.
     */
    public Boolean isNull(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        return node.isNull();
    }

    private JsonNode getNode(String path) {
        return JsonNodeUtils.walkNode(jsonNode, path);
    }

    private static void checkValue(String path, JsonNode node, String requiredType) {
        if (node.isValueNode()) {
            return;
        }
        if (node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' (an array) is not " + requiredType);
        } else {
            throw new IllegalArgumentException("'" + path + "' (a map) is not " + requiredType);
        }
    }


    
    /////////////////////////////////////////////////////////////////////////
    // getArray, getRepresentation, getLink
    /////////////////////////////////////////////////////////////////////////

    public JsonRepresentation getRepresentation(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }

        return new JsonRepresentation(node);
    }

    public JsonRepresentation getArray(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        if (node.isValueNode()) {
            throw new IllegalArgumentException("'" + path + "' (a value) is not an array");
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' (a map) is not an array");
        }
        return new JsonRepresentation(node);
    }

    public Link getLink(String path) {
        JsonNode node = getNode(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }

        if (node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' (an array) does not represent a link");
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

    /**
     * Convert a representation that contains a single node representing a link
     * into a {@link Link}.
     */
    public Link asLink() {
        if(getJsonNode().size() != 1) {
            throw new IllegalStateException("does not represent link");
        }
        String linkPropertyName = getJsonNode().getFieldNames().next();
        return getLink(linkPropertyName);
    }



    /////////////////////////////////////////////////////////////////////////
    // streaming support
    /////////////////////////////////////////////////////////////////////////

    public InputStream asInputStream() {
        return JsonNodeUtils.asInputStream(jsonNode);
    }

    /////////////////////////////////////////////////////////////////////////
    // xpath support
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Requires xom:xom:1.1 (LGPL) to be added as a dependency.
     */
    public JsonRepresentation xpath(String xpathExpression) {
        // puts object structure under a <o>
        Document document = toXomDoc();
        
        String prefix = jsonNode.isArray()?"a":"o";
        Nodes matchingNodes = document.query("/" + prefix + xpathExpression);
        Document doc = new Document(new nu.xom.Element(prefix));
        for (int i = 0; i < matchingNodes.size(); i++) {
            Node matchingNode = matchingNodes.get(i);
            matchingNode.detach();
            doc.getRootElement().appendChild(matchingNode);
        }
        JsonRepresentation matchedRepresentation = asJsonRepresentation(doc);
        if(matchedRepresentation == null) {
            return null;
        }
        return matchedRepresentation;
    }

    private Document toXomDoc() {
        String xml = toXml();
        Builder builder = new nu.xom.Builder();
        Document document;
        try {
            document = builder.build(xml, ".");
        } catch (Exception e) {
            // shouldn't occur
            throw new RuntimeException(e);
        }
        return document;
    }

    private String toXml() {
        XMLSerializer serializer = new XMLSerializer();
        JSON json = JSONSerializer.toJSON(jsonNode.toString());
        String xml = serializer.write(json);
        return xml;
    }
    

    private static JsonRepresentation asJsonRepresentation(nu.xom.Document xmlDoc) {
        try {
            return asJsonRepresentation(asJson(asInputStream(xmlDoc)));
        } catch (IOException e) {
            // shouldn't occur
            throw new RuntimeException(e);
        }
    }


    private static InputStream asInputStream(nu.xom.Document xmlDoc) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Serializer serializer = new nu.xom.Serializer(baos);
        serializer.write(xmlDoc);

        @SuppressWarnings("unused")
        String xml = new String(baos.toByteArray());

        InputStream bais = new ByteArrayInputStream(baos.toByteArray());
        return bais; 
    }
    
    private static JSON asJson(InputStream bais) {
        XMLSerializer xmlSerializer = new XMLSerializer();
        JSON json = xmlSerializer.readFromStream(bais);
        return json;
    }

    private static JsonRepresentation asJsonRepresentation(JSON json) throws JsonParseException, JsonMappingException, IOException {
        if(json == JSONNull.getInstance()) {
            return null;
        }
        String jsonStr = json.toString();
        JsonRepresentation jsonRepresentation = JsonMapper.instance().read(jsonStr, JsonRepresentation.class);
        return jsonRepresentation;
    }

    
    /////////////////////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return jsonNode.toString();
    }




}
