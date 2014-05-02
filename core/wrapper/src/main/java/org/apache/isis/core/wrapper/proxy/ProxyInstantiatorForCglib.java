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
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.isis.applib.services.wrapper.WrapperObject;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.CglibEnhanced;
import org.apache.isis.core.wrapper.handlers.DelegatingInvocationHandler;
import org.apache.isis.core.wrapper.internal.util.Util;

public class ProxyInstantiatorForCglib implements ProxyInstantiator {

    @SuppressWarnings("unchecked")
    public <T> T instantiateProxy(final DelegatingInvocationHandler<T> handler) {

        final T toProxy = handler.getDelegate();

        // handle if already proxied using cglib
        // (this is in case the CglibObjectFactory is configured, to handle lazy loading/object dirtying.  
        // Have decided not to port this over to the Javassist version, in anticipation of the CglibObjectFactory being dropped from Isis in v2.0.0.
        // (If that isn't the case, then this class is still available).
        if (CglibEnhanced.class.isAssignableFrom(toProxy.getClass())) {

            handler.setResolveObjectChangedEnabled(true);

            final Class<? extends Object> enhancedClass = toProxy.getClass();
            final Class<? extends Object> origSuperclass = toProxy.getClass().getSuperclass();

            final List<Class<?>> interfaces = Lists.newArrayList();
            interfaces.addAll(Arrays.asList(enhancedClass.getInterfaces()));
            interfaces.remove(Factory.class); // if there.
            interfaces.add(WrapperObject.class);

            return (T) Enhancer.create(origSuperclass, interfaces.toArray(new Class[] {}), newMethodInterceptor(handler));
        }

        final Class<T> clazz = (Class<T>) toProxy.getClass();

        if (clazz.isInterface()) {
            return Util.createInstance(clazz, handler, WrapperObject.class);
        } else {
            final Class<T> enhancedClass = createEnhancedClass(clazz, handler, WrapperObject.class);
            return Util.createInstance(enhancedClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> createEnhancedClass(final Class<T> toProxyClass, final InvocationHandler handler, final Class<?>... auxiliaryTypes) {

        // Create the proxy
        final Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(toProxyClass);
        enhancer.setInterfaces(auxiliaryTypes);
        enhancer.setCallbackType(newMethodInterceptor(handler).getClass());

        final Class<?> enhancedClass = enhancer.createClass();

        Enhancer.registerCallbacks(enhancedClass, new Callback[] { newMethodInterceptor(handler) });
        return (Class<T>) enhancedClass;
    }

    private static <T> MethodInterceptor newMethodInterceptor(final InvocationHandler handler) {
        return new MethodInterceptor() {
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                return handler.invoke(obj, method, args);
            }
        };
    }
    

}
