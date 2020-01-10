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

package org.apache.isis.core.metamodel.services;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class CollectionHelper {

    /**
     * If field is of type Collection<T> with generic type T present, then call action with the element type.
     * @param field
     * @param action
     */
    static void ifIsCollectionWithGenericTypeThen(Field field, Consumer<Class<?>> action) {

        final Class<?> typeToBeInjected = field.getType();

        if(Collection.class.isAssignableFrom(typeToBeInjected)) {
            final Type genericType = field.getGenericType();
            if(genericType instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) genericType;
                final Class<?> elementType = (Class<?>) parameterizedType.getActualTypeArguments()[0];

                action.accept(elementType);
            }
        }

    }

    /**
     * Collects elements from stream into a collection that is compatible with the given typeOfCollection.
     * @param typeOfCollection
     * @param elementStream
     * @return
     *
     * @throws IllegalArgumentException if the given typeOfCollection is not supported
     */
    static <T> Collection<T> collectIntoUnmodifiableCompatibleWithCollectionType (
            Class<?> typeOfCollection, Stream<? extends T> elementStream) {

        if(SortedSet.class.equals(typeOfCollection)) {
            return Collections.unmodifiableSortedSet(
                    elementStream.collect(Collectors.<T, SortedSet<T>>toCollection(TreeSet::new))
                    );
        }

        if(Set.class.equals(typeOfCollection)) {
            return Collections.unmodifiableSet(
                    elementStream.collect(Collectors.<T, Set<T>>toCollection(HashSet::new))
                    );
        }

        if(List.class.equals(typeOfCollection)) {
            return Collections.unmodifiableList(
                    elementStream.collect(Collectors.<T, List<T>>toCollection(ArrayList::new))
                    );
        }

        if(Collection.class.equals(typeOfCollection)) {
            return Collections.unmodifiableCollection(
                    elementStream.collect(Collectors.toCollection(ArrayList::new))
                    );
        }

        throw new IllegalArgumentException(
                String.format("Can not collect into %s. Only List, Set, SortedSet and Collection are supported.",
                        typeOfCollection.getClass().getName()));
    }

}
