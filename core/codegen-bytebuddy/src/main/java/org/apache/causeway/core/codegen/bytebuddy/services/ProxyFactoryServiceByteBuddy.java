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
import java.util.function.Function;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.proxy._ProxyFactory;
import org.apache.causeway.commons.internal.proxy._ProxyFactoryServiceAbstract;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.NamingStrategy;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import net.bytebuddy.matcher.ElementMatchers;

@Service
public class ProxyFactoryServiceByteBuddy extends _ProxyFactoryServiceAbstract {

    private final ClassLoadingStrategyAdvisor strategyAdvisor = new ClassLoadingStrategyAdvisor();

    @Override
    public <T> _ProxyFactory<T> factory(
            final Class<T> base,
            final Class<?>[] interfaces,
            final Class<?>[] constructorArgTypes) {

        final Objenesis objenesis = new ObjenesisStd();

        final Function<InvocationHandler, Class<? extends T>> proxyClassFactory = handler->
        nextProxyDef(base, interfaces)
        .intercept(InvocationHandlerAdapter.of(handler))
        .make()
        .load(_Context.getDefaultClassLoader(),
                strategyAdvisor.getSuitableStrategy(base))
        .getLoaded();

        return new _ProxyFactory<T>() {

            @Override
            public T createInstance(final InvocationHandler handler, final boolean initialize) {

                try {

                    if(initialize) {
                        ensureSameSize(constructorArgTypes, null);
                        return _Casts.uncheckedCast( createUsingConstructor(handler, null) );
                    } else {
                        return _Casts.uncheckedCast( createNotUsingConstructor(handler) );
                    }

                } catch (NoSuchMethodException | IllegalArgumentException | InstantiationException |
                        IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }

            }

            @Override
            public T createInstance(final InvocationHandler handler, final Object[] constructorArgs) {

                ensureNonEmtpy(constructorArgs);
                ensureSameSize(constructorArgTypes, constructorArgs);

                try {
                    return _Casts.uncheckedCast( createUsingConstructor(handler, constructorArgs) );
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                        IllegalArgumentException | InvocationTargetException | SecurityException  e) {
                    throw new RuntimeException(e);
                }
            }

            // -- HELPER (create w/o initialize)

            private Object createNotUsingConstructor(final InvocationHandler invocationHandler) {
                final Class<? extends T> proxyClass = proxyClassFactory.apply(invocationHandler);
                final Object object = objenesis.newInstance(proxyClass);
                return object;
            }

            // -- HELPER (create with initialize)

            private Object createUsingConstructor(final InvocationHandler invocationHandler, @Nullable final Object[] constructorArgs)
                    throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
                final Class<? extends T> proxyClass = proxyClassFactory.apply(invocationHandler);
                return proxyClass
                        .getConstructor(constructorArgTypes==null ? _Constants.emptyClasses : constructorArgTypes)
                        .newInstance(constructorArgs==null ? _Constants.emptyObjects : constructorArgs);
            }

        };

    }

    // -- HELPER

    private static <T> ImplementationDefinition<T> nextProxyDef(
            final Class<T> base,
            final Class<?>[] interfaces) {
        return new ByteBuddy()
                .with(new NamingStrategy.SuffixingRandom("bb"))
                .subclass(base)
                .implement(interfaces)
                .method(ElementMatchers.any());
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
