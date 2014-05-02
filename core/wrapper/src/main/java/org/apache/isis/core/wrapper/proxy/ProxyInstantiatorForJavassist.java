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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.apache.isis.applib.services.wrapper.WrapperObject;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.wrapper.handlers.DelegatingInvocationHandler;
import org.apache.isis.core.wrapper.internal.util.Util;

public class ProxyInstantiatorForJavassist implements ProxyInstantiator {

    @SuppressWarnings("unchecked")
    public <T> T instantiateProxy(final DelegatingInvocationHandler<T> handler) {

        final T toProxy = handler.getDelegate();

        final Class<T> clazz = (Class<T>) toProxy.getClass();

        if (clazz.isInterface()) {
            return Util.createInstance(clazz, handler, WrapperObject.class);
        } else {
            final Class<T> enhancedClass = createEnhancedClass(clazz, handler, WrapperObject.class);
            ProxyObject proxyObject = (ProxyObject) Util.createInstance(enhancedClass);
            proxyObject.setHandler(new MethodHandler() {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
                    return handler.invoke(self, thisMethod, args);
                }
            });
            return (T) proxyObject;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> Class<T> createEnhancedClass(final Class<T> toProxyClass, final InvocationHandler handler, final Class<?>... auxiliaryTypes) {
        
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(toProxyClass);
        proxyFactory.setInterfaces(ArrayExtensions.combine(toProxyClass.getInterfaces(), new Class<?>[]{org.apache.isis.core.metamodel.specloader.classsubstitutor.JavassistEnhanced.class}, auxiliaryTypes));

        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method m) {
                // ignore finalize()
                return !m.getName().equals("finalize") || m.isBridge();
            }
        });
        
        return proxyFactory.createClass();
    }

}
