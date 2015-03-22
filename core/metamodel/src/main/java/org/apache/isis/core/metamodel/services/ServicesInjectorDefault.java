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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;

/**
 * Must be a thread-safe.
 */
public class ServicesInjectorDefault implements ServicesInjectorSpi, SpecificationLoaderAware {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInjectorDefault.class);

    private final List<Object> services = Lists.newArrayList();

    /**
     * If no key, not yet searched for type; otherwise the {@link List} indicates
     * whether a service was found.
     */
    private final Map<Class<?>, List<Object>> servicesByType = Maps.newHashMap();

    public ServicesInjectorDefault() {
        this(null);
    }

    /**
     * For testing.  
     * 
     * <p>
     *     In production code, {@link #setSpecificationLookup(org.apache.isis.core.metamodel.spec.SpecificationLoader)}
     *     Pis used instead.
     * </p>
     * @param injectorMethodEvaluator
     */
    public ServicesInjectorDefault(final InjectorMethodEvaluator injectorMethodEvaluator) {
        this.injectorMethodEvaluator = injectorMethodEvaluator != null ? injectorMethodEvaluator : new InjectorMethodEvaluatorDefault();
    }

//region > init, shutdown

    @Override
    public void init() {
        autowireServicesAndContainer();
    }

    @Override
    public void shutdown() {
    }

    //endregion

    //region > setServices, replaceServices

    @Override
    public void setServices(final List<Object> services) {
        this.services.clear();
        addServices(services);
        autowireServicesAndContainer();
    }

    public ServicesInjectorDefault withServices(final List<Object> services) {
        setServices(services);
        return this;
    }

    @Override
    public <T> void replaceService(final T existingService, final T replacementService) {

        if(!services.remove(existingService)) {
            throw new IllegalArgumentException("Service to be replaced was not found (" + existingService + ")");
        }

        services.add(replacementService);
        // invalidate cache
        servicesByType.clear();

        autowireServicesAndContainer();
    }


    @Override
    public List<Object> getRegisteredServices() {
        return Collections.unmodifiableList(services);
    }

    private void addServices(final List<Object> services) {
        for (final Object service : services) {
            if (service instanceof List) {
                final List<Object> serviceList = ObjectExtensions.asListT(service, Object.class);
                addServices(serviceList);
            } else {
                addService(service);
            }
        }
    }

    private boolean addService(final Object service) {
        return services.add(service);
    }

    //endregion

    //region > injectServicesInto

    @Override
    public void injectServicesInto(final Object object) {
        Assert.assertNotNull("no services", services);

        injectServices(object, Collections.unmodifiableList(services));
    }

    @Override
    public void injectServicesInto(final List<Object> objects) {
        for (final Object object : objects) {
            injectServicesInto(object);
        }
    }

    //endregion

    //region > injectInto

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

    //endregion

    //region > helpers

    private void injectServices(final Object object, final List<Object> services) {

        final Class<?> cls = object.getClass();

        autowireViaFields(object, services, cls);
        autowireViaPrefixedMethods(object, services, cls, "set");
        autowireViaPrefixedMethods(object, services, cls, "inject");
    }

    private void autowireViaFields(final Object object, final List<Object> services, final Class<?> cls) {
        final List<Field> fields = Arrays.asList(cls.getDeclaredFields());
        final Iterable<Field> injectFields = Iterables.filter(fields, new Predicate<Field>() {
            @Override
            public boolean apply(final Field input) {
                final Inject annotation = input.getAnnotation(javax.inject.Inject.class);
                return annotation != null;
            }
        });

        for (final Field field : injectFields) {
            autowire(object, field, services);
        }
        
        // recurse up the hierarchy
        final Class<?> superclass = cls.getSuperclass();
        if(superclass != null) {
            autowireViaFields(object, services, superclass);
        }
    }

    private void autowire(final Object object, final Field field, final List<Object> services) {
        for (final Object service : services) {
            final Class<?> serviceClass = service.getClass();
            final boolean canInject = isInjectorFieldFor(field, serviceClass);
            if(canInject) {
                field.setAccessible(true);
                invokeInjectorField(field, object, service);
                return;
            }
        }
    }

    private void autowireViaPrefixedMethods(final Object object, final List<Object> services, final Class<?> cls, final String prefix) {
        final List<Method> methods = Arrays.asList(cls.getMethods());
        final Iterable<Method> prefixedMethods = Iterables.filter(methods, new Predicate<Method>(){
            public boolean apply(final Method method) {
                final String methodName = method.getName();
                return methodName.startsWith(prefix);
            }
        });
        
        for (final Method prefixedMethod : prefixedMethods) {
            autowire(object, prefixedMethod, services);
        }
    }

    private void autowire(final Object object, final Method prefixedMethod, final List<Object> services) {
        for (final Object service : services) {
            final Class<?> serviceClass = service.getClass();
            final boolean isInjectorMethod = injectorMethodEvaluator.isInjectorMethodFor(prefixedMethod, serviceClass);
            if(isInjectorMethod) {
                prefixedMethod.setAccessible(true);
                invokeInjectorMethod(prefixedMethod, object, service);
                return;
            }
        }
    }

    private boolean isInjectorFieldFor(final Field field, final Class<?> serviceClass) {
        final Class<?> type = field.getType();
        // don't think that type can ever be null, but Javadoc for java.lang.reflect.Field doesn't say
        return type != null && type.isAssignableFrom(serviceClass);
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

    private static void invokeInjectorField(final Field field, final Object target, final Object parameter) {
        try {
            field.set(target, parameter);
        } catch (final IllegalArgumentException e) {
            throw new MetaModelException(e);
        } catch (final IllegalAccessException e) {
            throw new MetaModelException(String.format("Cannot access the %s field in %s", field.getName(), target.getClass().getName()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected " + parameter + " into " + new ToString(target));
        }
    }

    private static void invokeInjectorMethod(final Method method, final Object target, final Object parameter) {
        final Object[] parameters = new Object[] { parameter };
        invokeMethod(method, target, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected " + parameter + " into " + new ToString(target));
        }
    }
    
    private void autowireServicesAndContainer() {
        injectServicesInto(this.services);
    }


    //endregion

    //region > lookupService, lookupServices

    @Override
    public <T> T lookupService(final Class<T> serviceClass) {
        final List<T> services = lookupServices(serviceClass);
        return !services.isEmpty() ? services.get(0) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> lookupServices(final Class<T> serviceClass) {
        locateAndCache(serviceClass);
        return (List<T>) servicesByType.get(serviceClass);
    };

    private void locateAndCache(final Class<?> serviceClass) {
        if(servicesByType.containsKey(serviceClass)) {
           return; 
        }

        final List<Object> matchingServices = Lists.newArrayList();
        addAssignableTo(serviceClass, services, matchingServices);

        servicesByType.put(serviceClass, matchingServices);
    }

    private static void addAssignableTo(final Class<?> type, final List<Object> candidates, final List<Object> filteredServicesAndContainer) {
        final Iterable<Object> filteredServices = Iterables.filter(candidates, ofType(type));
        filteredServicesAndContainer.addAll(Lists.newArrayList(filteredServices));
    }

    private static final Predicate<Object> ofType(final Class<?> cls) {
        return new Predicate<Object>() {
            @Override
            public boolean apply(final Object input) {
                return cls.isAssignableFrom(input.getClass());
            }
        };
    }

    //endregion

    //region > injected dependencies

    private InjectorMethodEvaluator injectorMethodEvaluator;

    @Override
    public void setSpecificationLookup(final SpecificationLoader specificationLookup) {
        injectorMethodEvaluator = specificationLookup;
    }

    //endregion
}
