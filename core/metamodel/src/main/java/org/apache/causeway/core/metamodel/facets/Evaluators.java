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
package org.apache.causeway.core.metamodel.facets;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.causeway.applib.exceptions.unrecoverable.MetaModelException;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.commons.internal.reflection._Reflect.InterfacePolicy;
import org.apache.causeway.commons.internal.reflection._Reflect.TypeHierarchyPolicy;
import org.apache.causeway.core.metamodel.commons.MethodUtil;
import org.apache.causeway.core.metamodel.commons.ThrowableExtensions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

//@Log4j2
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

        // if it so happens (eg. lombok) that both the field and its getter are annotated,
        // don't duplicate, let the method win

        val methodEvaluators = streamMethodEvaluators(cls, memberFilter, classCache)
                .collect(Can.toCan());
        val fieldEvaluators = streamFieldEvaluators(cls, memberFilter, classCache)
                .filter(fieldEvaluator->!fieldEvaluator.isSameAsAnyOf(methodEvaluators));

        return Stream.concat(
                methodEvaluators.stream(),
                fieldEvaluators);
    }

    private static Stream<MethodEvaluator> streamMethodEvaluators(
            final Class<?> cls,
            final Predicate<AnnotatedElement> memberFilter,
            final _ClassCache classCache) {

        return classCache
        .streamDeclaredMethods(cls)
        .filter(MethodUtil::isNotStatic)
        .filter(MethodUtil::isNoArg)
        .filter(MethodUtil::isNotVoid)
        .map(method->memberFilter.test(method)
                ? new MethodEvaluator(cls, method)
                : null)
        .filter(_NullSafe::isPresent);
    }

    private static <T extends Annotation> Stream<FieldEvaluator> streamFieldEvaluators(
            final Class<?> cls,
            final Predicate<AnnotatedElement> memberFilter,
            final _ClassCache classCache) {

        return classCache
        .streamDeclaredFields(cls)
        .map(field->memberFilter.test(field)
                ? new FieldEvaluator(cls, field, classCache.getterForField(cls, field))
                : null)
        .filter(_NullSafe::isPresent);
    }

    // -- EVALUATOR

    public static interface Evaluator {
        String name();
        Object value(Object obj);
    }

    private static abstract class EvaluatorAbstract
    implements Evaluator {

        @Getter(lazy = true, value = AccessLevel.PRIVATE)
        private final Try<MethodHandle> methodHandleRef = Try.call(this::createMethodHandle);

        protected abstract MethodHandle createMethodHandle() throws IllegalAccessException;

        @SneakyThrows
        @Override
        public Object value(final Object obj) {

            val mh = getMethodHandleRef()
            .mapFailure(this::failure)
            .ifFailureFail()
            .getValue()
            .orElseThrow(this::failure);

            try {
                return mh.invoke(obj);
            } catch (Throwable e) {
                throw ThrowableExtensions.handleInvocationException(e, name());
            }

        }

        // -- HELPER

        private String failureMessage() {
            return "failed to create a method handle for " + name();
        }

        private MetaModelException failure(final Throwable cause) {
            return new MetaModelException(failureMessage(), cause);
        }

        private MetaModelException failure() {
            return new MetaModelException(failureMessage());
        }

    }

    @RequiredArgsConstructor
    public static class MethodEvaluator
    extends EvaluatorAbstract {

        @Getter private final Class<?> correspondingClass;
        @Getter private final Method method;

        @Override
        public String name() {
            return method.getName() + "()";
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            return MethodHandles.lookup().unreflect(method);
        }
    }

    @RequiredArgsConstructor
    public static class FieldEvaluator
    extends EvaluatorAbstract {

        @Getter private final Class<?> correspondingClass;
        @Getter private final Field field;
        @Getter private final Optional<Method> correspondingGetter;

        @Override
        public String name() {
            return field.getName();
        }

        @Override
        protected MethodHandle createMethodHandle() throws IllegalAccessException {
            val getter = correspondingGetter.orElse(null);
            return getter!=null
                    ? MethodHandles.lookup().unreflect(getter)
                    : MethodHandles.lookup().unreflectGetter(field);
        }

        private boolean isSameAs(final MethodEvaluator methodEvaluator) {
            return correspondingGetter
            .map(getter->_Reflect.methodsSame(getter, methodEvaluator.getMethod()))
            .orElse(false);
        }

        private boolean isSameAsAnyOf(final Can<MethodEvaluator> methodEvaluators) {
            return methodEvaluators.stream().anyMatch(this::isSameAs);
        }

    }

}
