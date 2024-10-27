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

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.semantics.CollectionSemantics;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
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

    /**
     * Represents either a singular or plural type, with generic type arguments (if any) resolved.
     */
    public static interface ResolvedType {
        /**
         * The type either contained or not.
         */
        @NonNull Class<?> elementType();

        /**
         * Optionally the container type, the {@link #elementType()} is contained in,
         * such as {@link List}, {@link Collection}, etc.
         */
        @NonNull Optional<Class<?>> containerType();
        @NonNull Optional<CollectionSemantics> collectionSemantics();

        default boolean isSingular() { return containerType().isEmpty(); }
        default boolean isPlural() { return containerType().isPresent(); }
        default boolean isArray() { return containerType().map(Class::isArray).orElse(false); }
        /**
         * Always <code>true</code> for <i>scalar</i> or <i>array</i>.
         * Otherwise, whether {@link #containerType()} exactly matches
         * the container type from {@link #collectionSemantics()}.
         */
        default boolean isSupportedForActionParameter() {
            return isSingular()
                    || isArray()
                    ? true
                    : Objects.equals(
                            containerType().orElse(null),
                            collectionSemantics().map(CollectionSemantics::getContainerType).orElse(null));
        }

        // -- WITHERS

        default ResolvedType withElementType(final @NonNull Class<?> elementType) {
            return new SimpleTypeOfAnyCardinality(assertSingular(elementType), this.containerType(), this.collectionSemantics());
        }

        // -- FACTORIES

        public static ResolvedType singular(final @NonNull Class<?> singularType) {
            return new SimpleTypeOfAnyCardinality(assertSingular(singularType), Optional.empty(), Optional.empty());
        }
        public static ResolvedType plural(
                final @NonNull Class<?> elementType,
                final @NonNull Class<?> pluralType,
                final @NonNull CollectionSemantics collectionSemantics) {
            if(CollectionSemantics.valueOf(elementType).isPresent()) {
                System.err.printf("nested plural detected %s: will fail later%n", elementType);
            }
            return new SimpleTypeOfAnyCardinality(assertSingular(elementType),
                    Optional.of(assertPlural(pluralType)),
                    Optional.of(collectionSemantics));
        }

        // -- HELPER

        private static Class<?> assertSingular(final @NonNull Class<?> singularType) {
            _Assert.assertEquals(
                    Optional.empty(),
                    CollectionSemantics.valueOf(singularType),
                    ()->String.format("%s should not match any supported plural (collection) types", singularType));
            return singularType;
        }
        private static Class<?> assertPlural(final @NonNull Class<?> pluralType) {
            _Assert.assertTrue(
                    CollectionSemantics.valueOf(pluralType).isPresent(),
                    ()->String.format("%s should match a supported plural (collection) type", pluralType));
            return pluralType;
        }
    }

    /**
     * Represents a {@link Method} that has its generic bounded
     * return type (if any)
     * and parameter types (if any) resolved.
     */
    public static interface ResolvedMethod {
        Method method();
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
        default boolean isNoArg() { return paramCount()==0; }
        default boolean isSingleArg() { return paramCount()==1; }
        default boolean isReturnTypeATypeOf(final Class<?> typeOf) {
            return typeOf.isAssignableFrom(returnType());
        }
        default boolean isReturnTypeAnyTypeOf(final Can<Class<?>> allowedReturnTypes) {
            return allowedReturnTypes.stream()
                    .anyMatch(this::isReturnTypeATypeOf);
        }
        Class<?> resolveFirstGenericTypeArgumentOnParameter(int paramIndex);
        Class<?> resolveFirstGenericTypeArgumentOnMethodReturn();
        /**
         * Is NOT compliant with the weak-sameness relation
         * {@link ResolvedMethod#methodsWeaklySame(ResolvedMethod, ResolvedMethod)}.
         * Provides a comparator (with an arbitrarily chosen ordering relation) sensitive to any method differences.
         * @apiNote don't depend on the chosen ordering
         */
        public static int methodCompare(final ResolvedMethod a, final ResolvedMethod b) {
            if(a.method().equals(b.method())
                    && a.implementationClass().equals(b.implementationClass()) ) {
                return 0; // exact match
            }
            int c = a.name().compareTo(b.name());
            if(c!=0) {
                return c;
            }
            c = Integer.compare(a.paramCount(), b.paramCount());
            if(c!=0) {
                return c;
            }
            var paramsA = a.paramTypes();
            var paramsB = b.paramTypes();
            for(int i=0; i<a.paramCount(); ++i) {
                c = _Reflect.typesCompare(paramsA[i], paramsB[i]);
                if(c!=0) {
                    return c;
                }
            }
            c = _Reflect.typesCompare(a.returnType(), b.returnType());
            if(c!=0) {
                return _Reflect.shareSameTypeHierarchy(a.returnType(), b.returnType())
                        ? 0 // same
                        : c;
            }
            return 0; // equal
        }
        /**
         * Two {@link Method}s, while one overrides the other, might have different method-keys.
         * We want the cache to only keep the most specific of those methods.
         */
        public static boolean methodsWeaklySame(final ResolvedMethod a, final ResolvedMethod b) {
            if(a.method().equals(b.method())
                    && a.implementationClass().equals(b.implementationClass()) ) {
                return true; // exact match
            }
            return a.name().equals(b.name())
                && _Reflect.methodSignatureWeaklyMatch(a.paramTypes(), b.paramTypes());
        }
    }

    /**
     * Represents a {@link Constructor} that has its generic bounded
     * parameter types (if any) resolved.
     */
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
        Class<?> resolveFirstGenericTypeArgumentOnParameter(int paramIndex);
    }

    // -- FACTORIES

    /**
     * Resolves a type directly, if there is no {@link Method} to use as its context.
     */
    public ResolvedType forPluralType(
            final @NonNull Class<?> pluralType,
            final @NonNull CollectionSemantics collectionSemantics) {
        var resolvablePluralType = ResolvableType.forClass(pluralType);
        var genericTypeArg = resolvablePluralType.isArray()
                ? resolvablePluralType.getComponentType()
                : resolvablePluralType.getGeneric(0);
        return ResolvedType.plural(
                genericTypeArg.toClass(),
                pluralType,
                collectionSemantics);
    }

    /**
     * Resolves a constructor's parameter type.
     */
    public ResolvedType forConstructorParameter(final ResolvedConstructor resolvedConstructor, final int paramIndex) {
        var paramType = resolvedConstructor.paramType(paramIndex);
        return CollectionSemantics.valueOf(paramType)
            .map(collectionSemantics->
                ResolvedType.plural(
                        resolvedConstructor.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->ResolvedType.singular(paramType));
    }

    /**
     * Resolves a method's return type.
     */
    public ResolvedType forMethodReturn(final ResolvedMethod resolvedMethod) {
        var methodReturn = resolvedMethod.returnType();
        return CollectionSemantics.valueOf(methodReturn)
            .map(collectionSemantics->
                ResolvedType.plural(
                        resolvedMethod.resolveFirstGenericTypeArgumentOnMethodReturn(),
                        methodReturn,
                        collectionSemantics)
            )
            .orElseGet(()->ResolvedType.singular(methodReturn));
    }

    /**
     * Resolves a method's parameter type.
     */
    public ResolvedType forMethodParameter(final ResolvedMethod resolvedMethod, final int paramIndex) {
        var paramType = resolvedMethod.paramType(paramIndex);
        return CollectionSemantics.valueOf(paramType)
            .map(collectionSemantics->
                ResolvedType.plural(
                        resolvedMethod.resolveFirstGenericTypeArgumentOnParameter(paramIndex),
                        paramType,
                        collectionSemantics)
            )
            .orElseGet(()->ResolvedType.singular(paramType));
    }

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

    // -- IMPLEMENTATIONS

    @lombok.Value @Accessors(fluent=true)
    private static class SimpleTypeOfAnyCardinality implements ResolvedType {
        @Getter(onMethod_={@Override})
        private final @NonNull Class<?> elementType;
        @Getter(onMethod_={@Override})
        private final @NonNull Optional<Class<?>> containerType;
        @Getter(onMethod_={@Override})
        private final @NonNull Optional<CollectionSemantics> collectionSemantics;
    }

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
        @Override
        public Class<?> resolveFirstGenericTypeArgumentOnMethodReturn() {
            return genericTypeArg(ResolvableType.forMethodReturnType(method, implementationClass))
                    .toClass();
        }
        @Override
        public Class<?> resolveFirstGenericTypeArgumentOnParameter(final int paramIndex) {
            return genericTypeArg(ResolvableType.forMethodParameter(method, paramIndex, implementationClass))
                    .toClass();
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
//        private Try<SimpleResolvedMethod> adopt(final @NonNull ClassLoader classLoader) {
//            return Try.call(()->{
//                var ownerReloaded = Class.forName(implementationClass.getName(), true, classLoader);
//                var methodReloaded = ownerReloaded.getMethod(method.getName(), method.getParameterTypes());
//                return new SimpleResolvedMethod(methodReloaded, ownerReloaded);
//            });
//        }
//        /**[CAUSEWAY-3164] ensures reflection on generic type arguments works in a concurrent introspection setting*/
//        private Try<SimpleResolvedMethod> adoptIntoDefaultClassLoader() {
//            return adopt(_Context.getDefaultClassLoader());
//        }
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
        public Class<?> resolveFirstGenericTypeArgumentOnParameter(final int paramIndex) {
            return genericTypeArg(ResolvableType.forConstructorParameter(constructor, paramIndex, implementationClass))
                    .toClass();
        }
        @Override
        public String toString() {
            return String.format("ResolvedConstructor[%s(%s)]", implementationClass.getName(),
                    Can.ofArray(paramTypes).stream()
                        .map(Class::getSimpleName)
                        .collect(Collectors.joining(",")));
        }
        // -- HELPER
//        private Try<SimpleResolvedConstructor> adopt(final @NonNull ClassLoader classLoader) {
//            return Try.call(()->{
//                var ownerReloaded = Class.forName(implementationClass.getName(), true, classLoader);
//                var methodReloaded = ownerReloaded.getConstructor(constructor.getParameterTypes());
//                return new SimpleResolvedConstructor(methodReloaded, ownerReloaded);
//            });
//        }
//        /**[CAUSEWAY-3164] ensures reflection on generic type arguments works in a concurrent introspection setting*/
//        private Try<SimpleResolvedConstructor> adoptIntoDefaultClassLoader() {
//            return adopt(_Context.getDefaultClassLoader());
//        }
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
        var implType = _Reflect.mostSpecificType(a.implementationClass(), b.implementationClass());

        var m = BridgeMethodResolver.findBridgedMethod(
                ClassUtils.getMostSpecificMethod(a.method(), implType));
        if(m.isBridge()) {
            throw _Exceptions.unexpectedCodeReach();
        }
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

    private ResolvableType genericTypeArg(final ResolvableType pluralType){
        var genericTypeArg = pluralType.isArray()
                ? pluralType.getComponentType()
                : pluralType.getGeneric(0);
        return genericTypeArg;
    }

    // -- TESTING

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

            var candidate = _ClassCache.getInstance().findMethodUniquelyByNameOrFail(implementationClass, methodName);
            var paramTypesFound = Can.ofArray(candidate.paramTypes());
            var paramTypesRequested = Can.ofArray(parameterTypes);
            _Assert.assertEquals(paramTypesFound, paramTypesRequested);
            return candidate;
        }
    }

}
