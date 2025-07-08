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
package org.apache.causeway.viewer.restfulobjects.applib;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.io.JsonUtils;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.viewer.restfulobjects.applib.util.PathNode;

/**
 * A wrapper around {@link JsonNode} that provides some additional helper
 * methods.
 *
 * @since 1.x {@index}
 */
public class JsonRepresentation {

    private final static Pattern FORMAT_BIG_DECIMAL = Pattern.compile("big-decimal\\((\\d+),(\\d+)\\)");
    private final static Pattern FORMAT_BIG_INTEGER = Pattern.compile("big-integer\\((\\d+)\\)");
    private final static Map<Class<?>, Function<JsonNode, ?>> REPRESENTATION_INSTANTIATORS = initRepresentationInstantiators();

    public interface HasLinkToSelf {
        public LinkRepresentation getSelf();
    }

    public interface HasLinkToUp {
        public LinkRepresentation getUp();
    }

    public interface HasLinks {
        public JsonRepresentation getLinks();
    }

    public interface HasExtensions {
        public JsonRepresentation getExtensions();
    }

    private static <T> Function<JsonNode, ?> representationInstantiatorFor(final Class<T> representationType) {
        return REPRESENTATION_INSTANTIATORS.computeIfAbsent(representationType, __->t -> {
            try {
                return representationType.getConstructor(JsonNode.class).newInstance(t);
            } catch (final Exception e) {
                throw new IllegalArgumentException("Conversions from JsonNode to " + representationType + " are not supported");
            }
        });
    }

    public static JsonRepresentation jsonAsMap(final @Nullable String keyValuePairsAsJson) {
        var repr = JsonRepresentation.newMap();
        if(_Strings.isNotEmpty(keyValuePairsAsJson)) {
            final Map<Object, Object> keyValuePairs = _Casts.uncheckedCast(
                    JsonUtils.tryRead(Map.class, keyValuePairsAsJson)
                        .ifFailureFail()
                        .getValue().orElseThrow());

            keyValuePairs.forEach((key, value)->{
                repr.mapPutString(""+key, ""+value);
            });
        }
        return repr;
    }

    public static JsonRepresentation newMap(final String... keyValuePairs) {
        final JsonRepresentation repr = new JsonRepresentation(new ObjectNode(JsonNodeFactory.instance));
        String key = null;
        for (final String keyOrValue : keyValuePairs) {
            if (key != null) {
                repr.mapPutString(key, keyOrValue);
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

    // -- REPRESENTATION

    public JsonRepresentation getRepresentation(final String pathTemplate, final Object... args) {
        final String pathStr = String.format(pathTemplate, args);
        final JsonNode node = getNode(pathStr);

        return representsNull(node)
            ? null
            : new JsonRepresentation(node);
    }

    // -- ARRAY

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
        if (representsNull(node)) return null;

        if (!isArray(node)) {
            throw new IllegalArgumentException(formatExMsg(path, "is not an array"));
        }
        return new JsonRepresentation(node);
    }

    public JsonRepresentation getArrayEnsured(final String path) {
        return getArrayEnsured(path, getNode(path));
    }

    private JsonRepresentation getArrayEnsured(final String path, final JsonNode node) {
        if (representsNull(node)) return null;

        return new JsonRepresentation(node).ensureArray();
    }

    // -- MAP

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
        if (representsNull(node)) return null;

        if (isArray(node) || node.isValueNode()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a map"));
        }
        return new JsonRepresentation(node);
    }

    // -- NUMBER

    public boolean isNumber(final String path) {
        return isNumber(getNode(path));
    }

    public boolean isNumber() {
        return isNumber(asJsonNode());
    }

    private boolean isNumber(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isNumber();
    }

    public Number asNumber() {
        return getNumber(null, asJsonNode());
    }

    private Number getNumber(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "a number");
        if (!node.isNumber()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a number"));
        }
        return node.numberValue();
    }

    // -- INTEGRALNUMBER

