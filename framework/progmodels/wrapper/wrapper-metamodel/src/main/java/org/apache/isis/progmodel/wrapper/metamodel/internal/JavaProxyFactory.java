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
import java.lang.reflect.Proxy;

import org.apache.isis.progmodel.wrapper.applib.WrapperObject;

public class JavaProxyFactory<T> implements IProxyFactory<T> {
    @Override
    @SuppressWarnings("unchecked")
    public T createProxy(final T toProxy, final InvocationHandler handler) {
        final Class<T> proxyClass = (Class<T>) toProxy.getClass();
        return (T) Proxy.newProxyInstance(proxyClass.getClassLoader(), new Class[] { proxyClass }, handler);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T createProxy(final Class<T> toProxy, final InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(toProxy.getClassLoader(), new Class[] { toProxy, WrapperObject.class }, handler);
    }
}
