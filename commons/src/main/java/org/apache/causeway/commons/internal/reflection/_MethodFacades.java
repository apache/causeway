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
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedType;

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

    /**
     * Invocation helper, that converts a given object to a required type.
     */
    @FunctionalInterface
    public interface ParameterConverter {
        /**
         * Replaces {@code parameterValue} (if required) to be conform with the {@code parameterType}.
         */
        <T> T convert(final Class<T> parameterType, Object parameterValue);
        
        // -- UTILITY

        default Object[] convertAll(
                final @NonNull Executable executable,
                final @Nullable Object[] executionParameters) {
            final int paramCount = executable.getParameterCount();
            if(paramCount==0) {
                return _Constants.emptyObjects;
            }
            var parameterTypes = executable.getParameterTypes();
            var adaptedExecutionParameters = new Object[paramCount];
            for(int i=0; i<paramCount; ++i) {
                var origParam = _Arrays.get(executionParameters, i).orElse(null);
                adaptedExecutionParameters[i] = convert(parameterTypes[i], origParam);
            }
            return adaptedExecutionParameters;
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
        Optional<ResolvedConstructor> asConstructor();

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

        Object[] getArguments(Object[] executionParameters, ParameterConverter converter);

        <A extends Annotation> Optional<A> synthesize(final Class<A> annotationType);
        <A extends Annotation> Optional<A> synthesizeOnParameter(final Class<A> annotationType, int paramNum);

        default ResolvedMethod asMethodElseFail() {
            return asMethod().orElseThrow(()->
                _Exceptions.unrecoverable("Framework Bug: unexpeced method-facade, "
                        + "regular variant expected: %s", asMethodForIntrospection()));
        }
        default ResolvedConstructor asConstructorElseFail() {
            return asConstructor().orElseThrow(()->
                _Exceptions.unrecoverable("Framework Bug: unexpeced method-facade, "
                        + "wrapper of constructor expected: %s", asMethodForIntrospection()));
        }
        boolean isAnnotatedAsNullable();

        default ResolvedType resolveMethodReturn() {
            return _GenericResolver.forMethodReturn(this.asMethodForIntrospection());
        }

        default ResolvedType resolveParameter(final int paramIndex) {
            var executable = this.asExecutable();
            if(executable instanceof Method) {
                return _GenericResolver.forMethodParameter(this.asMethodForIntrospection(), paramIndex);
            }
            if(executable instanceof Constructor) {
                return _GenericResolver.forConstructorParameter(this.asConstructorElseFail(), paramIndex);
            }
            throw _Exceptions.unexpectedCodeReach();
        }
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
        @Override public Optional<ResolvedConstructor> asConstructor() {
            return Optional.empty();
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
            // don't replace with ... method.paramType(paramNum).getName();
            return method.method().getParameters()[paramNum].getName();
        }
        @Override public <A extends Annotation> Optional<A> synthesizeOnParameter(
                final Class<A> annotationType, final int paramNum) {
            return _Annotations.synthesize(method.method().getParameters()[paramNum], annotationType);
        }
        @Override public Object[] getArguments(final Object[] executionParameters, ParameterConverter converter) {
            return converter.convertAll(method.method(), executionParameters);
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
            // don't replace with ... patConstructor.paramType(paramNum).getName();
            return patConstructor.constructor().getParameters()[paramNum].getName();
        }
        @Override public Class<?> getDeclaringClass() {
            return method.method().getDeclaringClass();
        }
        @Override public Optional<ResolvedMethod> asMethod() {
            // only allowed for regular methods
            return Optional.empty();
        }
        @Override public Optional<ResolvedConstructor> asConstructor() {
            return Optional.of(patConstructor);
        }
        @Override public ResolvedMethod asMethodForIntrospection() {
            return method;
        }
        @Override public Executable asExecutable() {
            return patConstructor.constructor();
        }
        @Override @SneakyThrows
        public Object[] getArguments(final Object[] executionParameters, ParameterConverter converter) {
            var convertedArgs = converter.convertAll(patConstructor.constructor(), executionParameters);
            // converts input args into a single arg tuple type (PAT semantics)
            return new Object[] {patConstructor.constructor().newInstance(convertedArgs)};
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
