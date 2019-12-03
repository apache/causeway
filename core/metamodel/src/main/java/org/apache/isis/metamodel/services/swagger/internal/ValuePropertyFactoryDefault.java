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
package org.apache.isis.metamodel.services.swagger.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.context._Plugin;
import org.apache.isis.metamodel.services.swagger.internal.ValuePropertyPlugin.ValuePropertyCollector;
import org.springframework.stereotype.Component;

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

@Component
public class ValuePropertyFactoryDefault implements ValuePropertyFactory {

    private final Map<Class<?>, Factory> propertyFactoryByClass = _Maps.newHashMap();

    public static interface Factory extends Supplier<Property> {};

    public ValuePropertyFactoryDefault() {

        propertyFactoryByClass.put(boolean.class, BooleanProperty::new);
        propertyFactoryByClass.put(Boolean.class, BooleanProperty::new);

        propertyFactoryByClass.put(byte.class, IntegerProperty::new);
        propertyFactoryByClass.put(Byte.class, IntegerProperty::new);
        propertyFactoryByClass.put(short.class, IntegerProperty::new);
        propertyFactoryByClass.put(Short.class, IntegerProperty::new);
        propertyFactoryByClass.put(int.class, IntegerProperty::new);
        propertyFactoryByClass.put(Integer.class, IntegerProperty::new);
        propertyFactoryByClass.put(BigInteger.class, IntegerProperty::new);

        propertyFactoryByClass.put(long.class, LongProperty::new);
        propertyFactoryByClass.put(Long.class, LongProperty::new);
        propertyFactoryByClass.put(java.sql.Timestamp.class, LongProperty::new);

        propertyFactoryByClass.put(BigDecimal.class, DecimalProperty::new);

        propertyFactoryByClass.put(float.class, FloatProperty::new);
        propertyFactoryByClass.put(Float.class, FloatProperty::new);

        propertyFactoryByClass.put(double.class, DoubleProperty::new);
        propertyFactoryByClass.put(Double.class, DoubleProperty::new);

        propertyFactoryByClass.put(char.class, StringProperty::new);
        propertyFactoryByClass.put(Character.class, StringProperty::new);
        propertyFactoryByClass.put(char[].class, StringProperty::new);
        propertyFactoryByClass.put(String.class, StringProperty::new);

        propertyFactoryByClass.put(UUID.class, UUIDProperty::new);

        propertyFactoryByClass.put(java.util.Date.class, DateTimeProperty::new);
        propertyFactoryByClass.put(DateTime.class, DateTimeProperty::new);
        propertyFactoryByClass.put(LocalDateTime.class, DateTimeProperty::new);

        propertyFactoryByClass.put(java.sql.Date.class, DateProperty::new);
        propertyFactoryByClass.put(LocalDate.class, DateProperty::new);

        propertyFactoryByClass.put(byte[].class, ByteArrayProperty::new);
        propertyFactoryByClass.put(org.apache.isis.applib.value.Blob.class, ByteArrayProperty::new);

        // add propertyFactories from plugins
        discoverValueProperties().visitEntries(propertyFactoryByClass::put);

    }

    @Override
    public Property newProperty(Class<?> cls) {
        if(cls == null) {
            return null;
        }

        final Factory factory = propertyFactoryByClass.get(cls);
        if(factory != null) {
            return factory.get();
        }

        // special case, want to treat as a value
        if(cls.isEnum()) {
            final StringProperty property = new StringProperty();
            final Object[] enumConstants = cls.getEnumConstants();

            final List<String> enumNames = _Lists.map(
                    Arrays.asList(enumConstants), input->((Enum<?>)input).name());
            property.setEnum(enumNames);
            return property;
        }

        return null;
    }

    // -- HELPER

    private static ValuePropertyCollector discoverValueProperties() {
        final Set<ValuePropertyPlugin> plugins = _Plugin.loadAll(ValuePropertyPlugin.class);
        final ValuePropertyCollector collector = ValuePropertyPlugin.collector();
        plugins.forEach(plugin->{
            plugin.plugin(collector);
        });
        return collector;
    }

}
