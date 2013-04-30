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
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.codehaus.jackson.node.NullNode;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
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
        });

        putConverter(new JsonValueConverter("date", "jodalocaldate", LocalDate.class){

            final DateTimeFormatter yyyyMMdd = JsonRepresentation.yyyyMMdd;

            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    try {
                        final Date parsedDate = yyyyMMdd.parseDateTime(dateStr).toDate();
                        return adapterFor(parsedDate);
                    } catch (IllegalArgumentException ex) {
                        // fall through
                    }
                }
                return null;
            }

            @Override
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final LocalDate date = (LocalDate) unwrap(objectAdapter);
                final String dateStr = yyyyMMdd.print(date.toDateTimeAtStartOfDay());
                append(repr, dateStr, format, xIsisFormat);
            }
        });

        putConverter(new JsonValueConverter("date-time", "jodalocaldatetime", LocalDateTime.class){
            final DateTimeFormatter yyyyMMddHHmmss = JsonRepresentation.yyyyMMddTHHmmssZ;
            @Override
            public ObjectAdapter asAdapter(JsonRepresentation repr) {
                if (repr.isString()) {
                    final String dateStr = repr.asString();
                    try {
                        final Date parsedDate = yyyyMMddHHmmss.parseDateTime(dateStr).toDate();
                        return adapterFor(parsedDate);
                    } catch (IllegalArgumentException ex) {
                        // fall through
                    }
                }
                return null;
            }
    
            @Override
            public void appendValueAndFormat(ObjectAdapter objectAdapter, JsonRepresentation repr) {
                final LocalDateTime date = (LocalDateTime) unwrap(objectAdapter);
                final String dateStr = yyyyMMddHHmmss.print(date.toDateTime());
                append(repr, dateStr, format, xIsisFormat);
            }
        });
        
    }



    public static ObjectAdapter asAdapter(final ObjectSpecification objectSpec, final JsonRepresentation argRepr) {
        if (objectSpec == null) {
            String reason = "ObjectSpec is null, cannot validate";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        final EncodableFacet encodableFacet = objectSpec.getFacet(EncodableFacet.class);
        if (encodableFacet == null) {
            String reason = "ObjectSpec expected to have an EncodableFacet";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        
        if(!argRepr.mapHas("value")) {
            String reason = "No 'value' key";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }
        final JsonRepresentation argValueRepr = argRepr.getRepresentation("value");
        if(argValueRepr == null) {
            return null;
        }
        if (!argValueRepr.isValue()) {
            String reason = "Representation must be of a value";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        final JsonValueConverter jvc = converterBySpec.get(objectSpec.getSpecId());
        if(jvc == null) {
            // best effort
            if (argValueRepr.isString()) {
                final String argStr = argValueRepr.asString();
                return encodableFacet.fromEncodedString(argStr);
            }

            final String reason = "Unable to parse value";
            argRepr.mapPut("invalidReason", reason);
            throw new IllegalArgumentException(reason);
        }

        final ObjectAdapter asAdapter = jvc.asAdapter(argValueRepr);
        if(asAdapter != null) {
            return asAdapter;
        }
        
        // last attempt
        if (argValueRepr.isString()) {
            final String argStr = argValueRepr.asString();
            return encodableFacet.fromEncodedString(argStr);
        }

        final String reason = "Could not parse value '" + argValueRepr.asString() + "' as a " + objectSpec.getFullIdentifier();
        argRepr.mapPut("invalidReason", reason);
        throw new IllegalArgumentException(reason);
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
            append(repr, value, "decimal", "bigdecimal");
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
            repr.mapPut("x-isis-format", xIsisFormat);
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
