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
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

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
            return _GenericResolver.mostSpecific(this, other);
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
        Class<?>[] paramTypes();
        default int paramCount() {
            return constructor().getParameterCount();
        }
        default Class<?> paramType(final int paramIndex) {
            return paramTypes()[paramIndex];
        }
        default boolean isNoArg() { return paramCount()==0; }
        default boolean isSingleArg() { return paramCount()==1; }
    }

    // -- FACTORIES

    public Optional<ResolvedMethod> resolveMethod(
            final @NonNull Method method,
            final @NonNull Class<?> implementationClass) {
        return new SimpleResolvedMethod(method, implementationClass)
                .guardAgainstCannotResolve();
    }

    public ResolvedConstructor resolveConstructor(
            final @NonNull Constructor<?> constructor,
            final @NonNull Class<?> implementationClass) {
        return new SimpleResolvedConstructor(constructor, implementationClass);
    }

    /**
     * JUnit
     */
    @UtilityClass
    public static class testing {
        @SneakyThrows
        public ResolvedMethod resolveMethod(
                final @NonNull Class<?> implementationClass,
                final @NonNull String methodName,
                final Class<?>... parameterTypes) {

            val candidate = _ClassCache.getInstance().findMethodUniquelyByNameOrFail(implementationClass, methodName);
            val paramTypesFound = Can.ofArray(candidate.paramTypes());
            val paramTypesRequested = Can.ofArray(parameterTypes);
            _Assert.assertEquals(paramTypesFound, paramTypesRequested);
            return candidate;

//            return _GenericResolver.resolveMethod(implementationClass.getMethod(methodName, parameterTypes), implementationClass)
//                    .orElseThrow(()->new NoSuchMethodException(String.format("%s#%s(%s)", implementationClass, methodName,
//                            paramTypesRequested.stream().map(Class::getSimpleName).collect(Collectors.joining(",")))));
        }
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
        private final boolean isResolved;

        @EqualsAndHashCode.Exclude
        private Try<MethodHandle> methodHandle;

        public SimpleResolvedMethod(final Method method, final Class<?> implementationClass) {
            this.method = method;
            this.implementationClass = implementationClass;
            this.paramTypes = _GenericResolver.resolveParameterTypes(method, implementationClass);
            this.returnType = GenericTypeResolver.resolveReturnType(method, implementationClass);
            this.isResolved = isReturnTypeResolved()
                    && areParamsResolved();
        }
        public Optional<ResolvedMethod> guardAgainstCannotResolve() {
            return isResolved ? Optional.of(this) : Optional.empty();
        }
        //TODO[CAUSEWAY-3571] - does not work because the lookup is denied when the implementationClass is non public
        @Override
        public Try<MethodHandle> methodHandle() {
            return methodHandle != null
                    ? methodHandle
                    : (this.methodHandle = Try.call(()->MethodHandles.lookup().unreflect(method)));
        }
        @Override
        public String toString() {
            return String.format("ResolvedMethod[%s#%s(%s)]", implementationClass.getName(), name(),
                    Can.ofArray(paramTypes).stream()
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(",")));
        }
        //-- HELPER
        private boolean areParamsResolved() {
            if(isNoArg()) return true; // skip check
            final Type[] genericParameterTypes = method.getGenericParameterTypes();
            for(int i=0; i<method.getParameterCount(); ++i) {
                if((genericParameterTypes[i] instanceof TypeVariable<?>)
                        && paramTypes[i].equals(Object.class)) {
                    return false;
                }
            }
            return true;
        }
        private boolean isReturnTypeResolved() {
            if(!_Reflect.hasGenericReturn(method)) return true; // skip check
            return !returnType.equals(Object.class);
        }
    }

    @EqualsAndHashCode
    @Getter @Accessors(fluent=true)
    private static class SimpleResolvedConstructor implements ResolvedConstructor {

        private final Constructor<?> constructor;
        private final Class<?> implementationClass;

        @EqualsAndHashCode.Exclude
        private final Class<?>[] paramTypes;

        public SimpleResolvedConstructor(final Constructor<?> constructor, final Class<?> implementationClass) {
            this.constructor = constructor;
            this.implementationClass = implementationClass;
            this.paramTypes = _GenericResolver.resolveParameterTypes(constructor, implementationClass);
        }

        @Override
        public String toString() {
            return String.format("ResolvedConstructor[%s(%s)]", implementationClass.getName(),
                    Can.ofArray(paramTypes).stream()
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(",")));
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
     *
     */
    private ResolvedMethod mostSpecific(final ResolvedMethod a, final ResolvedMethod b) {
        if(a.equals(b)) return b; // an arbitrary pick

        // if declared types are different chose the mostSpecific type
        val implType = _Reflect.mostSpecificType(a.implementationClass(), b.implementationClass());

        val m = ClassUtils.getMostSpecificMethod(a.method(), implType);
        if(a.method().equals(m)) {
            return a;
        }
        if(b.method().equals(m)) {
            return b;
        }
        return _GenericResolver.resolveMethod(m, implType)
                .orElseThrow(()->_Exceptions.illegalArgument("most specific method\n"
                        + "%s is not resolvable while deciding for methods\n"
                        + "%s or\n"
                        + "%s",
                        m, a.method(), b.method()));
    }

}
