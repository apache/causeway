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
package org.apache.isis.core.metamodel.valuetypes;

import java.util.Map;

import org.apache.isis.schema.common.v2.ValueType;

import lombok.Value;

@Value
public class ValueTypeDefinition {

    public static ValueTypeDefinition collection(Class<?> clazz) {
        return new ValueTypeDefinition(clazz, ValueType.COLLECTION);
    }
    public static ValueTypeDefinition of(Class<?> clazz, ValueType valueType) {
        return new ValueTypeDefinition(clazz, valueType);
    }
    public static ValueTypeDefinition from(final Map.Entry<Class<?>, ValueType> entry) {
        return new ValueTypeDefinition(entry.getKey(), entry.getValue());
    }

    Class<?> clazz;
    ValueType valueType;

    private ValueTypeDefinition(Class<?> clazz, ValueType valueType) {
        this.clazz = clazz;
        this.valueType = valueType;
    }
}
