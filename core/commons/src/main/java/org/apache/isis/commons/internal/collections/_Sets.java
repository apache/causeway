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

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;

import static org.apache.isis.commons.internal.base._With.requires;
import static org.apache.isis.commons.internal.functions._Predicates.not;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Common Set creation idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Sets {

    private _Sets(){}

    // -- UNMODIFIABLE SET

    /**
     * Returns an unmodifiable set containing only the specified element.
     * @param element (required)
     * @return non null
     */
    public static <T> Set<T> singleton(T element) {
        requires(element, "element"); // don't accept null element
        return Collections.singleton(element);
    }

    /**
     * Returns an unmodifiable set containing only the specified element or
     * the empty list if the element is null.
     * @param element
     * @return non null
     */
    public static <T> Set<T> singletonOrElseEmpty(@Nullable T element) {
        return element != null ? Collections.singleton(element) : Collections.emptySet();
    }

    /**
     * Copies all elements into a new unmodifiable Set.
     * @param elements
     * @return non null
     */
    @SafeVarargs
    public static <T> Set<T> of(T ... elements) {
        requires(elements, "elements");// don't accept null as argument
        if(elements.length==0) {
            return Collections.emptySet();
        }
        final Set<T> setPreservingOrder = newLinkedHashSet();

        Stream.of(elements)
        .forEach(setPreservingOrder::add);

        return Collections.unmodifiableSet(setPreservingOrder);
    }

    /**
     * Copies all elements from iterable into a new unmodifiable Set preserving iteration order.
     * @param iterable
     * @return non null
     */
    public static <T> Set<T> unmodifiable(Iterable<T> iterable) {
        if(iterable==null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(
                _NullSafe.stream(iterable)
                .collect(Collectors.toSet()));
    }

    // -- TREE SET

    public static <T> TreeSet<T> newTreeSet() {
        return new TreeSet<T>();
    }

    public static <T> TreeSet<T> newTreeSet(@Nullable Comparator<T> comparator) {
        return comparator!=null ? new TreeSet<T>(comparator) : new TreeSet<T>();
    }

    public static <T> TreeSet<T> newTreeSet(@Nullable Iterable<T> iterable) {
        return _NullSafe.stream(iterable)
                .collect(Collectors.<T, TreeSet<T>>toCollection(TreeSet::new));
    }

    public static <T> TreeSet<T> newTreeSet(@Nullable Iterable<T> iterable, @Nullable Comparator<T> comparator) {
        return _NullSafe.stream(iterable)
                .collect(Collectors.<T, TreeSet<T>>toCollection(()->new TreeSet<T>(comparator)));
    }


    // -- HASH SET

    public static <T> HashSet<T> newHashSet() {
        return new HashSet<T>();
    }

    public static <T> HashSet<T> newHashSet(@Nullable Collection<T> collection) {
        if(collection==null) {
            return newHashSet();
        }
        return new HashSet<T>(collection);
    }

    public static <T> HashSet<T> newHashSet(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Sets::newHashSet,
                ()->Collectors.<T, HashSet<T>>toCollection(HashSet::new) );
    }

    // -- LINKED HASH SET

    public static <T> LinkedHashSet<T> newLinkedHashSet() {
        return new LinkedHashSet<T>();
    }

    public static <T> LinkedHashSet<T> newLinkedHashSet(@Nullable Collection<T> collection) {
        if(collection==null) {
            return newLinkedHashSet();
        }
        return new LinkedHashSet<T>(collection);
    }

    public static <T> LinkedHashSet<T> newLinkedHashSet(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Sets::newLinkedHashSet,
                ()->Collectors.<T, LinkedHashSet<T>>toCollection(LinkedHashSet::new) );
    }

    // -- CONCURRENT HASH SET

    public static <T> KeySetView<T, Boolean> newConcurrentHashSet() {
        return ConcurrentHashMap.newKeySet();
    }

    public static <T> KeySetView<T, Boolean> newConcurrentHashSet(@Nullable Collection<T> collection) {
        final KeySetView<T, Boolean> keySetView = newConcurrentHashSet();
        if(collection!=null) {
            keySetView.addAll(collection);
        }
        return keySetView;
    }

    public static <T> KeySetView<T, Boolean> newConcurrentHashSet(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Sets::newConcurrentHashSet,
                ()->Collectors.<T, KeySetView<T, Boolean>>toCollection(ConcurrentHashMap::newKeySet) );
    }

    // -- COPY ON WRITE ARRAY SET

    public static <T> CopyOnWriteArraySet<T> newCopyOnWriteArraySet() {
        return new CopyOnWriteArraySet<T>();
    }

    public static <T> CopyOnWriteArraySet<T> newCopyOnWriteArraySet(@Nullable Collection<T> collection) {
        if(collection==null) {
            return newCopyOnWriteArraySet();
        }
        return new CopyOnWriteArraySet<T>(collection);
    }

    public static <T> CopyOnWriteArraySet<T> newCopyOnWriteArraySet(@Nullable Iterable<T> iterable) {
        return _Collections.collectFromIterable(iterable, _Sets::newCopyOnWriteArraySet,
                ()->Collectors.<T, CopyOnWriteArraySet<T>>toCollection(CopyOnWriteArraySet::new) );
    }


    // -- SET OPERATIONS

    /**
     * Returns the intersection (set theory) of two given sets, not retaining any order.
     * Any {@code null} elements are ignored and will not be contained in the resulting set.
     * @param a
     * @param b
     * @return non null, unmodifiable
     */
    public static <T> Set<T> intersect(@Nullable Set<T> a, @Nullable Set<T> b) {
        if(a==null && b==null) {
            return Collections.emptySet();
        }
        if(a==null || b==null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(
                a.stream()
                .filter(Objects::nonNull)
                .filter(b::contains)
                .collect(Collectors.toSet()) );
    }

    /**
     * Returns the intersection (set theory) of two given (sorted) sets, 
     * retaining order only when natural order.
     * Any {@code null} elements are ignored and will not be contained in the resulting set.
     * @param a
     * @param b
     * @return non null, unmodifiable
     */
    public static <T> SortedSet<T> intersectSorted(@Nullable SortedSet<T> a, @Nullable SortedSet<T> b) {
        if(a==null && b==null) {
            return Collections.emptySortedSet();
        }
        if(a==null || b==null) {
            return Collections.emptySortedSet();
        }
        return Collections.unmodifiableSortedSet(
                a.stream()
                .filter(Objects::nonNull)
                .filter(b::contains)
                .collect(Collectors.toCollection(TreeSet::new)));
    }

    /**
     * Returns a new set containing all the elements of {@code a} that are not in {@code b}, 
     * not retaining any order. 
     * Any {@code null} elements are ignored and will not be contained in the resulting set.
     * @param <T>
     * @param a
     * @param b
     * @return {@code a - b}, non null, unmodifiable 
     */
    public static <T> Set<T> minus(@Nullable Set<T> a, @Nullable Set<T> b) {
        if(a==null || a.isEmpty()) {
            return Collections.emptySet();
        }
        if(b==null || b.isEmpty()) {
            return Collections.unmodifiableSet(new HashSet<>(a));
        }
        return Collections.unmodifiableSet(
                a.stream()
                .filter(Objects::nonNull)
                .filter(not(b::contains))
                .collect(Collectors.toSet()) );
    }
    
    /**
     * Returns a new (sorted) set containing all the elements of {@code a} that are not in {@code b}, 
     * retaining order only when natural order. 
     * Any {@code null} elements are ignored and will not be contained in the resulting set.
     * @param <T>
     * @param a
     * @param b
     * @return {@code a - b}, non null, unmodifiable 
     */
    public static <T> SortedSet<T> minusSorted(@Nullable SortedSet<T> a, @Nullable SortedSet<T> b) {
        if(a==null || a.isEmpty()) {
            return Collections.emptySortedSet();
        }
        if(b==null || b.isEmpty()) {
            return Collections.unmodifiableSortedSet(new TreeSet<>(a));
        }
        return Collections.unmodifiableSortedSet(
                a.stream()
                .filter(Objects::nonNull)
                .filter(not(b::contains))
                .collect(Collectors.toCollection(TreeSet::new)));
    }


}
