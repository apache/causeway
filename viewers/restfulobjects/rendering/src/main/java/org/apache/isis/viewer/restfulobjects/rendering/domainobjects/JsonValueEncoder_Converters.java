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
import java.util.function.Function;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.JsonValueEncoder.JsonValueConverter;

import lombok.val;

/**
 * Similar to Isis' value encoding, but with additional support for JSON
 * primitives.
 */
public final class JsonValueEncoder_Converters {

    public List<JsonValueConverter> asList(Function<Object, ManagedObject> pojoToAdapter) {
        
        val converters = _Lists.<JsonValueConverter>newArrayList();
        
        converters.add(new JsonValueConverter(null, "string", String.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    return pojoToAdapter.apply(repr.asString());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                val obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof String) {
                    repr.mapPut("value", (String) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        converters.add(new JsonValueConverter(null, "boolean", boolean.class, Boolean.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isBoolean()) {
                    return pojoToAdapter.apply(repr.asBoolean());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                val obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Boolean) {
                    repr.mapPut("value", (Boolean) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        converters.add(new JsonValueConverter("int", "byte", byte.class, Byte.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(repr.asNumber().byteValue());
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply((byte)(int)repr.asInt());
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply((byte)(long)repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger().byteValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                val obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Byte) {
                    repr.mapPut("value", (Byte) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        converters.add(new JsonValueConverter("int", "short", short.class, Short.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(repr.asNumber().shortValue());
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply((short)(int)repr.asInt());
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply((short)(long)repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger().shortValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Short) {
                    repr.mapPut("value", (Short) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        converters.add(new JsonValueConverter("int", "int", int.class, Integer.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isInt()) {
                    return pojoToAdapter.apply(repr.asInt());
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply(repr.asLong().intValue());
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger().intValue());
                }
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(repr.asNumber().intValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Integer) {
                    repr.mapPut("value", (Integer) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        converters.add(new JsonValueConverter("int", "long", long.class, Long.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isLong()) {
                    return pojoToAdapter.apply(repr.asLong());
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply(repr.asLong());
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger().longValue());
                }
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(repr.asNumber().longValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("decimal", "float", float.class, Float.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isDecimal()) {
                    return pojoToAdapter.apply(repr.asDouble().floatValue());
                }
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(repr.asNumber().floatValue());
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply(repr.asLong().floatValue());
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply(repr.asInt().floatValue());
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger().floatValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("decimal", "double", double.class, Double.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isDecimal()) {
                    return pojoToAdapter.apply(repr.asDouble());
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply(repr.asLong().doubleValue());
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply(repr.asInt().doubleValue());
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger().doubleValue());
                }
                if (repr.isBigDecimal()) {
                    return pojoToAdapter.apply(repr.asBigDecimal().doubleValue());
                }
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(repr.asNumber().doubleValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter(null, "char", char.class, Character.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String str = repr.asString();
                    if(str != null && str.length()>0) {
                        return pojoToAdapter.apply(str.charAt(0));
                    }
                }
                // in case a char literal was provided
                if(repr.isInt()) {
                    final Integer x = repr.asInt();
                    if(Character.MIN_VALUE <= x && x <= Character.MAX_VALUE) {
                        char c = (char) x.intValue();
                        return pojoToAdapter.apply(c);
                    }
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("big-integer(18)", "javamathbiginteger", BigInteger.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    return pojoToAdapter.apply(new BigInteger(repr.asString()));
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(repr.asBigInteger(format));
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply(BigInteger.valueOf(repr.asLong()));
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply(BigInteger.valueOf(repr.asInt()));
                }
                if (repr.isNumber()) {
                    return pojoToAdapter.apply(BigInteger.valueOf(repr.asNumber().longValue()));
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("big-decimal", "javamathbigdecimal", BigDecimal.class){
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    return pojoToAdapter.apply(new BigDecimal(repr.asString()));
                }
                if (repr.isBigDecimal()) {
                    return pojoToAdapter.apply(repr.asBigDecimal(format));
                }
                if (repr.isBigInteger()) {
                    return pojoToAdapter.apply(new BigDecimal(repr.asBigInteger()));
                }
                if (repr.isDecimal()) {
                    return pojoToAdapter.apply(BigDecimal.valueOf(repr.asDouble()));
                }
                if (repr.isLong()) {
                    return pojoToAdapter.apply(BigDecimal.valueOf(repr.asLong()));
                }
                if (repr.isInt()) {
                    return pojoToAdapter.apply(BigDecimal.valueOf(repr.asInt()));
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof BigDecimal) {
                    repr.mapPut("value", (BigDecimal) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, format != null ? format : this.format, xIsisFormat, suppressExtensions);
                return obj;
            }
        });

        converters.add(new JsonValueConverter("date", "jodalocaldate", LocalDate.class){

            // these formatters do NOT use withZoneUTC()
            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.date(),
                    ISODateTimeFormat.basicDate(),
                    DateTimeFormat.forPattern("yyyyMMdd"),
                    JsonRepresentation.yyyyMMdd
                    );

            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final LocalDate parsedDate = formatter.parseLocalDate(dateStr);
                            return pojoToAdapter.apply(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("date-time", "jodalocaldatetime", LocalDateTime.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );

            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final LocalDateTime parsedDate = formatter.parseLocalDateTime(dateStr);
                            return pojoToAdapter.apply(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("date-time", "jodadatetime", DateTime.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );

            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parsedDate = formatter.parseDateTime(dateStr);
                            return pojoToAdapter.apply(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("date-time", "javautildate", java.util.Date.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.util.Date parsedDate = parseDateTime.toDate();
                            return pojoToAdapter.apply(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("date", "javasqldate", java.sql.Date.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.date().withZoneUTC(),
                    ISODateTimeFormat.basicDate().withZoneUTC(),
                    JsonRepresentation.yyyyMMdd.withZoneUTC()
                    );
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.sql.Date parsedDate = new java.sql.Date(parseDateTime.getMillis());
                            return pojoToAdapter.apply(parsedDate);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("time", "javasqltime", java.sql.Time.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.hourMinuteSecond().withZoneUTC(),
                    ISODateTimeFormat.basicTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicTime().withZoneUTC(),
                    JsonRepresentation._HHmmss.withZoneUTC()
                    );
            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.sql.Time parsedTime = new java.sql.Time(parseDateTime.getMillis());
                            return pojoToAdapter.apply(parsedTime);
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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

        converters.add(new JsonValueConverter("utc-millisec", "javasqltimestamp", java.sql.Timestamp.class){

            @Override
            public ManagedObject asAdapter(JsonRepresentation repr, String format) {
                if (repr.isLong()) {
                    final Long millis = repr.asLong();
                    final java.sql.Timestamp parsedTimestamp = new java.sql.Timestamp(millis);
                    return pojoToAdapter.apply(parsedTimestamp);
                }
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    try {
                        final Long parseMillis = Long.parseLong(dateStr);
                        final java.sql.Timestamp parsedTimestamp = new java.sql.Timestamp(parseMillis);
                        return pojoToAdapter.apply(parsedTimestamp);
                    } catch (IllegalArgumentException ex) {
                        // fall through
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(ManagedObject objectAdapter, String format, JsonRepresentation repr, boolean suppressExtensions) {
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
        
        return converters;
    }

    
    static void appendFormats(JsonRepresentation repr, String format, String xIsisFormat, boolean suppressExtensions) {
        JsonValueEncoder.appendFormats(repr, format, xIsisFormat, suppressExtensions);
    }
    
    static Object unwrapAsObjectElseNullNode(ManagedObject adapter) {
        return JsonValueEncoder.unwrapAsObjectElseNullNode(adapter);
    }

}
