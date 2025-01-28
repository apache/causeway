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
package org.apache.causeway.commons.internal.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;

import org.jspecify.annotations.NonNull;
import lombok.experimental.UtilityClass;

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
@UtilityClass
public final class _Lists {

    // -- LIST ACCESS

    public <T> T firstElementIfAny(final @Nullable List<T> list) {
        if(_NullSafe.isEmpty(list)) {
            return null;
        }
        return list.get(0);
    }

    public <T> Optional<T> firstElement(final @Nullable List<T> list) {
        if(_NullSafe.isEmpty(list)) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(0));
    }

    public <T> T lastElementIfAny(final @Nullable List<T> list) {
        if(_NullSafe.isEmpty(list)) {
            return null;
        }
        return list.get(list.size()-1);
    }

    public <T> Optional<T> lastElement(final @Nullable List<T> list) {
        if(_NullSafe.isEmpty(list)) {
            return Optional.empty();
        }
        return Optional.ofNullable(list.get(list.size()-1));
    }

    // -- LIST CONCATENATION

    /**
     * Returns an unmodifiable list containing all elements from given list
     * and the specified element.
     */
    public <T> List<T> append(final @Nullable List<T> list, final @Nullable T element) {
        if(_NullSafe.isEmpty(list)) {
            return Collections.singletonList(element);
        }
        var resultList = new ArrayList<T>(list.size() + 1);
        resultList.addAll(list);
        resultList.add(element);
        return Collections.unmodifiableList(resultList);
    }

    /**
     * Returns an unmodifiable list containing all elements from given lists
     * list1 and list2.
     */
    public <T> List<T> concat(final @Nullable Collection<T> list1, final @Nullable Collection<T> list2) {
        var isEmpty1 = _NullSafe.isEmpty(list1);
        var isEmpty2 = _NullSafe.isEmpty(list2);

        if(isEmpty1) {
            return isEmpty2
                    ? Collections.emptyList()
                    : Collections.unmodifiableList(new ArrayList<T>(list2));
        }

        if(isEmpty2) {
            // at this point list1 is not empty
            return Collections.unmodifiableList(new ArrayList<T>(list1));
        }

        var resultList = new ArrayList<T>(list1.size() + list2.size());
        resultList.addAll(list1);
        resultList.addAll(list2);
        return Collections.unmodifiableList(resultList);
    }

    // -- UNMODIFIABLE LIST

    /**
     * Returns an unmodifiable list containing only the specified element.
     * @param element (required)
     * @return non null
     */
    public <T> List<T> singleton(final @NonNull T element) {
        return Collections.singletonList(element);
    }

    /**
     * Returns an unmodifiable list containing only the specified element or
     * the empty list if the element is null.
     * @param element
     * @return non null
     */
    public <T> List<T> singletonOrElseEmpty(final @Nullable T element) {
        return element != null ? Collections.singletonList(element) : Collections.emptyList();
    }

    public <T> List<T> ofNullable(final @Nullable T e0) {
        return Collections.singletonList(e0);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1) {
        var list = new ArrayList<T>(2);
        list.add(e0);
        list.add(e1);
        return Collections.unmodifiableList(list);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1, final @Nullable T e2) {
        var list = new ArrayList<T>(3);
        list.add(e0);
        list.add(e1);
        list.add(e2);
        return Collections.unmodifiableList(list);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1, final @Nullable T e2, final @Nullable T e3) {
        var list = new ArrayList<T>(4);
        list.add(e0);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        return Collections.unmodifiableList(list);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1, final @Nullable T e2, final @Nullable T e3,
            final @Nullable T e4) {
        var list = new ArrayList<T>(5);
        list.add(e0);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);
        return Collections.unmodifiableList(list);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1, final @Nullable T e2, final @Nullable T e3,
            final @Nullable T e4, final @Nullable T e5) {
        var list = new ArrayList<T>(6);
        list.add(e0);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);
        list.add(e5);
        return Collections.unmodifiableList(list);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1, final @Nullable T e2, final @Nullable T e3,
            final @Nullable T e4, final @Nullable T e5, final @Nullable T e6) {
        var list = new ArrayList<T>(7);
        list.add(e0);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);
        list.add(e5);
        list.add(e6);
        return Collections.unmodifiableList(list);
    }

    public <T> List<T> ofNullable(final @Nullable T e0, final @Nullable T e1, final @Nullable T e2, final @Nullable T e3,
            final @Nullable T e4, final @Nullable T e5, final @Nullable T e6, final @Nullable T e7) {
        var list = new ArrayList<T>(8);
        list.add(e0);
        list.add(e1);
        list.add(e2);
        list.add(e3);
        list.add(e4);
        list.add(e5);
        list.add(e6);
        list.add(e7);
        return Collections.unmodifiableList(list);
    }

    /**
     * Copies all elements into a new unmodifiable List.
     * @param elements - null-able
     * @return non null
     */
    public <T> List<T> ofArray(final @Nullable T[] elements) {
        if(elements==null
                || elements.length==0) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(_NullSafe.stream(elements)
                .collect(Collectors.toList()));
    }

    /**
     * Copies all elements from iterable into a new unmodifiable List.
     * @param iterable
     * @return non null
     */
    public <T> List<T> unmodifiable(final @Nullable Iterable<T> iterable) {
        if(iterable==null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(
                _NullSafe.stream(iterable)
                .collect(Collectors.toList()));
    }

    // -- ARRAY LIST

    public <T> ArrayList<T> newArrayList() {
        return new ArrayList<T>();
    }

    public <T> ArrayList<T> newArrayList(final int initialSize) {
        return new ArrayList<T>(initialSize);
    }

    public <T> ArrayList<T> newArrayList(final @Nullable Collection<T> collection) {
        if(collection==null) {
            return newArrayList();
        }
        return new ArrayList<T>(collection);
    }

    public <T> ArrayList<T> newArrayList(final @Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Lists::newArrayList,
                ()->Collectors.<T, ArrayList<T>>toCollection(ArrayList::new) );
    }

    // -- LINKED LIST

    public <T> LinkedList<T> newLinkedList() {
        return new LinkedList<T>();
    }

    public <T> LinkedList<T> newLinkedList(final @Nullable Collection<T> collection) {
        if(collection==null) {
            return newLinkedList();
        }
        return new LinkedList<T>(collection);
    }

    public <T> LinkedList<T> newLinkedList(final @Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Lists::newLinkedList,
                ()->Collectors.<T, LinkedList<T>>toCollection(LinkedList::new) );
    }

    // -- COPY ON WRITE LIST

    public <T> CopyOnWriteArrayList<T> newConcurrentList() {
        return new CopyOnWriteArrayList<T>();
    }

    public <T> CopyOnWriteArrayList<T> newConcurrentList(final @Nullable Collection<T> collection) {
        if(collection==null) {
            return newConcurrentList();
        }
        return new CopyOnWriteArrayList<T>(collection);
    }

    public <T> CopyOnWriteArrayList<T> newConcurrentList(final @Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Lists::newConcurrentList,
                ()->Collectors.<T, CopyOnWriteArrayList<T>>toCollection(CopyOnWriteArrayList::new) );
    }

    // -- TRANSFORMATIONS

    public <T, R> List<R> map(final @Nullable Collection<T> input, final Function<T, R> mapper) {
        return _NullSafe.stream(input)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public <T> List<T> filter(final @Nullable Collection<T> input, final Predicate<? super T> filter) {
        return _NullSafe.stream(input)
                .filter(filter)
                .collect(Collectors.toList());
    }

    // -- COLLECTORS

    public <T> Collector<T, ?, List<T>> toUnmodifiable(final Supplier<List<T>> collectionFactory) {

        return Collectors.collectingAndThen(
                Collectors.toCollection(collectionFactory),
                Collections::unmodifiableList);
    }

    public <T> Collector<T, ?, List<T>> toUnmodifiable() {
        return toUnmodifiable(ArrayList::new);
    }

}
