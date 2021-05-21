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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.springframework.core.annotation.MergedAnnotations;

import org.apache.isis.commons.internal.collections._Maps;

import lombok.Value;
import lombok.val;

final class _Annotations_SyntCache {

    // -- L2 CACHE

    @Value(staticConstructor = "of")
    private static final class Key {
        AnnotatedElement annotatedElement;
        Class<? extends Annotation> annotationType;
    }

    private Map<Key, Optional<?>> map = _Maps.newConcurrentHashMap();

    @SuppressWarnings("unchecked")
    <A extends Annotation> Optional<A> computeIfAbsent(
            AnnotatedElement annotatedElement,
            Class<A> annotationType,
            BiFunction<AnnotatedElement, Class<A>, Optional<A>> factory) {

        val key = Key.of(annotatedElement, annotationType);

        return (Optional<A>) map.computeIfAbsent(key, __->factory.apply(annotatedElement, annotationType));
    }

    // -- L1 CACHE

    private Map<AnnotatedElement, MergedAnnotations> mergedByTarget = _Maps.newConcurrentHashMap();

    MergedAnnotations computeIfAbsent(
            AnnotatedElement annotatedElement,
            Function<AnnotatedElement, MergedAnnotations> factory) {

        val key = annotatedElement;
        return mergedByTarget.computeIfAbsent(key, factory);
    }

    // -- CLEANUP

    void clear() {
        map.clear();
        mergedByTarget.clear();
    }


}
