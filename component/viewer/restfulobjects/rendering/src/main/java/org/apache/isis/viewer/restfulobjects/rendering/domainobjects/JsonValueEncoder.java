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
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.codehaus.jackson.node.NullNode;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.parseable.TextEntryParseException;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;

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
            return Lists.newArrayList(Iterables.transform(Arrays.asList(classes), new Function<Class<?>, ObjectSpecId>() {
                public ObjectSpecId apply(Class<?> cls) {
                    return new ObjectSpecId(cls.getName());
                }
            }));
        }
        
        /**
         * The value, otherwise <tt>null</tt>.
         */
        public abstract ObjectAdapter asAdapter(JsonRepresentation repr);
        
        public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
            append(repr, objectAdapter, format, xIsisFormat);
        }

        public Object asObject(ObjectAdapter objectAdapter) {
            return objectAdapter.getObject();
        }
    }
    
    private static Map<ObjectSpecId, JsonValueConverter> converterBySpec = Maps.newLinkedHashMap();
    
    private static void putConverter(JsonValueConverter jvc) {
        final List<ObjectSpecId> specIds = jvc.getSpecIds();
        for (ObjectSpecId specId : specIds) {
            converterBySpec.put(specId, jvc);
        }
    }

    static {
        putConverter(new JsonValueConverter(null, "string", String.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                if (repr.isString()) {
                    return adapterFor(repr.asString());
                } 
                return null;
            }
        });

        putConverter(new JsonValueConverter(null, "boolean", boolean.class, Boolean.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                if (repr.isBoolean()) {
                    return adapterFor(repr.asBoolean());
                } 
                return null;
            }
        });
        
        putConverter(new JsonValueConverter(null, "byte", byte.class, Byte.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
        });
        
        putConverter(new JsonValueConverter(null, "short", short.class, Short.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
        });
        
        putConverter(new JsonValueConverter("int", "int", int.class, Integer.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
        });
        
        putConverter(new JsonValueConverter("int", "long", long.class, Long.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                if (repr.isLong()) {
                    return adapterFor(repr.asLong());
                }
                if (repr.isInt()) {
                    return adapterFor(repr.asInt());
                }
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger().longValue());
                }
                if (repr.isNumber()) {
                    return adapterFor(repr.asNumber().longValue());
                }
                return null;
            }
        });
        
        putConverter(new JsonValueConverter("decimal", "float", float.class, Float.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
        });
        
        putConverter(new JsonValueConverter("decimal", "double", double.class, Double.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
        });
        
        putConverter(new JsonValueConverter(null, "char", char.class, Character.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
        });
        
        putConverter(new JsonValueConverter("int", "biginteger", BigInteger.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                if (repr.isBigInteger()) {
                    return adapterFor(repr.asBigInteger());
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
        });
        
        putConverter(new JsonValueConverter("decimal", "bigdecimal", BigDecimal.class){
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                // TODO: if inferring a BigDecimal, need to get the scale from somewhere...
                if (repr.isBigDecimal()) {
                    return adapterFor(repr.asBigDecimal());
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                super.appendValueAndFormat(objectAdapter, repr);
            }
        });

        putConverter(new JsonValueConverter("date", "jodalocaldate", LocalDate.class){

            final List<DateTimeFormatter> formatters = Arrays.asList(
                    JsonRepresentation.yyyyMMdd, 
                    DateTimeFormat.forPattern("yyyyMMdd"),
                    ISODateTimeFormat.basicDate()
                    );

            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter);
                if(obj instanceof LocalDate) {
                    final LocalDate date = (LocalDate) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTimeAtStartOfDay());
                    append(repr, dateStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });

        putConverter(new JsonValueConverter("date-time", "jodalocaldatetime", LocalDateTime.class){
            
            final List<DateTimeFormatter> formatters = Arrays.asList(
                    JsonRepresentation.yyyyMMddTHHmmssZ, 
                    DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ"), 
                    ISODateTimeFormat.basicDateTimeNoMillis(),
                    ISODateTimeFormat.basicDateTime()
                    );
            
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter); 
                if(obj instanceof LocalDateTime) {
                    final LocalDateTime date = (LocalDateTime) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTime());
                    append(repr, dateStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });

        putConverter(new JsonValueConverter("date-time", "jodadatetime", DateTime.class){
            
            final List<DateTimeFormatter> formatters = Arrays.asList(
                    JsonRepresentation.yyyyMMddTHHmmssZ, 
                    DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ"), 
                    ISODateTimeFormat.basicDateTimeNoMillis(),
                    ISODateTimeFormat.basicDateTime()
                    );
            
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter); 
                if(obj instanceof DateTime) {
                    final DateTime date = (DateTime) obj;
                    final String dateStr = formatters.get(0).print(date.toDateTime());
                    append(repr, dateStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });

        putConverter(new JsonValueConverter("date", "javautildate", java.util.Date.class){
            
            final List<DateTimeFormatter> formatters = Arrays.asList(
                    JsonRepresentation.yyyyMMddTHHmmssZ, 
                    DateTimeFormat.forPattern("yyyyMMdd'T'HHmmssZ"), 
                    ISODateTimeFormat.basicDateTimeNoMillis(),
                    ISODateTimeFormat.basicDateTime()
                    );
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter); 
                if(obj instanceof java.util.Date) {
                    final java.util.Date date = (java.util.Date) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    append(repr, dateStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });

        putConverter(new JsonValueConverter("date", "javasqldate", java.sql.Date.class){
            
            final List<DateTimeFormatter> formatters = Arrays.asList(
                            JsonRepresentation.yyyyMMdd, 
                            DateTimeFormat.forPattern("yyyyMMdd"),
                            ISODateTimeFormat.basicDate()
                            );
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter); 
                if(obj instanceof java.sql.Date) {
                    final java.sql.Date date = (java.sql.Date) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    append(repr, dateStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });

        putConverter(new JsonValueConverter("date", "javasqltime", java.sql.Time.class){
            
            final List<DateTimeFormatter> formatters = Arrays.asList(
                        JsonRepresentation._HHmmss, 
                        DateTimeFormat.forPattern("HHmmss"), 
                        ISODateTimeFormat.basicTime());
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter); 
                if(obj instanceof java.sql.Time) {
                    final java.sql.Time date = (java.sql.Time) obj;
                    final String dateStr = formatters.get(0).print(new DateTime(date));
                    append(repr, dateStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });

        putConverter(new JsonValueConverter("utc-millisec", "javasqltimestamp", java.sql.Timestamp.class){
            
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
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
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final Object obj = unwrap(objectAdapter); 
                if(obj instanceof java.sql.Timestamp) {
                    final java.sql.Timestamp date = (java.sql.Timestamp) obj;
                    final long millisStr = date.getTime();
                    append(repr, millisStr, format, xIsisFormat);
                } else {
                    append(repr, obj, format, xIsisFormat);
                }
            }
        });
    }



    public static ObjectAdapter asAdapter(final ObjectSpecification objectSpec, final JsonRepresentation argValueRepr) {
        if(argValueRepr == null) {
            return null;
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

        final ObjectAdapter asAdapter = jvc.asAdapter(argValueRepr);
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

    public static void appendValueAndFormat(ObjectSpecification objectSpec, ObjectAdapter objectAdapter, JsonRepresentation repr) {

        final JsonValueConverter jvc = converterBySpec.get(objectSpec.getSpecId());
        if(jvc != null) {
            jvc.appendValueAndFormat(objectAdapter, repr);
        } else {
            final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
            if (encodableFacet == null) {
                throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
            }
            Object value = objectAdapter != null? encodableFacet.toEncodedString(objectAdapter): NullNode.getInstance();
            append(repr, value, "string", "string");
        }
    }
    
    public static Object asObject(final ObjectAdapter objectAdapter) {
        if (objectAdapter == null) {
            throw new IllegalArgumentException("objectAdapter cannot be null");
        }
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();

        final JsonValueConverter jvc = converterBySpec.get(objectSpec.getSpecId());
        if(jvc != null) {
            return jvc.asObject(objectAdapter);
        } 
        
        // else
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            throw new IllegalArgumentException("objectSpec expected to have EncodableFacet");
        }
        return encodableFacet.toEncodedString(objectAdapter);
    }

    

    private static void append(JsonRepresentation repr, Object value, String format, String xIsisFormat) {
        repr.mapPut("value", value);
        if(format != null) {
            repr.mapPut("format", format);
        }
        if(xIsisFormat != null) {
            repr.mapPut("extensions.x-isis-format", xIsisFormat);
        }
    }

    private static void append(JsonRepresentation repr, ObjectAdapter value, String format, String xIsisFormat) {
        append(repr, unwrap(value), format, xIsisFormat);
    }
    
    private static Object unwrap(ObjectAdapter objectAdapter) {
        return objectAdapter != null? objectAdapter.getObject(): NullNode.getInstance();
    }



    private static ObjectAdapter adapterFor(Object value) {
        return getAdapterManager().adapterFor(value);
    }
    
    public static AdapterManager getAdapterManager() {
        return IsisContext.getPersistenceSession().getAdapterManager();
    }

}
