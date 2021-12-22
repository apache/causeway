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
package org.apache.isis.commons.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.val;

/**
 *
 * Immutable {@link Iterable}, that can specifically represent 3 possible variants of
 * {@link Cardinality}.
 * <p>
 * Java's {@link Optional}, can be seen as a holder of element(s), that is restricted to
 * cardinality ZERO or ONE. {@link Can} is the logical extension to that, allowing also a
 * cardinality of MULTIPLE.
 * <p>
 * Same idiomatic convention applies: References to {@link Can}
 * should never be initialized to {@code null}.
 * <p>
 * A {@link Can} must not contain elements equal to {@code null}.
 *
 * @param <T>
 * @since 2.0 {@index}
 */
public interface Can<T>
extends Iterable<T>, Comparable<Can<T>>, Serializable {

    /**
     * @return this Can's cardinality
     */
    Cardinality getCardinality();

    /**
     * @return number of elements this Can contains
     */
    int size();

    /**
     * Will only ever return an empty Optional, if the elementIndex is out of bounds.
     * @param elementIndex
     * @return optionally this Can's element with index {@code elementIndex},
     * based on whether this index is within bounds
     */
    Optional<T> get(int elementIndex);

    /**
     * Shortcut to {@code get(elementIndex).orElseThrow(...)}
     * <p>
     * Will only ever throw, if the elementIndex is out of bounds.
     * @param elementIndex
     * @return this Can's element with index {@code elementIndex}
     * @throws NoSuchElementException when the elementIndex is out of bounds
     * @see {@link #get(int)}
     */
    default T getElseFail(final int elementIndex) {
        return get(elementIndex)
                .orElseThrow(()->new NoSuchElementException(
                        "no element with elementIndex = " + elementIndex));
    }

    /**
     * For convenience allows the argument to be {@code null} treating {@code null}
     * equivalent to {@link Can#empty()}.
     * @see {@link Comparable#compareTo(Object)}
     */
    @Override
    int compareTo(final @Nullable Can<T> o);

    /**
     * @return Stream of elements this Can contains
     */
    Stream<T> stream();

    /**
     * @return possibly concurrent Stream of elements this Can contains
     */
    Stream<T> parallelStream();

    /**
     * @return this Can's first element or an empty Optional if no such element
     */
    Optional<T> getFirst();

    /**
     * Shortcut for {@code getFirst().orElseThrow(_Exceptions::noSuchElement)}
     * @throws NoSuchElementException if result is empty
     */
    default T getFirstOrFail() {
        return getFirst().orElseThrow(_Exceptions::noSuchElement);
    }

    /**
     * @return this Can's last element or an empty Optional if no such element
     */
    Optional<T> getLast();

    /**
     * Shortcut for {@code getLast().orElseThrow(_Exceptions::noSuchElement)}
     * @throws NoSuchElementException if result is empty
     */
    default T getLastOrFail() {
        return getLast().orElseThrow(_Exceptions::noSuchElement);
    }

    /**
     * @return this Can's single element or an empty Optional if this Can has any cardinality other than ONE
     */
    Optional<T> getSingleton();

    /**
     * Shortcut for {@code getSingleton().orElseThrow(_Exceptions::noSuchElement)}
     * @throws NoSuchElementException if result is empty
     */
    default T getSingletonOrFail() {
        return getSingleton().orElseThrow(_Exceptions::noSuchElement);
    }

    /**
     * @return whether this Can contains given {@code element}, that is, at least one contained element
     * passes the {@link Objects#equals(Object, Object)} test with respect to the given element.
     */
    boolean contains(@Nullable T element);

    // -- FACTORIES

    /**
     * Returns an empty {@code Can}.
     * @param <T>
     */
    @SuppressWarnings("unchecked") // this is how the JDK does it for eg. empty lists
    public static <T> Can<T> empty() {
        return (Can<T>) Can_Empty.INSTANCE;
    }

    /**
     * Returns either a {@code Can} with the given {@code element} or an empty {@code Can} if the
     * {@code element} is {@code null}.
     * @param <T>
     * @param element
     * @return non-null
     */
    public static <T> Can<T> ofNullable(final @Nullable T element) {
        if(element==null) {
            return empty();
        }
        return Can_Singleton.of(element);
    }

    /**
     * Returns either a {@code Can} with the given {@code element} or throws if the
     * {@code element} is {@code null}.
     * @param <T>
     * @param element
     * @return non-null
     * @throws NullPointerException if {@code element} is {@code null}
     */
    public static <T> Can<T> ofSingleton(final @NonNull T element) {
        return Can_Singleton.of(element);
    }

    /**
     * Var-arg version of {@link Can#ofArray(Object[])}.
     * @param <T>
     * @param array
     * @return non-null
     * @see Can#ofArray(Object[])
     */
    @SafeVarargs
    public static <T> Can<T> of(final T ... array) {
        return ofArray(array);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code array}
     * or an empty {@code Can} if the {@code array} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param array
     * @return non-null
     */
    public static <T> Can<T> ofArray(final @Nullable T[] array) {

        if(_NullSafe.size(array)==0) {
            return empty();
        }

        val nonNullElements = Stream.of(array)
                .filter(_NullSafe::isPresent)
                .collect(_CanFactory.toListWithSizeUpperBound(array.length));

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code collection}
     * or an empty {@code Can} if the {@code collection} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param collection
     * @return non-null
     */
    public static <T> Can<T> ofCollection(final @Nullable Collection<T> collection) {

        val inputSize = _NullSafe.size(collection);

        if(inputSize==0) {
            return empty();
        }

        val nonNullElements = collection.stream()
                .filter(_NullSafe::isPresent)
                .collect(_CanFactory.toListWithSizeUpperBound(inputSize));

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code iterable}
     * or an empty {@code Can} if the {@code iterable} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param iterable
     * @return non-null
     */
    public static <T> Can<T> ofIterable(final @Nullable Iterable<T> iterable) {

        if(iterable==null) {
            return empty();
        }

        val nonNullElements = new ArrayList<T>();
        iterable.forEach(element->{
            if(element!=null) {
                nonNullElements.add(element);
            }
        });

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code enumeration}
     * or an empty {@code Can} if the {@code enumeration} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * <p>
     * As side-effect, consumes given {@code enumeration}.
     * @param <T>
     * @param enumeration
     * @return non-null
     */
    public static <T> Can<T> ofEnumeration(final @Nullable Enumeration<T> enumeration) {

        if(enumeration==null) {
            return empty();
        }

        val nonNullElements = new ArrayList<T>();
        while(enumeration.hasMoreElements()) {
            val element = enumeration.nextElement();
            if(element!=null) {
                nonNullElements.add(element);
            }
        }
        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code stream}
     * or an empty {@code Can} if the {@code stream} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * <p>
     * As side-effect, consumes given {@code stream}.
     * @param <T>
     * @param stream
     * @return non-null
     */
    public static <T> Can<T> ofStream(final @Nullable Stream<T> stream) {

        if(stream==null) {
            return empty();
        }

        val nonNullElements = stream
                .filter(_NullSafe::isPresent)
                .collect(Collectors.toCollection(()->new ArrayList<>()));

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code instance}
     * or an empty {@code Can} if the {@code instance} is {@code null}. Any elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     * @param <T>
     * @param instance
     * @return non-null
     */
    public static <T> Can<T> ofInstance(final @Nullable Instance<T> instance) {
        if(instance==null || instance.isUnsatisfied()) {
            return empty();
        }
        if(instance.isResolvable()) {
            return Can_Singleton.of(instance.get());
        }
        val nonNullElements = instance.stream()
                .collect(Collectors.toCollection(()->new ArrayList<>()));

        return Can_Multiple.of(nonNullElements);

    }

    // -- OPERATORS

    /**
     * Returns a {@code Can} with all the elements from this {@code Can}, but
     * sorted based on
     * {@link Comparable#compareTo(Object)} order.
     * @return non-null
     */
    public Can<T> sorted(Comparator<? super T> comparator);

    /**
     * Returns a {@code Can} with all the elements from this {@code Can}, but
     * duplicated elements removed, based on
     * {@link Object#equals(Object)} object equality.
     * @return non-null
     */
    public Can<T> distinct();

    /**
     * Returns a {@code Can} with all the elements from this {@code Can}, but
     * duplicated elements removed, based on given {@code equality} relation.
     * @return non-null
     */
    public Can<T> distinct(@NonNull BiPredicate<T, T> equality);

    /**
     * Returns a {@code Can} with all the elements from this {@code Can}, but
     * contained in reversed order.
     * @return non-null
     */
    public Can<T> reverse();

    /**
     * Returns a {@code Can} with all the elements from this {@code Can},
     * that are accepted by the given {@code predicate}. If {@code predicate}
     * is {@code null} <em>all</em> elements are accepted.
     * @param predicate - if absent accepts all
     * @return non-null
     */
    public Can<T> filter(@Nullable Predicate<? super T> predicate);

    /**
     * Returns a {@code Can} with all the elements from this {@code Can}
     * 'transformed' by the given {@code mapper} function. Any resulting elements
     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * @param <R>
     * @param mapper - if absent throws if this {@code Can} is non-empty
     * @return non-null
     */
    default <R> Can<R> map(final @NonNull Function<? super T, R> mapper) {

        if(isEmpty()) {
            return empty();
        }

        val nonNullMappedElements =
                stream()
                .map(mapper)
                .filter(_NullSafe::isPresent)
                .collect(_CanFactory.toListWithSizeUpperBound(size()));

        return _CanFactory.ofNonNullElements(nonNullMappedElements);
    }

    default <R> Can<R> flatMap(final @NonNull Function<? super T, ? extends Can<? extends R>> mapper) {

        if(isEmpty()) {
            return empty();
        }

        return
                stream()
                .map(mapper)
                .filter(_NullSafe::isPresent)
                .flatMap(Can::stream)
                .collect(Can.toCan());
    }

    // -- CONCATENATION

    /**
     * Returns a {@code Can} with all the elements from given {@code can} joined by
     * the given {@code element}. If any of given {@code can} or {@code element} are {@code null}
     * these do not contribute any elements and are ignored.
     * @param <T>
     * @param can - nullable
     * @param element - nullable
     * @return non-null
     */
    public static <T> Can<T> concat(final @Nullable Can<T> can, final @Nullable T element) {
        if(can==null || can.isEmpty()) {
            return ofNullable(element);
        }
        if(element==null) {
            return can;
        }
        // at this point: can is not empty and variant is not null
        val newSize = can.size() + 1;
        val union = can.stream().collect(Collectors.toCollection(()->new ArrayList<>(newSize)));
        union.add(element);
        return Can_Multiple.of(union);
    }

    // -- TRAVERSAL

    /**
     * Returns an iterator that skips the first {@code skip} elements, then returns a
     * maximum of {@code limit} elements.
     * @param skip - similar to {@link Stream#skip(long)}
     * @param limit - similar to {@link Stream#limit(long)}
     */
    Iterator<T> iterator(int skip, int limit);

    Iterator<T> reverseIterator();

    @Override
    void forEach(@NonNull Consumer<? super T> action);

    /**
     * Similar to {@link #forEach(Consumer)}, but zipps in {@code zippedIn} to iterate through
     * its elements and passes them over as the second argument to the {@code action}.
     * @param <R>
     * @param zippedIn must have at least as much elements as this {@code Can}
     * @param action
     * @throws NoSuchElementException if {@code zippedIn} overflows
     */
    <R> void zip(Iterable<R> zippedIn, BiConsumer<? super T, ? super R> action);

    /**
     * Similar to {@link #map(Function)}, but zipps in {@code zippedIn} to iterate through
     * its elements and passes them over as the second argument to the {@code mapper}.
     * @param <R>
     * @param <Z>
     * @param zippedIn must have at least as much elements as this {@code Can}
     * @param mapper
     * @throws NoSuchElementException if {@code zippedIn} overflows
     */
    <R, Z> Can<R> zipMap(Iterable<Z> zippedIn, BiFunction<? super T, ? super Z, R> mapper);

    // -- MANIPULATION

    Can<T> add(@Nullable T element);

    /**
     * Adds the specified element to the list if it is not already present.
     * @param element
     * @return same or new instance
     */
    default Can<T> addUnique(final @Nullable T element) {
        if(contains(element)) {
            return this;
        }
        return add(element);
    }

    Can<T> addAll(@Nullable Can<T> other);

    /**
     * Inserts the specified element at the specified position in this list
     * (optional operation).  Shifts the element currently at that position
     * (if any) and any subsequent elements to the right (adds one to their
     * indices).
     *
     * @param index index at which the specified element is to be inserted
     * @param element element to be inserted
     * @return new instance
     * @throws ClassCastException if the class of the specified element
     *         prevents it from being added to this list
     * @throws NullPointerException if the specified element is null
     * @throws IllegalArgumentException if some property of the specified
     *         element prevents it from being added to this list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt; size()</tt>)
     */
    Can<T> add(int index, @Nullable T element);

    Can<T> replace(int index, @Nullable T element);

    /**
     * Removes the element at the specified position in this list (optional
     * operation).  Shifts any subsequent elements to the left (subtracts one
     * from their indices).  Returns the element that was removed from the
     * list.
     *
     * @param index the index of the element to be removed
     * @return new instance
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    Can<T> remove(int index);

    Can<T> remove(@Nullable T element);

    /**
     * Given <i>n</i> indices, returns an equivalent of
     * <pre>
     * Can.of(
     *     this.get(indices[0]).orElse(null),
     *     this.get(indices[1]).orElse(null),
     *     ...
     *     this.get(indices[n-1]).orElse(null)
     * )
     * </pre>
     * (where nulls are being ignored)
     * @param indices - null-able
     */
    Can<T> pickByIndex(@Nullable int ...indices);

    // -- SEARCH

    /**
     * Returns the index of the first occurrence of the specified element
     * in this list, or -1 if this list does not contain the element.
     * More formally, returns the lowest index <tt>i</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     *
     * @param element element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     * @throws ClassCastException if the type of the specified element
     *         is incompatible with this list
     *         (<a href="Collection.html#optional-restrictions">optional</a>)
     */
    int indexOf(@Nullable T element);

    // -- EQUALITY

    /**
     * @param other
     * @return whether this is element-wise equal to {@code other}
     */
    default boolean isEqualTo(final @Nullable Can<?> other) {
        if(other==null) {
            return false;
        }
        if(this.size()!=other.size()) {
            return false;
        }

        val otherIterator = other.iterator();

        for(T element: this) {
            val otherElement = otherIterator.next();
            if(!element.equals(otherElement)) {
                return false;
            }
        }

        return true;
    }

    // -- PARTIAL EQUALITY

    /**
     * Let {@literal n} be the number of elements in {@code other}.
     * Returns whether the first {@literal n} elements of this {@code Can} are
     * element-wise equal to {@code other}.
     * @param other
     */
    default boolean startsWith(final @Nullable Can<?> other) {
        if(other==null
                || other.isEmpty()) {
            return true;
        }
        if(this.size()<other.size()) {
            return false;
        }

        val thisIterator = this.iterator();
        val otherIterator = other.iterator();

        while(otherIterator.hasNext()) {
            val otherElement = otherIterator.next();
            val thisElement  = thisIterator.next();

            if(!thisElement.equals(otherElement)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Let {@literal n} be the number of elements in {@code other}.
     * Returns whether the last {@literal n} elements of this {@code Can} are
     * element-wise equal to {@code other}.
     * @param other
     */
    default boolean endsWith(final @Nullable Can<?> other) {
        if(other==null
                || other.isEmpty()) {
            return true;
        }
        if(this.size()<other.size()) {
            return false;
        }

        val thisIterator = this.reverseIterator();
        val otherIterator = other.reverseIterator();

        while(otherIterator.hasNext()) {
            val otherElement = otherIterator.next();
            val thisElement  = thisIterator.next();

            if(!thisElement.equals(otherElement)) {
                return false;
            }
        }
        return true;
    }

    // -- SHORTCUTS FOR PREDICATES

    default boolean isEmpty() {
        return getCardinality().isZero();
    }

    default boolean isNotEmpty() {
        return !getCardinality().isZero();
    }

    default boolean isCardinalityOne() {
        return getCardinality().isOne();
    }

    default boolean isCardinalityMultiple() {
        return getCardinality().isMultiple();
    }

    // -- COLLECTORS

    public static <T>
    Collector<T, ?, Can<T>> toCan() {

        return Collectors.collectingAndThen(
                Collectors.toList(),
                Can::ofCollection);
    }

    // -- CONVERSIONS

    /**
     * @return a serializable and immutable List, containing the elements of this Can
     */
    List<T> toList();

    /**
     * @return a serializable and immutable Set, containing the elements of this Can
     */
    Set<T> toSet();

    /**
     * @return a serializable and immutable Set, containing the elements of this Can
     */
    Set<T> toSet(@NonNull Consumer<T> onDuplicated);

//XXX to implement when needed
//    Set<T> toSortedSet();
//    Set<T> toSortedSet(Comparator<T> comparator);

    /**
     * @param <C>
     * @param collectionFactory
     * @return a collection, containing the elements of this Can
     */
    <C extends Collection<T>> C toCollection(Supplier<C> collectionFactory);

    /**
     * @param a the array into which the elements of this Can are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @return a non-null array, containing the elements of this Can
     */
    default T[] toArray(final T[] a) {
        return toList().toArray(a);
    }

    /**
     * @param elementType the {@code Class} object representing the component
     *          type of the new array
     * @return a non-null array, containing the elements of this Can
     */
    T[] toArray(Class<T> elementType);




}
