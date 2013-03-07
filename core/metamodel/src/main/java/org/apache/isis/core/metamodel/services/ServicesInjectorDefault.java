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

package org.apache.isis.core.metamodel.services;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.lang.CastUtils;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.log4j.Logger;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Must be a thread-safe.
 */
public class ServicesInjectorDefault implements ServicesInjectorSpi {

    private static final Logger LOG = Logger.getLogger(ServicesInjectorDefault.class);

    /**
     * If no key, not yet searched for type; otherwise the {@link List} indicates
     * whether a service was found.
     */
    private final Map<Class<?>, List<Object>> servicesByType = Maps.newHashMap();

    private final List<Object> services = Lists.newArrayList();
    
    private DomainObjectContainer container;


    
    // /////////////////////////////////////////////////////////
    // Constructor, init, shutdown
    // /////////////////////////////////////////////////////////


    @Override
    public void init() {
        autowireServicesAndContainer();
    }

    @Override
    public void shutdown() {
    }

    // /////////////////////////////////////////////////////////
    // Container
    // /////////////////////////////////////////////////////////

    @Override
    public DomainObjectContainer getContainer() {
        return container;
    }

    @Override
    public void setContainer(final DomainObjectContainer container) {
        ensureThatArg(container, is(not(nullValue())));
        this.container = container;
        autowireServicesAndContainer();
    }

    // /////////////////////////////////////////////////////////
    // Services
    // /////////////////////////////////////////////////////////

    @Override
    public void setServices(final List<Object> services) {
        this.services.clear();
        addServices(services);
        autowireServicesAndContainer();
    }

    @Override
    public List<Object> getRegisteredServices() {
        return Collections.unmodifiableList(services);
    }

    private void addServices(final List<Object> services) {
        for (final Object service : services) {
            if (service instanceof List) {
                final List<Object> serviceList = CastUtils.listOf(service, Object.class);
                addServices(serviceList);
            } else {
                addService(service);
            }
        }
    }

    private boolean addService(final Object service) {
        return services.add(service);
    }

    // /////////////////////////////////////////////////////////
    // Inject Dependencies
    // /////////////////////////////////////////////////////////

    @Override
    public void injectServicesInto(final Object object) {
        Assert.assertNotNull("no container", container);
        Assert.assertNotNull("no services", services);

        final List<Object> servicesCopy = Lists.newArrayList(services);
        servicesCopy.add(container);
        injectServices(object, servicesCopy);
    }

    @Override
    public void injectServicesInto(final List<Object> objects) {
        for (final Object object : objects) {
            injectServicesInto(object);
        }
    }

    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    /**
     * That is, injecting this injector...
     */
    @Override
    public void injectInto(final Object candidate) {
        if (ServicesInjectorAware.class.isAssignableFrom(candidate.getClass())) {
            final ServicesInjectorAware cast = ServicesInjectorAware.class.cast(candidate);
            cast.setServicesInjector(this);
        }
    }


    // /////////////////////////////////////////////////////////
    // Helpers
    // /////////////////////////////////////////////////////////

    private static void injectServices(final Object object, final List<Object> services) {
        final Class<?> cls = object.getClass();
        final Method[] methods = cls.getMethods();

        for (int j = 0; j < methods.length; j++) {
            for (final Object service : services) {
                final Class<?> serviceClass = service.getClass();
                boolean isInjectorMethod = isInjectorMethodFor(methods[j], serviceClass);
                if(isInjectorMethod) {
                    methods[j].setAccessible(true);
                    invokeSetMethod(methods[j], object, service);
                }
            }
        }
    }

    public static boolean isInjectorMethodFor(Method method, final Class<?> serviceClass) {
        final String methodName = method.getName();
        if (methodName.startsWith("set") || methodName.startsWith("inject")) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] != Object.class && parameterTypes[0].isAssignableFrom(serviceClass)) {
                return true;
            }
        }
        return false;
    }

    private static void invokeMethod(final Method method, final Object target, final Object[] parameters) {
        try {
            method.invoke(target, parameters);
        } catch (final SecurityException e) {
            throw new MetaModelException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass().getName()));
        } catch (final IllegalArgumentException e1) {
            throw new MetaModelException(e1);
        } catch (final IllegalAccessException e1) {
            throw new MetaModelException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass().getName()));
        } catch (final InvocationTargetException e) {
            final Throwable targetException = e.getTargetException();
            if (targetException instanceof RuntimeException) {
                throw (RuntimeException) targetException;
            } else {
                throw new MetaModelException(targetException);
            }
        }
    }

    private static void invokeSetMethod(final Method set, final Object target, final Object parameter) {
        final Object[] parameters = new Object[] { parameter };
        invokeMethod(set, target, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected " + parameter + " into " + new ToString(target));
        }
    }

    private void autowireServicesAndContainer() {
        injectServicesInto(this.services);
        injectServicesInto(this.container);
    }

    @Override
    public <T> T lookupService(Class<T> serviceClass) {
        List<T> services = lookupServices(serviceClass);
        return !services.isEmpty() ? services.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> lookupServices(Class<T> serviceClass) {
        locateAndCache(serviceClass);
        return (List<T>) servicesByType.get(serviceClass);
    };

    private void locateAndCache(Class<?> serviceClass) {
        if(servicesByType.containsKey(serviceClass)) {
           return; 
        }

        List<Object> matchingServices = Lists.newArrayList();
        addMatchingTo(serviceClass, services, matchingServices);
        addMatchingTo(serviceClass, singletonListFor(container), matchingServices);
        
        servicesByType.put(serviceClass, matchingServices);
    }

    private static List<Object> singletonListFor(Object obj) {
        return obj!=null? Collections.singletonList(obj): Collections.emptyList();
    }

    private static void addMatchingTo(Class<?> type, List<Object> candidates, List<Object> filteredServicesAndContainer) {
        Iterable<Object> filteredServices = Iterables.filter(candidates, ofType(type));
        filteredServicesAndContainer.addAll(Lists.newArrayList(filteredServices));
    }

    private static final Predicate<Object> ofType(final Class<?> cls) {
        return new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return cls.isAssignableFrom(input.getClass());
            }
        };
    }

}
