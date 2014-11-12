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

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceCreationClassException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.lang.ArrayExtensions;
import org.apache.isis.core.commons.lang.MethodExtensions;
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

    private final static Logger LOG = LoggerFactory.getLogger(ServiceInstantiator.class);


    public ServiceInstantiator() {
    }

    // //////////////////////////////////////

    /**
     * initially null, but checked before first use that has been set (through {@link #setConfiguration(org.apache.isis.core.commons.config.IsisConfiguration)}).
     */
    private Map<String, String> props;

    public void setConfiguration(IsisConfiguration configuration) {
        this.props = configuration.asMap();
    }

    private void ensureInitialized() {
        if(props == null) {
            throw new IllegalStateException("IsisConfiguration properties not set on ServiceInstantiator prior to first-use");
        }
    }


    // //////////////////////////////////////
    
    public <T> T createInstance(final Class<T> cls) {
        ensureInitialized();
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
                private ThreadLocal<T> serviceByThread = new ThreadLocal<>();
                
                @Override
                public Object invoke(final Object proxied, final Method proxyMethod, final Method proxiedMethod, final Object[] args) throws Throwable {

                    cacheMethodsIfNecessary(cls);

                    String proxyMethodName = proxyMethod.getName();

                    if(proxyMethodName.equals("__isis_startRequest")) {
                        T service = instantiate(cls);

                        callPostConstructIfPresent(service);

                        serviceByThread.set(service);
                        return null;
                    } else if(proxyMethodName.equals("__isis_endRequest")) {

                        final T service = serviceByThread.get();

                        callPreDestroyIfPresent(service);

                        serviceByThread.set(null);
                        return null;
                    } else {
                        T service = serviceByThread.get();
                        if(service == null) {
                            if (proxyMethodName.endsWith("toString")) {
                                return proxied.getClass().getName();
                            } else if (proxyMethodName.endsWith("hashCode")) {
                                return 0;
                            } else {
                                throw new IllegalStateException("No service found for thread; make sure ((RequestScopedService)service).__isis_startRequest() is called first");
                            }
                        }
                        final Object proxiedReturn = proxyMethod.invoke(service, args); 
                        return proxiedReturn;
                    }
                }
            });

            return newInstance;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IsisException(e);
        }
    }

    private Set<Class<?>> cached = Sets.newHashSet();
    private Map<Class<?>, Method> postConstructMethodsByServiceClass = Maps.newConcurrentMap();
    private Map<Class<?>, Method> preDestroyMethodsByServiceClass = Maps.newConcurrentMap();

    <T> void callPostConstructIfPresent(T service) {

        final Class<?> serviceClass = service.getClass();
        final Method postConstructMethod = postConstructMethodsByServiceClass.get(serviceClass);
        if(postConstructMethod == null) {
            return;
        }
        final int numParams = postConstructMethod.getParameterTypes().length;

        if(LOG.isDebugEnabled()) {
            LOG.debug("... calling @PostConstruct method: " + serviceClass.getName() + ": " + postConstructMethod.getName());
        }
        // unlike shutdown, we don't swallow exceptions; would rather fail early
        if(numParams == 0) {
            MethodExtensions.invoke(postConstructMethod, service);
        } else {
            MethodExtensions.invoke(postConstructMethod, service, new Object[]{props});
        }
    }

    <T> void callPreDestroyIfPresent(T service) throws InvocationTargetException, IllegalAccessException {

        final Class<?> serviceClass = service.getClass();
        final Method preDestroyMethod = preDestroyMethodsByServiceClass.get(serviceClass);
        if(preDestroyMethod == null) {
            return;
        }

        if(LOG.isDebugEnabled()) {
            LOG.debug("... calling @PreDestroy method: " + serviceClass.getName() + ": " + preDestroyMethod.getName());
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
            throw new InstanceCreationException("Could not instantiate an object of class '" + cls.getName() + "'; " + e.getMessage());
        } catch (final IllegalAccessException e) {
            throw new InstanceCreationException("Could not access the class '" + cls.getName() + "'; " + e.getMessage());
        }
    }

}
