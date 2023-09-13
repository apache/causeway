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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * @since 2.0
 */
@UtilityClass
public class _GenericResolver {

    // -- MODELS

    public static interface ResolvedMethod {
        Method method();
        Try<MethodHandle> methodHandle();
        Class<?> implementationClass();
        Class<?> returnType();
        Class<?>[] paramTypes();
        default String name() {
            return method().getName();
        }
        default int paramCount() {
            return method().getParameterCount();
        }
        default Class<?> paramType(final int paramIndex) {
            return paramTypes()[paramIndex];
        }
        default ResolvedMethod mostSpecific(final ResolvedMethod other) {
            return methodsWhichIsOverridingTheOther(this, other);
        }
        default boolean isStatic() {
            return Modifier.isStatic(method().getModifiers());
        }
        @Deprecated //TODO[CAUSEWAY-3571] don't bypass programming model constants
        default boolean isGetter() {
            return _Reflect.Filter.isGetter(method());
        }
        default boolean isNoArg() { return paramCount()==0; }
        default boolean isSingleArg() { return paramCount()==1; }
        default boolean isReturnTypeATypeOf(final Class<?> typeOf) {
            return typeOf.isAssignableFrom(returnType());
        }
        default boolean isReturnTypeAnyTypeOf(final Can<Class<?>> allowedReturnTypes) {
            return allowedReturnTypes.stream()
                    .anyMatch(this::isReturnTypeATypeOf);
        }
        /**
         * In compliance with the sameness relation {@link _Reflect#methodsSame(Method, Method)}
         * provides a comparator (with an arbitrarily chosen ordering relation).
         * @apiNote don't depend on the chosen ordering
         * @see _Reflect#methodsSame(Method, Method)
         */
        public static int methodWeakCompare(final ResolvedMethod a, final ResolvedMethod b) {
            return _Reflect.methodWeakCompare(a.method(), b.method());
        }
    }

    public static interface ResolvedConstructor {
        Constructor<?> constructor();
        Class<?> implementationClass();
        Class<?>[] parameterTypes();
    }

    // -- FACTORIES

    public ResolvedMethod resolveMethod(
            final @NonNull Method method,
            final @NonNull Class<?> implementationClass) {
        return new SimpleResolvedMethod(method, implementationClass);
    }

    /**
     * JUnit
     */
    @SneakyThrows
    public ResolvedMethod resolveMethod(
            final @NonNull Class<?> implementationClass,
            final @NonNull String methodName) {
        return resolveMethod(implementationClass.getMethod(methodName), implementationClass);
    }


    public ResolvedConstructor resolveConstructor(
            final @NonNull Constructor<?> constructor,
            final @NonNull Class<?> implementationClass) {
        return new SimpleResolvedConstructor(constructor, implementationClass);
    }

    // -- IMPLEMENTATIONS

    @EqualsAndHashCode
    @Getter @Accessors(fluent=true)
    private static class SimpleResolvedMethod implements ResolvedMethod {

        private final Method method;
        private final Class<?> implementationClass;

        @EqualsAndHashCode.Exclude
        private final Class<?>[] paramTypes;
        @EqualsAndHashCode.Exclude
        private final Class<?> returnType;

        @EqualsAndHashCode.Exclude
        private Try<MethodHandle> methodHandle;

        public SimpleResolvedMethod(final Method method, final Class<?> implementationClass) {
            this.method = method;
            this.implementationClass = implementationClass;
            this.paramTypes = _GenericResolver.resolveParameterTypes(method, implementationClass);
            this.returnType = GenericTypeResolver.resolveReturnType(method, implementationClass);
        }
        //TODO[CAUSEWAY-3571] - does not work because the lookup is denied when the implementationClass is non public
        @Override
        public Try<MethodHandle> methodHandle() {
            return methodHandle != null
                    ? methodHandle
                    : (this.methodHandle = Try.call(()->MethodHandles.lookup().unreflect(method)));
        }
    }

    @Getter @Accessors(fluent=true)
    private static class SimpleResolvedConstructor implements ResolvedConstructor {

        private final Constructor<?> constructor;
        private final Class<?> implementationClass;
        private final Class<?>[] parameterTypes;

        public SimpleResolvedConstructor(final Constructor<?> constructor, final Class<?> implementationClass) {
            this.constructor = constructor;
            this.implementationClass = implementationClass;
            this.parameterTypes = _GenericResolver.resolveParameterTypes(constructor, implementationClass);
        }
    }

    // -- HELPER

    @SuppressWarnings("deprecation") // proposed alternative is not publicly visible
    private Class<?>[] resolveParameterTypes(final Executable executable, final Class<?> implementationClass) {
        final var array = new Class<?>[executable.getParameterCount()];
        for (int i = 0; i < array.length; i++) {
            array[i] = GenericTypeResolver.resolveParameterType(methodParameter(executable, i), implementationClass);
        }
        return array;
    }

    private MethodParameter methodParameter(final Executable executable, final int paramIndex) {
        return (executable instanceof Method)
                ? new MethodParameter((Method) executable, paramIndex)
                : new MethodParameter((Constructor<?>) executable, paramIndex);
    }

    /**
     * If a and b are related, such that one overrides the other,
     * that one which is overriding the other is returned.
     * @implNote if both declaring type and return type are the same we (arbitrarily) return b
     */
    private ResolvedMethod methodsWhichIsOverridingTheOther(final ResolvedMethod a, final ResolvedMethod b) {
        val aType = a.method().getDeclaringClass();
        val bType = b.method().getDeclaringClass();
        if(aType.equals(bType)) {
            val aReturn = a.returnType();
            val bReturn = b.returnType();
            if(aReturn.equals(bReturn)) {
                // if a and b are not equal, this code path is expected unreachable
                return b;
            }
            return aReturn.isAssignableFrom(bReturn)
                    ? b
                    : a;
        }
        return aType.isAssignableFrom(bType)
                ? b
                : a;
    }

}
