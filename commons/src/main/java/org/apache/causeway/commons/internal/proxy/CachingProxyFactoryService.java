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

import java.lang.reflect.InvocationHandler;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.jspecify.annotations.Nullable;

/**
 * Partly implements {@link ProxyFactoryService} and adds caching.
 *
 * @since 3.4
 */
public abstract class CachingProxyFactoryService implements ProxyFactoryService {

    private final Map<Class<?>, ProxyFactory<?>> proxyFactoryCache =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<String, Class<?>> proxyClassCache =
            Collections.synchronizedMap(new WeakHashMap<>());

    @SuppressWarnings("unchecked")
    @Override
    public final <T> Class<? extends T> proxyClass(
            InvocationHandler handler,
            Class<T> base,
            Class<?>[] interfaces,
            @Nullable List<AdditionalField> additionalFields) {
        var proxyClass = handler instanceof CachableInvocationHandler cachableInvocationHandler
                ? proxyClassCache.computeIfAbsent(cachableInvocationHandler.key(), __->
                    createProxyClass(handler, base, interfaces, additionalFields))
                : createProxyClass(handler, base, interfaces, additionalFields);
        return (Class<? extends T>) proxyClass;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <T> ProxyFactory<T> factory(
            Class<T> proxyClass,
            @Nullable Class<?>[] constructorArgTypes) {

        var factory = proxyFactoryCache.computeIfAbsent(proxyClass, __->
            createFactory(proxyClass, constructorArgTypes));

        return (ProxyFactory<T>) factory;
    }

    // -- ABSTRACT METHODS

    protected abstract <T> Class<? extends T> createProxyClass(
            InvocationHandler handler,
            Class<T> base,
            Class<?>[] interfaces,
            @Nullable List<AdditionalField> additionalFields);

    protected abstract <T> ProxyFactory<T> createFactory(
            Class<T> proxyClass,
            @Nullable Class<?>[] constructorArgTypes);

}