    /**
     * Is a long, an int or a {@link BigInteger}.
     */
    public boolean isIntegralNumber(final String path) {
        return isIntegralNumber(getNode(path));
    }

    /**
     * Is a long, an int or a {@link BigInteger}.
     */
    public boolean isIntegralNumber() {
        return isIntegralNumber(asJsonNode());
    }

    private boolean isIntegralNumber(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isIntegralNumber();
    }

    // -- BOOLEAN

    public boolean isBoolean(final String path) {
        return isBoolean(getNode(path));
    }

    public boolean isBoolean() {
        return isBoolean(asJsonNode());
    }

    private boolean isBoolean(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isBoolean();
    }

    /**
     * Use {@link #isBoolean(String)} to check first, if required.
     */
    public Boolean getBoolean(final String path) {
        return getBoolean(path, getNode(path));
    }

    /**
     * Use {@link #isBoolean()} to check first, if required.
     */
    public Boolean asBoolean() {
        return getBoolean(null, asJsonNode());
    }

    private Boolean getBoolean(final String path, final JsonNode node) {
        if (representsNull(node)) return null;

        checkValue(path, node, "a boolean");
        if (!node.isBoolean()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a boolean"));
        }
        return node.booleanValue();
    }

    // -- BYTE

    /**
     * Use {@link #isIntegralNumber(String)} to test if number (it is not possible to check if a byte, however).
     */
    public Byte getByte(final String path) {
        final JsonNode node = getNode(path);
        return getByte(path, node);
    }

    /**
     * Use {@link #isIntegralNumber()} to test if number (it is not possible to check if a byte, however).
     */
    public Byte asByte() {
        return getByte(null, asJsonNode());
    }

    private Byte getByte(final String path, final JsonNode node) {
        if (representsNull(node)) return null;

        checkValue(path, node, "an byte");
        if (!node.isNumber()) {
            // there is no node.isByte()
            throw new IllegalArgumentException(formatExMsg(path, "is not a number"));
        }
        return node.numberValue().byteValue();
    }

    // -- SHORT

    /**
     * Use {@link #isIntegralNumber(String)} to check if number (it is not possible to check if a short, however).
     */
    public Short getShort(final String path) {
        final JsonNode node = getNode(path);
        return getShort(path, node);
    }

    /**
     * Use {@link #isIntegralNumber()} to check if number (it is not possible to check if a short, however).
     */
    public Short asShort() {
        return getShort(null, asJsonNode());
    }

    private Short getShort(final String path, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        checkValue(path, node, "an short");
        if (!node.isNumber()) {
            // there is no node.isShort()
            throw new IllegalArgumentException(formatExMsg(path, "is not a number"));
        }
        return node.shortValue();
    }

    // -- CHAR

    /**
     * Use {@link #isString(String)} to check if string (it is not possible to check if a character, however).
     */
    public Character getChar(final String path) {
        final JsonNode node = getNode(path);
        return getChar(path, node);
    }

    /**
     * Use {@link #isString()} to check if string (it is not possible to check if a character, however).
     */
    public Character asChar() {
        return getChar(null, asJsonNode());
    }

    private Character getChar(final String path, final JsonNode node) {
        if (representsNull(node)) return null;

        checkValue(path, node, "an short");
        if (!node.isTextual()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not textual"));
        }
        final String textValue = node.textValue();
        if(textValue == null || textValue.length() == 0) {
            return null;
        }
        return textValue.charAt(0);
    }

    // -- INT

    public boolean isInt(final String path) {
        return isInt(getNode(path));
    }

    public boolean isInt() {
        return isInt(asJsonNode());
    }

    private boolean isInt(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isInt();
    }

    /**
     * Use {@link #isInt(String)} to check first, if required.
     */
    public Integer getInt(final String path) {
        final JsonNode node = getNode(path);
        return getInt(path, node);
    }

