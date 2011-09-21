package org.apache.isis.viewer.json.applib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Constructor;
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

    private static Map<Class<?>, Function<JsonNode, ?>> REPRESENTATION_INSTANTIATORS = Maps.newHashMap();
    static {
        REPRESENTATION_INSTANTIATORS.put(String.class, new Function<JsonNode, String>() {
            @Override
            public String apply(JsonNode input) {
                if(!input.isTextual()) {
                    throw new IllegalStateException("found node that is not a string " + input.toString());
                }
                return input.getTextValue();
            }});
        REPRESENTATION_INSTANTIATORS.put(JsonNode.class, new Function<JsonNode, JsonNode>() {
            @Override
            public JsonNode apply(JsonNode input) {
                return input;
            }});
    }

    private static <T> Function<JsonNode, ?> representationInstantiatorFor(final Class<T> representationType) {
        Function<JsonNode, ?> transformer = REPRESENTATION_INSTANTIATORS.get(representationType);
        if(transformer == null) {
            transformer = new Function<JsonNode, T>() {
                @Override
                public T apply(JsonNode input) {
                    try {
                        Constructor<T> constructor = representationType.getConstructor(JsonNode.class);
                        return constructor.newInstance(input);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Conversions from JsonNode to " + representationType + " are not supported");
                    }
                }
                
            };
            REPRESENTATION_INSTANTIATORS.put(representationType, transformer);
        }
        return transformer;
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

    public JsonNode asJsonNode() {
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
    // getRepresentation
    /////////////////////////////////////////////////////////////////////////

    public JsonRepresentation getRepresentation(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }

        return new JsonRepresentation(node);
    }

    /////////////////////////////////////////////////////////////////////////
    // getArray, isArray, asArray
    /////////////////////////////////////////////////////////////////////////
    
    public JsonRepresentation getArray(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }
        if (!node.isArray()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not an array"));
        }
        return new JsonRepresentation(node);
    }


    
    /////////////////////////////////////////////////////////////////////////
    // getMap, isMap, asMap
    /////////////////////////////////////////////////////////////////////////
    
    public JsonRepresentation getMap(String path) {
        JsonNode node = getNode(path);
        if (representsNull(node)) {
            return null;
        }
        if (node.isArray() || node.isValueNode()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a map"));
        }
        return new JsonRepresentation(node);
    }

    
    /////////////////////////////////////////////////////////////////////////
    // isBoolean, getBoolean, asBoolean 
    /////////////////////////////////////////////////////////////////////////

    public boolean isBoolean(String path) {
        return isBoolean(getNode(path));
    }

    public boolean isBoolean() {
        return isBoolean(asJsonNode());
    }

    private boolean isBoolean(JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isBoolean();
    }

    public Boolean getBoolean(String path) {
        return getBoolean(path, getNode(path));
    }

    public Boolean asBoolean() {
        return getBoolean(null, asJsonNode());
    }

    private Boolean getBoolean(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a boolean");
        if (!node.isBoolean()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a boolean"));
        }
        return node.getBooleanValue();
    }


    
    /////////////////////////////////////////////////////////////////////////
    // isBigInteger, getBigInteger, asBigInteger
    /////////////////////////////////////////////////////////////////////////

    public boolean isBigInteger(String path) {
        return isBigInteger(getNode(path));
    }

    public boolean isBigInteger() {
        return isBigInteger(asJsonNode());
    }

    private boolean isBigInteger(JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isBigInteger();
    }

    public BigInteger getBigInteger(String path) {
        JsonNode node = getNode(path);
        return getBigInteger(path, node);
    }

    public BigInteger asBigInteger() {
        return getBigInteger(null, asJsonNode());
    }

    private BigInteger getBigInteger(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a biginteger");
        if (!node.isBigInteger()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a biginteger"));
        }
        return node.getBigIntegerValue();
    }


    
    /////////////////////////////////////////////////////////////////////////
    // isInt, getInt, asInt
    /////////////////////////////////////////////////////////////////////////

    public boolean isInt(String path) {
        return isInt(getNode(path));
    }

    public boolean isInt() {
        return isInt(asJsonNode());
    }

    private boolean isInt(JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isInt();
    }
    
    public Integer getInt(String path) {
        JsonNode node = getNode(path);
        return getInt(path, node);
    }

    public Integer asInt() {
        return getInt(null, asJsonNode());
    }

    private Integer getInt(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "an int");
        if (!node.isInt()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not an int"));
        }
        return node.getIntValue();
    }

    
    /////////////////////////////////////////////////////////////////////////
    // isLong, getLong, asLong
    /////////////////////////////////////////////////////////////////////////
    
    public boolean isLong(String path) {
        return isLong(getNode(path));
    }

    public boolean isLong() {
        return isLong(asJsonNode());
    }

    private boolean isLong(JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isLong();
    }

    public Long getLong(String path) {
        JsonNode node = getNode(path);
        return getLong(path, node);
    }

    public Long asLong() {
        return getLong(null, asJsonNode());
    }

    private Long getLong(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a long");
        if (!node.isLong()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a long"));
        }
        return node.getLongValue();
    }


    /////////////////////////////////////////////////////////////////////////
    // isDouble, getDouble, asDouble
    /////////////////////////////////////////////////////////////////////////

    public boolean isDouble(String path) {
        return isDouble(getNode(path));
    }

    public boolean isDouble() {
        return isDouble(asJsonNode());
    }

    private boolean isDouble(JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isDouble();
    }

    public Double getDouble(String path) {
        JsonNode node = getNode(path);
        return getDouble(path, node);
    }

    public Double asDouble() {
        return getDouble(null, asJsonNode());
    }

    private Double getDouble(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a double");
        if (!node.isDouble()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a double"));
        }
        return node.getDoubleValue();
    }

    /////////////////////////////////////////////////////////////////////////
    // getString, isString, asString
    /////////////////////////////////////////////////////////////////////////

    
    public boolean isString(String path) {
        return isString(getNode(path));
    }

    public boolean isString() {
        return isString(asJsonNode());
    }

    private boolean isString(JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isTextual();
    }

    public String getString(String path) {
        JsonNode node = getNode(path);
        return getString(path, node);
    }

    public String asString() {
        return getString(null, asJsonNode());
    }

    private String getString(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a string");
        if (!node.isTextual()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a string"));
        }
        return node.getTextValue();
    }

    
    /////////////////////////////////////////////////////////////////////////
    // getLink, asLink, isLink
    /////////////////////////////////////////////////////////////////////////


    public boolean isLink() {
        return isLink(asJsonNode());
    }

    public boolean isLink(String path) {
        return isLink(getNode(path));
    }

    public boolean isLink(JsonNode node) {
        if (representsNull(node) || node.isArray() || node.isValueNode()) {
            return false;
        }

        Link link = new Link(node);
        if(link.getHref() == null || link.getRel() == null) {
            return false;
        }
        return true;
    }

    public Link getLink(String path) {
        return getLink(path, getNode(path));
    }

    public Link asLink() {
        return getLink(null, asJsonNode());
    }
    
    private Link getLink(String path, JsonNode node) {
        if (representsNull(node)) {
            return null;
        }

        if (node.isArray()) {
            throw new IllegalArgumentException(formatExMsg(path, "is an array that does not represent a link"));
        }
        if (node.isValueNode()) {
            throw new IllegalArgumentException(formatExMsg(path, "is a value that does not represent a link"));
        }

        Link link = new Link(node);
        if(link.getHref() == null || link.getRel() == null) {
            throw new IllegalArgumentException(formatExMsg(path, "is a map that does not fully represent a link"));
        }
        return link;
    }

    
    /////////////////////////////////////////////////////////////////////////
    // getNull, isNull
    /////////////////////////////////////////////////////////////////////////
    
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

    

    /////////////////////////////////////////////////////////////////////////
    // mapValueAsLink
    /////////////////////////////////////////////////////////////////////////
    
    /**
     * Convert a representation that contains a single node representing a link
     * into a {@link Link}.
     */
    public Link mapValueAsLink() {
        if(asJsonNode().size() != 1) {
            throw new IllegalStateException("does not represent link");
        }
        String linkPropertyName = asJsonNode().getFieldNames().next();
        return getLink(linkPropertyName);
    }

    /**
     * Convert underlying representation into an array.
     */
    protected ArrayNode asArrayNode() {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        return (ArrayNode) asJsonNode();
    }

    /**
     * Convert underlying representation into an object (map).
     */
    protected ObjectNode asObjectNode() {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        return (ObjectNode) asJsonNode();
    }



    /////////////////////////////////////////////////////////////////////////
    // asInputStream
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

    public String asUrlEncoded() {
        return JsonNodeUtils.asUrlEncoded(asJsonNode());
    }



    /////////////////////////////////////////////////////////////////////////
    // mutable (array)
    /////////////////////////////////////////////////////////////////////////

    public void arrayAdd(Object value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(new POJONode(value));
    }

    public void arrayAdd(JsonRepresentation value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value.asJsonNode());
    }

    public void arrayAdd(String value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(JsonNode value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(long value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(int value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(double value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(float value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(boolean value) {
        if(!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public Iterable<JsonRepresentation> arrayIterable() {
        return arrayIterable(JsonRepresentation.class);
    }

    public <T> Iterable<T> arrayIterable(final Class<T> requiredType) {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return arrayIterator(requiredType);
            }
        };
    }

    public Iterator<JsonRepresentation> arrayIterator() {
        return arrayIterator(JsonRepresentation.class);
    }

    public <T> Iterator<T> arrayIterator(final Class<T> requiredType) {
        ensureIsAnArrayAtLeastAsLargeAs(0);
        Function<JsonNode, ?> transformer = representationInstantiatorFor(requiredType);
        ArrayNode arrayNode = (ArrayNode)jsonNode;
        Iterator<JsonNode> iterator = arrayNode.iterator();
        Function<JsonNode, T> typedTransformer = asT(transformer); // necessary to do in two steps
        return Iterators.transform(iterator, typedTransformer);
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<JsonNode, T> asT(Function<JsonNode, ?> transformer) {
        return (Function<JsonNode, T>) transformer;
    }

    public JsonRepresentation arrayGet(int i) {
        ensureIsAnArrayAtLeastAsLargeAs(i);
        return new JsonRepresentation(jsonNode.get(i));
    }

    public void arraySetElementAt(int i, JsonRepresentation objectRepr) {
        ensureIsAnArrayAtLeastAsLargeAs(i);
        if(objectRepr.isArray()) {
            throw new IllegalArgumentException("Representation being set cannot be an array");
        }
        // can safely downcast because *this* representation is an array
        ArrayNode arrayNode = (ArrayNode)jsonNode;
        arrayNode.set(i, objectRepr.asJsonNode());
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

    public void mapPut(String key, Object value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), new POJONode(value));
    }

    public void mapPut(String key, JsonRepresentation value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        if(node.has(path.getTail())) {
            throw new IllegalStateException("already has key " + key);
        }
        node.put(path.getTail(), value.asJsonNode());
    }

    public void mapPut(String key, String value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(String key, JsonNode value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if(value == null) {
            return;
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(String key, long value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(String key, int value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(String key, double value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(String key, float value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(String key, boolean value) {
        if(!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        Path path = Path.parse(key);
        ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
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
    // helpers
    /////////////////////////////////////////////////////////////////////////

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

    private static JsonRepresentation asJsonRepresentation(JSON json) throws JsonParseException, JsonMappingException, IOException {
        if(json == JSONNull.getInstance()) {
            return null;
        }
        String jsonStr = json.toString();
        return JsonMapper.instance().read(jsonStr, JsonRepresentation.class);
    }

    private static String formatExMsg(String pathIfAny, String errorText) {
        StringBuilder buf = new StringBuilder();
        if(pathIfAny != null) {
            buf.append("'").append(pathIfAny).append("' ");
        }
        buf.append(errorText);
        return buf.toString();
    }




    /////////////////////////////////////////////////////////////////////////
    // toString
    /////////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return jsonNode.toString();
    }



}
