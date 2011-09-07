package org.apache.isis.viewer.json.applib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.apache.isis.viewer.json.applib.util.JsonNodeUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.POJONode;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * A wrapper around {@link JsonNode} that provides some additional helper
 * methods, including searching using xpath (requires optional XOM dependency).
 */
public class JsonRepresentation {

    public interface LinksToSelf {
        public Link getSelf();
    }

    public interface HasLinks {
        public JsonRepresentation getLinks();
    }

    public interface HasExtensions {
        public JsonRepresentation getExtensions();
    }

    private static Map<Class<?>, Function<JsonNode, ?>> JSON_NODE_TRANSFORMERS = Maps.newHashMap();
    static {
        JSON_NODE_TRANSFORMERS.put(String.class, new Function<JsonNode, String>() {
            @Override
            public String apply(JsonNode input) {
                if(!input.isTextual()) {
                    throw new IllegalStateException("found node that is not a string " + input.toString());
                }
                return input.getTextValue();
            }});
        JSON_NODE_TRANSFORMERS.put(JsonNode.class, new Function<JsonNode, JsonNode>() {
            @Override
            public JsonNode apply(JsonNode input) {
                return input;
            }});
        JSON_NODE_TRANSFORMERS.put(JsonRepresentation.class, new Function<JsonNode, JsonRepresentation>() {
            @Override
            public JsonRepresentation apply(JsonNode input) {
                return new JsonRepresentation(input);
            }});
        JSON_NODE_TRANSFORMERS.put(Link.class, new Function<JsonNode, Link>() {
            @Override
            public Link apply(JsonNode input) {
                return new Link(input);
            }});
    }
    

