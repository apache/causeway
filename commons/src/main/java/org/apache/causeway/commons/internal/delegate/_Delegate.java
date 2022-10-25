/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.commons.internal.delegate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.internal.reflection._ClassCache;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Creates a proxy that delegates method calls to a delegate instance.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0
 */
@UtilityClass
public class _Delegate {

    /**
     * For given {@code bluePrint} creates an instance, that implements all interfaces of
     * {@code bluePrint}, while delegating method calls to the {@code delegate} object.
     * The {@code delegate} object, does not necessarily need to implement any of the
     * {@code bluePrint}'s interfaces.
     * <p>
     * Particularly useful in connection with {@link lombok.experimental.Delegate}.
     * @param <T>
     * @param bluePrint
     * @param delegate
     * @see lombok.experimental.Delegate
     */
    @SuppressWarnings("unchecked")
    public <T> T createProxy(final Class<T> bluePrint, final Object delegate) {
        Class<?>[] ifcs = ClassUtils.getAllInterfacesForClass(
                bluePrint, bluePrint.getClassLoader());
        return (T) Proxy.newProxyInstance(
                delegate.getClass().getClassLoader(), ifcs,
                new DelegatingInvocationHandler(delegate));
    }

    @RequiredArgsConstructor
    static class DelegatingInvocationHandler implements InvocationHandler {

        final Object delegate;
        final _ClassCache classCache = _ClassCache.getInstance();

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args)
                throws Throwable {

            if (method.getName().equals("equals")) {
                // Only consider equal when proxies are identical.
                return (proxy == args[0]);
            } else if (method.getName().equals("hashCode")) {
                // Use hashCode of proxy rather than the delegate.
                return System.identityHashCode(proxy);
            } else if (method.getName().equals("toString")) {
                // delegate Object.toString() method
                return "Proxy(" + delegate.toString() + ")";
            }

            // Invoke method with same signature on delegate ()
            try {
                val delegateMethod =
                        classCache.lookupPublicOrDeclaredMethod(delegate.getClass(),
                                method.getName(), method.getParameterTypes());
                return delegateMethod.invoke(delegate, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }


}
