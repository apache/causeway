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
package org.apache.isis.commons.internal.reflection;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.context._Context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class _ClassCache implements AutoCloseable {

    public static _ClassCache getInstance() {
        return _Context.computeIfAbsent(_ClassCache.class, _ClassCache::new);
    }

    public void add(final Class<?> type) {
        inspectType(type);
    }

    /**
     * A drop-in replacement for {@link Class#getMethod(String, Class...)} that only looks up
     * public methods and does not throw {@link NoSuchMethodException}s.
     */
    public Method lookupPublicMethod(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return lookupMethod(false, type, name, paramTypes);
    }

    /**
     * Variant of {@link #lookupPublicMethod(Class, String, Class[])}
     * that in addition looks up declared methods. (including non-public,
     * but not including inherited non-public ones)
     */
    public Method lookupPublicOrDeclaredMethod(final Class<?> type, final String name, final Class<?>[] paramTypes) {
        return lookupMethod(true, type, name, paramTypes);
    }

    public Stream<Method> streamPublicMethods(final Class<?> type) {
        return inspectType(type).publicMethodsByKey.values().stream();
    }

    public Stream<Method> streamPublicOrDeclaredMethods(final Class<?> type) {
        val classModel = inspectType(type);
        return Stream.concat(
                classModel.publicMethodsByKey.values().stream(),
                classModel.nonPublicDeclaredMethodsByKey.values().stream());
    }

    public Stream<Method> streamDeclaredMethods(final Class<?> type) {
        return inspectType(type).declaredMethods.stream();
    }

    /**
     * Returns a Stream of declared Methods, that pass the given {@code filter},
     * while as an optimization, memoizing the result under given
     * {@code attributeName}.
     * @param type
     * @param attributeName
     * @param filter
     */
    public Stream<Method> streamDeclaredMethodsHaving(
            final Class<?> type,
            final String attributeName,
            final Predicate<Method> filter) {

        val classModel = inspectType(type);

        synchronized(classModel.declaredMethodsByAttribute) {
            return classModel.declaredMethodsByAttribute
            .computeIfAbsent(attributeName, key->classModel.declaredMethods.filter(filter))
            .stream();
        }
    }

    // -- IMPLEMENATION DETAILS

    @RequiredArgsConstructor
    private static class ClassModel {
        private final Map<MethodKey, Method> publicMethodsByKey = new HashMap<>();
        private final Map<MethodKey, Method> nonPublicDeclaredMethodsByKey = new HashMap<>();
        private final Can<Method> declaredMethods;
        private final Map<String, Can<Method>> declaredMethodsByAttribute = new HashMap<>();
    }

    private final Map<Class<?>, ClassModel> inspectedTypes = new HashMap<>();

    @AllArgsConstructor(staticName = "of") @EqualsAndHashCode
    private static final class MethodKey {
        private final Class<?> type; // method's declaring class
        private final String name; // method name
        private final Class<?>[] paramTypes;

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

    // -- HELPER

    private ClassModel inspectType(final Class<?> type) {
        synchronized(inspectedTypes) {

            return inspectedTypes.computeIfAbsent(type, __->{

                val declaredMethods = type.getDeclaredMethods();

                val methods = new ClassModel(Can.ofArray(declaredMethods));

                for(val method : declaredMethods) {
                    methods.nonPublicDeclaredMethodsByKey.put(MethodKey.of(type, method), method);
                }

                for(val method : type.getMethods()) {
                    val key = MethodKey.of(type, method);
                    methods.publicMethodsByKey.put(key, method);
                    methods.nonPublicDeclaredMethodsByKey.remove(key);
                }

                return methods;

            });
        }
    }

    private Method lookupMethod(
            final boolean includeDeclaredMethods,
            final Class<?> type,
            final String name,
            final Class<?>[] paramTypes) {

        val methods = inspectType(type);

        val key = MethodKey.of(type, name, _Arrays.emptyToNull(paramTypes));

        val publicMethod = methods.publicMethodsByKey.get(key);
        if(publicMethod!=null) {
            return publicMethod;
        }
        if(includeDeclaredMethods) {
            return methods.nonPublicDeclaredMethodsByKey.get(key);
        }
        return null;
    }


}
