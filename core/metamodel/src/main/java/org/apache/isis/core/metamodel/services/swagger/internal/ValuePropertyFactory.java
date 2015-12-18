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
package org.apache.isis.core.metamodel.services.swagger.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.ByteArrayProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import io.swagger.models.properties.UUIDProperty;

public class ValuePropertyFactory {

    private final Map<Class, Factory> propertyFactoryByClass = Maps.newHashMap();

    static interface Factory {
        Property newProperty();
    }
    public ValuePropertyFactory() {

        final Factory booleanFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new BooleanProperty();
            }
        };
        propertyFactoryByClass.put(boolean.class, booleanFactory);
        propertyFactoryByClass.put(Boolean.class, booleanFactory);

        final Factory integerFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new IntegerProperty();
            }
        };
        propertyFactoryByClass.put(byte.class, integerFactory);
        propertyFactoryByClass.put(Byte.class, integerFactory);
        propertyFactoryByClass.put(short.class, integerFactory);
        propertyFactoryByClass.put(Short.class, integerFactory);
        propertyFactoryByClass.put(int.class, integerFactory);
        propertyFactoryByClass.put(Integer.class, integerFactory);
        propertyFactoryByClass.put(BigInteger.class, integerFactory);

        final Factory longFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new LongProperty();
            }
        };
        propertyFactoryByClass.put(long.class, longFactory);
        propertyFactoryByClass.put(Long.class, longFactory);
        propertyFactoryByClass.put(java.sql.Timestamp.class, longFactory);

        final Factory decimalFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new DecimalProperty();
            }
        };
        propertyFactoryByClass.put(BigDecimal.class, decimalFactory);

        final Factory floatFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new FloatProperty();
            }
        };
        propertyFactoryByClass.put(float.class, floatFactory);
        propertyFactoryByClass.put(Float.class, floatFactory);

        final Factory doubleFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new DoubleProperty();
            }
        };
        propertyFactoryByClass.put(double.class, doubleFactory);
        propertyFactoryByClass.put(Double.class, doubleFactory);

        final Factory stringFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new StringProperty();
            }
        };
        propertyFactoryByClass.put(char.class, stringFactory);
        propertyFactoryByClass.put(Character.class, stringFactory);
        propertyFactoryByClass.put(char[].class, stringFactory);
        propertyFactoryByClass.put(String.class, stringFactory);

        final Factory uuidFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new UUIDProperty();
            }
        };
        propertyFactoryByClass.put(UUID.class, uuidFactory);

        final Factory dateTimeFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new DateTimeProperty();
            }
        };
        propertyFactoryByClass.put(java.util.Date.class, dateTimeFactory);
        propertyFactoryByClass.put(DateTime.class, dateTimeFactory);
        propertyFactoryByClass.put(LocalDateTime.class, dateTimeFactory);
        propertyFactoryByClass.put(org.apache.isis.applib.value.DateTime.class, dateTimeFactory);

        final Factory dateFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new DateProperty();
            }
        };
        propertyFactoryByClass.put(java.sql.Date.class, dateFactory);
        propertyFactoryByClass.put(LocalDate.class, dateFactory);
        propertyFactoryByClass.put(org.apache.isis.applib.value.Date.class, dateFactory);

        final Factory byteArrayFactory = new Factory() {
            @Override
            public Property newProperty() {
                return new ByteArrayProperty();
            }
        };
        propertyFactoryByClass.put(byte[].class, byteArrayFactory);
        propertyFactoryByClass.put(org.apache.isis.applib.value.Blob.class, byteArrayFactory);
    }

    public Property newProperty(Class<?> cls) {
        if(cls == null) {
            return null;
        }

        final Factory factory = propertyFactoryByClass.get(cls);
        if(factory != null) {
            return factory.newProperty();
        }

        // special case, want to treat as a value
        if(cls.isEnum()) {
            final StringProperty property = new StringProperty();
            final Object[] enumConstants = cls.getEnumConstants();

            final List<String> enumNames = Lists.newArrayList(
                    Iterables.transform(Arrays.asList(enumConstants), new Function<Object, String>() {
                        @Nullable @Override public String apply(@Nullable final Object input) {
                            return ((Enum<?>)input).name();
                        }
                    }));
            property.setEnum(enumNames);
            return property;
        }

        return null;
    }

}
