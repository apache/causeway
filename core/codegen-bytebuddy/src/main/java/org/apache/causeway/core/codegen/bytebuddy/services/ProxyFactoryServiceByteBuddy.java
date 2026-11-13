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
package org.apache.causeway.core.codegen.bytebuddy.services;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.objenesis.ObjenesisStd;
import org.springframework.stereotype.Service;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.proxy.CachingProxyFactoryService;
import org.apache.causeway.commons.internal.proxy.ProxyFactory;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

@Service
public class ProxyFactoryServiceByteBuddy extends CachingProxyFactoryService {

    private final ClassLoadingStrategyAdvisor strategyAdvisor = new ClassLoadingStrategyAdvisor();

    private record ProxyFactoryByteBuddy<T>(
            Class<T> proxyClass,
            Class<?>[] constructorArgTypes,
            ObjenesisStd objenesis) implements ProxyFactory<T> {

        @Override public T createInstance(final boolean initialize) {

            try {

                if(initialize) {
                    ensureSameSize(constructorArgTypes, null);
                    return _Casts.uncheckedCast( createUsingConstructor(null) );
                } else {
                    return _Casts.uncheckedCast( createNotUsingConstructor() );
                }

            } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException |
                    IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

        }

        @Override public T createInstance(final Object[] constructorArgs) {

            ensureNonEmtpy(constructorArgs);
            ensureSameSize(constructorArgTypes, constructorArgs);

            try {
                return _Casts.uncheckedCast( createUsingConstructor(constructorArgs) );
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                    IllegalArgumentException | InvocationTargetException | SecurityException  e) {
                throw new RuntimeException(e);
            }
        }

        // -- HELPER (create w/o initialize)

        private Object createNotUsingConstructor() {
            final Object object = objenesis.newInstance(proxyClass);
            return object;
        }

        // -- HELPER (create with initialize)

        private Object createUsingConstructor(final @Nullable Object[] constructorArgs)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
            return proxyClass
                    .getConstructor(constructorArgTypes==null ? _Constants.emptyClasses : constructorArgTypes)
                    .newInstance(constructorArgs==null ? _Constants.emptyObjects : constructorArgs);
        }

    };

    @Override
    public <T> Class<? extends T> createProxyClass(
            final InvocationHandler handler,
            final Class<T> base,
            final Class<?>[] interfaces,
            @Nullable List<AdditionalField> additionalFields) {

        return proxyDef(base, interfaces, additionalFields)
                .intercept(InvocationHandlerAdapter.of(handler))
                .make()
                .load(_Context.getDefaultClassLoader(),
                        strategyAdvisor.getSuitableStrategy(base))
                .getLoaded();
    }

    @Override
    public <T> ProxyFactory<T> createFactory(
            final Class<T> proxyClass,
            final Class<?>[] constructorArgTypes) {
        return new ProxyFactoryByteBuddy<T>(proxyClass, constructorArgTypes, new ObjenesisStd());
    }

    // -- HELPER

    /**
     * @implNote could not find a simple way to use the ByteBuddy builder
     *      to add zero, one or multiple additional fields via {@code defineField},
     *      so we do those 3 cases conditionally all picking up on a shared
     *      {@code prolog}
     */
    private static <T> ImplementationDefinition<T> proxyDef(
            final Class<T> base,
            final Class<?>[] interfaces,
            @Nullable List<AdditionalField> additionalFields) {

        var prolog = new ByteBuddy()
                .with(new NamingStrategy.SuffixingRandom("bb"))
                .subclass(base)
                .implement(interfaces);

        int additionalFieldCount = _NullSafe.size(additionalFields);
        if(additionalFieldCount==0) {
            return prolog
                .method(ElementMatchers.any());
        }
        if(additionalFieldCount==1) {
            var additionalField = additionalFields.get(0);
            return prolog
                .defineField(additionalField.name(), additionalField.type(), additionalField.modifiers())
                .method(ElementMatchers.any());
        }

        // when more than one additional field ...
        var fieldIterator = additionalFields.iterator();
        var firstAdditionalField = fieldIterator.next();
        var def = prolog
            .defineField(firstAdditionalField.name(), firstAdditionalField.type(), firstAdditionalField.modifiers());
        while(fieldIterator.hasNext()) {
            var additionalField = fieldIterator.next();
            def = def.defineField(additionalField.name(), additionalField.type(), additionalField.modifiers());
        }
        return def.method(ElementMatchers.any());
    }

    private static void ensureSameSize(final Class<?>[] a, final Object[] b) {
        if(_NullSafe.size(a) != _NullSafe.size(b)) {
            throw new IllegalArgumentException(String.format("Constructor arg count expected %d, got %d.",
                    _NullSafe.size(a), _NullSafe.size(b) ));
        }
    }

    private static void ensureNonEmtpy(final Object[] a) {
        if(_NullSafe.isEmpty(a)) {
            throw new IllegalArgumentException(String.format("Contructor args count expected > 0, got %d.",
                    _NullSafe.size(a) ));
        }
    }

}