    public static JsonRepresentation newMap() {
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
    // getInt, getLong, getDouble, getString
    /////////////////////////////////////////////////////////////////////////


    public Boolean getBoolean(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a boolean");
        if (!node.isBoolean()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not a boolean");
        }
        return node.getBooleanValue();
    }

    public BigInteger getBigInteger(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a biginteger");
        if (!node.isBigInteger()) {
            throw new IllegalArgumentException("'" + path + "' (" + node.toString() + ") is not a biginteger");
        }
        return node.getBigIntegerValue();
    }

    public Integer getInt(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
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
        if (representsNull(node)) {
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
        if (representsNull(node)) {
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
        if (representsNull(node)) {
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
            // not exclude if node.isNull, cos that's the point of this. 
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
            // not exclude if node.isNull, cos that's the point of this. 
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

    private static boolean representsNull(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull();
    }


    
    /////////////////////////////////////////////////////////////////////////
    // getArray, getRepresentation, getLink
    /////////////////////////////////////////////////////////////////////////

    public JsonRepresentation getRepresentation(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }

        return new JsonRepresentation(node);
    }

    public JsonRepresentation getArray(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' is not an array");
        }
        return new JsonRepresentation(node);
    }

    public JsonRepresentation getMap(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }
        if (node.isArray() || node.isValueNode()) {
            throw new IllegalArgumentException("'" + path + "' is not a map");
        }
        return new JsonRepresentation(node);
    }

    public Link getLink(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }

        if (node.isArray()) {
            throw new IllegalArgumentException("'" + path + "' (an array) does not represent a link");
        }
        if (node.isValueNode()) {
            throw new IllegalArgumentException("'" + path + "' (a value) does not represent a link");
        }

        Link link = new Link(node);
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


    /**
     * Convert underlying representation into an array.
     */
    protected ArrayNode nodeAsArray() {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        return (ArrayNode) getJsonNode();
    }

    /**
     * Convert underlying representation into an object (map).
     */
    protected ObjectNode nodeAsMap() {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        return (ObjectNode) getJsonNode();
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
        try {
            // puts object structure under a <o>
            org.jdom.Document jdomDoc = new SAXBuilder().build(new StringReader(toXml()));

            String prefix = jsonNode.isArray()?"a":"o";
            XPath xpath = XPath.newInstance("/" + prefix + xpathExpression);
            
            @SuppressWarnings("unchecked")
            List<Element> matchingElements = xpath.selectNodes(jdomDoc);
            
            org.jdom.Document doc = new org.jdom.Document(new org.jdom.Element(prefix));
            for (Element el: matchingElements) {
                el.detach();
                doc.getRootElement().addContent(el);
            }
            JsonRepresentation matchedRepresentation = asJsonRepresentation(doc);
            if(matchedRepresentation == null) {
                return null;
            }
            return matchedRepresentation;

        } catch (JDOMException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    private String toXml() {
        XMLSerializer serializer = new XMLSerializer();
        JSON json = JSONSerializer.toJSON(jsonNode.toString());
        String xml = serializer.write(json);
        return xml;
    }
    

    private static JsonRepresentation asJsonRepresentation(org.jdom.Document doc) {
        try {
            return asJsonRepresentation(asJson(asInputStream(doc)));
        } catch (IOException e) {
            // shouldn't occur
            throw new RuntimeException(e);
        }
    }


    private static InputStream asInputStream(org.jdom.Document doc) throws IOException {
        XMLOutputter outputter = new XMLOutputter();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        outputter.output(doc, baos);

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
        return JsonMapper.instance().read(jsonStr, JsonRepresentation.class);
    }


    public String asUrlEncoded() {
        return JsonNodeUtils.asUrlEncoded(getJsonNode());
    }



    /////////////////////////////////////////////////////////////////////////
    // mutable (array)
    /////////////////////////////////////////////////////////////////////////

    public void add(Object value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(new POJONode(value));
    }

    public void add(JsonRepresentation value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value.getJsonNode());
    }

    public void add(String value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public void add(JsonNode value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public void add(long value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public void add(int value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public void add(double value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public void add(float value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public void add(boolean value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        nodeAsArray().add(value);
    }

    public <T> Iterable<T> arrayIterable(final Class<T> requiredType) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return arrayIterator(requiredType);
            }
        };
    }

    public <T> Iterator<T> arrayIterator(final Class<T> requiredType) {
        ensureIsAnArrayAtLeastAsLargeAs(0);
        Function<JsonNode, ?> transformer = JSON_NODE_TRANSFORMERS.get(requiredType);
        if(transformer == null) {
            throw new IllegalArgumentException("Conversions from JsonNode to " + requiredType + " are not supported");
        }
        ArrayNode arrayNode = (ArrayNode)jsonNode;
        Iterator<JsonNode> iterator = arrayNode.iterator();
        Function<JsonNode, T> typedTransformer = asT(transformer); // necessary to do in two steps
        return Iterators.transform(iterator, typedTransformer);
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<JsonNode, T> asT(Function<JsonNode, ?> transformer) {
        return (Function<JsonNode, T>) transformer;
    }

    public JsonRepresentation elementAt(int i) {
        ensureIsAnArrayAtLeastAsLargeAs(i);
        return new JsonRepresentation(jsonNode.get(i));
    }

    public void setElementAt(int i, JsonRepresentation objectRepr) {
        ensureIsAnArrayAtLeastAsLargeAs(i);
        if(objectRepr.isArray()) {
            throw new IllegalArgumentException("Representation being set cannot be an array");
        }
        // can safely downcast because *this* representation is an array
        ArrayNode arrayNode = (ArrayNode)jsonNode;
        arrayNode.set(i, objectRepr.getJsonNode());
    }

    private void ensureIsAnArrayAtLeastAsLargeAs(int i) {
        if (!jsonNode.isArray()) {
            throw new IllegalStateException("Is not an array");
        }
        if(i >= arraySize()) {
            throw new IndexOutOfBoundsException("array has " + arraySize() + " elements"); 
        }
    }

    /////////////////////////////////////////////////////////////////////////
    // mutable (map)
    /////////////////////////////////////////////////////////////////////////

    public void put(String key, Object value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), new POJONode(value));
    }

    public void put(String key, JsonRepresentation value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        if(node.has(path.getTail())) {
            throw new IllegalStateException("already has key " + key);
        }
        node.put(path.getTail(), value.getJsonNode());
    }

    public void put(String key, String value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void put(String key, JsonNode value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void put(String key, long value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void put(String key, int value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void put(String key, double value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void put(String key, float value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void put(String key, boolean value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(nodeAsMap(), path.getHead());
        node.put(path.getTail(), value);
    }

    private static class Path {
        private final List<String> head;
        private final String tail;

        private Path(List<String> head, String tail) {
            this.head = Collections.unmodifiableList(head);
            this.tail = tail;
        }
        
        public List<String> getHead() {
            return head;
        }

        public String getTail() {
            return tail;
        }

        public static Path parse(String pathStr) {
            List<String> keyList = Lists.newArrayList(Arrays.asList(pathStr.split("\\.")));
            if(keyList.size() == 0) {
                throw new IllegalArgumentException(String.format("Malformed path '%s'", pathStr));
            }
            String tail = keyList.remove(keyList.size()-1);
            return new Path(keyList, tail);
        }
    }
    

    /////////////////////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return jsonNode.toString();
    }







}
