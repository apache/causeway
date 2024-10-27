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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.semantics.AccessorSemantics;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 * <p>
 * Motivation: JDK reflection API has no Class.getMethod(name, ...) variant that does not produce an expensive
 * stack-trace, when no such method exists.
 * </p>
 * @apiNote
 * thread-save, implements AutoCloseable so we can put it on the _Context, which then automatically
 * takes care of the lifecycle
 * @since 2.0
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class _ClassCache implements AutoCloseable {

    public static _ClassCache getInstance() {
        return _Context.computeIfAbsent(_ClassCache.class, ()->new _ClassCache(_Context.getDefaultClassLoader()));
    }

    /**
     * JUnit support.
     */
    public static void invalidate() {
        _Context.put(_ClassCache.class, new _ClassCache(_Context.getDefaultClassLoader()), true);
    }

    private final @Nullable ClassLoader classLoader;

    public void add(final Class<?> type) {
        classModel(type);
    }

    // -- TYPE SPECIFIC SEMANTICS

    /**
     * whether type is annotated with {@link XmlRootElement}
     */
    public boolean hasJaxbRootElementSemantics(final Class<?> type) {
        return classModel(type).hasJaxbRootElementSemantics;
    }

    /**
     * whether type is annotated with {@link Named}
     */
    public boolean isAnnotatedWithNamed(final Class<?> type) {
        return classModel(type).isAnnotatedWithNamed;
    }

    // -- CONSTRUCTOR SEMANTICS

    public <T> Stream<ResolvedConstructor> streamPublicConstructors(final Class<T> type) {
        return _Casts.uncheckedCast(classModel(type).publicConstructorsByKey.values().stream());
    }

    public <T> Stream<ResolvedConstructor> streamPublicConstructorsWithInjectSemantics(final Class<T> type) {
        return _Casts.uncheckedCast(classModel(type).constructorsWithInjectSemanticsByKey.values().stream());
    }

    public Optional<ResolvedConstructor> lookupPublicConstructor(final Class<?> type, final Class<?>[] paramTypes) {
        return Optional.ofNullable(lookupConstructor(false, type, paramTypes));
    }

    // -- POST CONSTRUCT SEMANTICS

    public Stream<Method> streamPostConstructMethods(final Class<?> type) {
        return classModel(type).postConstructMethodsByKey.values().stream()
                .map(ResolvedMethod::method);
    }

    // -- METHOD SEMANTICS

    /**
     * A drop-in replacement for {@link Class#getMethod(String, Class...)} that only looks up
     * public methods and does not throw {@link NoSuchMethodException}s.
     */
    public Optional<ResolvedMethod> lookupPublicMethod(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return Optional.ofNullable(findMethod(false, type, name, paramTypes));
    }
    @SneakyThrows
    public ResolvedMethod lookupPublicMethodElseFail(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return lookupPublicMethod(type, name, paramTypes)
                .orElseThrow(()->_Exceptions.noSuchMethodException(type, name, paramTypes));
    }

    /**
     * Variant of {@link #lookupPublicMethod(Class, String, Class[])}
     * that in addition looks up declared methods. (including non-public,
     * but not including inherited non-public ones)
     */
    public Optional<ResolvedMethod> lookupResolvedMethod(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return Optional.ofNullable(findMethod(true, type, name, paramTypes));
    }
    @SneakyThrows
    public ResolvedMethod lookupResolvedMethodElseFail(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return lookupResolvedMethod(type, name, paramTypes)
                .orElseThrow(()->_Exceptions.noSuchMethodException(type, name, paramTypes));
    }

    public Stream<ResolvedMethod> streamPublicMethods(final Class<?> type) {
        return classModel(type).publicMethodsByKey.values().stream();
    }

    public Stream<ResolvedMethod> streamResolvedMethods(final Class<?> type) {
        return classModel(type).resolvedMethodsByKey.values().stream();
    }

    @SneakyThrows
    public ResolvedMethod findMethodUniquelyByNameOrFail(final Class<?> type, final String methodName) {
        var matchingMethods = streamResolvedMethods(type)
                .filter(method->method.name().equals(methodName))
                .collect(Can.toCan());
        return matchingMethods.isCardinalityMultiple()
                ? matchingMethods.reduce(ResolvedMethod::mostSpecific)
                        .getSingleton()
                        .orElseThrow(()->_Exceptions.illegalState("unable to determine most specific of methods %s", matchingMethods))
                : matchingMethods.getSingleton()
                    .orElseThrow(()->_Exceptions.noSuchMethodException(type, methodName));
    }

    // -- FIELD SEMANTICS

    public Stream<Field> streamDeclaredFields(final Class<?> type) {
        return classModel(type).declaredFields.stream();
    }

    // -- FIELD vs GETTER

    public Optional<ResolvedMethod> getterForField(final Class<?> type, final Field field) {
        var capitalizedFieldName = _Strings.capitalize(field.getName());
        return Stream.of("get", "is")
        .map(prefix->prefix + capitalizedFieldName)
        .map(methodName->lookupResolvedMethod(type, methodName, _Constants.emptyClasses).orElse(null))
        .filter(_NullSafe::isPresent)
        .filter(resolvedMethod->AccessorSemantics.isGetter(resolvedMethod))
        .findFirst();
    }

    public Optional<Field> fieldForGetter(final Class<?> type, final Method getterCandidate) {
        return Optional.ofNullable(findFieldForGetter(getterCandidate));
    }

    // -- METHOD STREAMS

    /**
     * Returns a Stream of declared Methods, that pass the given {@code filter},
     * while as an optimization, memoizing the result under given
     * {@code attributeName}.
     * @param type
     * @param attributeName
     * @param filter
     */
    public Stream<ResolvedMethod> streamDeclaredMethodsHaving(
            final Class<?> type,
            final String attributeName,
            final Predicate<ResolvedMethod> filter) {

        var classModel = classModel(type);

        synchronized(classModel.declaredMethodsByAttribute) {
            return classModel.declaredMethodsByAttribute
            .computeIfAbsent(attributeName, key->classModel
                    .resolvedMethodsByKey.values().stream()
                    .filter(filter)
                    .collect(Can.toCan()))
            .stream();
        }
    }

    // -- ATTRIBUTES

    public _ClassCache setAttribute(
            final Class<?> type,
            final String attributeName,
            final String value) {
        var classModel = classModel(type);
        classModel.attributeMap.put(attributeName, value);
        return this;
    }

    public Optional<String> lookupAttribute(
            final Class<?> type,
            final String attributeName) {
        var classModel = classModel(type);
        return Optional.ofNullable(classModel.attributeMap.get(attributeName));
    }

    // -- IMPLEMENATION DETAILS

    @RequiredArgsConstructor
    private static class ClassModel {
        private final Can<Field> declaredFields;
        private final boolean hasJaxbRootElementSemantics;
        private final boolean isAnnotatedWithNamed;

        private final Map<ConstructorKey, ResolvedConstructor> publicConstructorsByKey = new HashMap<>();
        private final Map<ConstructorKey, ResolvedConstructor> constructorsWithInjectSemanticsByKey = new HashMap<>();

        private final Map<MethodKey, ResolvedMethod> resolvedMethodsByKey = new HashMap<>();
        private final Map<MethodKey, ResolvedMethod> publicMethodsByKey = new HashMap<>();
        private final Map<MethodKey, ResolvedMethod> postConstructMethodsByKey = new HashMap<>();

        private final Map<String, Can<ResolvedMethod>> declaredMethodsByAttribute = new HashMap<>();
        private final Map<String, String> attributeMap = new ConcurrentHashMap<>();
    }

    private final Map<Class<?>, ClassModel> inspectedTypes = new HashMap<>();

    @AllArgsConstructor(staticName = "of") @EqualsAndHashCode
    private static final class ConstructorKey {
        private final Class<?> type; // constructors's declaring class
        private final @Nullable Class<?>[] paramTypes;

        public static ConstructorKey of(final Class<?> type, final Constructor<?> constructor) {
            return ConstructorKey.of(type, _Arrays.emptyToNull(constructor.getParameterTypes()));
        }
    }

    @AllArgsConstructor(staticName = "of") @EqualsAndHashCode
    private static final class MethodKey {
        /** Method's implementing class (not necessary the same as its declaring class) */
        private final Class<?> implementingClass;
        /** Method's name */
        private final String name;
        private final @Nullable Class<?>[] paramTypes;
        public static MethodKey of(final Class<?> type, final Method method) {
            return MethodKey.of(type, method.getName(), _Arrays.emptyToNull(method.getParameterTypes()));
        }
    }

    @Override
    public void close() throws Exception {
        synchronized(inspectedTypes) {
            inspectedTypes.clear();
        }
    }

    // -- UTILITY

    public static boolean methodExcludeFilter(final Method method) {
        return method.isBridge()
                || Modifier.isStatic(method.getModifiers())
                || method.getDeclaringClass().equals(Object.class)
                || (_Reflect.isNonFinalObjectMethod(method)
                        // keep overwritten toString() methods, see TitleFacetFromToStringMethod
                        && !_Reflect.isOverwrittenToString(method));
    }

    public static boolean methodIncludeFilter(final Method method) {
        return !methodExcludeFilter(method);
    }

    // -- HELPER

    private ClassModel classModel(final Class<?> type) {
        synchronized(inspectedTypes) {
            return inspectedTypes.computeIfAbsent(type, this::inspectType);
        }
    }

    /**
     * [CAUSEWAY-3164] ensures reflection on generic type arguments works in a concurrent introspection setting
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Class<?> reloadType(final Class<?> _type) {
        return Optional.ofNullable(classLoader)
                .filter(cl->!_type.isPrimitive())
                .filter(cl->!_Reflect.isJavaApiClass(_type))
                .filter(cl->!cl.equals(_type.getClassLoader()))
                .map(cl->Try.call(()->Class.forName(_type.getName(), true, cl))
                        .ifFailure(e->System.err.printf("ClassCache: reloading of type %s failed with %s%n", _type.getName(), e))
                        .getValue().orElse(null))
                .orElse((Class) _type);
    }

    private ClassModel inspectType(final Class<?> _type) {

        final Class<?> type = reloadType(_type);

        //TODO[CAUSEWAY-3556] optimization candidate (needs inspectedTypes to be a ConcurrentHashMap)
//                var declaredMethods = _Reflect.streamTypeHierarchy(type, InterfacePolicy.INCLUDE)
//                    .filter(cls->cls.equals(Object.class))
//                    .flatMap(cls->{
//                        return cls.equals(type)
//                            ? _NullSafe.stream(cls.getDeclaredMethods())
//                                .filter(_ClassCache::methodIncludeFilter)
//                            : inspectType(cls).declaredMethods.stream();
//                    })
//                    .collect(Can.toCan());

        var model = new ClassModel(
                Can.ofArray(type.getDeclaredFields()),
                _Annotations.isPresent(type, XmlRootElement.class),
                _Annotations.isPresent(type, Named.class));

        // process public constructors
        var publicConstr = type.getConstructors();
        for(var constr : publicConstr) {
            var key = ConstructorKey.of(type, constr);
            var resolvedConstr = _GenericResolver.resolveConstructor(constr, type);
            // collect public constructors
            model.publicConstructorsByKey.put(key, resolvedConstr);
            // collect public constructors with inject semantics
            if(isInjectSemantics(constr)) {
                model.constructorsWithInjectSemanticsByKey.put(key, resolvedConstr);
            }
        }

        // process all methods (public and non-public and inherited)
        _Reflect.streamAllMethods(type, true)
        .filter(_ClassCache::methodIncludeFilter)
        .map(method->_GenericResolver.resolveMethod(method, type).orElse(null))
        .filter(_NullSafe::isPresent)
        .forEach(resolved->{
            var key = MethodKey.of(type, resolved.method());
            var methodToKeep =
                    putIntoMapHonoringOverridingRelation(model.resolvedMethodsByKey, key, resolved);
            // collect post-construct methods
            if(isPostConstruct(methodToKeep.method())) {
                model.postConstructMethodsByKey.put(key, methodToKeep);
            }
        });

        // process public methods
        _NullSafe.stream(type.getMethods())
        .filter(_ClassCache::methodIncludeFilter)
        .map(method->_GenericResolver.resolveMethod(method, type).orElse(null))
        .filter(_NullSafe::isPresent)
        .forEach(resolved->{
            var key = MethodKey.of(type, resolved.method());
            putIntoMapHonoringOverridingRelation(model.publicMethodsByKey, key, resolved);
        });

        return model;
    }

    /**
     * Handles the case well, when a method is already in the map and is about to be overwritten.
     * We keep or put that one that overrides the other (in a Java language sense) and return the winner so to speak.
     */
    private static ResolvedMethod putIntoMapHonoringOverridingRelation(
            final Map<MethodKey, ResolvedMethod> map, final MethodKey key, final ResolvedMethod method) {
        var methodWithSameKey = map.get(key); // in case the map is already populated
        var methodToKeep = methodWithSameKey==null
            ? method
            /* key-clash originating from one method overriding the other
             * we need to keep the most specific one */
            : methodWithSameKey.mostSpecific(method);
        map.put(key, methodToKeep);

        var weaklySame = map.values().stream()
            .filter(m->ResolvedMethod.methodsWeaklySame(methodToKeep, m))
            .collect(Can.toCan());

        if(weaklySame.isCardinalityMultiple()) {

            map.values().removeIf(weaklySame::contains);

            var winner = weaklySame.reduce(ResolvedMethod::mostSpecific)
                    .getSingletonOrFail();
            var winnerKey = MethodKey.of(winner.implementationClass(), winner.method());
            map.put(winnerKey, winner);
            return winner;
        }

        return methodToKeep;
    }

    /**
     * signature: any
     * access: public and non-public
     */
    private boolean isInjectSemantics(final Constructor<?> con) {
        return _Annotations.synthesize(con, Inject.class).isPresent()
                || _Annotations.synthesize(con, Autowired.class).map(annot->annot.required()).orElse(false);
    }

    /**
     * return-type: void
     * signature: no args
     * access: public and non-public
     */
    private boolean isPostConstruct(final Method method) {
        return void.class.equals(method.getReturnType())
                && method.getParameterCount()==0
                ? _Annotations.synthesize(method, PostConstruct.class).isPresent()
                : false;
    }

    private ResolvedConstructor lookupConstructor(
            final boolean includeDeclaredConstructors,
            final Class<?> type,
            final Class<?>[] paramTypes) {

        var model = classModel(type);
        var key = ConstructorKey.of(type, _Arrays.emptyToNull(paramTypes));

        var publicConstructor = model.publicConstructorsByKey.get(key);
        return publicConstructor;
    }

    @Nullable
    private ResolvedMethod findMethod(
            final boolean includeDeclaredMethods,
            final Class<?> type,
            final String name,
            final Class<?>[] requiredParamTypes) {

        // we need to lookup by name then find first (weak) match

        var model = classModel(type);

        var publicMethod = model.publicMethodsByKey.values().stream()
                .filter(m->m.name().equals(name))
                .filter(m->_Reflect.methodSignatureAssignableTo(m.paramTypes(), requiredParamTypes))
                .findFirst()
                .orElse(null);
        if(publicMethod!=null) {
            return publicMethod;
        }

        if(includeDeclaredMethods) {
            var resolvedMethod = model.resolvedMethodsByKey.values().stream()
                .filter(m->m.name().equals(name))
                .filter(m->_Reflect.methodSignatureAssignableTo(m.paramTypes(), requiredParamTypes))
                .findFirst()
                .orElse(null);
            return resolvedMethod;
        }
        return null;
    }

    // -- HELPER - FIELD FOR GETTER

    private static Field findFieldForGetter(final Method getterCandidate) {
        if(ReflectionUtils.isObjectMethod(getterCandidate)) {
            return null;
        }
        var fieldNameCandidate = fieldNameForGetter(getterCandidate);
        if(fieldNameCandidate==null) {
            return null;
        }
        var declaringClass = getterCandidate.getDeclaringClass();
        return ReflectionUtils.findField(declaringClass, fieldNameCandidate);
    }

    private static String fieldNameForGetter(final Method getter) {
        if(getter.getParameterCount()>0) {
            return null;
        }
        if(getter.getReturnType()==void.class) {
            return null;
        }
        var methodName = getter.getName();
        String fieldName = null;
        if(methodName.startsWith("is") &&  methodName.length() > 2) {
            fieldName = methodName.substring(2);
        } else if(methodName.startsWith("get") &&  methodName.length() > 3) {
            fieldName = methodName.substring(3);
        } else {
            return null;
        }
        return _Strings.decapitalize(fieldName);
    }

}
