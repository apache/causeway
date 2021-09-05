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
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.reflection._ClassCache;
import org.apache.isis.commons.internal.reflection._Reflect;
import org.apache.isis.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.isis.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.isis.core.metamodel.commons.MethodUtil;
import org.apache.isis.core.metamodel.commons.ThrowableExtensions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public final class Evaluators  {

    /**
     * Streams all fields and no-arg methods having a specified annotationType,
     * each wrapped with an {@link Evaluator} object.
     */
    public static <T extends Annotation> Stream<Evaluator> streamEvaluators(
            final @NonNull Class<?> cls,
            final @NonNull Predicate<AnnotatedElement> memberFilter,
            final @NonNull TypeHierarchyPolicy typeHierarchyPolicy,
            final @NonNull InterfacePolicy interfacePolicy) {

        return typeHierarchyPolicy.isIncludeTypeHierarchy()
                ? _Reflect
                    .streamTypeHierarchy(cls, interfacePolicy)
                    .flatMap(type->streamAnnotatedMemberEvaluators(type, memberFilter))
                : streamAnnotatedMemberEvaluators(cls, memberFilter);
    }

    // -- HELPER

    private static <T extends Annotation> Stream<Evaluator> streamAnnotatedMemberEvaluators(
            final Class<?> cls,
            final Predicate<AnnotatedElement> memberFilter) {

        val classCache = _ClassCache.getInstance();

        return Stream.concat(
                streamMethodEvaluators(cls, memberFilter, classCache),
                streamFieldEvaluators(cls, memberFilter, classCache));
    }

    private static Stream<Evaluator> streamMethodEvaluators(
            final Class<?> cls,
            final Predicate<AnnotatedElement> memberFilter,
            final _ClassCache classCache) {

        return classCache
        .streamDeclaredMethods(cls)
        .filter(MethodUtil::isNotStatic)
        .filter(MethodUtil::isNoArg)
        .filter(MethodUtil::isNotVoid)
        .map(method->memberFilter.test(method) ? new MethodEvaluator(cls, method) : null)
        .filter(_NullSafe::isPresent)
        .map(Evaluator.class::cast);
    }

    private static <T extends Annotation> Stream<Evaluator> streamFieldEvaluators(
            final Class<?> cls,
            final Predicate<AnnotatedElement> memberFilter,
            final _ClassCache classCache) {

        return classCache
        .streamDeclaredFields(cls)
        .map(field->memberFilter.test(field) ? new FieldEvaluator(cls, field) : null)
        .filter(_NullSafe::isPresent)
        .map(Evaluator.class::cast);
    }

    // -- EVALUATOR

    public static abstract class Evaluator {

        private MethodHandle mh;

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

    @RequiredArgsConstructor
    public static class MethodEvaluator extends Evaluator {

        @Getter private final Class<?> correspondingClass;
        @Getter private final Method method;

        @Override
        public String name() {
            return method.getName() + "()";
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return _Reflect.handleOf(method);
        }
    }

    @RequiredArgsConstructor
    public static class FieldEvaluator extends Evaluator {

        @Getter private final Class<?> correspondingClass;
        @Getter private final Field field;

        @Override
        public String name() {
            return field.getName();
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return _Reflect.handleOfGetterOn(field);
        }

        public Optional<Method> lookupGetter() {
            try {
                return Optional.ofNullable(
                        _Reflect.getGetter(correspondingClass, field.getName())    );
            } catch (IntrospectionException e) {
                log.warn("failed reflective introspection on {} field {}",
                        correspondingClass, field.getName(), e);
            }
            return Optional.empty();
        }

    }

}
