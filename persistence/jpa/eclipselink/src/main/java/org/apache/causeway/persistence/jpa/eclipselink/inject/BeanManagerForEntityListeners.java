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
package org.apache.causeway.persistence.jpa.eclipselink.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMember;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedParameter;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanAttributes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Decorator;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.enterprise.inject.spi.InterceptionFactory;
import jakarta.enterprise.inject.spi.InterceptionType;
import jakarta.enterprise.inject.spi.Interceptor;
import jakarta.enterprise.inject.spi.ObserverMethod;
import jakarta.enterprise.inject.spi.ProducerFactory;
import jakarta.inject.Provider;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;

/**
 * Incomplete implementation of a {@link BeanManager}, solely for the purpose of enabling
 * injection point resolving on jakarta.persistence.EntityListeners.
 * <p>
 * Classes listed with the {@link jakarta.persistence.EntityListeners} annotation are not managed
 * by Spring, hence injection point resolving for these is not supported out of the box. However,
 * EclipseLink allows to configure a {@link BeanManager}, that is used for injection point
 * resolving. This implementation is limited to support only no-arg constructors.
 *
 * @since 2.0
 */
@RequiredArgsConstructor
public class BeanManagerForEntityListeners implements BeanManager {

    private final Provider<ServiceInjector> serviceInjectorProvider;

    @Override
    public <T> CreationalContext<T> createCreationalContext(final Contextual<T> contextual) {
        return _Util.createCreationalContext(contextual);
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(final Class<T> type) {
        return _Util.createAnnotatedType(type);
    }

    // -- IGNORED

    @Override
    public Object getReference(final Bean<?> bean, final Type beanType, final CreationalContext<?> ctx) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Object getInjectableReference(final InjectionPoint ij, final CreationalContext<?> ctx) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Set<Bean<?>> getBeans(final Type beanType, final Annotation... qualifiers) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Set<Bean<?>> getBeans(final String name) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Bean<?> getPassivationCapableBean(final String id) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <X> Bean<? extends X> resolve(final Set<Bean<? extends X>> beans) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public void validate(final InjectionPoint injectionPoint) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(final T event, final Annotation... qualifiers) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public List<Decorator<?>> resolveDecorators(final Set<Type> types, final Annotation... qualifiers) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(final InterceptionType type, final Annotation... interceptorBindings) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isScope(final Class<? extends Annotation> annotationType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isNormalScope(final Class<? extends Annotation> annotationType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isPassivatingScope(final Class<? extends Annotation> annotationType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isQualifier(final Class<? extends Annotation> annotationType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isInterceptorBinding(final Class<? extends Annotation> annotationType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isStereotype(final Class<? extends Annotation> annotationType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(final Class<? extends Annotation> bindingType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Set<Annotation> getStereotypeDefinition(final Class<? extends Annotation> stereotype) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean areQualifiersEquivalent(final Annotation qualifier1, final Annotation qualifier2) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean areInterceptorBindingsEquivalent(final Annotation interceptorBinding1,
            final Annotation interceptorBinding2) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public int getQualifierHashCode(final Annotation qualifier) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public int getInterceptorBindingHashCode(final Annotation interceptorBinding) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Context getContext(final Class<? extends Annotation> scopeType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public ELResolver getELResolver() {
        throw _Exceptions.notImplemented();
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(final ExpressionFactory expressionFactory) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T> InjectionTargetFactory<T> getInjectionTargetFactory(final AnnotatedType<T> annotatedType) {
        return _Util.createInjectionTargetFactory(annotatedType, serviceInjectorProvider);
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(final AnnotatedField<? super X> field, final Bean<X> declaringBean) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(final AnnotatedMethod<? super X> method, final Bean<X> declaringBean) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T> BeanAttributes<T> createBeanAttributes(final AnnotatedType<T> type) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public BeanAttributes<?> createBeanAttributes(final AnnotatedMember<?> type) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T> Bean<T> createBean(final BeanAttributes<T> attributes, final Class<T> beanClass,
            final InjectionTargetFactory<T> injectionTargetFactory) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T, X> Bean<T> createBean(final BeanAttributes<T> attributes, final Class<X> beanClass,
            final ProducerFactory<X> producerFactory) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public InjectionPoint createInjectionPoint(final AnnotatedField<?> field) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public InjectionPoint createInjectionPoint(final AnnotatedParameter<?> parameter) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T extends Extension> T getExtension(final Class<T> extensionClass) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public <T> InterceptionFactory<T> createInterceptionFactory(final CreationalContext<T> ctx, final Class<T> clazz) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Event<Object> getEvent() {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Instance<Object> createInstance() {
        throw _Exceptions.notImplemented();
    }

    @Override
    public Collection<Context> getContexts(final Class<? extends Annotation> scopeType) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isMatchingBean(final Set<Type> beanTypes, final Set<Annotation> beanQualifiers, final Type requiredType,
            final Set<Annotation> requiredQualifiers) {
        throw _Exceptions.notImplemented();
    }

    @Override
    public boolean isMatchingEvent(final Type specifiedType, final Set<Annotation> specifiedQualifiers, final Type observedEventType,
            final Set<Annotation> observedEventQualifiers) {
        throw _Exceptions.notImplemented();
    }

}
