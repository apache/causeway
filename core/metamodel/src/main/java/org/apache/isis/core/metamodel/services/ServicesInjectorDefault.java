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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
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
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.core.metamodel.specloader.ServiceInitializer;

/**
 * Must be a thread-safe.
 */
public class ServicesInjectorDefault implements ServicesInjectorSpi {

    private static final Logger LOG = LoggerFactory.getLogger(ServicesInjectorDefault.class);

    /**
     * This is mutable internally, but only ever exposed (in {@link #getRegisteredServices()}) as immutable.
     */
    private final List<Object> services = Lists.newArrayList();

    /**
     * If no key, not yet searched for type; otherwise the corresponding value is a {@link List} of all
     * services that are assignable to the type.  It's possible that this is an empty list.
     */
    private final Map<Class<?>, List<Object>> servicesAssignableToType = Maps.newHashMap();

    private final Map<Class<?>, Object> serviceByConcreteType = Maps.newHashMap();

    private final InjectorMethodEvaluator injectorMethodEvaluator;

    public ServicesInjectorDefault(final List<Object> services) {
        this(services, null);
    }

    /**
     * For testing.  
     */
    public ServicesInjectorDefault(final List<Object> services, final InjectorMethodEvaluator injectorMethodEvaluator) {
        this.services.addAll(services);
        this.injectorMethodEvaluator = injectorMethodEvaluator != null ? injectorMethodEvaluator : new InjectorMethodEvaluatorDefault();

        autowireServicesAndContainer();
    }

    //region > replaceServices

    @Override
    public <T> void replaceService(final T existingService, final T replacementService) {

        if(!services.remove(existingService)) {
            throw new IllegalArgumentException("Service to be replaced was not found (" + existingService + ")");
        }

        services.add(replacementService);

        // invalidate
        servicesAssignableToType.clear();
        serviceByConcreteType.clear();

        autowireServicesAndContainer();
    }

    @Override
    public boolean isRegisteredService(final Class<?> cls) {
        // lazily construct cache
        if(serviceByConcreteType.isEmpty()) {
            for (Object service : services) {
                final Class<?> concreteType = service.getClass();
                serviceByConcreteType.put(concreteType, service);
            }
        }
        return serviceByConcreteType.containsKey(cls);
    }

    @Override
    public <T> void addFallbackIfRequired(final Class<T> serviceClass, final T serviceInstance) {
        if(!contains(services, serviceClass)) {
            // add to beginning;
            // (when first introduced, this feature has been used for the
            // FixtureScriptsDefault so that appears it top of prototyping menu; not
            // more flexible than this currently just because of YAGNI).
            services.add(0, serviceInstance);
        }
    }

    /**
     * Validate domain service Ids are unique, and that the {@link PostConstruct} method, if present, must either
     * take no arguments or take a {@link Map} object), and that the {@link PreDestroy} method, if present, must take
     * no arguments.
     *
     * <p>
     * TODO: there seems to be some duplication/overlap with {@link ServiceInitializer}.
     */
    @Override
    public void validateServices() {
        validate(getRegisteredServices());
    }

    private static void validate(List<Object> serviceList) {
        for (Object service : serviceList) {
            final Method[] methods = service.getClass().getMethods();
            for (Method method : methods) {
                validatePostConstructMethods(service, method);
                validatePreDestroyMethods(service, method);
            }
        }
        ListMultimap<String, Object> servicesById = ArrayListMultimap.create();
        for (Object service : serviceList) {
            String id = ServiceUtil.id(service);
            servicesById.put(id, service);
        }
        for (Map.Entry<String, Collection<Object>> servicesForId : servicesById.asMap().entrySet()) {
            String serviceId = servicesForId.getKey();
            Collection<Object> services = servicesForId.getValue();
            if(services.size() > 1) {
                throw new IllegalStateException(
                        String.format("Service ids must be unique; serviceId '%s' is declared by domain services %s",
                                serviceId, classNamesFor(services)));
            }
        }
    }

    private static String classNamesFor(Collection<Object> services) {
        StringBuilder buf = new StringBuilder();
        for (Object service : services) {
            if(buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(service.getClass().getName());
        }
        return buf.toString();
    }

    private static void validatePostConstructMethods(Object service, Method method) {
        final PostConstruct postConstruct = method.getAnnotation(PostConstruct.class);
        if(postConstruct == null) {
            return;
        }
        final int numParams = method.getParameterTypes().length;
        if(numParams == 0) {
            return;
        }
        if(numParams == 1 && method.getParameterTypes()[0].isAssignableFrom(Map.class)) {
            return;
        }
        throw new IllegalStateException("Domain service " + service.getClass().getName() + " has @PostConstruct method " + method.getName() + "; such methods must take either no argument or 1 argument of type Map<String,String>");
    }

    private static void validatePreDestroyMethods(Object service, Method method) {
        final PreDestroy preDestroy = method.getAnnotation(PreDestroy.class);
        if(preDestroy == null) {
            return;
        }
        final int numParams = method.getParameterTypes().length;
        if(numParams == 0) {
            return;
        }
        throw new IllegalStateException("Domain service " + service.getClass().getName() + " has @PreDestroy method " + method.getName() + "; such methods must take no arguments");
    }


    static boolean contains(final List<Object> services, final Class<?> serviceClass) {
        for (Object service : services) {
            if(serviceClass.isAssignableFrom(service.getClass())) {
                return true;
            }
        }
        return false;
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
        return (List<T>) servicesAssignableToType.get(serviceClass);
    };

    private void locateAndCache(final Class<?> serviceClass) {
        if(servicesAssignableToType.containsKey(serviceClass)) {
           return; 
        }

        final List<Object> matchingServices = Lists.newArrayList();
        addAssignableTo(serviceClass, services, matchingServices);

        servicesAssignableToType.put(serviceClass, matchingServices);
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

}
