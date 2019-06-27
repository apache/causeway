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
package org.apache.isis.metamodel.services;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.beans.factory.InjectionPoint;
import org.springframework.core.MethodParameter;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Collections;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.metamodel.exceptions.MetaModelException;
import org.apache.isis.metamodel.spec.InjectorMethodEvaluator;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Singleton @Log4j2
public class ServiceInjectorDefault implements ServiceInjector {
    
    private static final String KEY_SET_PREFIX = "isis.services.injector.setPrefix";
    private static final String KEY_INJECT_PREFIX = "isis.services.injector.injectPrefix";
    
    @Inject IsisConfiguration configuration;
    @Inject ServiceRegistry serviceRegistry;
    @Inject InjectorMethodEvaluator injectorMethodEvaluator;
    
    private final Map<Class<?>, Method[]> methodsByClassCache = _Maps.newHashMap();
    private final Map<Class<?>, Field[]> fieldsByClassCache = _Maps.newHashMap();
    
    @Override
    public <T> T injectServicesInto(T domainObject, Consumer<InjectionPoint> onNotResolvable) {
        injectServices(domainObject, onNotResolvable);
        return domainObject;
    }
    
    @PostConstruct
    public void init() {
        autowireSetters = configuration.getBoolean(KEY_SET_PREFIX, false);
        autowireInject = configuration.getBoolean(KEY_INJECT_PREFIX, true);
    }
    
    // -- HELPERS
    
    boolean autowireSetters;
    boolean autowireInject;    

    private void injectServices(final Object targetPojo, Consumer<InjectionPoint> onNotResolvable) {

        final Class<?> cls = targetPojo.getClass();

        injectToFields(targetPojo, cls, onNotResolvable);

        if(autowireSetters) {
            injectViaPrefixedMethods(targetPojo, cls, "set", onNotResolvable);
        }
        if(autowireInject) {
            injectViaPrefixedMethods(targetPojo, cls, "inject", onNotResolvable);
        }
    }

    private void injectToFields(final Object targetPojo, final Class<?> cls, Consumer<InjectionPoint> onNotResolvable) {

        _NullSafe.stream(fieldsByClassCache.computeIfAbsent(cls, __->cls.getDeclaredFields()))
        .filter(isAnnotatedForInjection())
        .forEach(field->injectToField(targetPojo, field, onNotResolvable));

        // recurse up the object's class hierarchy
        final Class<?> superclass = cls.getSuperclass();
        if(superclass != null) {
            injectToFields(targetPojo, superclass, onNotResolvable);
        }
    }

    private void injectToField(final Object targetPojo, final Field field, Consumer<InjectionPoint> onNotResolvable) {

        final Class<?> typeToBeInjected = field.getType();
        // don't think that type can ever be null,
        // but Javadoc for java.lang.reflect.Field doesn't say
        if(typeToBeInjected == null) {
            return;
        }

        // inject matching services into a field of type Collection<T> if a generic type T is present
        final Class<?> elementType = _Collections.inferElementTypeIfAny(field);
        if(elementType!=null) {
            injectToField_nonScalar(targetPojo, field, elementType, onNotResolvable);
            return;
        }
        
        val beans = serviceRegistry.select(typeToBeInjected, field.getAnnotations());
        
        if(beans.isEmpty()) {
            onNotResolvable.accept(new InjectionPoint(field));
            return;
        }
        
        val bean = beans.getFirst().get();
        invokeInjectorField(field, targetPojo, bean);

    }
    
    @SuppressWarnings("unchecked")
    private void injectToField_nonScalar(
            final Object targetPojo, 
            final Field field, 
            final Class<?> elementType, 
            final Consumer<InjectionPoint> onNotResolvable) {
        
        final Class<? extends Collection<Object>> collectionTypeToBeInjected =
                (Class<? extends Collection<Object>>) field.getType();
        
        val beans = serviceRegistry.select(elementType, field.getAnnotations());
        if(!beans.isEmpty()) {
            final Collection<Object> collectionOfServices = beans.stream()
                    .filter(isOfType(elementType))
                    // javac does require an explicit type argument here, 
                    // while eclipse compiler does not ...
                    .collect(_Collections.<Object>toUnmodifiableOfType(collectionTypeToBeInjected));

            invokeInjectorField(field, targetPojo, collectionOfServices);
        } else {
            onNotResolvable.accept(new InjectionPoint(field));
        }
        
    }

    private void injectViaPrefixedMethods(
            final Object targetPojo,
            final Class<?> cls,
            final String prefix, 
            final Consumer<InjectionPoint> onNotResolvable) {

        _NullSafe.stream(methodsByClassCache.computeIfAbsent(cls, __->cls.getMethods()))
        .filter(nameStartsWith(prefix))
        .forEach(prefixedMethod->injectIntoSetter(targetPojo, prefixedMethod, onNotResolvable));
    }

    private void injectIntoSetter(
            final Object targetPojo,
            final Method setter, 
            final Consumer<InjectionPoint> onNotResolvable) {
        
        final Class<?> typeToBeInjected = injectorMethodEvaluator.getTypeToBeInjected(setter);
        if(typeToBeInjected == null) {
            return;
        }
        
        val instance = serviceRegistry.select(typeToBeInjected, setter.getAnnotations());
        if(instance.isCardinalityOne()) {
            val bean = instance.getSingleton().get();
            invokeInjectorMethod(setter, targetPojo, bean);    
        } else {
            onNotResolvable.accept(new InjectionPoint(new MethodParameter(setter, 0)));
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
        if (log.isDebugEnabled()) {
            log.debug("injected {} into {}", parameter, new ToString(target));
        }
    }

    private static void invokeInjectorMethod(final Method method, final Object target, final Object parameter) {
        final Object[] parameters = new Object[] { parameter };
        invokeMethod(method, target, parameters);
        if (log.isDebugEnabled()) {
            log.debug("injected {} into {}", parameter, new ToString(target));
        }
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
    
    // -- TESTING
    
    /**
     * JUnit Test support. 
     */
    public static ServiceInjectorDefault getInstanceAndInit(
            IsisConfiguration configuration,
            ServiceRegistry serviceRegistry,
            InjectorMethodEvaluator injectorMethodEvaluator) {
        val instance = new ServiceInjectorDefault();
        
        instance.configuration = configuration;
        instance.serviceRegistry = serviceRegistry;
        instance.injectorMethodEvaluator = injectorMethodEvaluator;
        
        instance.init();
        
        return instance;
    }


}
