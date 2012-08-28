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

package org.apache.isis.progmodel.wrapper.metamodel.internal;

import java.lang.reflect.InvocationHandler;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;

import org.apache.isis.progmodel.wrapper.applib.WrapperObject;

/**
 * Factory generating a mock for a class.
 * <p>
 * Note that this class is stateful
 */
public class CgLibClassProxyFactory<T> implements IProxyFactory<T> {

    @Override
    @SuppressWarnings("unchecked")
    public T createProxy(final T toProxy, final InvocationHandler handler) {
        final Class<T> proxyClass = (Class<T>) toProxy.getClass();
        return createProxy(proxyClass, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createProxy(final Class<T> toProxyClass, final InvocationHandler handler) {

        final MethodInterceptor interceptor = new InvocationHandlerMethodInterceptor(handler);

        // Create the proxy
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(toProxyClass);
        enhancer.setInterfaces(new Class[] { WrapperObject.class });
        enhancer.setCallbackType(interceptor.getClass());

        final Class<?> enhancedClass = enhancer.createClass();

        Enhancer.registerCallbacks(enhancedClass, new Callback[] { interceptor });

        Factory factory;
        try {
            factory = (Factory) ClassInstantiatorFactoryCE.getInstantiator().newInstance(enhancedClass);
        } catch (final InstantiationException e) {
            throw new RuntimeException("Fail to instantiate mock for " + toProxyClass + " on " + ClassInstantiatorFactoryCE.getJVM() + " JVM");
        }

        return (T) factory;
    }

}
