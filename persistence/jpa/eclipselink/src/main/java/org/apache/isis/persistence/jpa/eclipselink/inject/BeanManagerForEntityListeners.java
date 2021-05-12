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
package org.apache.isis.persistence.jpa.eclipselink.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMember;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Decorator;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.enterprise.inject.spi.InterceptionFactory;
import javax.enterprise.inject.spi.InterceptionType;
import javax.enterprise.inject.spi.Interceptor;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.enterprise.inject.spi.ProducerFactory;
import javax.inject.Provider;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.RequiredArgsConstructor;

/**
 * Incomplete implementation of a {@link BeanManager}, solely for the purpose of enabling
 * injection point resolving on javax.persistence.EntityListeners.
 * <p>
 * Classes listed with the {@link javax.persistence.EntityListeners} annotation are not managed
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
    public <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return _Util.createCreationalContext(contextual);
    }

    @Override
    public <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {
        return _Util.createAnnotatedType(type);
    }

    @Override
    public <T> InjectionTarget<T> createInjectionTarget(AnnotatedType<T> type) {
        return _Util.createInjectionTarget(type, serviceInjectorProvider);
    }

    // -- IGNORED

    @Override
    public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Object getInjectableReference(InjectionPoint ij, CreationalContext<?> ctx) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Set<Bean<?>> getBeans(String name) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Bean<?> getPassivationCapableBean(String id) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public void validate(InjectionPoint injectionPoint) {
        _Exceptions.throwNotImplemented();
    }

    @Override
    public void fireEvent(Object event, Annotation... qualifiers) {
        _Exceptions.throwNotImplemented();
    }

    @Override
    public <T> Set<ObserverMethod<? super T>> resolveObserverMethods(T event, Annotation... qualifiers) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public List<Decorator<?>> resolveDecorators(Set<Type> types, Annotation... qualifiers) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public List<Interceptor<?>> resolveInterceptors(InterceptionType type, Annotation... interceptorBindings) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public boolean isScope(Class<? extends Annotation> annotationType) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public boolean isNormalScope(Class<? extends Annotation> annotationType) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public boolean isPassivatingScope(Class<? extends Annotation> annotationType) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public boolean isQualifier(Class<? extends Annotation> annotationType) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public boolean isInterceptorBinding(Class<? extends Annotation> annotationType) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public boolean isStereotype(Class<? extends Annotation> annotationType) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public Set<Annotation> getInterceptorBindingDefinition(Class<? extends Annotation> bindingType) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Set<Annotation> getStereotypeDefinition(Class<? extends Annotation> stereotype) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public boolean areQualifiersEquivalent(Annotation qualifier1, Annotation qualifier2) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public boolean areInterceptorBindingsEquivalent(Annotation interceptorBinding1,
            Annotation interceptorBinding2) {
        _Exceptions.throwNotImplemented();
        return false;
    }

    @Override
    public int getQualifierHashCode(Annotation qualifier) {
        _Exceptions.throwNotImplemented();
        return 0;
    }

    @Override
    public int getInterceptorBindingHashCode(Annotation interceptorBinding) {
        _Exceptions.throwNotImplemented();
        return 0;
    }

    @Override
    public Context getContext(Class<? extends Annotation> scopeType) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public ELResolver getELResolver() {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public ExpressionFactory wrapExpressionFactory(ExpressionFactory expressionFactory) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <T> InjectionTargetFactory<T> getInjectionTargetFactory(AnnotatedType<T> annotatedType) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedField<? super X> field, Bean<X> declaringBean) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <X> ProducerFactory<X> getProducerFactory(AnnotatedMethod<? super X> method, Bean<X> declaringBean) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <T> BeanAttributes<T> createBeanAttributes(AnnotatedType<T> type) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public BeanAttributes<?> createBeanAttributes(AnnotatedMember<?> type) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <T> Bean<T> createBean(BeanAttributes<T> attributes, Class<T> beanClass,
            InjectionTargetFactory<T> injectionTargetFactory) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <T, X> Bean<T> createBean(BeanAttributes<T> attributes, Class<X> beanClass,
            ProducerFactory<X> producerFactory) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public InjectionPoint createInjectionPoint(AnnotatedField<?> field) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public InjectionPoint createInjectionPoint(AnnotatedParameter<?> parameter) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public <T> InterceptionFactory<T> createInterceptionFactory(CreationalContext<T> ctx, Class<T> clazz) {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Event<Object> getEvent() {
        _Exceptions.throwNotImplemented();
        return null;
    }

    @Override
    public Instance<Object> createInstance() {
        _Exceptions.throwNotImplemented();
        return null;
    }


}