    /**
     * Use {@link #isInt()} to check first, if required.
     */
    public Integer asInt() {
        return getInt(null, asJsonNode());
    }

    private Integer getInt(final String path, final JsonNode node) {
        if (representsNull(node))  return null;

        checkValue(path, node, "an int");
        if (!node.isInt()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not an int"));
        }
        return node.intValue();
    }

    // -- LONG

    public boolean isLong(final String path) {
        return isLong(getNode(path));
    }

    public boolean isLong() {
        return isLong(asJsonNode());
    }

    private boolean isLong(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isLong();
    }

    /**
     * Use {@link #isLong(String)} to check first, if required.
     */
    public Long getLong(final String path) {
        final JsonNode node = getNode(path);
        return getLong(path, node);
    }

    /**
     * Use {@link #isLong()} to check first, if required.
     */
    public Long asLong() {
        return getLong(null, asJsonNode());
    }

    private Long getLong(final String path, final JsonNode node) {
        if (representsNull(node))  return null;

        checkValue(path, node, "a long");
        if(node.isInt()) {
            return Long.valueOf(node.intValue());
        }
        if(node.isLong()) {
            return node.longValue();
        }
        throw new IllegalArgumentException(formatExMsg(path, "is not a long"));
    }

    // -- FLOAT

    /**
     * Use {@link #isDecimal(String)} to test if a decimal value
     */
    public Float getFloat(final String path) {
        final JsonNode node = getNode(path);
        return getFloat(path, node);
    }

    /**
     * Use {@link #isNumber()} to test if number (it is not possible to check if a float, however).
     */
    public Float asFloat() {
        return getFloat(null, asJsonNode());
    }

    private Float getFloat(final String path, final JsonNode node) {
        if (representsNull(node))  return null;

        checkValue(path, node, "a float");
        if (!node.isNumber()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a number"));
        }
        return node.floatValue();
    }

    // -- DECIMAL

    public boolean isDecimal(final String path) {
        return isDecimal(getNode(path));
    }

    public boolean isDecimal() {
        return isDecimal(asJsonNode());
    }

    private boolean isDecimal(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && (node.isDouble() || node.isFloat());
    }

    /**
     * Use {@link #isDecimal(String)} to check first, if required.
     */
    public Double getDouble(final String path) {
        final JsonNode node = getNode(path);
        return getDouble(path, node);
    }

    /**
     * Use {@link #isDecimal()} to check first, if required.
     */
    public Double asDouble() {
        return getDouble(null, asJsonNode());
    }

    private Double getDouble(final String path, final JsonNode node) {
        if (representsNull(node))  return null;

        checkValue(path, node, "a double");
        if (!node.isDouble()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a double"));
        }
        return node.doubleValue();
    }

    // -- BIGINTEGER

    public boolean isBigInteger(final String path) {
        return isBigInteger(getNode(path));
    }

    public boolean isBigInteger() {
        return isBigInteger(asJsonNode());
    }

    private boolean isBigInteger(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && (node.isBigInteger() || node.isLong() || node.isInt() || node.isTextual() && parseableAsBigInteger(node.textValue()));
    }

