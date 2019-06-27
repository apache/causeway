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
package org.apache.isis.viewer.restfulobjects.rendering.domainobjects;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import com.fasterxml.jackson.databind.node.NullNode;

/**
 * Similar to Isis' value encoding, but with additional support for JSON
 * primitives.
 */
public final class JsonValueEncoder {

    private JsonValueEncoder(){}

    public static class ExpectedStringRepresentingValueException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
    }

    public static abstract class JsonValueConverter {

        protected final String format;
        protected final String xIsisFormat;
        private final Class<?>[] classes;

        public JsonValueConverter(String format, String xIsisFormat, Class<?>... classes) {
            this.format = format;
            this.xIsisFormat = xIsisFormat;
            this.classes = classes;
        }

        public List<ObjectSpecId> getSpecIds() {
            return _NullSafe.stream(classes)
            .map((Class<?> cls) ->ObjectSpecId.of(cls.getName()))
            .collect(Collectors.toList());
        }

        /**
         * The value, otherwise <tt>null</tt>.
         */
        public abstract ObjectAdapter asAdapter(JsonRepresentation repr, String format);

        public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
            final Object value = unwrapAsObjectElseNullNode(objectAdapter);
            repr.mapPut("value", value);
            appendFormats(repr, this.format, this.xIsisFormat, suppressExtensions);
            return value;
        }

        public Object asObject(ObjectAdapter objectAdapter, String format) {
            return objectAdapter.getPojo();
        }
    }

    private static Map<ObjectSpecId, JsonValueConverter> converterBySpec = _Maps.newLinkedHashMap();

    private static void putConverter(JsonValueConverter jvc) {
        final List<ObjectSpecId> specIds = jvc.getSpecIds();
        for (ObjectSpecId specId : specIds) {
            converterBySpec.put(specId, jvc);
        }
    }

    static {
        putConverter(new JsonValueConverter(null, "string", String.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    return adapterFor(repr.asString());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof String) {
                    final String str = (String) obj;
                    repr.mapPut("value", str);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter(null, "boolean", boolean.class, Boolean.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isBoolean()) {
                    return adapterFor(repr.asBoolean());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Boolean) {
                    final Boolean b = (Boolean) obj;
                    repr.mapPut("value", b);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("int", "byte", byte.class, Byte.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().byteValue());
                }
                if (repr.isInt()) {
                    return adapterFor((byte)(int)repr.asInt());
                }
                if (repr.isLong()) {
                    return adapterFor((byte)(long)repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().byteValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Byte) {
                    final Byte b = (Byte) obj;
                    repr.mapPut("value", b);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("int", "short", short.class, Short.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().shortValue());
                }
                if (repr.isInt()) {
                    return adapterFor((short)(int)repr.asInt());
                }
                if (repr.isLong()) {
                    return adapterFor((short)(long)repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().shortValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Short) {
                    final Short s = (Short) obj;
                    repr.mapPut("value", s);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("int", "int", int.class, Integer.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isInt()) {
                    return adapterFor(repr.asInt());
                }
                if (repr.isLong()) {
                    return adapterFor((int)(long)repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().intValue());
                }
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().intValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Integer) {
                    final Integer i = (Integer) obj;
                    repr.mapPut("value", i);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("int", "long", long.class, Long.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isLong()) {
                    return adapterFor(repr.asLong());
                }
                if (repr.isInt()) {
                    return adapterFor(repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().longValue());
                }
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().longValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Long) {
                    final Long l = (Long) obj;
                    repr.mapPut("value", l);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("decimal", "float", float.class, Float.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isDouble()) {
                    return adapterFor((float)(double)repr.asDouble());
                }
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().floatValue());
                }
                if (repr.isLong()) {
                    return adapterFor((float)repr.asLong());
                }
                if (repr.isInt()) {
                    return adapterFor((float)repr.asInt());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().floatValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Float) {
                    final Float f = (Float) obj;
                    repr.mapPut("value", f);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("decimal", "double", double.class, Double.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isDouble()) {
                    return adapterFor(repr.asDouble());
                }
                if (repr.isLong()) {
                    return adapterFor((double)repr.asLong());
                }
                if (repr.isInt()) {
                    return adapterFor((double)repr.asInt());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().doubleValue());
                }
                if (repr.isBigDecimal()) {
                    return adapterFor(repr.asBigDecimal().doubleValue());
                }
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().doubleValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Double) {
                    final Double d = (Double) obj;
                    repr.mapPut("value", d);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter(null, "char", char.class, Character.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String str = repr.asString();
                    if(str != null && str.length()>0) {
                        return adapterFor(str.charAt(0));
                    }
                }
                // in case a char literal was provided
                if(repr.isInt()) {
                    final Integer x = repr.asInt();
                    if(Character.MIN_VALUE <= x && x <= Character.MAX_VALUE) {
                        char c = (char) x.intValue();
                        return adapterFor(c);
                    }
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Character) {
                    final Character c = (Character) obj;
                    repr.mapPut("value", c);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("big-integer(18)", "javamathbiginteger", BigInteger.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    return adapterFor(new BigInteger(repr.asString()));
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger(format));
                }
                if (repr.isLong()) {
                    return adapterFor(BigInteger.valueOf(repr.asLong()));
                }
                if (repr.isInt()) {
                    return adapterFor(BigInteger.valueOf(repr.asInt()));
                }
                if (repr.isNumber()) {
                    return adapterFor(BigInteger.valueOf(repr.asNumber().longValue()));
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof BigInteger) {
                    final BigInteger bi = (BigInteger) obj;
                    repr.mapPut("value", bi);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, format != null? format: this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("big-decimal", "javamathbigdecimal", BigDecimal.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    return adapterFor(new BigDecimal(repr.asString()));
                }
                if (repr.isBigDecimal()) {
                    return adapterFor(repr.asBigDecimal(format));
                }
                if (repr.isBigInteger()) {
                    return adapterFor(new BigDecimal(repr.asBigInteger()));
                }
                if (repr.isDouble()) {
                    return adapterFor(BigDecimal.valueOf(repr.asDouble()));
                }
                if (repr.isLong()) {
                    return adapterFor(BigDecimal.valueOf(repr.asLong()));
                }
                if (repr.isInt()) {
                    return adapterFor(BigDecimal.valueOf(repr.asInt()));
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof BigDecimal) {
                    final BigDecimal bd = (BigDecimal) obj;
                    repr.mapPut("value", bd);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, format != null ? format : this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("date", "jodalocaldate", LocalDate.class){

            // these formatters do NOT use withZoneUTC()
            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.date(),
                    ISODateTimeFormat.basicDate(),
                    DateTimeFormat.forPattern("yyyyMMdd"),
                    JsonRepresentation.yyyyMMdd
                    );

            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final LocalDate parsedDate = formatter.parseLocalDate(dateStr);
                            return adapterFor(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof LocalDate) {
                    final LocalDate date = (LocalDate) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTimeAtStartOfDay());
                    repr.mapPut("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("date-time", "jodalocaldatetime", LocalDateTime.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );

            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final LocalDateTime parsedDate = formatter.parseLocalDateTime(dateStr);
                            return adapterFor(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof LocalDateTime) {
                    final LocalDateTime date = (LocalDateTime) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTime());
                    repr.mapPut("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("date-time", "jodadatetime", DateTime.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );

            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parsedDate = formatter.parseDateTime(dateStr);
                            return adapterFor(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof DateTime) {
                    final DateTime date = (DateTime) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTime());
                    repr.mapPut("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("date-time", "javautildate", java.util.Date.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.util.Date parsedDate = parseDateTime.toDate();
                            return adapterFor(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.util.Date) {
                    final java.util.Date date = (java.util.Date) obj;
                    final DateTimeFormatter dateTimeFormatter = formatters.get(0);
                    final String dateStr = dateTimeFormatter.print(new DateTime(date));
                    repr.mapPut("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("date", "javasqldate", java.sql.Date.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.date().withZoneUTC(),
                    ISODateTimeFormat.basicDate().withZoneUTC(),
                    JsonRepresentation.yyyyMMdd.withZoneUTC()
                    );
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.sql.Date parsedDate = new java.sql.Date(parseDateTime.getMillis());
                            return adapterFor(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.sql.Date) {
                    final java.sql.Date date = (java.sql.Date) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    repr.mapPut("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("time", "javasqltime", java.sql.Time.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.hourMinuteSecond().withZoneUTC(),
                    ISODateTimeFormat.basicTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicTime().withZoneUTC(),
                    JsonRepresentation._HHmmss.withZoneUTC()
                    );
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.sql.Time parsedTime = new java.sql.Time(parseDateTime.getMillis());
                            return adapterFor(parsedTime);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.sql.Time) {
                    final java.sql.Time date = (java.sql.Time) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    repr.mapPut("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        putConverter(new JsonValueConverter("utc-millisec", "javasqltimestamp", java.sql.Timestamp.class){

            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr, String format) {
                if (repr.isLong()) {
                    final Long millis = repr.asLong();
                    final java.sql.Timestamp parsedTimestamp = new java.sql.Timestamp(millis);
                    return adapterFor(parsedTimestamp);
                }
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    try {
                        final Long parseMillis = Long.parseLong(dateStr);
                        final java.sql.Timestamp parsedTimestamp = new java.sql.Timestamp(parseMillis);
                        return adapterFor(parsedTimestamp);
                    } catch (IllegalArgumentException ex) {
                        // fall through
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ObjectAdapter objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.sql.Timestamp) {
                    final java.sql.Timestamp date = (java.sql.Timestamp) obj;
                    final long millisStr = date.getTime();
                    repr.mapPut("value", millisStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });
    }



    public static ObjectAdapter asAdapter(
    		final ObjectSpecification objectSpec, 
    		final JsonRepresentation argValueRepr, 
    		final String format) {
    	
        if(argValueRepr == null) {
            return null;
        }
        if (objectSpec == null) {
            throw new IllegalArgumentException("ObjectSpecification is required");
        }
        if (!argValueRepr.isValue()) {
            throw new IllegalArgumentException("Representation must be of a value");
        }
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            String reason = "ObjectSpec expected to have an EncodableFacet";
            throw new IllegalArgumentException(reason);
        }

        final ObjectSpecId specId = objectSpec.getSpecId();
        final JsonValueConverter jvc = converterBySpec.get(specId);
        if(jvc == null) {
            // best effort
            if (argValueRepr.isString()) {
                final String argStr = argValueRepr.asString();
                return encodableFacet.fromEncodedString(argStr);
            }

            throw new IllegalArgumentException("Unable to parse value");
        }

        final ObjectAdapter asAdapter = jvc.asAdapter(argValueRepr, format);
        if(asAdapter != null) {
            return asAdapter;
        }

        // last attempt
        if (argValueRepr.isString()) {
            final String argStr = argValueRepr.asString();
            try {
                return encodableFacet.fromEncodedString(argStr);
            } catch(TextEntryParseException ex) {
                throw new IllegalArgumentException(ex.getMessage());
            }
        }

        throw new IllegalArgumentException("Could not parse value '" + argValueRepr.asString() + "' as a " + objectSpec.getFullIdentifier());
    }

    public static Object appendValueAndFormat(ObjectSpecification objectSpec, ObjectAdapter objectAdapter, JsonRepresentation repr, String format, boolean suppressExtensions) {

        final JsonValueConverter jvc = converterBySpec.get(objectSpec.getSpecId());
        if(jvc != null) {
            return jvc.appendValueAndFormat(objectAdapter, format, repr, suppressExtensions);
        } else {
            final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
            if (encodableFacet == null) {
                throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
            }
            Object value = objectAdapter != null? encodableFacet.toEncodedString(objectAdapter): NullNode.getInstance();
            repr.mapPut("value", value);
            appendFormats(repr, "string", "string", suppressExtensions);
            return value;
        }
    }

    public static Object asObject(final ObjectAdapter objectAdapter, final String format) {
        if (objectAdapter == null) {
            throw new IllegalArgumentException("objectAdapter cannot be null");
        }
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();

        final JsonValueConverter jvc = converterBySpec.get(objectSpec.getSpecId());
        if(jvc != null) {
            return jvc.asObject(objectAdapter, format);
        }

        // else
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        return encodableFacet.toEncodedString(objectAdapter);
    }


    private static void appendFormats(JsonRepresentation repr, String format, String xIsisFormat, boolean suppressExtensions) {
        if(format != null) {
            repr.mapPut("format", format);
        }
        if(!suppressExtensions && xIsisFormat != null) {
            repr.mapPut("extensions.x-isis-format", xIsisFormat);
        }
    }

    private static Object unwrapAsObjectElseNullNode(ObjectAdapter objectAdapter) {
        return objectAdapter != null? objectAdapter.getPojo(): NullNode.getInstance();
    }

    // -- ObjectAdapter Provider
    
    private static ObjectAdapter adapterFor(Object value) {
        return pojoToAdapterFunction().apply(value);
    }

    private static Function<Object, ObjectAdapter> testPojoToAdapterFunction;

    // for testing purposes only
    static void testSetAdapterManager(Function<Object, ObjectAdapter> adapterManager) {
        JsonValueEncoder.testPojoToAdapterFunction = adapterManager;
    }

    private static Function<Object, ObjectAdapter> pojoToAdapterFunction() {
        return testPojoToAdapterFunction != null
        		? testPojoToAdapterFunction
        				: IsisContext.newManagedObjectContext()::adapterOfPojo;
    }
    

}
