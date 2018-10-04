/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.services;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Multimaps;
import org.apache.isis.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationDefault;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.exceptions.MetaModelException;
import org.apache.isis.core.metamodel.services.configinternal.ConfigurationServiceInternal;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.InjectorMethodEvaluator;
import org.apache.isis.core.metamodel.specloader.InjectorMethodEvaluatorDefault;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.authorization.AuthorizationManager;

/**
 * The repository of services, also able to inject into any object.
 *
 * <p>
 *    Implementation is (and must be) a thread-safe.
 * </p>
 *
 */
public class ServicesInjector implements ApplicationScopedComponent, ServiceRegistry {


    private static final Logger LOG = LoggerFactory.getLogger(ServicesInjector.class);

    public static final String KEY_SET_PREFIX = "isis.services.injector.setPrefix";
    public static final String KEY_INJECT_PREFIX = "isis.services.injector.injectPrefix";

    // -- CONSTRUCTOR, FIELDS
    /**
     * This is mutable internally, but only ever exposed (in {@link #streamRegisteredServices()}).
     */
    private final List<Object> services;

    /**
     * If no key, not yet searched for type; otherwise the corresponding value is a {@link List} of all
     * services that are assignable to the type.  It's possible that this is an empty list.
     */
    private final ListMultimap<Class<?>, Object> servicesAssignableToType = _Multimaps.newListMultimap();
    private final _Lazy<Map<Class<?>, Object>> serviceByConcreteType = _Lazy.of(this::initServiceByConcreteType);
    private final Map<Class<?>, Method[]> methodsByClassCache = _Maps.newHashMap();
    private final Map<Class<?>, Field[]> fieldsByClassCache = _Maps.newHashMap();

    private final InjectorMethodEvaluator injectorMethodEvaluator;
    private final boolean autowireSetters;
    private final boolean autowireInject;

    public ServicesInjector(final List<Object> services, final IsisConfiguration configuration) {
        this(services, null, configuration);
    }

    public static ServicesInjector forTesting(
            final List<Object> services,
            final IsisConfigurationDefault configuration,
            final InjectorMethodEvaluator injectorMethodEvaluator) {
        return new ServicesInjector(services, injectorMethodEvaluator, defaultAutowiring(configuration));
    }

    private static IsisConfiguration defaultAutowiring(final IsisConfigurationDefault configuration) {
        configuration.put(KEY_SET_PREFIX, ""+true);
        configuration.put(KEY_INJECT_PREFIX, ""+false);
        return configuration;
    }

    private ServicesInjector(
            final List<Object> services,
            final InjectorMethodEvaluator injectorMethodEvaluator,
            final IsisConfiguration configuration) {
        
        this.services = new ArrayList<>(services);//_Lists.unmodifiable(services);

        this.injectorMethodEvaluator =
                injectorMethodEvaluator != null
                ? injectorMethodEvaluator
                        : new InjectorMethodEvaluatorDefault();

        this.autowireSetters = configuration.getBoolean(KEY_SET_PREFIX, true);
        this.autowireInject = configuration.getBoolean(KEY_INJECT_PREFIX, false);
    }

    public boolean isRegisteredService(final Class<?> cls) {
        return serviceByConcreteType.get().containsKey(cls);
    }

    public boolean isRegisteredServiceInstance(final Object pojo) {
        if(pojo==null) {
            return false;
        }
        final Class<?> key = pojo.getClass();
        final Object serviceInstance = serviceByConcreteType.get().get(key);
        return Objects.equals(pojo, serviceInstance);
    }
    
    
    public <T> void addFallbackIfRequired(final Class<T> serviceClass, final T serviceInstance) {
        if(!contains(services, serviceClass)) {
            // add to beginning;
            // (when first introduced, this feature has been used for the
            // FixtureScriptsDefault so that appears it top of prototyping menu; not
            // more flexible than this currently just because of YAGNI).
            services.add(0, serviceInstance);
            
            //[ahuber] currently seems the only entry-point that modifies the services list
            //hence we also invalidate the lazy lookup
            serviceByConcreteType.clear();
        }
    }

    /**
     * Validate domain service Ids are unique.
     */
    public void validateServices() {
        validate(streamServices());
    }

    private static void validate(final Stream<Object> serviceInstances) {
        
        final ListMultimap<String, Object> servicesById = _Multimaps.newListMultimap();
        serviceInstances.forEach(serviceInstance->{
            String id = ServiceUtil.idOfPojo(serviceInstance);
            servicesById.putElement(id, serviceInstance);
        });

        final String errorMsg = servicesById.entrySet().stream()
        .filter(entry->entry.getValue().size()>1) // filter for duplicates
        .map(entry->{
            String serviceId = entry.getKey();
            List<Object> duplicateServiceEntries = entry.getValue();
            return String.format("serviceId '%s' is declared by domain services %s",
                    serviceId, classNamesFor(duplicateServiceEntries));
        })
        .collect(Collectors.joining(", "));
         
        if(_Strings.isNotEmpty(errorMsg)) {
            throw new IllegalStateException("Service ids must be unique! "+errorMsg);
        }
    }

