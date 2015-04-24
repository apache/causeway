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

package org.apache.isis.core.wrapper.proxy;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.MapMaker;

import org.apache.isis.applib.services.wrapper.WrapperObject;
import org.apache.isis.applib.services.wrapper.WrappingObject;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.JavassistEnhanced;
import org.apache.isis.core.wrapper.handlers.DelegatingInvocationHandler;
import org.apache.isis.core.wrapper.internal.util.Util;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

public class ProxyInstantiatorForJavassist implements ProxyInstantiator {

    /**
     * Lazily constructed cache.
     */
    private final Map<Class, ProxyFactory> proxyFactoryByClass;

    public ProxyInstantiatorForJavassist() {
        this(new MapMaker().concurrencyLevel(10).<Class, ProxyFactory>makeMap());
    }

    public ProxyInstantiatorForJavassist(Map<Class, ProxyFactory> proxyFactoryByClass) {
        this.proxyFactoryByClass = proxyFactoryByClass;
    }

    @SuppressWarnings("unchecked")
    public <T> T instantiateProxy(final DelegatingInvocationHandler<T> handler) {

        final T toProxy = handler.getDelegate();

        final Class<T> clazz = (Class<T>) toProxy.getClass();

        if (clazz.isInterface()) {
            return Util.createInstance(clazz, handler, WrapperObject.class);
        } else {
            final ProxyFactory proxyFactory = proxyFactoryFor(clazz);

            final Class<T> enhancedClass = proxyFactory.createClass();
            final Proxy proxy = (Proxy) Util.createInstance(enhancedClass);

            proxy.setHandler(new MethodHandler() {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    return handler.invoke(self, thisMethod, args);
                }
            });

            return (T) proxy;
        }
    }

    private <T> ProxyFactory proxyFactoryFor(final Class<T> toProxyClass) {
        ProxyFactory proxyFactory = proxyFactoryByClass.get(toProxyClass);
        if(proxyFactory == null) {
            proxyFactory = createProxyFactoryFor(toProxyClass);
            proxyFactoryByClass.put(toProxyClass, proxyFactory);
        }
        return proxyFactory;
    }

    private <T> ProxyFactory createProxyFactoryFor(final Class<T> toProxyClass) {

        final ProxyFactory proxyFactory = new ProxyFactory();

        proxyFactory.setSuperclass(toProxyClass);
        final Class[] types = ArrayExtensions.combine(
                toProxyClass.getInterfaces(),
                new Class<?>[] { JavassistEnhanced.class, WrappingObject.class });
        proxyFactory.setInterfaces(types);

        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method m) {
                // ignore finalize()
                return !m.getName().equals("finalize") || m.isBridge();
            }
        });

        // this is the default, I believe
        // calling it here only because I know that it will throw an exception if the code were
        // in the future changed such that caching is invalid
        // (ie fail fast if future change could introduce a bug)
        proxyFactory.setUseCache(true);

        return proxyFactory;
    }

}
