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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.semantics.AccessorSemantics;

import lombok.AccessLevel;
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

    public enum Attribute {
        /**
         * Corresponds to the bean name of Spring managed beans.
         */
        SPRING_NAMED,
        /**
         * Corresponds to DomainObject#mixinMethod().
         */
        MIXIN_MAIN_METHOD_NAME;
    }

    @Nullable private final ClassLoader classLoader;
    private static _ClassCache defaultInstance() { return new _ClassCache(_Context.getDefaultClassLoader()); }
    public static _ClassCache getInstance() {
        return _Context.computeIfAbsent(_ClassCache.class, ()->defaultInstance());
    }

    /**
     * JUnit support.
     */
    public static void invalidate() {
        _Context.put(_ClassCache.class, defaultInstance(), true);
    }

    public void add(final Class<?> type) {
        classModel(type);
    }

    // -- TYPE SPECIFIC SEMANTICS

    /**
     * whether type is explicitly named with {@link Named} or similar
     */
    public boolean isNamed(final Class<?> type) {
        return classModel(type).head().named()!=null;
    }

    public void setSpringNamed(final Class<?> beanClass, final String beanDefinitionName) {
        _Assert.assertNotEmpty(beanDefinitionName);
        head(beanClass).attributeMap().put(Attribute.SPRING_NAMED, beanDefinitionName);
    }

    public String getLogicalName(final Class<?> type) {
        var head = head(type);
        return Optional.ofNullable(head.attributeMap().get(Attribute.SPRING_NAMED))
                .or(()->Optional.ofNullable(head.named()))
                .or(()->Optional.ofNullable(type.getCanonicalName()))
                .orElseGet(type::getName);
    }

    public ClassModelHead head(final Class<?> type) {
        return classModel(type).head();
    }

    private ClassModelBody body(final Class<?> type) {
        return classModel(type).body();
    }

    // -- CONSTRUCTOR SEMANTICS

    public <T> Stream<ResolvedConstructor> streamPublicConstructors(final Class<T> type) {
        return _Casts.uncheckedCast(body(type).publicConstructorsByKey.values().stream());
    }

    public <T> Stream<ResolvedConstructor> streamPublicConstructorsWithInjectSemantics(final Class<T> type) {
        return _Casts.uncheckedCast(body(type).constructorsWithInjectSemanticsByKey.values().stream());
    }

    public Optional<ResolvedConstructor> lookupPublicConstructor(final Class<?> type, final Class<?>[] paramTypes) {
        return Optional.ofNullable(lookupConstructor(false, type, paramTypes));
    }

    // -- POST CONSTRUCT SEMANTICS

    public Stream<Method> streamPostConstructMethods(final Class<?> type) {
        return body(type).postConstructMethodsByKey.values().stream()
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
        return body(type).publicMethodsByKey.values().stream();
    }

    public Stream<ResolvedMethod> streamResolvedMethods(final Class<?> type) {
        return body(type).resolvedMethodsByKey.values().stream();
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
        return body(type).declaredFields.stream();
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

        synchronized(classModel.body().declaredMethodsByAttribute) {
            return classModel.body().declaredMethodsByAttribute
            .computeIfAbsent(attributeName, key->classModel.body()
                    .resolvedMethodsByKey.values().stream()
                    .filter(filter)
                    .collect(Can.toCan()))
            .stream();
        }
    }

    // -- IMPLEMENATION DETAILS

    public record ClassModelHead(
            MergedAnnotations mergedAnnotations,
            /** explicit name if any */
            @Nullable String named,
            Map<Attribute, String> attributeMap) {

        static ClassModelHead create(final Class<?> type) {
            var mergedAnnotations = MergedAnnotations.from(type, SearchStrategy.TYPE_HIERARCHY);
            return new ClassModelHead(mergedAnnotations,
                    _ClassCacheUtil.inferName(type, mergedAnnotations),
                    new ConcurrentHashMap<>());
        }

        public <A extends Annotation> Optional<A> annotation(final Class<A> annotationType) {
            return _Annotations_SynthesizedMergedAnnotationInvocationHandler
                    .createProxy(mergedAnnotations, Optional.empty(), annotationType);
        }

        /**
         * whether type is annotated with annotationType
         */
        public boolean hasAnnotation(final Class<? extends Annotation> annotationType) {
            return mergedAnnotations.get(annotationType).isPresent();
        }

        /**
         * whether type is annotated with {@link XmlRootElement}
         */
        public boolean hasJaxbRootElementSemantics() {
            return hasAnnotation(XmlRootElement.class);
        }

        /**
         * whether type is JDO persistable (but NOT embedded only)
         */
        public boolean isJdoPersistenceCapable() {
            return _ClassCacheUtil.isJdoPersistenceCapable(mergedAnnotations)
                    && !_ClassCacheUtil.isJdoEmbeddedOnly(mergedAnnotations);
        }

        public Can<String> springProfiles() {
            var profileAnnot = mergedAnnotations.get(Profile.class);
            if(!profileAnnot.isPresent()) return Can.empty();
            return Can.ofArray(profileAnnot.getStringArray("value"));
        }

    }

    private record ClassModelBody(
            Can<Field> declaredFields,
            Map<ConstructorKey, ResolvedConstructor> publicConstructorsByKey,
            Map<ConstructorKey, ResolvedConstructor> constructorsWithInjectSemanticsByKey,

            Map<MethodKey, ResolvedMethod> resolvedMethodsByKey,
            Map<MethodKey, ResolvedMethod> publicMethodsByKey,
            Map<MethodKey, ResolvedMethod> postConstructMethodsByKey,

            Map<String, Can<ResolvedMethod>> declaredMethodsByAttribute) {

        ClassModelBody(final Can<Field> declaredFields) {
            this(declaredFields,
                    new HashMap<>(), new HashMap<>(), new HashMap<>(),
                    new HashMap<>(), new HashMap<>(), new HashMap<>());
        }

//        private static ClassModelBody EMPTY = new ClassModelBody(
//                Can.empty(),
//                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap(),
//                Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());

        private static ClassModelBody create(final Class<?> type, final ClassModelHead head) {

            var body = new ClassModelBody(Can.ofArray(type.getDeclaredFields()));

            // process public constructors
            var publicConstr = type.getConstructors();
            for(var constr : publicConstr) {
                var key = new ConstructorKey(type, constr);
                var resolvedConstr = _GenericResolver.resolveConstructor(constr, type);
                // collect public constructors
                body.publicConstructorsByKey.put(key, resolvedConstr);
                // collect public constructors with inject semantics
                if(isInjectSemantics(constr)) {
                    body.constructorsWithInjectSemanticsByKey.put(key, resolvedConstr);
                }
            }

            // process all methods (public and non-public and inherited)
            _Reflect.streamAllMethods(type, true)
            .filter(_ClassCache::methodIncludeFilter)
            .map(method->_GenericResolver.resolveMethod(method, type).orElse(null))
            .filter(_NullSafe::isPresent)
            .forEach(resolved->{
                var key = new MethodKey(type, resolved.method());
                var methodToKeep =
                        putIntoMapHonoringOverridingRelation(body.resolvedMethodsByKey, key, resolved);
                // collect post-construct methods
                if(isPostConstruct(methodToKeep.method())) {
                    body.postConstructMethodsByKey.put(key, methodToKeep);
                }
            });

            // process public methods
            _NullSafe.stream(type.getMethods())
            .filter(_ClassCache::methodIncludeFilter)
            .map(method->_GenericResolver.resolveMethod(method, type).orElse(null))
            .filter(_NullSafe::isPresent)
            .forEach(resolved->{
                var key = new MethodKey(type, resolved.method());
                putIntoMapHonoringOverridingRelation(body.publicMethodsByKey, key, resolved);
            });

            return body;
        }

    }

    private record ClassModel(
            ClassModelHead head,
            _Lazy<ClassModelBody> lazyBody) {
        ClassModelBody body() {
            return lazyBody.get();
        }
    }

    private final Map<Class<?>, ClassModel> inspectedTypes = new HashMap<>();

    private record ConstructorKey(
            Class<?> type, // constructors's declaring class
            @Nullable Class<?>[] paramTypes) {
        ConstructorKey(final Class<?> type, final Class<?>[] paramTypes) {
            this.type = type;
            this.paramTypes = _Arrays.emptyToNull(paramTypes);
        }
        ConstructorKey(final Class<?> type, final Constructor<?> constructor) {
            this(type, constructor.getParameterTypes());
        }
        // java puzzler, why do we need to explicitly declare equals and hash code here?
        @Override public int hashCode() { return type.hashCode(); }
        @Override public final boolean equals(final Object o) {
            return o instanceof ConstructorKey other
                    ? this.type.equals(other.type)
                            && Arrays.equals(this.paramTypes, other.paramTypes())
                    : false;
        }
    }

    private record MethodKey(
        /** Method's implementing class (not necessary the same as its declaring class) */
        Class<?> implementingClass,
        /** Method's name */
        String name,
        @Nullable Class<?>[] paramTypes) {
        MethodKey(final Class<?> implementingClass, final String name, final Class<?>[] paramTypes) {
            this.implementingClass = implementingClass;
            this.name = name;
            this.paramTypes = _Arrays.emptyToNull(paramTypes);
        }
        MethodKey(final Class<?> type, final Method method) {
            this(type, method.getName(), method.getParameterTypes());
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
        var head = ClassModelHead.create(type);
        return new ClassModel(head, _Lazy.threadSafe(()->ClassModelBody.create(type, head)));
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
            var winnerKey = new MethodKey(winner.implementationClass(), winner.method());
            map.put(winnerKey, winner);
            return winner;
        }

        return methodToKeep;
    }

    /**
     * signature: any
     * access: public and non-public
     */
    private static boolean isInjectSemantics(final Constructor<?> con) {
        return _Annotations.synthesize(con, Inject.class).isPresent()
                || _Annotations.synthesize(con, Autowired.class).map(annot->annot.required()).orElse(false);
    }

    /**
     * return-type: void
     * signature: no args
     * access: public and non-public
     */
    private static boolean isPostConstruct(final Method method) {
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
        var key = new ConstructorKey(type, paramTypes);

        var publicConstructor = model.body().publicConstructorsByKey.get(key);
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

        var publicMethod = model.body().publicMethodsByKey.values().stream()
                .filter(m->m.name().equals(name))
                .filter(m->_Reflect.methodSignatureAssignableTo(m.paramTypes(), requiredParamTypes))
                .findFirst()
                .orElse(null);
        if(publicMethod!=null) {
            return publicMethod;
        }

        if(includeDeclaredMethods) {
            var resolvedMethod = model.body().resolvedMethodsByKey.values().stream()
                .filter(m->m.name().equals(name))
                .filter(m->_Reflect.methodSignatureAssignableTo(m.paramTypes(), requiredParamTypes))
                .findFirst()
                .orElse(null);
            return resolvedMethod;
        }
        return null;
    }

}
