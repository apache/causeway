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
package org.apache.causeway.commons.internal.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;

import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * <p>
 * Provides a layer of abstraction on top of the {@link Method} type (Java reflection API).
 * @since 2.0
 */
@UtilityClass
public class _MethodFacades {

    public MethodFacade paramsAsTuple(
            final @NonNull ResolvedMethod method,
            final @NonNull ResolvedConstructor patConstructor) {
        _Reflect.guardAgainstSynthetic(method.method());
        return new ParamsAsTupleMethod(patConstructor, method);
    }

    /**
     * use with care:
     * <ul>
     * <li>property accessors</li>
     * <li>collections accessors</li>
     * <li>support methods</li>
     * <li>JUnit testing</li>
     * </ul>
     */
    public MethodFacade regular(
            final @NonNull ResolvedMethod method) {
        _Reflect.guardAgainstSynthetic(method.method());
        return new RegularMethod(method);
    }

    /**
     * JUnit
     */
    @UtilityClass
    public static class testing {
        public MethodFacade regular(
                final @NonNull Method method) {
            return _MethodFacades.regular(_GenericResolver.resolveMethod(method, method.getDeclaringClass())
                    .orElseThrow());
        }
    }

    public static interface MethodFacade {

        Class<?>[] getParameterTypes();
        Class<?> getParameterType(int paramNum);
        int getParameterCount();
        Class<?> getReturnType();
        String getName();
        String getParameterName(int paramNum);
        Class<?> getDeclaringClass();
        Optional<ResolvedMethod> asMethod();

        /**
         * exposes the underlying method, use with care:
         * <ul>
         * <li>to get the method return type</li>
         * <li>for annotation inspection on the method (as annotated element) - don't use for param annotation processing</li>
         * <li>for the method-remover</li>
         * <li>for invocation</li>
         * </ul>
         */
        ResolvedMethod asMethodForIntrospection();

        /**
         * This is a convenience method for scenarios where a Method or Constructor reference is treated in a generic fashion.
         * Used to introspect parameter annotations.
         */
        Executable asExecutable();

        Object[] getArguments(Object[] executionParameters);

        <A extends Annotation> Optional<A> synthesize(final Class<A> annotationType);
        <A extends Annotation> Optional<A> synthesizeOnParameter(final Class<A> annotationType, int paramNum);

        default ResolvedMethod asMethodElseFail() {
            return asMethod().orElseThrow(()->
                _Exceptions.unrecoverable("Framework Bug: unexpeced method-facade, "
                        + "regular variant expected: %s", asMethodForIntrospection()));
        }
        boolean isAnnotatedAsNullable();
    }

    /**
     * Wraps a {@link Method}, implemented as a transparent pass through.
     */
    @lombok.Value
    private final static class RegularMethod implements MethodFacade {

        private final ResolvedMethod method;

        @Override public Class<?> getDeclaringClass() {
            return method.method().getDeclaringClass();
        }
        @Override public int getParameterCount() {
            return method.paramCount();
        }
        @Override public Class<?>[] getParameterTypes() {
            return method.paramTypes();
        }
        @Override public Class<?> getParameterType(final int paramNum) {
            return method.paramType(paramNum);
        }
        @Override public String getName() {
            return method.name();
        }
        @Override public Class<?> getReturnType() {
            return method.returnType();
        }
        @Override public Optional<ResolvedMethod> asMethod() {
            return Optional.of(method);
        }
        @Override public Executable asExecutable() {
            return method.method();
        }
        @Override public <A extends Annotation> Optional<A> synthesize(final Class<A> annotationType) {
            return _Annotations.synthesize(method.method(), annotationType);
        }
        @Override public ResolvedMethod asMethodForIntrospection() {
            return method;
        }
        @Override public String getParameterName(final int paramNum) {
            return method.method().getParameters()[paramNum].getName();
        }
        @Override public <A extends Annotation> Optional<A> synthesizeOnParameter(
                final Class<A> annotationType, final int paramNum) {
            return _Annotations.synthesize(method.method().getParameters()[paramNum], annotationType);
        }
        @Override public Object[] getArguments(final Object[] executionParameters) {
            return executionParameters;
        }
        @Override public boolean isAnnotatedAsNullable() {
            return _NullSafe.stream(method.method().getAnnotations())
                    .map(annot->annot.annotationType().getSimpleName())
                    .anyMatch(name->name.equals("Nullable"));
        }
        @Override public String toString() {
            return method.method().toString();
        }
    }


    @lombok.Value
    private final static class ParamsAsTupleMethod implements MethodFacade {

        private final ResolvedConstructor patConstructor;
        private final ResolvedMethod method;

        @Override public Class<?>[] getParameterTypes() {
            return patConstructor.paramTypes();
        }
        @Override public Class<?> getParameterType(final int paramNum) {
            return patConstructor.paramType(paramNum);
        }
        @Override public int getParameterCount() {
            return patConstructor.paramCount();
        }
        @Override public Class<?> getReturnType() {
            return method.returnType();
        }
        @Override public String getName() {
            return method.name();
        }
        @Override public String getParameterName(final int paramNum) {
            return patConstructor.constructor().getParameters()[paramNum].getName();
        }
        @Override public Class<?> getDeclaringClass() {
            return method.method().getDeclaringClass();
        }
        @Override public Optional<ResolvedMethod> asMethod() {
            // only allowed for regular methods
            return Optional.empty();
        }
        @Override public ResolvedMethod asMethodForIntrospection() {
            return method;
        }
        @Override public Executable asExecutable() {
            return patConstructor.constructor();
        }
        @Override @SneakyThrows
        public Object[] getArguments(final Object[] executionParameters) {
            // converts input args into a single arg tuple type (PAT semantics)
            return new Object[] {patConstructor.constructor().newInstance(executionParameters)};
        }
        @Override public <A extends Annotation> Optional<A> synthesize(final Class<A> annotationType) {
            return _Annotations.synthesize(method.method(), annotationType);
        }
        @Override public <A extends Annotation> Optional<A> synthesizeOnParameter(final Class<A> annotationType, final int paramNum) {
            return _Annotations.synthesize(patConstructor.constructor().getParameters()[paramNum], annotationType);
        }
        @Override public boolean isAnnotatedAsNullable() {
            return _NullSafe.stream(method.method().getAnnotations())
                    .map(annot->annot.annotationType().getSimpleName())
                    .anyMatch(name->name.equals("Nullable"));
        }
        @Override public String toString() {
            return method.method().toString();
        }
    }

}
