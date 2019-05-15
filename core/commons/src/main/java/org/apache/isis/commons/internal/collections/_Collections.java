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

package org.apache.isis.commons.internal.collections;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;

import static org.apache.isis.commons.internal.base._With.requires;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Collection creation and adapting idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0.0
 */
public final class _Collections {

    private _Collections(){}

    // -- PREDICATES

    /**
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface
     */
    public static boolean isCollectionType(@Nullable final Class<?> cls) {
        return cls!=null ? java.util.Collection.class.isAssignableFrom(cls) : false;
    }

    /**
     * For convenience also provided in {@link _Arrays}.
     * @param cls
     * @return whether {@code cls} implements the java.util.Collection interface
     * or represents an array
     */
    public static boolean isCollectionOrArrayType(final Class<?> cls) {
        return _Collections.isCollectionType(cls) || _Arrays.isArrayType(cls);
    }

    // -- COLLECTION UNMODIFIABLE ADAPTERS (FOR LIST)

    /**
     * Adapts the {@code list} as unmodifiable collection.
     * Same as {@link Collections#unmodifiableCollection(List)}.
     *
     * @param list
     * @return null if {@code list} is null
     */
    public static <T> Collection<T> asUnmodifiableCollection(@Nullable final List<T> list) {
        if(list==null) {
            return null;
        }
        return Collections.unmodifiableCollection(list);
    }

