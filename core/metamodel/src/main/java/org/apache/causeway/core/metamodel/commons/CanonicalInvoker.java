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
package org.apache.causeway.core.metamodel.commons;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.reflection._Reflect;

import lombok.Builder;
import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * Utility for method invocation pre-processing.
 * <p>
 * For a given array of parameters, we intercept and adapt those,
 * that are not compatible with the expected target parameter type.
 * <p>
 * We do this for collection parameter types List, Set, SortedSet, Collection, Can, Arrays
 * missing arguments and primitives that are not initialized.
 */
@UtilityClass
public class CanonicalInvoker {

    @lombok.Value @Builder
    public static class ObjectConstructionRequest {
        final @NonNull Constructor<?> constructor;
        final @Nullable Object[] params;
        final @NonNull @Builder.Default ParameterAdapter parameterAdapter = ParameterAdapter.DEFAULT;
        public Object[] getAdaptedParameters() {
            return getParameterAdapter().adaptAll(getConstructor(), getParams());
        }
    }

    @lombok.Value @Builder
    public static class MethodInvocationRequest {
        final @NonNull Method method;
        final @NonNull Object targetPojo;
        final @Nullable Object[] params;
        final @NonNull @Builder.Default ParameterAdapter parameterAdapter = ParameterAdapter.DEFAULT;
        public Object[] getAdaptedParameters() {
            return getParameterAdapter().adaptAll(getMethod(), getParams());
        }
    }

    // -- CONSTRUCT

    public <T> T construct(final Constructor<T> constructor, final @Nullable Object[] executionParameters) {
        return _Casts.uncheckedCast(construct(ObjectConstructionRequest.builder()
                .constructor(constructor)
                .params(executionParameters)
                .build()));
    }

    public Object construct(final ObjectConstructionRequest constructionRequest) {
        val adaptedExecutionParameters = constructionRequest.getAdaptedParameters();

        // supports effective private constructors as well
        return _Reflect.invokeConstructor(constructionRequest.getConstructor(), adaptedExecutionParameters)
        .mapFailure(ex->toVerboseException(constructionRequest.getConstructor(), adaptedExecutionParameters, ex))
        .getValue().orElseThrow();
    }

    // -- INVOKE

    public void invokeAll(final Iterable<Method> methods, final Object object) {
        methods.forEach(method->invoke(method, object));
    }

    public Object invoke(
            final Method method,
            final Object targetPojo) {
        return invoke(MethodInvocationRequest.builder()
                .method(method)
                .targetPojo(targetPojo)
                .build());
    }

    public Object invoke(
            final Method method,
            final Object targetPojo,
            final @Nullable Object[] executionParameters) {
        return invoke(MethodInvocationRequest.builder()
                .method(method)
                .targetPojo(targetPojo)
                .params(executionParameters)
                .build());
    }

    public Object invoke(final MethodInvocationRequest invocationRequest) {

        val adaptedExecutionParameters = invocationRequest.getAdaptedParameters();

        // supports effective private methods as well
        return _Reflect.invokeMethodOn(
                invocationRequest.getMethod(),
                invocationRequest.getTargetPojo(),
                adaptedExecutionParameters)
        .mapFailure(ex->toVerboseException(
                invocationRequest.getMethod(),
                adaptedExecutionParameters,
                ex))
        .ifFailureFail()
        .getValue().orElse(null);
    }

    // -- HELPER

    private Throwable toVerboseException(
            final Executable executable,
            final Object[] adaptedExecutionParameters,
            final Throwable e) {

        final Class<?>[] parameterTypes = executable.getParameterTypes();
        final int expectedParamCount = _NullSafe.size(parameterTypes);
        final int actualParamCount = _NullSafe.size(adaptedExecutionParameters);
        if(expectedParamCount!=actualParamCount) {
            return new IllegalArgumentException(String.format(
                    "param-count mismatch: expected %d, got %d\n",
                    expectedParamCount, actualParamCount), e);
        }

        // if method or constructor was invoked with incompatible param types, then the Throwable
        // we receive here is of type IllegalArgumentException; in which case we can provide additional
        // information, but also at the expense of a potentially hiding the original cause, namely when the
        // IllegalArgumentException has a different origin and the param incompatibility check is a
        // false positive
        if(e instanceof IllegalArgumentException) {
            boolean paramTypeMismatchEncountered = false;
            val sb = new StringBuilder();
            for(int j=0;j<parameterTypes.length;++j) {
                final Class<?> parameterType = parameterTypes[j];
                val incompatible = !isValueCompatibleWithType(
                        _Arrays.get(adaptedExecutionParameters, j),
                        parameterTypes[j]);

                paramTypeMismatchEncountered = paramTypeMismatchEncountered
                        || incompatible;

                if(incompatible) {

                    final String parameterValueTypeLiteral = _Arrays.get(adaptedExecutionParameters, j)
                            .map(Object::getClass)
                            .map(Class::getName)
                            .orElse("missing or null");

                    final String expected = parameterType.getName();
                    final String actual = parameterValueTypeLiteral;

                    sb.append(String.format("param-type[%d]: expected '%s', got '%s'\n",
                        j, expected, actual));
                }
            }
            if(paramTypeMismatchEncountered) {
                sb.insert(0, String.format("expected param type mismatch in %s\n", executable));
                return new IllegalArgumentException(sb.toString(), e);
            }
        }

        return ThrowableExtensions.handleInvocationException(e, executable.getName());
    }

    private boolean isValueCompatibleWithType(
            final @NonNull Optional<Object> value,
            final @NonNull Class<?> type) {

        if(!value.isPresent()) {
            // null is not compatible with an expected primitive type
            // but null is compatible with any other expected type
            return !type.isPrimitive();
        }

        val runtimeType = value.get().getClass();

        if(ClassExtensions.equalsWhenBoxing(runtimeType, type)) {
            return true;
        }

        return type.isAssignableFrom(runtimeType);
    }

}
