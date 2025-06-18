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
package org.apache.causeway.commons.internal.proxy;

import java.lang.reflect.Type;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Internal service, that generates {@link _ProxyFactory}(s). 
 * 
 * @since 2.0
 */
public interface _ProxyFactoryService {

    <T> _ProxyFactory<T> factory(
            Class<T> base,
            @Nullable Class<?>[] interfaces,
            @Nullable List<AdditionalField> additionalFields,
            @Nullable Class<?>[] constructorArgTypes);

    <T> _ProxyFactory<T> factory(
            Class<T> toProxyClass, 
            @Nullable Class<?> additionalClass,
            @Nullable List<AdditionalField> additionalFields);

    /**
     * Marker interface for entities/services that have been enhanced with
     * the framework's proxy factory.
     */
    interface ProxyEnhanced {

    }
    
    /**
     * Defines a field, that can be added to a proxy class.
     */
    record AdditionalField(
            String name, 
            Type type, 
            int modifiers) {
    }
}
