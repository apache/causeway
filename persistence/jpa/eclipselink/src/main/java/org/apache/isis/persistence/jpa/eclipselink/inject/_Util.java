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
import java.util.Collections;
import java.util.Set;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.inject.Provider;

import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.SneakyThrows;

final class _Util {

    static <T> CreationalContext<T> createCreationalContext(Contextual<T> contextual) {
        return new CreationalContext<T>() {

            @Override
            public void push(T incompleteInstance) {
                // silently ignore
            }

            @Override
            public void release() {
                // silently ignore
            }

        };
    }

    static <T> AnnotatedType<T> createAnnotatedType(Class<T> type) {

        return new AnnotatedType<T>() {

            @Override
            public Class<T> getJavaClass() {
                return type;
            }

            @Override
            public Type getBaseType() {
                _Exceptions.throwNotImplemented();
                return null;
            }

            @Override
            public Set<Type> getTypeClosure() {
                _Exceptions.throwNotImplemented();
                return null;
            }

            @Override
            public <X extends Annotation> X getAnnotation(Class<X> annotationType) {
                _Exceptions.throwNotImplemented();
                return null;
            }

            @Override
            public Set<Annotation> getAnnotations() {
                _Exceptions.throwNotImplemented();
                return null;
            }

            @Override
            public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
                _Exceptions.throwNotImplemented();
                return false;
            }

            @Override
            public Set<AnnotatedConstructor<T>> getConstructors() {
                _Exceptions.throwNotImplemented();
                return null;
            }

            @Override
            public Set<AnnotatedMethod<? super T>> getMethods() {
                _Exceptions.throwNotImplemented();
                return null;
            }

            @Override
            public Set<AnnotatedField<? super T>> getFields() {
                _Exceptions.throwNotImplemented();
                return null;
            }
        };
    }

    static <T> InjectionTarget<T> createInjectionTarget(
            final AnnotatedType<T> type,
            final Provider<ServiceInjector> serviceInjectorProvider) {

        return new InjectionTarget<T>() {

            @Override @SneakyThrows
            public T produce(CreationalContext<T> ctx) {
                return type.getJavaClass().newInstance();
            }

            @Override
            public void inject(T instance, CreationalContext<T> ctx) {
                serviceInjectorProvider.get().injectServicesInto(instance);
            }

            @Override
            public void dispose(T instance) {
                // silently ignore
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                // silently ignore
                return Collections.emptySet();
            }

            @Override
            public void postConstruct(T instance) {
                // silently ignore
            }

            @Override
            public void preDestroy(T instance) {
                // silently ignore
            }
        };
    }

}
