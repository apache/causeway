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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

import org.apache.isis.core.metamodel.specloader.classsubstitutor.CglibEnhanced;
import org.apache.isis.progmodel.wrapper.applib.WrapperObject;

public class CgLibProxy<T> {

    private final DelegatingInvocationHandler<T> handler;

    public CgLibProxy(final DelegatingInvocationHandler<T> handler) {
        this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    public T proxy() {

        final T toProxy = handler.getDelegate();

        // handle if already proxied using cglib.
        if (CglibEnhanced.class.isAssignableFrom(toProxy.getClass())) {

            handler.setResolveObjectChangedEnabled(true);

            final Class<? extends Object> enhancedClass = toProxy.getClass();
            final Class<? extends Object> origSuperclass = toProxy.getClass().getSuperclass();

            final List<Class> interfaces = new ArrayList<Class>();
            interfaces.addAll(Arrays.asList(enhancedClass.getInterfaces()));
            interfaces.remove(Factory.class); // if there.
            interfaces.add(WrapperObject.class);

            return (T) Enhancer.create(origSuperclass, interfaces.toArray(new Class[] {}), new InvocationHandlerMethodInterceptor(handler));
        }

        final Class<T> clazz = (Class<T>) toProxy.getClass();

        T proxy = null;
        try {
            final IProxyFactory<T> proxyFactory = clazz.isInterface() ? new JavaProxyFactory<T>() : new CgLibClassProxyFactory<T>();
            proxy = proxyFactory.createProxy(clazz, handler);
        } catch (final RuntimeExceptionWrapper e) {
            throw (RuntimeException) e.getRuntimeException().fillInStackTrace();
        }
        return proxy;
    }

}
