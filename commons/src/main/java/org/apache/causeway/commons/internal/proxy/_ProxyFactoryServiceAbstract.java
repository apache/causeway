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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.collections._Arrays;

/**
 * Partly implements {@link _ProxyFactoryService} and adds caching.
 * 
 * @since 2.0
 */
public abstract class _ProxyFactoryServiceAbstract implements _ProxyFactoryService {

    @NonNull
    private final Map<Class<?>, _ProxyFactory<?>> proxyFactoryByClass =
            Collections.synchronizedMap(new WeakHashMap<>());

    @Override
    public final <T> _ProxyFactory<T> factory(
            final Class<T> classToBeProxied, 
            final Class<?> additionalClass,
            final @Nullable List<AdditionalField> additionalFields) {
        _ProxyFactory<T> proxyFactory = _Casts.uncheckedCast(proxyFactoryByClass.get(classToBeProxied));
        if(proxyFactory == null) {
            proxyFactory = createFactory(classToBeProxied, additionalClass, additionalFields);
            proxyFactoryByClass.put(classToBeProxied, proxyFactory);
        }
        return proxyFactory;
    }

    private <T> _ProxyFactory<T> createFactory(
            final Class<T> classToBeProxied,
            final Class<?> additionalClass, 
            final @Nullable List<AdditionalField> additionalFields) {

        final Class<?>[] interfaces = _Arrays.combine(
                classToBeProxied.getInterfaces(),
                ProxyEnhanced.class, additionalClass);
        
        return factory(classToBeProxied, interfaces, additionalFields, null);
    }

}
