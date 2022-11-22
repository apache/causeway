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
package org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Named;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.rendering.service.swagger.internal.ValuePropertyPlugin.ValuePropertyCollector;

import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.ByteArraySchema;
import io.swagger.v3.oas.models.media.DateSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.media.UUIDSchema;

@Component
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".ValuePropertyFactoryDefault")
public class ValuePropertyFactoryDefault implements ValuePropertyFactory {

    private final Map<Class<?>, Factory> propertyFactoryByClass = _Maps.newHashMap();

    @Autowired(required = false) private List<ValuePropertyPlugin> valuePropertyPlugins;

    public static interface Factory extends Supplier<Schema> {};

    public ValuePropertyFactoryDefault() {

        propertyFactoryByClass.put(boolean.class, BooleanSchema::new);
        propertyFactoryByClass.put(Boolean.class, BooleanSchema::new);

        propertyFactoryByClass.put(byte.class, IntegerSchema::new);
        propertyFactoryByClass.put(Byte.class, IntegerSchema::new);
        propertyFactoryByClass.put(short.class, IntegerSchema::new);
        propertyFactoryByClass.put(Short.class, IntegerSchema::new);
        propertyFactoryByClass.put(int.class, IntegerSchema::new);
        propertyFactoryByClass.put(Integer.class, IntegerSchema::new);

      //TODO[ISIS-3292] implement or replace ...
//        propertyFactoryByClass.put(BigInteger.class, IntegerSchema::new);

//        propertyFactoryByClass.put(long.class, LongSchema::new);
//        propertyFactoryByClass.put(Long.class, LongSchema::new);
//        propertyFactoryByClass.put(java.sql.Timestamp.class, LongSchema::new);

        propertyFactoryByClass.put(BigDecimal.class, NumberSchema::new);

      //TODO[ISIS-3292] implement or replace ...
//        propertyFactoryByClass.put(float.class, FloatSchema::new);
//        propertyFactoryByClass.put(Float.class, FloatSchema::new);
//
//        propertyFactoryByClass.put(double.class, DoubleSchema::new);
//        propertyFactoryByClass.put(Double.class, DoubleSchema::new);

        propertyFactoryByClass.put(char.class, StringSchema::new);
        propertyFactoryByClass.put(Character.class, StringSchema::new);
        propertyFactoryByClass.put(char[].class, StringSchema::new);
        propertyFactoryByClass.put(String.class, StringSchema::new);

        propertyFactoryByClass.put(UUID.class, UUIDSchema::new);

        propertyFactoryByClass.put(java.util.Date.class, DateTimeSchema::new);
        propertyFactoryByClass.put(DateTime.class, DateTimeSchema::new);
        propertyFactoryByClass.put(LocalDateTime.class, DateTimeSchema::new);

        propertyFactoryByClass.put(java.sql.Date.class, DateSchema::new);
        propertyFactoryByClass.put(LocalDate.class, DateSchema::new);

        propertyFactoryByClass.put(byte[].class, ByteArraySchema::new);
        propertyFactoryByClass.put(org.apache.causeway.applib.value.Blob.class, ByteArraySchema::new);

        // add propertyFactories from plugins
        discoverValueProperties().visitEntries(propertyFactoryByClass::put);

    }

    @Override
    public Schema newProperty(final Class<?> cls) {
        if(cls == null) {
            return null;
        }

        final Factory factory = propertyFactoryByClass.get(cls);
        if(factory != null) {
            return factory.get();
        }

        // special case, want to treat as a value
        if(cls.isEnum()) {
            final StringSchema property = new StringSchema();
            final Object[] enumConstants = cls.getEnumConstants();

            final List<String> enumNames = _Lists.map(
                    Arrays.asList(enumConstants), input->((Enum<?>)input).name());
            property.setEnum(enumNames);
            return property;
        }

        return null;
    }

    // -- HELPER

    private ValuePropertyCollector discoverValueProperties() {

        final ValuePropertyCollector collector = ValuePropertyPlugin.collector();
        _NullSafe.stream(valuePropertyPlugins).forEach(plugin->{
            plugin.plugin(collector);
        });
        return collector;
    }

}