    private static boolean parseableAsBigInteger(final String str) {
        try {
            new BigInteger(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Use {@link #isBigInteger(String)} to check first, if required.
     */
    public BigInteger getBigInteger(final String path) {
        return getBigInteger(path, (String)null);
    }

    /**
     * Use {@link #isBigInteger(String)} to check first, if required.
     */
    public BigInteger getBigInteger(final String path, final String formatRequested) {
        final JsonNode node;
        final String format;
        if(formatRequested != null) {
            node = getNode(path);
            format = formatRequested;
        } else {
            final NodeAndFormat nodeAndFormat = getNodeAndFormat(path);
            node = nodeAndFormat.node;
            format = nodeAndFormat.format;
        }
        return getBigInteger(path, format, node);
    }

    /**
     * Use {@link #isBigInteger()} to check first, if required.
     */
    public BigInteger asBigInteger() {
        return asBigInteger(null);
    }

    public BigInteger asBigInteger(final String format) {
        return getBigInteger(null, format, asJsonNode());
    }

    private BigInteger getBigInteger(final String path, final String format, final JsonNode node) {
        if (representsNull(node)) {
            return null;
        }
        final String requiredType = "a biginteger";
        if(!isBigInteger(node)) {
            throw new IllegalArgumentException(formatExMsg(path, "is not " + requiredType));
        }
        checkValue(path, node, requiredType);
        final BigInteger bigInteger = getBigInteger(path, node);
        if(format != null) {
            final Matcher matcher = FORMAT_BIG_INTEGER.matcher(format);
            if(matcher.matches()) {
                final int precision = Integer.parseInt(matcher.group(1));
                final BigInteger maxAllowed = BigInteger.TEN.pow(precision);
                if(bigInteger.compareTo(maxAllowed) > 0) {
                    throw new IllegalArgumentException(String.format("Value '%s' larger than that allowed by format '%s'", bigInteger, format));
                }
            }
        }
        return bigInteger;
    }

    private BigInteger getBigInteger(final String path, final JsonNode node) {
        if (node.isBigInteger()) {
            return node.bigIntegerValue();
        }
        if (node.isTextual()) {
            return new BigInteger(node.textValue());
        }
        if (node.isLong()) {
            return BigInteger.valueOf(node.longValue());
        }
        if (node.isInt()) {
            return BigInteger.valueOf(node.intValue());
        }
        throw new IllegalArgumentException(formatExMsg(path, "is not a biginteger, is not any other integral number, is not text parseable as a biginteger"));
    }

    // -- BIGDECIMAL

    public boolean isBigDecimal(final String path) {
        return isBigDecimal(getNode(path));
    }

    public boolean isBigDecimal() {
        return isBigDecimal(asJsonNode());
    }

    private boolean isBigDecimal(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && (node.isBigDecimal() || node.isDouble() || node.isLong() || node.isInt() || node.isBigInteger() || node.isTextual() && parseableAsBigDecimal(node.textValue()));
    }

    private static boolean parseableAsBigDecimal(final String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Use {@link #isBigDecimal(String)} to check first, if required.
     */
    public BigDecimal getBigDecimal(final String path) {
        return getBigDecimal(path, (String)null);
    }

    /**
     * Use {@link #isBigDecimal(String)} to check first, if required.
     */
    public BigDecimal getBigDecimal(final String path, final String formatRequested) {
        final JsonNode node;
        final String format;
        if(formatRequested != null) {
            node = getNode(path);
            format = formatRequested;
        } else {
            final NodeAndFormat nodeAndFormat = getNodeAndFormat(path);
            node = nodeAndFormat.node;
            format = nodeAndFormat.format;
        }
        return getBigDecimal(path, format, node);
    }

    /**
     * Use {@link #isBigDecimal()} to check first, if required.
     */
    public BigDecimal asBigDecimal() {
        return asBigDecimal(null);
    }

    /**
     * Use {@link #isBigDecimal()} to check first, if required.
     */
    public BigDecimal asBigDecimal(final String format) {
        return getBigDecimal(null, format, asJsonNode());
    }

    private BigDecimal getBigDecimal(final String path, final String format, final JsonNode node) {
        if (representsNull(node))  return null;

        final String requiredType = "a bigdecimal";
        if(!isBigDecimal(node)) {
            throw new IllegalArgumentException(formatExMsg(path, "is not " + requiredType));
        }
        checkValue(path, node, requiredType);
        final BigDecimal bigDecimal = getBigDecimal(path, node);
        if(format != null) {
            final Matcher matcher = FORMAT_BIG_DECIMAL.matcher(format);
            if(matcher.matches()) {
                final int precision = Integer.parseInt(matcher.group(1));
                final int scale = Integer.parseInt(matcher.group(2));
                final BigDecimal maxAllowed = BigDecimal.TEN.pow(precision-scale);
                if(bigDecimal.compareTo(maxAllowed) > 0) {
                    throw new IllegalArgumentException(String.format("Value '%s' larger than that allowed by format '%s'", bigDecimal, format));
                }
                return bigDecimal.setScale(scale, RoundingMode.HALF_EVEN);
            }
        }
        return bigDecimal;
    }

    private BigDecimal getBigDecimal(final String path, final JsonNode node) {
        if (node.isBigDecimal()) {
            return node.decimalValue();
        }
        if (node.isTextual()) {
            return new BigDecimal(node.textValue());
        }
        if (node.isLong()) {
            return new BigDecimal(node.longValue());
        }
        if (node.isDouble()) {
            // there will be rounding errors, most likely
            return BigDecimal.valueOf(node.doubleValue());
        }
        if (node.isBigInteger()) {
            return new BigDecimal(node.bigIntegerValue());
        }
        if (node.isInt()) {
            return new BigDecimal(node.intValue());
        }
        throw new IllegalArgumentException(formatExMsg(path, "is not a bigdecimal, is not any other numeric, is not text parseable as a bigdecimal"));
    }

    // -- STRING

    public boolean isString(final String path) {
        return isString(getNode(path));
    }

    public boolean isString() {
        return isString(asJsonNode());
    }

    private boolean isString(final JsonNode node) {
        return !representsNull(node) && node.isValueNode() && node.isTextual();
    }

    /**
     * Use {@link #isString(String)} to check first, if required.
     */
    public String getString(final String path) {
        final JsonNode node = getNode(path);
        return getString(path, node);
    }

    /**
     * Use {@link #isString()} to check first, if required.
     */
    public String asString() {
        return getString(null, asJsonNode());
    }

    private String getString(final String path, final JsonNode node) {
        if (representsNull(node))  return null;

        checkValue(path, node, "a string");
        if (!node.isTextual()) {
            throw new IllegalArgumentException(formatExMsg(path, "is not a string"));
        }
        return node.textValue();
    }

    public String asArg() {
        if (isValue()) {
            return asJsonNode().asText();
        } else {
            return asJsonNode().toString();
        }
    }

    // -- LINK

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

    /**
     * Use {@link #isLink(String)} to check first, if required.
     */
    public LinkRepresentation getLink(final String path) {
        return getLink(path, getNode(path));
    }

    /**
     * Use {@link #isLink()} to check first, if required.
     */
    public LinkRepresentation asLink() {
        return getLink(null, asJsonNode());
    }

    private LinkRepresentation getLink(final String path, final JsonNode node) {
        if (representsNull(node))  return null;

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

    // -- NULL

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
     *
     * <p>
     * Use {@link #isNull(String)} to check first, if required.
     */
    public JsonRepresentation getNull(final String path) {
        return getNull(path, getNode(path));
    }

    /**
     * Either returns a {@link JsonRepresentation} that indicates that the
     * wrapped node has <tt>null</tt> value (ie
     * {@link JsonRepresentation#isNull()}), or returns <tt>null</tt> if there
     * was no node with the provided path.
     *
     * <p>
     * Use {@link #isNull()} to check first, if required.
     */
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

    /**
     * Convert a representation that contains a single node representing a link
     * into a {@link LinkRepresentation}.
     */
    public LinkRepresentation mapValueAsLink() {
        if (asJsonNode().size() != 1) {
            throw new IllegalStateException("does not represent link");
        }
        final String linkPropertyName = asJsonNode().fieldNames().next();
        return getLink(linkPropertyName);
    }

    /**
     * Returns the underlying JSON (UTF-8 String) as {@link ByteArrayInputStream}.
     */
    public InputStream asInputStream() {
        return new ByteArrayInputStream(jsonNode.toString().getBytes(StandardCharsets.UTF_8));
    }

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

    public String asUrlEncoded() {
        return UrlUtils.urlEncodeUtf8(asJsonNode().toString());
    }

    // -- MUTABLE (ARRAY)

    public JsonRepresentation arrayAdd(final Object value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(new POJONode(value));
        return this;
    }

    public JsonRepresentation arrayAdd(final JsonRepresentation value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value.asJsonNode());
        return this;
    }

    public JsonRepresentation arrayAdd(final String value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public JsonRepresentation arrayAdd(final JsonNode value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public JsonRepresentation arrayAdd(final long value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public JsonRepresentation arrayAdd(final int value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public JsonRepresentation arrayAdd(final double value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public JsonRepresentation arrayAdd(final float value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public JsonRepresentation arrayAdd(final boolean value) {
        if (!isArray()) {
            throw new IllegalStateException("does not represent array");
        }
        asArrayNode().add(value);
        return this;
    }

    public Stream<JsonRepresentation> streamArrayElements() {
        return streamArrayElements(JsonRepresentation.class);
    }

    public <T> Stream<T> streamArrayElements(final Class<T> requiredType) {
        ensureIsAnArrayAtLeastAsLargeAs(0);
        final Function<JsonNode, ?> transformer = representationInstantiatorFor(requiredType);
        final ArrayNode arrayNode = (ArrayNode) jsonNode;
        final Iterator<JsonNode> iterator = arrayNode.iterator();
        // necessary to do in two steps
        final Function<JsonNode, T> typedTransformer = asT(transformer);
        return _NullSafe.stream(iterator)
                .map(typedTransformer);
    }

    @SuppressWarnings("unchecked")
    private static <T> Function<JsonNode, T> asT(final Function<JsonNode, ?> transformer) {
        return (Function<JsonNode, T>) transformer;
    }

    public JsonRepresentation arrayGet(final int i) {
        ensureIsAnArrayAtLeastAsLargeAs(i+1);
        return new JsonRepresentation(jsonNode.get(i));
    }

    public JsonRepresentation arraySetElementAt(final int i, final JsonRepresentation objectRepr) {
        ensureIsAnArrayAtLeastAsLargeAs(i+1);
        if (objectRepr.isArray()) {
            throw new IllegalArgumentException("Representation being set cannot be an array");
        }
        // can safely downcast because *this* representation is an array
        final ArrayNode arrayNode = (ArrayNode) jsonNode;
        arrayNode.set(i, objectRepr.asJsonNode());
        return this;
    }

    private void ensureIsAnArrayAtLeastAsLargeAs(final int i) {
        if (!jsonNode.isArray()) {
            throw new IllegalStateException("Is not an array");
        }
        if (i > size()) {
            throw new IndexOutOfBoundsException("array has only " + size() + " elements");
        }
    }

    // -- MUTABLE (MAP)

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

    public JsonRepresentation mapPut(final String key, final List<Object> value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null)  return this;

        final JsonRepresentation array = JsonRepresentation.newArray();
        for (final Object v : value) {
            array.arrayAdd(v);
        }
        mapPutJsonRepresentation(key, array);
        return this;
    }

    public JsonRepresentation mapPut(final String key, final Object value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.set(path.tail(), value != null ? new POJONode(value) : NullNode.getInstance());
        return this;
    }

    public JsonRepresentation mapPutJsonRepresentation(final String key, final JsonRepresentation value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) return this;

        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.set(path.tail(), value.asJsonNode());
        return this;
    }

    public JsonRepresentation mapPutString(final String key, final String value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) return this;

        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.put(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutJsonNode(final String key, final JsonNode value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        if (value == null) return this;

        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.set(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutByte(final String key, final byte value) {
        return mapPutInt(key, value);
    }

    public JsonRepresentation mapPutByteNullable(final String key, final Byte value) {
        return value != null ? mapPutByte(key, value.byteValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutShort(final String key, final short value) {
        return mapPutInt(key, value);
    }

    public JsonRepresentation mapPutShortNullable(final String key, final Short value) {
        return value != null ? mapPutShort(key, value.shortValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutInt(final String key, final int value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.put(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutIntNullable(final String key, final Integer value) {
        return value != null ? mapPutInt(key, value.intValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutLong(final String key, final long value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.put(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutLongNullable(final String key, final Long value) {
        return value != null ? mapPutLong(key, value.longValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutFloat(final String key, final float value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.put(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutFloatNullable(final String key, final Float value) {
        return value != null ? mapPutFloat(key, value.floatValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutDouble(final String key, final double value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.put(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutDoubleNullable(final String key, final Double value) {
        return value != null ? mapPutDouble(key, value.doubleValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutBoolean(final String key, final boolean value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        node.put(path.tail(), value);
        return this;
    }

    public JsonRepresentation mapPutBooleanNullable(final String key, final Boolean value) {
        return value != null ? mapPutBoolean(key, value.booleanValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutChar(final String key, final char value) {
        return mapPutString(key, ""+value);
    }

    public JsonRepresentation mapPutCharNullable(final String key, final Character value) {
        return value != null ? mapPutChar(key, value.charValue()) : mapPut(key, value);
    }

    public JsonRepresentation mapPutBigInteger(final String key, final BigInteger value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        if (value != null) {
            node.put(path.tail(), value.toString());
        } else {
            node.set(path.tail(), NullNode.getInstance());
        }
        return this;
    }

    public JsonRepresentation mapPutEntries(final Iterable<Entry<String, JsonRepresentation>> entries) {
        for (Entry<String, JsonRepresentation> entry : entries) {
            mapPutEntry(entry);
        }
        return this;
    }

    public JsonRepresentation mapPutEntry(final Entry<String, JsonRepresentation> entry) {
        mapPutJsonRepresentation(entry.getKey(), entry.getValue());
        return this;
    }

    public JsonRepresentation mapPutBigDecimal(final String key, final BigDecimal value) {
        if (!isMap()) {
            throw new IllegalStateException("does not represent map");
        }
        final Path path = Path.parse(key);
        final ObjectNode node = walkNodeUpTo(asObjectNode(), path.head());
        if (value != null) {
            node.put(path.tail(), value.toString());
        } else {
            node.set(path.tail(), NullNode.getInstance());
        }
        return this;
    }

    // -- FORMATS

    public JsonRepresentation putFormat(final @Nullable String format) {
        if(format != null) {
            mapPutString("format", format);
        }
        return this;
    }

    public JsonRepresentation putExtendedFormat(final @Nullable String format) {
        if(format != null) {
            mapPutString("extensions.x-causeway-format", format);
        }
        return this;
    }

    // -- PATH

    private record Path(
        List<String> head,
        String tail) {

        private Path(final List<String> head, final String tail) {
            this.head = Collections.unmodifiableList(head);
            this.tail = tail;
        }

        public static Path parse(final String pathStr) {
            final List<String> keyList = new ArrayList<>(Arrays.asList(pathStr.split("\\.")));
            if (keyList.size() == 0) {
                throw new IllegalArgumentException(String.format("Malformed path '%s'", pathStr));
            }
            final String tail = keyList.remove(keyList.size() - 1);
            return new Path(keyList, tail);
        }
    }

    public Stream<Map.Entry<String, JsonRepresentation>> streamMapEntries() {
        ensureIsAMap();
        return _NullSafe.stream(jsonNode.properties())
                .map(MAP_ENTRY_JSON_NODE_TO_JSON_REPRESENTATION);
    }

    private void ensureIsAMap() {
        if (!jsonNode.isObject()) {
            throw new IllegalStateException("Is not a map");
        }
    }

    private static final Function<Entry<String, JsonNode>, Entry<String, JsonRepresentation>> MAP_ENTRY_JSON_NODE_TO_JSON_REPRESENTATION = input -> new Map.Entry<String, JsonRepresentation>() {

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

    // -- HELPERS

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
        return getNodeAndFormat(path).node;
    }

    private static class NodeAndFormat {
        JsonNode node;
        String format;
        NodeAndFormat(final JsonNode jsonNode, final String format) {
            node = jsonNode;
            this.format = format;
        }
    }

    /**
     * Walks the path to the specified node, and also returns the value of 'format' in the parent node if present.
     */
    private NodeAndFormat getNodeAndFormat(final String path) {
        JsonNode jsonNode = this.jsonNode;
        final List<String> keys = PathNode.split(path);
        String format = null;
        for (final String key : keys) {
            final PathNode pathNode = PathNode.parse(key);
            if (!pathNode.key().isEmpty()) {
                // grab format (if present) before moving down the path
                format = getFormatValueIfAnyFrom(jsonNode);
                jsonNode = jsonNode.path(pathNode.key());
            } else {
                // pathNode is criteria only; don't change jsonNode
            }
            if (jsonNode.isNull()) {
                return new NodeAndFormat(jsonNode, format);
            }
            if (!pathNode.hasCriteria()) {
                continue;
            }
            if (!jsonNode.isArray()) {
                return new NodeAndFormat(NullNode.getInstance(), format);
            }
            // grab format (if present) before moving down the path
            format = getFormatValueIfAnyFrom(jsonNode);
            jsonNode = matching(jsonNode, pathNode);
            if (jsonNode.isNull()) {
                return new NodeAndFormat(jsonNode, format);
            }
        }
        return new NodeAndFormat(jsonNode, format);
    }

    private String getFormatValueIfAnyFrom(final JsonNode jsonNode) {
        String format;
        final JsonNode formatNode = jsonNode.get("format");
        format = formatNode != null && formatNode.isTextual()? formatNode.textValue(): null;
        return format;
    }

    private JsonNode matching(final JsonNode jsonNode, final PathNode pathNode) {
        final JsonRepresentation asList = new JsonRepresentation(jsonNode);

        final List<JsonNode> matching = asList.streamArrayElements(JsonNode.class)
                .filter(input->pathNode.matches(new JsonRepresentation(input)))
                .collect(Collectors.toList());
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

    // -- OBJECT CONTRACT (NO SEMANTIC EQUALITY) ... use toString() to check for semantic equality

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((jsonNode == null) ? 0 : jsonNode.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        JsonRepresentation other = (JsonRepresentation) obj;
        if (jsonNode == null) {
            if (other.jsonNode != null)
            return false;
        } else if (!jsonNode.equals(other.jsonNode))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return jsonNode.toString();
    }

    // -- HELPER

    private static Map<Class<?>, Function<JsonNode, ?>> initRepresentationInstantiators() {
        var map = new ConcurrentHashMap<Class<?>, Function<JsonNode, ?>>();

        map.put(String.class, input -> {
            if (!input.isTextual()) {
                throw new IllegalStateException("found node that is not a string " + input.toString());
            }
            return input.textValue();
        });
        map.put(JsonNode.class, input -> input);

        return map;
    }

    /**
     * Walks the path, ensuring keys exist and are maps, or creating required
     * maps as it goes.
     *
     * <p>For example, if given a list ("a", "b", "c") and starting with an empty
     * map, then will create:
     *
     * <pre>
     * {
     *   "a": {
     *     "b: {
     *       "c": {
     *       }
     *     }
     *   }
     * }
     * </pre>
     */
    private static ObjectNode walkNodeUpTo(ObjectNode node, final List<String> keys) {
        for (final String key : keys) {
            JsonNode jsonNode = node.get(key);
            if (jsonNode == null) {
                jsonNode = new ObjectNode(JsonNodeFactory.instance);
                node.set(key, jsonNode);
            } else {
                if (!jsonNode.isObject()) {
                    throw _Exceptions.illegalArgument("walking path: '%s', existing key '%s' is not a map", keys, key);
                }
            }
            node = (ObjectNode) jsonNode;
        }
        return node;
    }

}
