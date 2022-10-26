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
package org.apache.causeway.viewer.restfulobjects.rendering.service.valuerender;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Has support for JSON primitives.
 */
final class _JsonValueConverters {

    @RequiredArgsConstructor
    public static enum DefaultFormat {
        STRING(String.class, null, "string"),
        BOOLEAN(Boolean.class, null, "boolean"),
        BYTE(Byte.class, "int", "byte"),
        SHORT(Short.class, "int", "short"),
        INT(Integer.class, "int", "int"),
        LONG(Long.class, "int", "long"),
        FLOAT(Float.class, "decimal", "float"),
        DOUBLE(Double.class, "decimal", "double"),
        CHAR(Character.class, null, "char"),

        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        BIGINTEGER(BigInteger.class, "big-integer(18)", "javamathbiginteger"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        BIGDECIMAL(BigDecimal.class, "big-decimal", "javamathbigdecimal"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JODALOCALDATE(LocalDate.class, "date", "jodalocaldate"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JODALOCALDATETIME(LocalDateTime.class, "date-time", "jodalocaldatetime"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JODADATETIME(DateTime.class, "date-time", "jodadatetime"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JAVAUTILDATE(java.util.Date.class, "date-time", "javautildate"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JAVASQLDATE(java.sql.Date.class, "date", "javasqldate"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JAVASQLTIME(java.sql.Time.class, "time", "javasqltime"),
        @Deprecated //should be covered 100% per {@link ValueSemanticsProvider}
        JAVASQLTIMESTAMP(java.sql.Timestamp.class, "utc-millisec", "javasqltimestamp"),

        ;
        final Class<?> valueClass;
        final String format;
        final String extendedFormat;
    }

    public List<JsonValueConverter> asList() {

        val converters = _Lists.<JsonValueConverter>newArrayList();

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.STRING){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    return repr.asString();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                val obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof String) {
                    repr.mapPutString("value", (String) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.BOOLEAN){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isBoolean()) {
                    return repr.asBoolean();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                val obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Boolean) {
                    repr.mapPutBooleanNullable("value", (Boolean) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.BYTE){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isNumber()) {
                    return repr.asNumber().byteValue();
                }
                if (repr.isInt()) {
                    return (byte)(int)repr.asInt();
                }
                if (repr.isLong()) {
                    return (byte)(long)repr.asLong();
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger().byteValue();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                val obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Byte) {
                    repr.mapPutByteNullable("value", (Byte) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.SHORT){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isNumber()) {
                    return repr.asNumber().shortValue();
                }
                if (repr.isInt()) {
                    return (short)(int)repr.asInt();
                }
                if (repr.isLong()) {
                    return (short)(long)repr.asLong();
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger().shortValue();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Short) {
                    repr.mapPutShortNullable("value", (Short) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.INT){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isInt()) {
                    return repr.asInt();
                }
                if (repr.isLong()) {
                    return repr.asLong().intValue();
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger().intValue();
                }
                if (repr.isNumber()) {
                    return repr.asNumber().intValue();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Integer) {
                    repr.mapPutIntNullable("value", (Integer) obj);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.LONG){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isLong()) {
                    return repr.asLong();
                }
                if (repr.isInt()) {
                    return repr.asLong();
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger().longValue();
                }
                if (repr.isNumber()) {
                    return repr.asNumber().longValue();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Long) {
                    final Long l = (Long) obj;
                    repr.mapPutLongNullable("value", l);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.FLOAT){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isDecimal()) {
                    return repr.asDouble().floatValue();
                }
                if (repr.isNumber()) {
                    return repr.asNumber().floatValue();
                }
                if (repr.isLong()) {
                    return repr.asLong().floatValue();
                }
                if (repr.isInt()) {
                    return repr.asInt().floatValue();
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger().floatValue();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Float) {
                    final Float f = (Float) obj;
                    repr.mapPutFloatNullable("value", f);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.DOUBLE){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isDecimal()) {
                    return repr.asDouble();
                }
                if (repr.isLong()) {
                    return repr.asLong().doubleValue();
                }
                if (repr.isInt()) {
                    return repr.asInt().doubleValue();
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger().doubleValue();
                }
                if (repr.isBigDecimal()) {
                    return repr.asBigDecimal().doubleValue();
                }
                if (repr.isNumber()) {
                    return repr.asNumber().doubleValue();
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Double) {
                    final Double d = (Double) obj;
                    repr.mapPutDoubleNullable("value", d);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.CHAR){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String str = repr.asString();
                    if(str != null && str.length()>0) {
                        return str.charAt(0);
                    }
                }
                // in case a char literal was provided
                if(repr.isInt()) {
                    final Integer x = repr.asInt();
                    if(Character.MIN_VALUE <= x && x <= Character.MAX_VALUE) {
                        char c = (char) x.intValue();
                        return c;
                    }
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof Character) {
                    final Character c = (Character) obj;
                    repr.mapPutCharNullable("value", c);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.BIGINTEGER){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    return new BigInteger(repr.asString());
                }
                if (repr.isBigInteger()) {
                    return repr.asBigInteger(format);
                }
                if (repr.isLong()) {
                    return BigInteger.valueOf(repr.asLong());
                }
                if (repr.isInt()) {
                    return BigInteger.valueOf(repr.asInt());
                }
                if (repr.isNumber()) {
                    return BigInteger.valueOf(repr.asNumber().longValue());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof BigInteger) {
                    final BigInteger bi = (BigInteger) obj;
                    repr.mapPutBigInteger("value", bi);
                    // custom format if constrained
                    context.maxTotalDigits(objectAdapter)
                    .ifPresentOrElse(
                            totalDigits->repr.putFormat(String.format("big-integer(%d)", totalDigits)),
                            ()->repr.putFormat(format));
                } else {
                    repr.mapPut("value", obj);
                    repr.putFormat(format);
                }
                if(!context.isSuppressExtensions()) {
                    repr.putExtendedFormat(extendedFormat);
                }
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.BIGDECIMAL){
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    return new BigDecimal(repr.asString());
                }
                if (repr.isBigDecimal()) {
                    return repr.asBigDecimal(format);
                }
                if (repr.isBigInteger()) {
                    return new BigDecimal(repr.asBigInteger());
                }
                if (repr.isDecimal()) {
                    return BigDecimal.valueOf(repr.asDouble());
                }
                if (repr.isLong()) {
                    return BigDecimal.valueOf(repr.asLong());
                }
                if (repr.isInt()) {
                    return BigDecimal.valueOf(repr.asInt());
                }
                return null;
            }
            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof BigDecimal) {
                    final BigDecimal bd = (BigDecimal) obj;
                    repr.mapPutBigDecimal("value", bd);

                    // custom format if constrained
                    final int totalDigits = context.maxTotalDigits(objectAdapter).orElse(-1);
                    final int scale = context.maxFractionalDigits(objectAdapter).orElse(-1);
                    if(totalDigits>-1
                            && scale>-1) {
                        val formatOverride = String.format("big-decimal(%d,%d)", totalDigits, scale);
                        repr.putFormat(formatOverride);
                    } else {
                        repr.putFormat(format);
                    }

                } else {
                    repr.mapPut("value", obj);
                    repr.putFormat(format);
                }

                if(!context.isSuppressExtensions()) {
                    repr.putExtendedFormat(extendedFormat);
                }

                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JODALOCALDATE){

            // these formatters do NOT use withZoneUTC()
            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.date(),
                    ISODateTimeFormat.basicDate(),
                    DateTimeFormat.forPattern("yyyyMMdd"),
                    JsonRepresentation.yyyyMMdd
                    );

            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final LocalDate parsedDate = formatter.parseLocalDate(dateStr);
                            return parsedDate;
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof LocalDate) {
                    final LocalDate date = (LocalDate) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTimeAtStartOfDay());
                    repr.mapPutString("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JODALOCALDATETIME){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );

            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final LocalDateTime parsedDate = formatter.parseLocalDateTime(dateStr);
                            return parsedDate;
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof LocalDateTime) {
                    final LocalDateTime date = (LocalDateTime) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTime());
                    repr.mapPutString("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JODADATETIME){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );

            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parsedDate = formatter.parseDateTime(dateStr);
                            return parsedDate;
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof DateTime) {
                    final DateTime date = (DateTime) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTime());
                    repr.mapPutString("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JAVAUTILDATE){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.dateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.dateTime().withZoneUTC(),
                    ISODateTimeFormat.basicDateTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicDateTime().withZoneUTC(),
                    JsonRepresentation.yyyyMMddTHHmmssZ.withZoneUTC()
                    );
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.util.Date parsedDate = parseDateTime.toDate();
                            return parsedDate;
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.util.Date) {
                    final java.util.Date date = (java.util.Date) obj;
                    final DateTimeFormatter dateTimeFormatter = formatters.get(0);
                    final String dateStr = dateTimeFormatter.print(new DateTime(date));
                    repr.mapPutString("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JAVASQLDATE){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.date().withZoneUTC(),
                    ISODateTimeFormat.basicDate().withZoneUTC(),
                    JsonRepresentation.yyyyMMdd.withZoneUTC()
                    );
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.sql.Date parsedDate = new java.sql.Date(parseDateTime.getMillis());
                            return parsedDate;
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.sql.Date) {
                    final java.sql.Date date = (java.sql.Date) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    repr.mapPutString("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JAVASQLTIME){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    ISODateTimeFormat.hourMinuteSecond().withZoneUTC(),
                    ISODateTimeFormat.basicTimeNoMillis().withZoneUTC(),
                    ISODateTimeFormat.basicTime().withZoneUTC(),
                    JsonRepresentation._HHmmss.withZoneUTC()
                    );
            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            final DateTime parseDateTime = formatter.parseDateTime(dateStr);
                            final java.sql.Time parsedTime = new java.sql.Time(parseDateTime.getMillis());
                            return parsedTime;
                        } catch (IllegalArgumentException ex) {
                            // fall through
                        }
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.sql.Time) {
                    final java.sql.Time date = (java.sql.Time) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    repr.mapPutString("value", dateStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        converters.add(new JsonValueConverter.Abstract(DefaultFormat.JAVASQLTIMESTAMP){

            @Override
            public Object recoverValueAsPojo(final JsonRepresentation repr, final Context context) {
                if (repr.isLong()) {
                    final Long millis = repr.asLong();
                    final java.sql.Timestamp parsedTimestamp = new java.sql.Timestamp(millis);
                    return parsedTimestamp;
                }
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    try {
                        final Long parseMillis = Long.parseLong(dateStr);
                        final java.sql.Timestamp parsedTimestamp = new java.sql.Timestamp(parseMillis);
                        return parsedTimestamp;
                    } catch (IllegalArgumentException ex) {
                        // fall through
                    }
                }
                return null;
            }

            @Override
            public Object appendValueAndFormat(final ManagedObject objectAdapter, final Context context,
                    final JsonRepresentation repr) {
                final Object obj = unwrapAsObjectElseNullNode(objectAdapter);
                if(obj instanceof java.sql.Timestamp) {
                    final java.sql.Timestamp date = (java.sql.Timestamp) obj;
                    final long millisStr = date.getTime();
                    repr.mapPutLong("value", millisStr);
                } else {
                    repr.mapPut("value", obj);
                }
                appendFormats(repr, context);
                return obj;
            }
        });

        return converters;
    }

}
