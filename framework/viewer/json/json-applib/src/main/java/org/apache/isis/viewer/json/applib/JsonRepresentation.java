/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.json.applib;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.NullNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.POJONode;

import org.apache.isis.viewer.json.applib.links.LinkRepresentation;
import org.apache.isis.viewer.json.applib.util.JsonNodeUtils;
import org.apache.isis.viewer.json.applib.util.PathNode;
import org.apache.isis.viewer.json.applib.util.UrlEncodingUtils;

/**
 * A wrapper around {@link JsonNode} that provides some additional helper
 * methods.
 */
public class JsonRepresentation {

    public interface LinksToSelf {
        public LinkRepresentation getSelf();
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
            public String apply(final JsonNode input) {
                if (!input.isTextual()) {
                    throw new IllegalStateException("found node that is not a string " + input.toString());
                }
                return input.getTextValue();
            }
        });
        REPRESENTATION_INSTANTIATORS.put(JsonNode.class, new Function<JsonNode, JsonNode>() {
            @Override
            public JsonNode apply(final JsonNode input) {
                return input;
            }
        });
    }

    private static <T> Function<JsonNode, ?> representationInstantiatorFor(final Class<T> representationType) {
        Function<JsonNode, ?> transformer = REPRESENTATION_INSTANTIATORS.get(representationType);
        if (transformer == null) {
            transformer = new Function<JsonNode, T>() {
                @Override
                public T apply(final JsonNode input) {
                    try {
                        final Constructor<T> constructor = representationType.getConstructor(JsonNode.class);
                        return constructor.newInstance(input);
                    } catch (final Exception e) {
                        throw new IllegalArgumentException("Conversions from JsonNode to " + representationType + " are not supported");
                    }
                }

            };
            REPRESENTATION_INSTANTIATORS.put(representationType, transformer);
        }
        return transformer;
    }

    public static JsonRepresentation newMap(final String... keyValuePairs) {
        final JsonRepresentation repr = new JsonRepresentation(new ObjectNode(JsonNodeFactory.instance));
        String key = null;
        for (final String keyOrValue : keyValuePairs) {
            if (key != null) {
                repr.mapPut(key, keyOrValue);
                key = null;
            } else {
                key = keyOrValue;
            }
        }
        if (key != null) {
            throw new IllegalArgumentException("must provide an even number of keys and values");
        }
        return repr;
    }

    public static JsonRepresentation newArray() {
        return newArray(0);
    }

    public static JsonRepresentation newArray(final int initialSize) {
        final ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
        for (int i = 0; i < initialSize; i++) {
            arrayNode.addNull();
        }
        return new JsonRepresentation(arrayNode);
    }

    protected final JsonNode jsonNode;

    public JsonRepresentation(final JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public JsonNode asJsonNode() {
        return jsonNode;
    }

    public int size() {
        if (!isMap() && !isArray()) {
            throw new IllegalStateException("not a map or an array");
        }
        return jsonNode.size();
    }

    /**
     * Node is a value (nb: could be {@link #isNull() null}).
     */
    public boolean isValue() {
        return jsonNode.isValueNode();
    }

    // ///////////////////////////////////////////////////////////////////////
    // getRepresentation
    // ///////////////////////////////////////////////////////////////////////

    public JsonRepresentation getRepresentation(final String pathTemplate, final Object... args) {
        final String pathStr = String.format(pathTemplate, args);

        final JsonNode node = getNode(pathStr);

        if (representsNull(node)) {
            return null;
        }

        return new JsonRepresentation(node);
    }

    // ///////////////////////////////////////////////////////////////////////
    // isArray, getArray, asArray
    // ///////////////////////////////////////////////////////////////////////

    public boolean isArray(final String path) {
        return isArray(getNode(path));
    }

    public boolean isArray() {
        return isArray(asJsonNode());
    }

    private boolean isArray(final JsonNode node) {
        return !representsNull(node) && node.isArray();
    }

    public JsonRepresentation getArray(final String path) {
        return getArray(path, getNode(path));
    }

    public JsonRepresentation asArray() {
        return getArray(null, asJsonNode());
    }

    private JsonRepresentation getArray(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }

        if (!isArray(node)) {
            throw new IllegalArgumentException(formatExMsg(path, "is not an array"));
        }
        return new JsonRepresentation(node);
    }

    public JsonRepresentation getArrayEnsured(final String path) {
        return getArrayEnsured(path, getNode(path));
    }

    private JsonRepresentation getArrayEnsured(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        return new JsonRepresentation(node).ensureArray();
    }

    // ///////////////////////////////////////////////////////////////////////
    // isMap, getMap, asMap
    // ///////////////////////////////////////////////////////////////////////

    public boolean isMap(final String path) {
        return isMap(getNode(path));
    }

    public boolean isMap() {
        return isMap(asJsonNode());
    }

    private boolean isMap(final JsonNode node) {
        return !representsNull(node) && !node.isArray() && !node.isValueNode();
    }

    public JsonRepresentation getMap(final String path) {
        return getMap(path, getNode(path));
    }

    public JsonRepresentation asMap() {
        return getMap(null, asJsonNode());
    }

    private JsonRepresentation getMap(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        if (isArray(node) || node.isValueNode()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a map"));
        }
        return new JsonRepresentation(node);
    }

    // ///////////////////////////////////////////////////////////////////////
    // isBoolean, getBoolean, asBoolean
    // ///////////////////////////////////////////////////////////////////////

    public boolean isBoolean(final String path) {
        return isBoolean(getNode(path));
    }

    public boolean isBoolean() {
        return isBoolean(asJsonNode());
    }

    private boolean isBoolean(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isBoolean();
    }

    public Boolean getBoolean(final String path) {
        return getBoolean(path, getNode(path));
    }

    public Boolean asBoolean() {
        return getBoolean(null, asJsonNode());
    }

    private Boolean getBoolean(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a boolean");
        if (!node.isBoolean()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a boolean"));
        }
        return node.getBooleanValue();
    }

    // ///////////////////////////////////////////////////////////////////////
    // isBigInteger, getBigInteger, asBigInteger
    // ///////////////////////////////////////////////////////////////////////

    public boolean isBigInteger(final String path) {
        return isBigInteger(getNode(path));
    }

    public boolean isBigInteger() {
        return isBigInteger(asJsonNode());
    }

    private boolean isBigInteger(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isBigInteger();
    }

    public BigInteger getBigInteger(final String path) {
        final JsonNode node = getNode(path);
        return getBigInteger(path, node);
    }

    public BigInteger asBigInteger() {
        return getBigInteger(null, asJsonNode());
    }

    private BigInteger getBigInteger(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a biginteger");
        if (!node.isBigInteger()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a biginteger"));
        }
        return node.getBigIntegerValue();
    }

    // ///////////////////////////////////////////////////////////////////////
    // isBigDecimal, getBigDecimal, asBigDecimal
    // ///////////////////////////////////////////////////////////////////////

    public boolean isBigDecimal(final String path) {
        return isBigDecimal(getNode(path));
    }

    public boolean isBigDecimal() {
        return isBigDecimal(asJsonNode());
    }

    private boolean isBigDecimal(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isBigDecimal();
    }

    public BigDecimal getBigDecimal(final String path) {
        final JsonNode node = getNode(path);
        return getBigDecimal(path, node);
    }

    public BigDecimal asBigDecimal() {
        return getBigDecimal(null, asJsonNode());
    }

    private BigDecimal getBigDecimal(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a biginteger");
        if (!node.isBigDecimal()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a biginteger"));
        }
        return node.getDecimalValue();
    }

    // ///////////////////////////////////////////////////////////////////////
    // isInt, getInt, asInt
    // ///////////////////////////////////////////////////////////////////////

    public boolean isInt(final String path) {
        return isInt(getNode(path));
    }

    public boolean isInt() {
        return isInt(asJsonNode());
    }

    private boolean isInt(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isInt();
    }

    public Integer getInt(final String path) {
        final JsonNode node = getNode(path);
        return getInt(path, node);
    }

    public Integer asInt() {
        return getInt(null, asJsonNode());
    }

    private Integer getInt(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "an int");
        if (!node.isInt()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not an int"));
        }
        return node.getIntValue();
    }

    // ///////////////////////////////////////////////////////////////////////
    // isLong, getLong, asLong
    // ///////////////////////////////////////////////////////////////////////

    public boolean isLong(final String path) {
        return isLong(getNode(path));
    }

    public boolean isLong() {
        return isLong(asJsonNode());
    }

    private boolean isLong(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isLong();
    }

    public Long getLong(final String path) {
        final JsonNode node = getNode(path);
        return getLong(path, node);
    }

    public Long asLong() {
        return getLong(null, asJsonNode());
    }

    private Long getLong(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a long");
        if (!node.isLong()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a long"));
        }
        return node.getLongValue();
    }

    // ///////////////////////////////////////////////////////////////////////
    // isDouble, getDouble, asDouble
    // ///////////////////////////////////////////////////////////////////////

    public boolean isDouble(final String path) {
        return isDouble(getNode(path));
    }

    public boolean isDouble() {
        return isDouble(asJsonNode());
    }

    private boolean isDouble(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isDouble();
    }

    public Double getDouble(final String path) {
        final JsonNode node = getNode(path);
        return getDouble(path, node);
    }

    public Double asDouble() {
        return getDouble(null, asJsonNode());
    }

    private Double getDouble(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a double");
        if (!node.isDouble()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a double"));
        }
        return node.getDoubleValue();
    }

    // ///////////////////////////////////////////////////////////////////////
    // getString, isString, asString
    // ///////////////////////////////////////////////////////////////////////

    public boolean isString(final String path) {
        return isString(getNode(path));
    }

    public boolean isString() {
        return isString(asJsonNode());
    }

    private boolean isString(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isTextual();
    }

    public String getString(final String path) {
        final JsonNode node = getNode(path);
        return getString(path, node);
    }

    public String asString() {
        return getString(null, asJsonNode());
    }

    private String getString(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a string");
        if (!node.isTextual()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a string"));
        }
        return node.getTextValue();
    }

    public String asArg() {
        if (isValue()) {
            return asJsonNode().getValueAsText();
        } else {
            return asJsonNode().toString();
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // isLink, getLink, asLink
    // ///////////////////////////////////////////////////////////////////////

    public boolean isLink() {
        return isLink(asJsonNode());
    }

    public boolean isLink(final String path) {
        return isLink(getNode(path));
    }

    public boolean isLink(final JsonNode node) {
        if (representsNull(node) || isArray(node) || node.isValueNode()) {
            return false;
        }

        final LinkRepresentation link = new LinkRepresentation(node);
        if (link.getHref() == null) {
            return false;
        }
        return true;
    }

    public LinkRepresentation getLink(final String path) {
        return getLink(path, getNode(path));
    }

    public LinkRepresentation asLink() {
        return getLink(null, asJsonNode());
    }

    private LinkRepresentation getLink(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }

        if (isArray(node)) {
            throw new IllegalArgumentException(formatExMsg(path, "is an array that does not represent a link"));
        }
        if (node.isValueNode()) {
            throw new IllegalArgumentException(formatExMsg(path, "is a value that does not represent a link"));
        }

        final LinkRepresentation link = new LinkRepresentation(node);
        if (link.getHref() == null) {
            throw new IllegalArgumentException(formatExMsg(path, "is a map that does not fully represent a link"));
        }
        return link;
    }

    // ///////////////////////////////////////////////////////////////////////
    // getNull, isNull
    // ///////////////////////////////////////////////////////////////////////

    public boolean isNull() {
        return isNull(asJsonNode());
    }

    /**
     * Indicates that the wrapped node has <tt>null</tt> value (ie
     * {@link JsonRepresentation#isNull()}), or returns <tt>null</tt> if there
     * was no node with the provided path.
     */
    public Boolean isNull(final String path) {
        return isNull(getNode(path));
    }

    private Boolean isNull(final JsonNode node) {
        if (node == null || node.isMissingNode()) {
            // not exclude if node.isNull, cos that's the point of this.
            return null;
        }
        return node.isNull();
    }

    /**
     * Either returns a {@link JsonRepresentation} that indicates that the
     * wrapped node has <tt>null</tt> value (ie
     * {@link JsonRepresentation#isNull()}), or returns <tt>null</tt> if there
     * was no node with the provided path.
     */
    public JsonRepresentation getNull(final String path) {
        return getNull(path, getNode(path));
    }

    public JsonRepresentation asNull() {
        return getNull(null, asJsonNode());
    }

    private JsonRepresentation getNull(final String path, final JsonNode node) {
        if (node == null || node.isMissingNode()) {
            // exclude if node.isNull, cos that's the point of this.
            return null;
        }
        checkValue(path, node, "the null value");
        if (!node.isNull()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not the null value"));
        }
        return new JsonRepresentation(node);
    }

    // ///////////////////////////////////////////////////////////////////////
    // mapValueAsLink
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Convert a representation that contains a single node representing a link
     * into a {@link LinkRepresentation}.
     */
    public LinkRepresentation mapValueAsLink() {
        if (asJsonNode().size() != 1) {
            throw new IllegalStateException("does not represent link");
        }
        final String linkPropertyName = asJsonNode().getFieldNames().next();
        return getLink(linkPropertyName);
    }

    // ///////////////////////////////////////////////////////////////////////
    // asInputStream
    // ///////////////////////////////////////////////////////////////////////

    public InputStream asInputStream() {
        return JsonNodeUtils.asInputStream(jsonNode);
    }

    // ///////////////////////////////////////////////////////////////////////
    // asArrayNode, asObjectNode
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Convert underlying representation into an array.
     */
    protected ArrayNode asArrayNode() {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        return (ArrayNode) asJsonNode();
    }

    /**
     * Convert underlying representation into an object (map).
     */
    protected ObjectNode asObjectNode() {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        return (ObjectNode) asJsonNode();
    }

    // ///////////////////////////////////////////////////////////////////////
    // asT
    // ///////////////////////////////////////////////////////////////////////

    /**
     * Convenience to simply &quot;downcast&quot;.
     * 
     * <p>
     * In fact, the method creates a new instance of the specified type, which
     * shares the underlying {@link #jsonNode jsonNode}.
     */
    public <T extends JsonRepresentation> T as(final Class<T> cls) {
        try {
            final Constructor<T> constructor = cls.getConstructor(JsonNode.class);
            return constructor.newInstance(jsonNode);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // asUrlEncoded
    // ///////////////////////////////////////////////////////////////////////

    public String asUrlEncoded() {
        return UrlEncodingUtils.urlEncode(asJsonNode());
    }

    // ///////////////////////////////////////////////////////////////////////
    // mutable (array)
    // ///////////////////////////////////////////////////////////////////////

    public void arrayAdd(final Object value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(new POJONode(value));
    }

    public void arrayAdd(final JsonRepresentation value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value.asJsonNode());
    }

    public void arrayAdd(final String value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(final JsonNode value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(final long value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(final int value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(final double value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(final float value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
    }

    public void arrayAdd(final boolean value) {
        if (!isArray()) {
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
        final Function<JsonNode, ?> transformer = representationInstantiatorFor(requiredType);
        final ArrayNode arrayNode = (ArrayNode) jsonNode;
        final Iterator<JsonNode> iterator = arrayNode.iterator();
        final Function<JsonNode, T> typedTransformer = asT(transformer); // necessary
                                                                         // to
                                                                         // do
                                                                         // in
                                                                         // two
                                                                         // steps
        return Iterators.transform(iterator, typedTransformer);
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<JsonNode, T> asT(final Function<JsonNode, ?> transformer) {
        return (Function<JsonNode, T>) transformer;
    }

    public JsonRepresentation arrayGet(final int i) {
        ensureIsAnArrayAtLeastAsLargeAs(i);
        return new JsonRepresentation(jsonNode.get(i));
    }

    public void arraySetElementAt(final int i, final JsonRepresentation objectRepr) {
        ensureIsAnArrayAtLeastAsLargeAs(i);
        if (objectRepr.isArray()) {
            throw new IllegalArgumentException("Representation being set cannot be an array");
        }
        // can safely downcast because *this* representation is an array
        final ArrayNode arrayNode = (ArrayNode) jsonNode;
        arrayNode.set(i, objectRepr.asJsonNode());
    }

    private void ensureIsAnArrayAtLeastAsLargeAs(final int i) {
        if (!jsonNode.isArray()) {
            throw new IllegalStateException("Is not an array");
        }
        if (i >= size()) {
            throw new IndexOutOfBoundsException("array has " + size() + " elements");
        }
    }

    // ///////////////////////////////////////////////////////////////////////
    // mutable (map)
    // ///////////////////////////////////////////////////////////////////////

    public boolean mapHas(final String key) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        ObjectNode node = asObjectNode();

        final String[] paths = key.split("\\.");
        for (int i = 0; i < paths.length; i++) {
            final String path = paths[i];
            final boolean has = node.has(path);
            if (!has) {
                return false;
            }
            if (i + 1 < paths.length) {
                // not on last
                final JsonNode subNode = node.get(path);
                if (!subNode.isObject()) {
                    return false;
                }
                node = (ObjectNode) subNode;
            }
        }
        return true;
    }

    public void mapPut(final String key, final List<Object> value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) {
            return;
        }
        final JsonRepresentation array = JsonRepresentation.newArray();
        for (final Object v : value) {
            array.arrayAdd(v);
        }
        mapPut(key, array);
    }

    public void mapPut(final String key, final Object value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) {
            return;
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), new POJONode(value));
    }

    public void mapPut(final String key, final JsonRepresentation value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) {
            return;
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value.asJsonNode());
    }

    public void mapPut(final String key, final String value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) {
            return;
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(final String key, final JsonNode value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) {
            return;
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(final String key, final long value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(final String key, final int value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(final String key, final double value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(final String key, final float value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    public void mapPut(final String key, final boolean value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = JsonNodeUtils.walkNodeUpTo(asObjectNode(), path.getHead());
        node.put(path.getTail(), value);
    }

    private static class Path {
        private final List<String> head;
        private final String tail;

        private Path(final List<String> head, final String tail) {
            this.head = Collections.unmodifiableList(head);
            this.tail = tail;
        }

        public List<String> getHead() {
            return head;
        }

        public String getTail() {
            return tail;
        }

        public static Path parse(final String pathStr) {
            final List<String> keyList = Lists.newArrayList(Arrays.asList(pathStr.split("\\.")));
            if (keyList.size() == 0) {
                throw new IllegalArgumentException(String.format("Malformed path '%s'", pathStr));
            }
            final String tail = keyList.remove(keyList.size() - 1);
            return new Path(keyList, tail);
        }
    }

    public Iterable<Map.Entry<String, JsonRepresentation>> mapIterable() {
        ensureIsAMap();
        return new Iterable<Map.Entry<String, JsonRepresentation>>() {

            @Override
            public Iterator<Entry<String, JsonRepresentation>> iterator() {
                return mapIterator();
            }
        };
    }

    public Iterator<Map.Entry<String, JsonRepresentation>> mapIterator() {
        ensureIsAMap();
        return Iterators.transform(jsonNode.getFields(), MAP_ENTRY_JSON_NODE_TO_JSON_REPRESENTATION);
    }

    private void ensureIsAMap() {
        if (!jsonNode.isObject()) {
            throw new IllegalStateException("Is not a map");
        }
    }

    private final static Function<Entry<String, JsonNode>, Entry<String, JsonRepresentation>> MAP_ENTRY_JSON_NODE_TO_JSON_REPRESENTATION = new Function<Entry<String, JsonNode>, Entry<String, JsonRepresentation>>() {

        @Override
        public Entry<String, JsonRepresentation> apply(final Entry<String, JsonNode> input) {
            return new Map.Entry<String, JsonRepresentation>() {

                @Override
                public String getKey() {
                    return input.getKey();
                }

                @Override
                public JsonRepresentation getValue() {
                    return new JsonRepresentation(input.getValue());
                }

                @Override
                public JsonRepresentation setValue(final JsonRepresentation value) {
                    final JsonNode setValue = input.setValue(value.asJsonNode());
                    return new JsonRepresentation(setValue);
                }
            };
        }
    };

    // ///////////////////////////////////////////////////////////////////////
    // helpers
    // ///////////////////////////////////////////////////////////////////////

    /**
     * A reciprocal of the behaviour of the automatic dereferencing of arrays
     * that occurs when there is only a single instance.
     * 
     * @see #toJsonNode(List)
     */
    public JsonRepresentation ensureArray() {
        if (jsonNode.isArray()) {
            return this;
        }
        final JsonRepresentation arrayRepr = JsonRepresentation.newArray();
        arrayRepr.arrayAdd(jsonNode);
        return arrayRepr;
    }

    private JsonNode getNode(final String path) {
        JsonNode jsonNode = this.jsonNode;
        final String[] keys = path.split("\\.");
        for (final String key : keys) {
            final PathNode pathNode = PathNode.parse(key);
            if (!pathNode.getKey().isEmpty()) {
                jsonNode = jsonNode.path(pathNode.getKey());
            } else {
                // pathNode is criteria only; don't change jsonNode
            }
            if (jsonNode.isNull()) {
                return jsonNode;
            }
            if (!pathNode.hasCriteria()) {
                continue;
            }
            if (!jsonNode.isArray()) {
                return NullNode.getInstance();
            }
            jsonNode = matching(jsonNode, pathNode);
            if (jsonNode.isNull()) {
                return jsonNode;
            }
        }
        return jsonNode;
    }

    private JsonNode matching(final JsonNode jsonNode, final PathNode pathNode) {
        final JsonRepresentation asList = new JsonRepresentation(jsonNode);
        final Iterable<JsonNode> filtered = Iterables.filter(asList.arrayIterable(JsonNode.class), new Predicate<JsonNode>() {
            @Override
            public boolean apply(final JsonNode input) {
                return pathNode.matches(new JsonRepresentation(input));
            }
        });
        final List<JsonNode> matching = Lists.newArrayList(filtered);
        return toJsonNode(matching);
    }

    private static JsonNode toJsonNode(final List<JsonNode> matching) {
        switch (matching.size()) {
        case 0:
            return NullNode.getInstance();
        case 1:
            return matching.get(0);
        default:
            final ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
            arrayNode.addAll(matching);
            return arrayNode;
        }
    }

    private static void checkValue(final String path, final JsonNode node, final String requiredType) {
        if (node.isValueNode()) {
            return;
        }
        throw new IllegalArgumentException(formatExMsg(path, "is not " + requiredType));
    }

    private static boolean representsNull(final JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull();
    }

    private static String formatExMsg(final String pathIfAny, final String errorText) {
        final StringBuilder buf = new StringBuilder();
        if (pathIfAny != null) {
            buf.append("'").append(pathIfAny).append("' ");
        }
        buf.append(errorText);
        return buf.toString();
    }

    // ///////////////////////////////////////////////////////////////////////
    // toString
    // ///////////////////////////////////////////////////////////////////////

    @Override
    public String toString() {
        return jsonNode.toString();
    }

}
