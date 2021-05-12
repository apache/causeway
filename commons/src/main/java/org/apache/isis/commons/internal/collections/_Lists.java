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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.base._With;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common List creation idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Lists {

    private _Lists(){}

    // -- LIST ACCESS

    public static <T> T lastElementIfAny(@Nullable List<T> list) {
        if(_NullSafe.isEmpty(list)) {
            return null;
        }
        return list.get(list.size()-1);
    }

    // -- UNMODIFIABLE LIST

    /**
     * Returns an unmodifiable list containing only the specified element.
     * @param element (required)
     * @return non null
     */
    public static <T> List<T> singleton(T element) {
        _With.requires(element, "element"); // don't accept null element
        return Collections.singletonList(element);
    }

    /**
     * Returns an unmodifiable list containing only the specified element or
     * the empty list if the element is null.
     * @param element
     * @return non null
     */
    public static <T> List<T> singletonOrElseEmpty(@Nullable T element) {
        return element != null ? Collections.singletonList(element) : Collections.emptyList();
    }

    /**
     * Copies all elements into a new unmodifiable List.
     * @param elements
     * @return non null
     */
    @SafeVarargs
    public static <T> List<T> of(T ... elements) {
        _With.requires(elements, "elements"); // don't accept null as argument
        if(elements.length==0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    /**
     * Copies all elements from iterable into a new unmodifiable List.
     * @param iterable
     * @return non null
     */
    public static <T> List<T> unmodifiable(@Nullable Iterable<T> iterable) {
        if(iterable==null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(
                _NullSafe.stream(iterable)
                .collect(Collectors.toList()));
    }

    // -- ARRAY LIST

    public static <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public static <T> ArrayList<T> newArrayList(final int initialSize) {
        return new ArrayList<T>(initialSize);
    }

    public static <T> ArrayList<T> newArrayList(@Nullable Collection<T> collection) {
        if(collection==null) {
            return newArrayList();
        }
        return new ArrayList<T>(collection);
    }

    public static <T> ArrayList<T> newArrayList(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Lists::newArrayList,
                ()->Collectors.<T, ArrayList<T>>toCollection(ArrayList::new) );
    }

    // -- LINKED LIST

    public static <T> LinkedList<T> newLinkedList() {
        return new LinkedList<T>();
    }

    public static <T> LinkedList<T> newLinkedList(@Nullable Collection<T> collection) {
        if(collection==null) {
            return newLinkedList();
        }
        return new LinkedList<T>(collection);
    }

    public static <T> LinkedList<T> newLinkedList(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Lists::newLinkedList,
                ()->Collectors.<T, LinkedList<T>>toCollection(LinkedList::new) );
    }

    // -- COPY ON WRITE LIST

    public static <T> CopyOnWriteArrayList<T> newConcurrentList() {
        return new CopyOnWriteArrayList<T>();
    }

    public static <T> CopyOnWriteArrayList<T> newConcurrentList(@Nullable Collection<T> collection) {
        if(collection==null) {
            return newConcurrentList();
        }
        return new CopyOnWriteArrayList<T>(collection);
    }

    public static <T> CopyOnWriteArrayList<T> newConcurrentList(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Lists::newConcurrentList,
                ()->Collectors.<T, CopyOnWriteArrayList<T>>toCollection(CopyOnWriteArrayList::new) );
    }


    // -- TRANSFORMATIONS

    public static <T, R> List<R> map(@Nullable Collection<T> input, Function<T, R> mapper) {
        return _NullSafe.stream(input)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static <T> List<T> filter(@Nullable Collection<T> input, Predicate<? super T> filter) {
        return _NullSafe.stream(input)
                .filter(filter)
                .collect(Collectors.toList());
    }

    // -- COLLECTORS

    public static <T>
    Collector<T, ?, List<T>> toUnmodifiable(Supplier<List<T>> collectionFactory) {

        return Collectors.collectingAndThen(
                Collectors.toCollection(collectionFactory),
                Collections::unmodifiableList);
    }

    public static <T>
    Collector<T, ?, List<T>> toUnmodifiable() {
        return toUnmodifiable(ArrayList::new);
    }



}
