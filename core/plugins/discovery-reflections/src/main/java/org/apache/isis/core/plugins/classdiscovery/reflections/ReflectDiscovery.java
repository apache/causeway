/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.plugins.classdiscovery.reflections;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;

import org.reflections.Reflections;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.core.plugins.classdiscovery.ClassDiscovery;

/**
 *
 * package private utility class
 *
 */
class ReflectDiscovery implements ClassDiscovery {

    private final Reflections reflections;

    // -- CONSTRUCTORS

    public static ReflectDiscovery of(List<String> packagePrefixes) {
        return new ReflectDiscovery(packagePrefixes);
    }

    public static ReflectDiscovery of(String packageNamePrefix) {
        return new ReflectDiscovery(packageNamePrefix);
    }

    public static ReflectDiscovery of(final Object... params) {
        return new ReflectDiscovery(params);
    }

    // -- HIDDEN CONSTRUCTOR

    private ReflectDiscovery(final Object... params) {
        this.reflections = new Reflections(params);
    }

    // -- IMPLEMENTATION

    @Override @NotNull
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {

        Objects.requireNonNull(annotation);

        // ensure unique entries
        return streamTypesAnnotatedWith(annotation).collect(Collectors.toCollection(HashSet::new));
    }

    @Override @NotNull
    public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> type) {

        Objects.requireNonNull(type);

        // ensure unique entries
        return streamSubTypesOf(type).collect(Collectors.toCollection(HashSet::new));
    }

    // -- HELPER


    private Stream<Class<?>> streamTypesAnnotatedWith(Class<? extends Annotation> annotation) {

        // ensure non-null elements
        return _NullSafe.stream(reflections.getTypesAnnotatedWith(annotation))
                .filter(_NullSafe::isPresent);
    }

    private <T> Stream<Class<? extends T>> streamSubTypesOf(final Class<T> type) {

        // ensure non-null elements
        return _NullSafe.stream(reflections.getSubTypesOf(type))
                .filter(_NullSafe::isPresent);
    }



}
