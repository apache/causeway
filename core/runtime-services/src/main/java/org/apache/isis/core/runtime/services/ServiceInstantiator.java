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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.factory.InstanceCreationClassException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.metamodel.specloader.classsubstitutor.ProxyEnhanced;
import org.apache.isis.core.plugins.codegen.ProxyFactory;

//import javassist.util.proxy.MethodFilter;
//import javassist.util.proxy.MethodHandler;
//import javassist.util.proxy.ProxyFactory;
//import javassist.util.proxy.ProxyObject;

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

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInstantiator.class);

    public ServiceInstantiator() {
    }

    // //////////////////////////////////////

    public Object createInstance(String type) {
        final Class<?> cls = loadClass(type);
        if(cls == null || cls.isAnonymousClass()) {
            // eg a test class
            return null;
        }

        return createInstance(cls);
    }

    private Class<?> loadClass(final String className) {
        try {
            LOG.debug("loading class for service: {}", className);
            return _Context.loadClassAndInitialize(className);
        } catch (final ClassNotFoundException ex) {
            throw new InitialisationException(String.format("Cannot find class '%s' for service", className));
        }
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

        final Class<?>[] interfaces = ArrayExtensions.combine(
                cls.getInterfaces(),
                new Class<?>[] { RequestScopedService.class, ProxyEnhanced.class });

        final ProxyFactory<T> proxyFactory = ProxyFactory.builder(cls)
                .interfaces(interfaces)
                .build();

        final InvocationHandler handler = new InvocationHandler() {

            // Allow serviceByThread to be propagated from the thread that starts the request
            // to any child-threads, hence InheritableThreadLocal.
            private InheritableThreadLocal<T> serviceByThread = new InheritableThreadLocal<>();

            @Override
            public Object invoke(final Object proxied, final Method proxyMethod, final Object[] args)
                    throws Throwable {

                cacheMethodsIfNecessary(cls);

                if(proxyMethod.getName().equals("__isis_startRequest")) {

                    T service = instantiate(cls);
                    serviceByThread.set(service);

                    ServiceInjector serviceInjector = (ServiceInjector) args[0];
                    serviceInjector.injectServicesInto(service);

                    return null;

                } else if(proxyMethod.getName().equals("__isis_postConstruct")) {

                    final T service = serviceByThread.get();

                    callPostConstructIfPresent(service);

                    return null;

                } else if(proxyMethod.getName().equals("__isis_preDestroy")) {

                    final T service = serviceByThread.get();

                    callPreDestroyIfPresent(service);

                    return null;

                } else if(proxyMethod.getName().equals("__isis_endRequest")) {

                    serviceByThread.set(null);
                    return null;

                } else if(proxyMethod.getName().equals("hashCode") && proxyMethod.getParameterTypes().length == 0) {

                    final T service = serviceByThread.get();
                    return service != null? service.hashCode(): this.hashCode();

                } else if(proxyMethod.getName().equals("equals") && proxyMethod.getParameterTypes().length == 1 && proxyMethod.getParameterTypes()[0] == Object.class) {

                    final T service = serviceByThread.get();
                    return service != null? service.equals(args[0]): this.equals(args[0]);

                } else if(proxyMethod.getName().equals("toString") && proxyMethod.getParameterTypes().length == 0) {

                    final T service = serviceByThread.get();
                    return service != null? service.toString(): this.toString();

                } else {
                    T service = serviceByThread.get();
                    if(service == null) {
                        // shouldn't happen...
                        throw new IllegalStateException("No service of type " + cls + " is available on this ");
                    }
                    final Object proxiedReturn = proxyMethod.invoke(service, args);
                    return proxiedReturn;
                }
            }

        };

        return proxyFactory.createInstance(handler, false);
    }

    private Set<Class<?>> cached = _Sets.newHashSet();
    private Map<Class<?>, Method> postConstructMethodsByServiceClass = _Maps.newConcurrentHashMap();
    private Map<Class<?>, Method> preDestroyMethodsByServiceClass = _Maps.newConcurrentHashMap();

    <T> void callPostConstructIfPresent(T service) {

        final Class<?> serviceClass = service.getClass();
        final Method postConstructMethod = postConstructMethodsByServiceClass.get(serviceClass);
        if(postConstructMethod == null) {
            return;
        }
        final int numParams = postConstructMethod.getParameterTypes().length;

        if(LOG.isDebugEnabled()) {
            LOG.debug("... calling @PostConstruct method: {}: {}", serviceClass.getName(), postConstructMethod.getName());
        }
        // unlike shutdown, we don't swallow exceptions; would rather fail early
        if(numParams == 0) {
            MethodExtensions.invoke(postConstructMethod, service);
        } else {
            //TODO[2039] MethodExtensions.invoke(postConstructMethod, service, new Object[]{props});
            throw new UnsupportedOperationException("post-construct methods must not take any arguments");
        }
    }

    <T> void callPreDestroyIfPresent(T service) throws InvocationTargetException, IllegalAccessException {

        final Class<?> serviceClass = service.getClass();
        final Method preDestroyMethod = preDestroyMethodsByServiceClass.get(serviceClass);
        if(preDestroyMethod == null) {
            return;
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("... calling @PreDestroy method: {}: {}", serviceClass.getName(), preDestroyMethod.getName());
        }
        try {
            MethodExtensions.invoke(preDestroyMethod, service);
        } catch(Exception ex) {
            // do nothing
            LOG.warn("... @PreDestroy method threw exception - continuing anyway", ex);
        }
    }


    private void cacheMethodsIfNecessary(Class<?> serviceClass) {
        if(cached.contains(serviceClass)) {
            return;
        }
        cacheMethods(serviceClass);
        cached.add(serviceClass);
    }

    private void cacheMethods(Class<?> serviceClass) {
        final Method[] methods = serviceClass.getMethods();

        // @PostConstruct
        Method postConstructMethod = null;
        for (final Method method : methods) {

            final PostConstruct postConstructAnnotation = method.getAnnotation(PostConstruct.class);
            if (postConstructAnnotation == null) {
                continue;
            }
            if (postConstructMethod != null) {
                throw new RuntimeException("Found more than one @PostConstruct method; service is: " + serviceClass.getName() + ", found " + postConstructMethod.getName() + " and " + method.getName());
            }

            final Class<?>[] parameterTypes = method.getParameterTypes();
            switch (parameterTypes.length) {
            case 0:
                break;
            case 1:
                if (Map.class != parameterTypes[0]) {
                    throw new RuntimeException("@PostConstruct method must be no-arg or 1-arg accepting java.util.Map; method is: " + serviceClass.getName() + "#" + method.getName());
                }
                break;
            default:
                throw new RuntimeException("@PostConstruct method must be no-arg or 1-arg accepting java.util.Map; method is: " + serviceClass.getName() + "#" + method.getName());
            }
            postConstructMethod = method;
        }

        // @PreDestroy
        Method preDestroyMethod = null;
        for (final Method method : methods) {

            final PreDestroy preDestroyAnnotation = method.getAnnotation(PreDestroy.class);
            if(preDestroyAnnotation == null) {
                continue;
            }
            if(preDestroyMethod != null) {
                throw new RuntimeException("Found more than one @PreDestroy method; service is: " + serviceClass.getName() + ", found " + preDestroyMethod.getName() + " and " + method.getName());
            }

            final Class<?>[] parameterTypes = method.getParameterTypes();
            switch(parameterTypes.length) {
            case 0:
                break;
            default:
                throw new RuntimeException("@PreDestroy method must be no-arg; method is: " + serviceClass.getName() + "#" + method.getName());
            }
            preDestroyMethod = method;
        }

        if(postConstructMethod != null) {
            postConstructMethodsByServiceClass.put(serviceClass, postConstructMethod);
        }
        if(preDestroyMethod != null) {
            preDestroyMethodsByServiceClass.put(serviceClass, preDestroyMethod);
        }
    }

    // //////////////////////////////////////

    private static <T> T instantiate(final Class<T> cls) {
        try {
            return cls.newInstance();
        } catch (final NoClassDefFoundError e) {
            throw new InstanceCreationClassException("Class found '" + cls + "', but is missing a dependent class", e);
        } catch (final InstantiationException e) {
            throw new InstanceCreationException("Could not instantiate an object of class '" + cls.getName() + "'; " + e.getMessage(), e);
        } catch (final IllegalAccessException e) {
            throw new InstanceCreationException("Could not access the class '" + cls.getName() + "'; " + e.getMessage(), e);
        }
    }


}