    private static String classNamesFor(Collection<Object> services) {
        return stream(services)
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    static boolean contains(final List<Object> services, final Class<?> serviceClass) {
        return stream(services)
                .anyMatch(isOfType(serviceClass));
    }

    
    /**
     * @return Stream of all currently registered service types.
     */
    public Stream<Class<?>> streamServiceTypes() {
        return serviceByConcreteType.get().keySet().stream();
    }
    
    @Override
    public Stream<Object> streamServices() {
        return services.stream();
    }
    
    // -- INJECT SERVICES INTO

    /**
     * Provided by the <tt>ServicesInjector</tt> when used by framework.
     *
     * <p>
     * Called in multiple places from metamodel and facets.
     */
    @Override
    public <T> T injectServicesInto(final T object) {
        injectServices(object, services);
        return object;
    }

    /**
     * As per {@link #injectServicesInto(Object)}, but for all objects in the
     * list.
     */
    public void injectServicesInto(final List<Object> objects) {
        for (final Object object : objects) {
            injectInto(object); // if implements ServiceInjectorAware
            injectServicesInto(object); // via @javax.inject.Inject or setXxx(...)
        }
    }

    // -- INJECT INTO

    /**
     * That is, injecting this injector...
     */
    public void injectInto(final Object candidate) {
        if (ServicesInjectorAware.class.isAssignableFrom(candidate.getClass())) {
            final ServicesInjectorAware cast = ServicesInjectorAware.class.cast(candidate);
            cast.setServicesInjector(this);
        }
    }
    
    // -- SERVICE LOOKUP
    
    /**
     * Returns all domain services implementing the requested type, in the order
     * that they were registered in <tt>isis.properties</tt>.
     *
     * <p>
     * Typically there will only ever be one domain service implementing a given type,
     * (eg {@link PublishingService}), but for some services there can be more than one
     * (eg {@link ExceptionRecognizer}).
     *
     * @see #lookupService(Class)
     */
    @Programmatic
    @Override
    public <T> Stream<T> streamServices(final Class<T> serviceClass) {
        return servicesAssignableToType
                .computeIfAbsent(serviceClass, this::locateMatchingServices)
                .stream()
                .map(x->uncheckedCast(x));
    }

    // -- HELPERS

    private void injectServices(final Object object, final List<Object> services) {

        final Class<?> cls = object.getClass();

        autowireViaFields(object, services, cls);

        if(autowireSetters) {
            autowireViaPrefixedMethods(object, services, cls, "set");
        }
        if(autowireInject) {
            autowireViaPrefixedMethods(object, services, cls, "inject");
        }
    }

    private void autowireViaFields(final Object object, final List<Object> services, final Class<?> cls) {

        _NullSafe.stream(fieldsByClassCache.computeIfAbsent(cls, __->cls.getDeclaredFields()))
        .filter(isAnnotatedForInjection())
        .forEach(field->autowire(object, field, services));

        // recurse up the object's class hierarchy
        final Class<?> superclass = cls.getSuperclass();
        if(superclass != null) {
            autowireViaFields(object, services, superclass);
        }
    }

    private void autowire(
            final Object object,
            final Field field,
            final List<Object> services) {

        final Class<?> typeToBeInjected = field.getType();
        // don't think that type can ever be null,
        // but Javadoc for java.lang.reflect.Field doesn't say
        if(typeToBeInjected == null) {
            return;
        }

        // inject matching services into a field of type Collection<T> if a generic type T is present
        final Class<?> elementType = _Collections.inferElementTypeIfAny(field);
        if(elementType!=null) {
            @SuppressWarnings("unchecked")
            final Class<? extends Collection<Object>> collectionTypeToBeInjected =
            (Class<? extends Collection<Object>>) typeToBeInjected;

            final Collection<Object> collectionOfServices = _NullSafe.stream(services)
                    .filter(_NullSafe::isPresent)
                    .filter(isOfType(elementType))
                    .collect(_Collections.toUnmodifiableOfType(collectionTypeToBeInjected));

            invokeInjectorField(field, object, collectionOfServices);
        }

        for (final Object service : services) {
            final Class<?> serviceClass = service.getClass();
            if(typeToBeInjected.isAssignableFrom(serviceClass)) {
                invokeInjectorField(field, object, service);
                return;
            }
        }
    }

    private void autowireViaPrefixedMethods(
            final Object object,
            final List<Object> services,
            final Class<?> cls,
            final String prefix) {

        _NullSafe.stream(methodsByClassCache.computeIfAbsent(cls, __->cls.getMethods()))
        .filter(nameStartsWith(prefix))
        .forEach(prefixedMethod->autowire(object, prefixedMethod, services));
    }

    private void autowire(
            final Object object,
            final Method prefixedMethod,
            final List<Object> services) {

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

    private static void invokeMethod(final Method method, final Object target, final Object[] parameters) {
        try {
            method.invoke(target, parameters);
        } catch (final SecurityException | IllegalAccessException e) {
            throw new MetaModelException(String.format("Cannot access the %s method in %s", method.getName(), target.getClass().getName()));
        } catch (final IllegalArgumentException e1) {
            throw new MetaModelException(e1);
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
            field.setAccessible(true);
            field.set(target, parameter);
        } catch (final IllegalArgumentException e) {
            throw new MetaModelException(e);
        } catch (final IllegalAccessException e) {
            throw new MetaModelException(String.format("Cannot access the %s field in %s", field.getName(), target.getClass().getName()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected {} into {}", parameter, new ToString(target));
        }
    }

    private static void invokeInjectorMethod(final Method method, final Object target, final Object parameter) {
        final Object[] parameters = new Object[] { parameter };
        invokeMethod(method, target, parameters);
        if (LOG.isDebugEnabled()) {
            LOG.debug("injected {} into {}", parameter, new ToString(target));
        }
    }

    // -- AUTOWIRE

    @Programmatic
    public void autowire() {
        injectServicesInto(this.services);
    }

    // -- LOOKUP SERVICE(S)
    
    private <T> List<Object> locateMatchingServices(final Class<T> serviceClass) {
        final List<Object> matchingServices = services.stream()
            .filter(isOfType(serviceClass))
            .collect(Collectors.toList());
        return matchingServices;
    }

    // -- LAZY INIT
    
    private Map<Class<?>, Object> initServiceByConcreteType(){
        final Map<Class<?>, Object> map = _Maps.newHashMap();
        for (Object service : services) {
            final Class<?> concreteType = service.getClass();
            map.put(concreteType, service);
        }
        return map;
    }
    
    // -- REFLECTIVE PREDICATES

    private static final Predicate<Object> isOfType(final Class<?> cls) {
        return obj->cls.isAssignableFrom(obj.getClass());
    }

    private static final Predicate<Method> nameStartsWith(final String prefix) {
        return method->method.getName().startsWith(prefix);
    }

    private static final Predicate<Field> isAnnotatedForInjection() {
        return field->field.getAnnotation(javax.inject.Inject.class) != null;
    }

    // -- CONVENIENCE LOOKUPS (singletons only, cached)

    private AuthenticationManager authenticationManager;

    @Programmatic
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager != null
                ? authenticationManager
                        : (authenticationManager = lookupServiceElseFail(AuthenticationManager.class));
    }

    private AuthorizationManager authorizationManager;

    @Programmatic
    public AuthorizationManager getAuthorizationManager() {
        return authorizationManager != null
                ? authorizationManager
                        : (authorizationManager = lookupServiceElseFail(AuthorizationManager.class));
    }

    private SpecificationLoader specificationLoader;

    @Programmatic
    public SpecificationLoader getSpecificationLoader() {
        return specificationLoader != null
                ? specificationLoader
                        : (specificationLoader = lookupServiceElseFail(SpecificationLoader.class));
    }

    private AuthenticationSessionProvider authenticationSessionProvider;
    @Programmatic
    public AuthenticationSessionProvider getAuthenticationSessionProvider() {
        return authenticationSessionProvider != null
                ? authenticationSessionProvider
                        : (authenticationSessionProvider = lookupServiceElseFail(AuthenticationSessionProvider.class));
    }

    private PersistenceSessionServiceInternal persistenceSessionServiceInternal;
    @Programmatic
    public PersistenceSessionServiceInternal getPersistenceSessionServiceInternal() {
        return persistenceSessionServiceInternal != null
                ? persistenceSessionServiceInternal
                        : (persistenceSessionServiceInternal = lookupServiceElseFail(PersistenceSessionServiceInternal.class));
    }

    private ConfigurationServiceInternal configurationServiceInternal;
    @Programmatic
    public ConfigurationServiceInternal getConfigurationServiceInternal() {
        return configurationServiceInternal != null
                ? configurationServiceInternal
                        : (configurationServiceInternal = lookupServiceElseFail(ConfigurationServiceInternal.class));
    }

    private DeploymentCategoryProvider deploymentCategoryProvider;
    @Programmatic
    public DeploymentCategoryProvider getDeploymentCategoryProvider() {
        return deploymentCategoryProvider != null
                ? deploymentCategoryProvider
                        : (deploymentCategoryProvider = lookupServiceElseFail(DeploymentCategoryProvider.class));
    }




}
