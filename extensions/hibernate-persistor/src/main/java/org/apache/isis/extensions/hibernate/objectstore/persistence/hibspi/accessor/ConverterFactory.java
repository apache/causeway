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


package org.apache.isis.extensions.hibernate.objectstore.persistence.hibspi.accessor;

import java.util.HashMap;

import org.apache.isis.metamodel.spec.feature.ObjectAssociation;


/**
 * Factory to return a {@link PropertyConverter} for conversion between a specific Isis value holder type and
 * standard Java data type.
 * 
 * <p>
 * Additional Converters can be added by calling {@link #add(Class, PropertyConverter)}.
 * 
 * <p>
 * Note that this is a {@link #getInstance() singleton.}
 * 
 * <p>
 * TODO: find some way to hook into <tt>@Value</tt> support.
 */
public class ConverterFactory {
    
    private final HashMap<String, PropertyConverter> converters = new HashMap<String, PropertyConverter>();
    
    private static ConverterFactory instance = new ConverterFactory();

    private ConverterFactory() {
    }

    /**
     * Return a {@link PropertyConverter} which converts from a value holder to a persistent class.
     * 
     * @param specification
     *            Class of the value holder to convert from eg WholeNumber.class
     */
    public PropertyConverter getConverter(final Class<?> specification) {
        return getConverter(specification.getName());
    }

    /**
     * Return a PropertyConverter which converts from a value holder to a persistent class.
     * 
     * @param specification
     *            name of the value holder to convert from
     * @return the PropertyConverter, or null if none is registered
     */
    public PropertyConverter getConverter(final String specification) {
        return (PropertyConverter) converters.get(specification);
    }

    public PropertyConverter getConverter(final ObjectAssociation field) {
        return getConverter(field.getSpecification().getFullName());
    }

    /**
     * Add a converter for the specified value type
     */
    public void add(final Class<?> valueType, final PropertyConverter converter) {
        if (!converters.containsKey(valueType.getName())) {
            converters.put(valueType.getName(), converter);
        }
    }

    /**
     * Return the singleton ConverterFactory
     */
    public static ConverterFactory getInstance() {
        return instance;
    }
}
