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

package org.apache.isis.core.runtime.services;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import javax.enterprise.context.RequestScoped;

import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceCreationClassException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.JavassistEnhanced;

/**
 * Instantiates the service, taking into account whether the service is annotated with
 * {@link RequestScoped} or not.
 * 
 * <p>
 * For regular (non-annotated, global, singleton) services, this class simply instantiates 
 * the object.  This is then held as a singleton by Isis.
 * 
 * <p>
 * For annotated, request-scoped services, this class instantiates a (singleton) proxy object 
 * that internally uses a threadlocal to dispatch to the actual service object, one per thread.
 * The proxy additionally implements {@link RequestScopedService} interface, allowing the
 * system to initialize the proxy for the thread with every request, and tear down afterwards.
 * 
 * <p>
 * Doing the thread-local stuff within the service proxy means that, for the rest of Isis,
 * services can continue to be considered to be singletons.
 * 
 * <p>
 * <b>Note</b>: there is one limitation to using proxies, namely that field-level injection into
 * request-scoped services is not (yet) supported.
 */
public final class ServiceInstantiator {

    public ServiceInstantiator() {
    }

    // //////////////////////////////////////
    
    public <T> T createInstance(final Class<T> cls) {
        if(cls.isAnnotationPresent(RequestScoped.class)) {
            return instantiateRequestScopedProxy(cls);
        } else {
            return instantiateSingleton(cls);
        }
    }

    // //////////////////////////////////////

    
    private static <T> T instantiateSingleton(final Class<T> cls) {
        return instantiate(cls);
    }
    
    // //////////////////////////////////////
    
     
    private <T> T instantiateRequestScopedProxy(final Class<T> cls) {
        final ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setSuperclass(cls);
        proxyFactory.setInterfaces(ArrayExtensions.combine(cls.getInterfaces(), new Class<?>[] { RequestScopedService.class, JavassistEnhanced.class }));

        // ignore finalize()
        proxyFactory.setFilter(new MethodFilter() {
            @Override
            public boolean isHandled(final Method m) {
                return !m.getName().equals("finalize");
            }
        });

        @SuppressWarnings("unchecked")
        final Class<T> proxySubclass = proxyFactory.createClass();
        try {
            final T newInstance = proxySubclass.newInstance();
            final ProxyObject proxyObject = (ProxyObject) newInstance;
            proxyObject.setHandler(new MethodHandler() {
                private ThreadLocal<T> serviceByThread = new ThreadLocal<T>();
                
                @Override
                public Object invoke(final Object proxied, final Method proxyMethod, final Method proxiedMethod, final Object[] args) throws Throwable {
                    if(proxyMethod.getName().equals("__isis_startRequest")) {
                        T service = instantiate(cls);
                        serviceByThread.set(service);
                        return null;
                    } else if(proxyMethod.getName().equals("__isis_endRequest")) {
                        serviceByThread.set(null);
                        return null;
                    } else {
                        T service = serviceByThread.get();
                        if(service == null) {
                            throw new IllegalStateException("No service found for thread; make sure ((RequestScopedService)service).__isis_startRequest() is called first");
                        }
                        final Object proxiedReturn = proxyMethod.invoke(service, args); 
                        return proxiedReturn;
                    }
                }
            });

            return newInstance;
        } catch (final InstantiationException e) {
            throw new IsisException(e);
        } catch (final IllegalAccessException e) {
            throw new IsisException(e);
        }
    }

    // //////////////////////////////////////
    
    private static <T> T instantiate(final Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Class found '" + cls + "', but is missing a dependent class", e);
        } catch (final InstantiationException e) {
            throw new InstanceCreationException("Could not instantiate an object of class '" + cls.getName() + "'; " + e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new InstanceCreationException("Could not access the class '" + cls.getName() + "'; " + e.getMessage());
        }
    }

}