    /**
     * Adapts the {@code list} as unmodifiable list.
     * Same as {@link Collections#unmodifiableList(List)}.
     *
     * @param list
     * @return
     */
    public static <T> List<T> asUnmodifiableList(@Nullable final List<T> list) {
        if(list==null) {
            return null;
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Preserves order, adapts the {@code list} as Set.<br/><br/>
     *
     * Any duplicate elements of the list will not be added to the set.
     * An element e1 is a duplicate of e2 if {@code e1.equals(e2) == true}.
     *
     * @param list
     * @return null if {@code list} is null
     */
    public static <T> Set<T> asUnmodifiableSet(@Nullable final List<T> list) {
        if(list==null) {
            return null;
        }
        return Collections.unmodifiableSet(
                (Set<T>)
                list.stream()
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * Preserves order, adapts the {@code list} as SortedSet.<br/><br/>
     *
     * Any duplicate elements of the list will not be added to the set.
     * An element e1 is a duplicate of e2 if {@code e1.equals(e2) == true}.
     *
     * @param list
     * @return null if {@code list} is null
     */
    public static <T> SortedSet<T> asUnmodifiableSortedSet(@Nullable final List<T> list) {
        if(list==null) {
            return null;
        }
        return _Collections_SortedSetOfList.of(list);
    }

    // -- COMMON COLLECTORS

    public static <T> Collector<T, ?, HashSet<T>> toHashSet() {
        return Collectors.toCollection(HashSet::new);
    }

    public static <T> Collector<T, ?, ArrayList<T>> toArrayList() {
        return Collectors.toCollection(ArrayList::new);
    }

    // -- STREAM TO UMODIFIABLE COLLECTION COLLECTORS

    /**
     * @return a collector that collects elements of a stream into an unmodifiable List
     */
    public static <T> Collector<T, List<T>, List<T>> toUnmodifiableList() {
        return new _Collections_Collector<>(ArrayList::new, Collections::unmodifiableList);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable Set
     */
    public static <T> Collector<T, Set<T>, Set<T>> toUnmodifiableSet() {
        return new _Collections_Collector<>(HashSet::new, Collections::unmodifiableSet);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable SortedSet
     */
    public static <T> Collector<T, SortedSet<T>, SortedSet<T>> toUnmodifiableSortedSet() {
        return new _Collections_Collector<>(TreeSet::new, Collections::unmodifiableSortedSet);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable Collection
     */
    public static <T> Collector<T, Collection<T>, Collection<T>> toUnmodifiableCollection() {
        return new _Collections_Collector<>(ArrayList::new, Collections::unmodifiableCollection);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable SortedSet
     */
    public static <T> Collector<T, SortedSet<T>, SortedSet<T>> toUnmodifiableSortedSet(
            @Nullable Comparator<T> comparator) {

        if(comparator==null) {
            return toUnmodifiableSortedSet();
        }
        return new _Collections_Collector<>(()->new TreeSet<>(comparator), Collections::unmodifiableSortedSet);
    }

    /**
     * @return a collector that collects elements of a stream into an unmodifiable
     * List, Set, SortedSet or Collection.
     * @throws IllegalArgumentException if the {@link typeOfCollection} is not one of
     * List, Set, SortedSet or Collection.
     */
    public static <T> Collector<T, ?, ? extends Collection<T>> toUnmodifiableOfType(Class<?> typeOfCollection) {

        requires(typeOfCollection, "typeOfCollection");

        if(SortedSet.class.equals(typeOfCollection)) {
            return toUnmodifiableSortedSet();
        }

        if(Set.class.equals(typeOfCollection)) {
            return toUnmodifiableSet();
        }

        if(List.class.equals(typeOfCollection)) {
            return toUnmodifiableList();
        }

        if(Collection.class.equals(typeOfCollection)) {
            return toUnmodifiableCollection();
        }

        throw new IllegalArgumentException(
                String.format("Can not collect into %s. Only List, Set, SortedSet and Collection are supported.",
                        typeOfCollection.getClass().getName()));
    }

    // -- COLLECT FROM ITERABLE

    /*
     * package private utility for a slightly heap pollution reduced collection,
     * if the iterable is a collection and we know the size of the result in advance
     *
     * @param iterable
     * @param factory
     * @param elementCollector
     * @return
     */
    static <T, R> R collectFromIterable(
            @Nullable Iterable<T> iterable,
            Function<Collection<T>, R> factory,
            Supplier<Collector<T, ?, R>> elementCollector) {

        if(iterable==null) {
            return factory.apply(null);
        }
        if(iterable instanceof Collection) {
            return factory.apply((Collection<T>) iterable);
        }
        return _NullSafe.stream(iterable)
                .collect(elementCollector.get());
    }

    // -- ELEMENT TYPE INFERENCE

    /**
     * If the {@code collectionType} represents a collection then returns returns the inferred element type of the
     * specified {@code genericType}
     * @param collectionType
     * @param genericType as associated with {@code collectionType} (as available for fields or method parameters)
     * @return inferred type or null if inference fails
     */
    public static @Nullable Class<?> inferElementTypeIfAny(
            @Nullable final Class<?> collectionType,
            @Nullable final Type genericType) {

        if(collectionType == null || genericType==null) {
            return null;
        }

        if(!isCollectionType(collectionType)) {
            return null;
        }

        if(genericType instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) genericType;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if(actualTypeArguments.length == 1) {
                // handle e.g. List<Sometype>
                final Type actualTypeArgument = actualTypeArguments[0];
                if(actualTypeArgument instanceof Class) {
                    final Class<?> actualType = (Class<?>) actualTypeArgument;
                    return actualType;
                }
                // also handle e.g. List<Sometype<T>>
                if(actualTypeArgument instanceof ParameterizedType) {
                    final Type innerParameterizedType = ((ParameterizedType) actualTypeArgument).getRawType();
                    if(innerParameterizedType instanceof Class) {
                        final Class<?> actualType = (Class<?>) innerParameterizedType;
                        return actualType;
                    }
                }
            }
        }

        return null;
    }

    /**
     * If the {@code field} represents a collection then returns the inferred element type for this collection (if any).
     *
     * @param field
     * @return inferred type or null if inference fails
     */
    public static @Nullable Class<?> inferElementTypeIfAny(@Nullable final Field field) {
        return inferElementTypeIfAny(field.getType(), field.getGenericType());
    }

    // -- TO STRING
    
    public static String toStringJoining(Collection<?> collecion, String delimiter) {
        return collecion.stream()
                .map(x->""+x)
                .collect(Collectors.joining(delimiter));
    }
    
    public static String toStringJoiningNewLine(Collection<?> collecion) {
        return toStringJoining(collecion, "\n");
    }

}
