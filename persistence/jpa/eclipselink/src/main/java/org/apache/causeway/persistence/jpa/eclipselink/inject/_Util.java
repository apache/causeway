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
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedConstructor;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.AnnotatedMethod;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.inject.Provider;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.SneakyThrows;

final class _Util {

    static <T> CreationalContext<T> createCreationalContext(final Contextual<T> contextual) {
        return new CreationalContext<T>() {

            @Override
            public void push(final T incompleteInstance) {
                // silently ignore
            }

            @Override
            public void release() {
                // silently ignore
            }

        };
    }

    static <T> AnnotatedType<T> createAnnotatedType(final Class<T> type) {

        return new AnnotatedType<T>() {

            @Override
            public Class<T> getJavaClass() {
                return type;
            }

            @Override
            public Type getBaseType() {
                throw _Exceptions.notImplemented();
            }

            @Override
            public Set<Type> getTypeClosure() {
                throw _Exceptions.notImplemented();
            }

            @Override
            public <X extends Annotation> X getAnnotation(final Class<X> annotationType) {
                throw _Exceptions.notImplemented();
            }

            @Override
            public Set<Annotation> getAnnotations() {
                throw _Exceptions.notImplemented();
            }

            @Override
            public boolean isAnnotationPresent(final Class<? extends Annotation> annotationType) {
                throw _Exceptions.notImplemented();
            }

            @Override
            public Set<AnnotatedConstructor<T>> getConstructors() {
                throw _Exceptions.notImplemented();
            }

            @Override
            public Set<AnnotatedMethod<? super T>> getMethods() {
                throw _Exceptions.notImplemented();
            }

            @Override
            public Set<AnnotatedField<? super T>> getFields() {
                throw _Exceptions.notImplemented();
            }
        };
    }

    static <T> InjectionTarget<T> createInjectionTarget(
            final AnnotatedType<T> type,
            final Provider<ServiceInjector> serviceInjectorProvider) {

        return new InjectionTarget<T>() {

            @Override @SneakyThrows
            public T produce(final CreationalContext<T> ctx) {
                return type.getJavaClass().getConstructor(_Constants.emptyClasses).newInstance();
            }

            @Override
            public void inject(final T instance, final CreationalContext<T> ctx) {
                serviceInjectorProvider.get().injectServicesInto(instance);
            }

            @Override
            public void dispose(final T instance) {
                // silently ignore
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                // silently ignore
                return Collections.emptySet();
            }

            @Override
            public void postConstruct(final T instance) {
                // silently ignore
            }

            @Override
            public void preDestroy(final T instance) {
                // silently ignore
            }
        };
    }

    static <T> InjectionTargetFactory<T> createInjectionTargetFactory(
            final AnnotatedType<T> type,
            final Provider<ServiceInjector> serviceInjectorProvider) {

        return bean -> new InjectionTarget<T>() {

            @Override @SneakyThrows
            public T produce(final CreationalContext<T> ctx) {
                return type.getJavaClass().getConstructor(_Constants.emptyClasses).newInstance();
            }

            @Override
            public void inject(final T instance, final CreationalContext<T> ctx) {
                serviceInjectorProvider.get().injectServicesInto(instance);
            }

            @Override
            public void dispose(final T instance) {
                // silently ignore
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                // silently ignore
                return Collections.emptySet();
            }

            @Override
            public void postConstruct(final T instance) {
                // silently ignore
            }

            @Override
            public void preDestroy(final T instance) {
                // silently ignore
            }
        };

    }

}
