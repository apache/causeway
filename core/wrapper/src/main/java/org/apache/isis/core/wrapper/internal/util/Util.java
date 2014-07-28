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

package org.apache.isis.core.wrapper.internal.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.objenesis.ObjenesisHelper;

public final class Util {

    private Util(){}
    
    @SuppressWarnings("unchecked")
    public static <T> T createInstance(final Class<T> toProxy, final InvocationHandler handler, final Class<?>... auxiliaryTypes) {
        return (T) Proxy.newProxyInstance(toProxy.getClassLoader(), combine(toProxy, auxiliaryTypes) , handler);
    }
 
    /**
     * Return a new instance of the specified class. The recommended way is
     * without calling any constructor. This is usually done by doing like
     * <code>ObjectInputStream.readObject()</code> which is JVM specific.
     */
    @SuppressWarnings("unchecked")
    public static <T> T createInstance(final Class<T> enhancedClass) {
        return (T) ObjenesisHelper.newInstance(enhancedClass);
    }
    
    private static Class<?>[] combine(Class<?> first, Class<?>... rest) {
        Class<?>[] all = new Class<?>[rest.length+1];
        all[0] = first;
        System.arraycopy(rest, 0, all, 1, rest.length);
        return all;
    }

}
