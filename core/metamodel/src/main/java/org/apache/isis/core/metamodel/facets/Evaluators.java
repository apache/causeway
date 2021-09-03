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

package org.apache.isis.core.metamodel.facets;

import java.beans.IntrospectionException;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.commons.ThrowableExtensions;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Evaluators  {

    /**
     * Streams all fields and no-arg methods having a specified annotationType,
     * each wrapped with an {@link Evaluator} object.
     */
    public static <T extends Annotation> Stream<Evaluator<T>> streamEvaluators(
            final @NonNull Class<?> cls,
            final @NonNull Class<T> annotationType,
            final @NonNull TypeHierarchyPolicy typeHierarchyPolicy,
            final @NonNull InterfacePolicy interfacePolicy) {

        return typeHierarchyPolicy.isIncludeTypeHierarchy()
                ? _Reflect
                    .streamTypeHierarchy(cls, interfacePolicy)
                    .flatMap(type->streamAnnotatedMemberEvaluators(type, annotationType))
                : streamAnnotatedMemberEvaluators(cls, annotationType);
    }

    // -- HELPER

    private static <T extends Annotation> Stream<Evaluator<T>> streamAnnotatedMemberEvaluators(
            final Class<?> cls,
            final Class<T> annotationType) {

        val classCache = _ClassCache.getInstance();

        return Stream.concat(
                streamMethodEvaluators(cls, annotationType, classCache),
                streamFieldEvaluators(cls, annotationType, classCache));
    }

    private static <T extends Annotation> Stream<Evaluator<T>> streamMethodEvaluators(
            final Class<?> cls,
            final Class<T> annotationType,
            final _ClassCache classCache) {

        return classCache
        .streamDeclaredMethods(cls)
        .filter(MethodUtil::isNotStatic)
        .filter(MethodUtil::isNoArg)
        .map(method->MethodEvaluator.create(method, annotationType))
        .flatMap(Optional::stream);
    }

    private static <T extends Annotation> Stream<Evaluator<T>> streamFieldEvaluators(
            final Class<?> cls,
            final Class<T> annotationType,
            final _ClassCache classCache) {

        return classCache
        .streamDeclaredFields(cls)
        .map(field->FieldEvaluator.create(field, annotationType))
        .flatMap(Optional::stream);
    }

    // -- EVALUATOR

    public static abstract class Evaluator<T extends Annotation> {
        @Getter private final T annotation;
        private MethodHandle mh;

        protected Evaluator(final T annotation) {
            this.annotation = annotation;
        }

        protected abstract MethodHandle createMethodHandle() throws IllegalAccessException;
        public abstract String name();

        public Object value(final Object obj) {
            if(mh==null) {
                try {
                    mh = createMethodHandle();
                } catch (IllegalAccessException e) {
                    throw new MetaModelException("illegal access of " + name(), e);
                }
            }

            try {
                return mh.invoke(obj);
            } catch (Throwable e) {
                return ThrowableExtensions.handleInvocationException(e, name());
            }

        }
    }

    public static class MethodEvaluator<T extends Annotation> extends Evaluator<T> {

        static <T extends Annotation> Optional<MethodEvaluator<T>> create(
                final Method method,
                final Class<T> annotationType) {

            return Optional.ofNullable(method.getAnnotation(annotationType))
                    .map(annot->new MethodEvaluator<>(method, annot));
        }

        @Getter private final Method method;

        MethodEvaluator(final Method method, final T annotation) {
            super(annotation);
            this.method = method;
        }

        @Override
        public String name() {
            return method.getName() + "()";
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return _Reflect.handleOf(method);
        }
    }

    public static class FieldEvaluator<T extends Annotation> extends Evaluator<T> {

        static <T extends Annotation> Optional<FieldEvaluator<T>> create(
                final Field field,
                final Class<T> annotationType) {

            return Optional.ofNullable(field.getAnnotation(annotationType))
                    .map(annot->new FieldEvaluator<>(field, annot));
        }

        @Getter private final Field field;

        FieldEvaluator(final Field field, final T annotation) {
            super(annotation);
            this.field = field;
        }

        @Override
        public String name() {
            return field.getName();
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return _Reflect.handleOfGetterOn(field);
        }

        public Optional<Method> getGetter(final Class<?> originatingClass) {
            try {
                return Optional.ofNullable(
                        _Reflect.getGetter(originatingClass, field.getName())    );
            } catch (IntrospectionException e) {
                log.warn("failed reflective introspection on {} field {}",
                        originatingClass, field.getName(), e);
            }
            return Optional.empty();
        }

    }

}
