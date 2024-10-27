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
package org.apache.causeway.commons.collections;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

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
 *
 * <p>
 * <b>IMPORTANT:</b>A {@link Can} must not contain {@code null} elements. If you need to store {@code null}, then
 * use a different data structure, for example a regular {@link java.util.List java.util.List}.
 *
 * @param <T>
 * @since 2.0 {@index}
 */
public interface Can<T>
extends ImmutableCollection<T>, Comparable<Can<T>>, Serializable {

    /**
     * Will (only ever) return an empty {@link Optional}, if the elementIndex is out of bounds.
     * @param elementIndex
     * @return optionally this Can's element with index {@code elementIndex},
     * based on whether this index is within bounds
     */
    Optional<T> get(int elementIndex);

    /**
     * Shortcut for {@code get(this.size() - 1 - (-offset))}
     * @param offset - expected zero or negative (zero returning the last element)
     * @see #get(int)
     */
    default Optional<T> getRelativeToLast(final int offset) {
        return get(size() - 1 + offset);
    }

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
     * Shortcut for {@code getElseFail(this.size() - 1 - (-offset))}
     * @param offset - expected zero or negative (zero returning the last element)
     * @see #getElseFail(int)
     */
    default T getRelativeToLastElseFail(final int offset) {
        return getElseFail(size() - 1 + offset);
    }

    /**
     * For convenience allows the argument to be {@code null} treating {@code null}
     * equivalent to {@link Can#empty()}.
     * @see {@link Comparable#compareTo(Object)}
     */
    @Override
    int compareTo(final @Nullable Can<T> o);

    /**
     * @return this Can's first element or an empty Optional if no such element
     */
    Optional<T> getFirst();

    /**
     * Shortcut for {@code getFirst().orElseThrow(_Exceptions::noSuchElement)}
     * @throws NoSuchElementException if result is empty
     */
    default T getFirstElseFail() {
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
    default T getLastElseFail() {
        return getLast().orElseThrow(_Exceptions::noSuchElement);
    }

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
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
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
     * or an empty {@code Can} if the {@code array} is {@code null}.
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * </p>
     * @param <T>
     * @param array
     * @return non-null
     */
    public static <T> Can<T> ofArray(final @Nullable T[] array) {
        if(_NullSafe.size(array)==0) {
            return empty();
        }

        var nonNullElements = Stream.of(array)
                .filter(_NullSafe::isPresent)
                .collect(_CanFactory.toListWithSizeUpperBound(array.length));

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code collection}
     * or an empty {@code Can} if the {@code collection} is {@code null}.
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * @param <T>
     * @param collection
     * @return non-null
     */
    public static <T> Can<T> ofCollection(final @Nullable Collection<T> collection) {

        var inputSize = _NullSafe.size(collection);

        if(inputSize==0) {
            return empty();
        }

        var nonNullElements = collection.stream()
                .filter(_NullSafe::isPresent)
                .collect(_CanFactory.toListWithSizeUpperBound(inputSize));

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code iterable}
     * or an empty {@code Can} if the {@code iterable} is {@code null}.
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * @param <T>
     * @param iterable
     * @return non-null
     */
    public static <T> Can<T> ofIterable(final @Nullable Iterable<T> iterable) {

        if(iterable==null) {
            return empty();
        }
        // Can implements Iterable, hence there is a potential shortcut, assuming un-modifaiablitity.
        if(iterable instanceof Can) {
            return (Can<T>)iterable;
        }

        var nonNullElements = new ArrayList<T>();
        iterable.forEach(element->{
            if(element!=null) {
                nonNullElements.add(element);
            }
        });

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code enumeration}
     * or an empty {@code Can} if the {@code enumeration} is {@code null}.
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * <p>
     * <b>NOTE:</b> As side-effect, consumes given {@code enumeration}.
     *
     * @param <T>
     * @param enumeration
     * @return non-null
     */
    public static <T> Can<T> ofEnumeration(final @Nullable Enumeration<T> enumeration) {

        if(enumeration==null) {
            return empty();
        }

        var nonNullElements = new ArrayList<T>();
        while(enumeration.hasMoreElements()) {
            var element = enumeration.nextElement();
            if(element!=null) {
                nonNullElements.add(element);
            }
        }
        return _CanFactory.ofNonNullElements(nonNullElements);
    }

    /**
     * Returns either a {@code Can} with all the elements from given {@code stream}
     * or an empty {@code Can} if the {@code stream} is {@code null}.
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * <p>
     * <b>NOTE:</b> As side-effect, consumes given {@code stream}.
     *
     * @param <T>
     * @param stream
     * @return non-null
     */
    public static <T> Can<T> ofStream(final @Nullable Stream<T> stream) {

        if(stream==null) {
            return empty();
        }

        var nonNullElements = stream
                .filter(_NullSafe::isPresent)
                .collect(Collectors.toCollection(()->new ArrayList<>()));

        return _CanFactory.ofNonNullElements(nonNullElements);
    }

//    /**
//     * Returns either a {@code Can} with all the elements from given {@code instance}
//     * or an empty {@code Can} if the {@code instance} is {@code null}. Any elements
//     * equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
//     * @param <T>
//     * @param instance
//     * @return non-null
//     */
//    public static <T> Can<T> ofInstance(final @Nullable Instance<T> instance) {
//        if(instance==null || instance.isUnsatisfied()) {
//            return empty();
//        }
//        if(instance.isResolvable()) {
//            return Can_Singleton.of(instance.get());
//        }
//        var nonNullElements = instance.stream()
//                .collect(Collectors.toCollection(()->new ArrayList<>()));
//
//        return Can_Multiple.of(nonNullElements);
//
//    }

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
     * 'transformed' by the given {@code mapper} function.
     *
     * <p>
     * <b>NOTE:</b> Any elements equal to {@code null} are ignored and will not be contained in the resulting {@code Can}.
     *
     * @param <R>
     * @param mapper - if absent throws if this {@code Can} is non-empty
     * @return non-null
     */
    default <R> Can<R> map(final @NonNull Function<? super T, R> mapper) {

        if(isEmpty()) {
            return empty();
        }

        var nonNullMappedElements =
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

    /**
     * Performs a reduction on all elements, returning a {@link Can} containing
     * either a singleton reduction result or an empty {@link Can}.
     * @return non-null
     * @apiNote Reduction operating on an <i>empty</i> or <i>singleton</i> {@link Can}
     *      acts as identity operation,
     *      where given {@code accumulator} is actually never called.
     * @see Stream#reduce(BinaryOperator)
     */
    Can<T> reduce(BinaryOperator<T> accumulator);

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
        var newSize = can.size() + 1;
        var union = can.stream().collect(Collectors.toCollection(()->new ArrayList<>(newSize)));
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
     * Similar to {@link #forEach(Consumer)}, but zips in {@code zippedIn} to iterate through
     * its elements and passes them over as the second argument to the {@code action}.
     * @param <R>
     * @param zippedIn must have at least as much elements as this {@code Can}
     * @param action
     * @throws NoSuchElementException if {@code zippedIn} overflows
     */
    <R> void zip(Iterable<R> zippedIn, BiConsumer<? super T, ? super R> action);

    /**
     * Similar to {@link #map(Function)}, but zips in {@code zippedIn} to iterate through
     * its elements and passes them over as the second argument to the {@code mapper}.
     * @param <R>
     * @param <Z>
     * @param zippedIn must have at least as much elements as this {@code Can}
     * @param mapper
     * @throws NoSuchElementException if {@code zippedIn} overflows
     * @see {@link #zipStream(Iterable, BiFunction)}
     */
    <R, Z> Can<R> zipMap(Iterable<Z> zippedIn, BiFunction<? super T, ? super Z, R> mapper);

    /**
     * Semantically equivalent to {@link #zipMap(Iterable, BiFunction)}.stream().
     * <p> (Actual implementations might be optimized.)
     * @apiNote the resulting Stream will not contain {@code null} elements 
     * @param <R>
     * @param <Z>
     * @param zippedIn must have at least as much elements as this {@code Can}
     * @param mapper
     * @throws NoSuchElementException if {@code zippedIn} overflows
     * @see {@link #zipMap(Iterable, BiFunction)}
     */
    <R, Z> Stream<R> zipStream(Iterable<Z> zippedIn, BiFunction<? super T, ? super Z, R> mapper);

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
     * <p>
     * In other words: Out of bounds picking is simply ignored.
     * @param indices - null-able
     */
    Can<T> pickByIndex(@Nullable int ...indices);

    /**
     * Returns a {@link Can} that is made of the elements from this {@link Can},
     * picked by index using the given {@link IntStream} (in the order of picking).
     * <p>
     * Out of bounds picking is simply ignored.
     */
    Can<T> pickByIndex(@Nullable IntStream intStream);

    // -- SUB SETS AND PARTITIONS

    /**
     * Returns a sub-{@link Can} that is made of elements from this {@link Can},
     * starting with indices from {@code startInclusive}.
     * <p>
     * Out of bounds picking is simply ignored.
     *
     * @param startInclusive the (inclusive) initial index
     */
    Can<T> subCan(int startInclusive);

    /**
     * Returns a sub-{@link Can} that is made of elements from this {@link Can},
     * when selected by indices from given range {@code [startInclusive, endExclusive)}.
     * <p>
     * Out of bounds picking is simply ignored.
     *
     * @param startInclusive the (inclusive) initial index
     * @param endExclusive the exclusive upper bound index
     *      - if negative is interpreted as {@code this.size() - abs(endExclusive)}
     */
    Can<T> subCan(int startInclusive, int endExclusive);

    /**
     * Returns consecutive {@link #subCan(int, int) subCan},
     * each of the same maxInnerSize, while the final sub-{@link Can} may be smaller.
     * <p>
     * For example,
     * partitioning a {@link Can} containing {@code [a, b, c, d, e]} with a partition
     * size of 3 yields {@code [[a, b, c], [d, e]]} -- an outer {@link Can} containing
     * two inner {@link Can}s of three and two elements, all in the original order.
     *
     * @param maxInnerSize
     *            the desired size of each sub-{@link Can}s (the last may be smaller)
     * @return a {@link Can} of consecutive sub-{@link Can}s
     * @apiNote an alternative approach would be to distribute inner sizes as fair as possible,
     *      but this method does not
     */
    Can<Can<T>> partitionInnerBound(int maxInnerSize);

    /**
     * Tries to split this {@link Can} into outerSizeYield consecutive {@link #subCan(int, int) subCan},
     * each of the same calculated max-inner-size, while the final sub-{@link Can} may be smaller.
     * <p>
     * An outer cardinality of outerSizeYield is either exactly met or under-represented,
     * based on how many elements are actually available.
     *
     * @param outerSizeYield
     *            the desired number of sub-{@link Can}s
     * @return a {@link Can} of consecutive sub-{@link Can}s
     * @apiNote an alternative approach would be to distribute inner sizes as fair as possible,
     *      but this method does not
     */
    Can<Can<T>> partitionOuterBound(int outerSizeYield);

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

        var otherIterator = other.iterator();

        for(T element: this) {
            var otherElement = otherIterator.next();
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

        var thisIterator = this.iterator();
        var otherIterator = other.iterator();

        while(otherIterator.hasNext()) {
            var otherElement = otherIterator.next();
            var thisElement  = thisIterator.next();

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

        var thisIterator = this.reverseIterator();
        var otherIterator = other.reverseIterator();

        while(otherIterator.hasNext()) {
            var otherElement = otherIterator.next();
            var thisElement  = thisIterator.next();

            if(!thisElement.equals(otherElement)) {
                return false;
            }
        }
        return true;
    }

    // -- SHORTCUTS FOR PREDICATES

    @Override
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
     * @return a serializable and unmodifiable {@link List}, containing the elements of this Can
     */
    List<T> toList();

    /**
     * @return a (serializable and modifiable) {@link ArrayList}, containing the elements of this Can
     */
    List<T> toArrayList();

    /**
     * @return a serializable and unmodifiable {@link Set}, containing the elements of this Can
     */
    Set<T> toSet();

    /**
     * @return a serializable and unmodifiable {@link Set}, containing the elements of this Can
     */
    Set<T> toSet(@NonNull Consumer<T> onDuplicated);

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

    // -- TO MAP

    /**
     * Returns a {@link Map} with values from this {@link Can},
     * and keys as produced by given {@code keyExtractor}.
     * <p>
     * The result is protected from modification.
     * (If you instead need a modifiable result, use the {@link #collect(Collector)} method.)
     * <p>
     * On duplicate keys, behavior is unspecified.
     * @param keyExtractor a mapping function to produce keys, must be non-null
     */
    <K> Map<K, T> toMap(
            @NonNull Function<? super T, ? extends K> keyExtractor);

    /**
     * Returns a {@link Map} with values from this {@link Can},
     * and keys as produced by given {@code keyExtractor}.
     * <p>
     * The result is protected from modification.
     * (If you instead need a modifiable result, use the {@link #collect(Collector)} method.)
     * @param keyExtractor a mapping function to produce keys, must be non-null
     * @param mergeFunction a merge function, used to resolve collisions between
     *                      values associated with the same key, as supplied
     *                      to {@link Map#merge(Object, Object, BiFunction)}
     * @param mapFactory a supplier providing a new empty {@code Map}
     *                   into which the results will be inserted
     */
    <K, M extends Map<K, T>> Map<K, T> toMap(
            @NonNull Function<? super T, ? extends K> keyExtractor,
            @NonNull BinaryOperator<T> mergeFunction,
            @NonNull Supplier<M> mapFactory);

    // -- COLLECT

    /**
     * Semantically equivalent to {@link #stream()}
     * .{@link Stream#collect(Collector) collect(collector)}.
     * <p>
     * (Actual implementations might be optimized.)
     * <p>
     * Whether the result is protected from modification,
     * is up to given {@link Collector}.
     * @param <R>
     * @param <A>
     * @param collector
     */
    <R, A> R collect(@NonNull Collector<? super T, A, R> collector);

    // -- GROUP BY

    /**
     * Groups elements of this {@link Can} into a multi-valued {@link Map},
     * according to given classification function.
     * <p>
     * The result is protected from modification.
     * (If you instead need a modifiable result, use the {@link #collect(Collector)} method.)
     */
    <K> Map<K, Can<T>> groupBy(
            @NonNull Function<? super T, ? extends K> classifier);

    /**
     * Groups elements of this {@link Can} into a multi-valued {@link Map},
     * according to given classification function.
     * <p>
     * The result is protected from modification.
     * (If you instead need a modifiable result, use the {@link #collect(Collector)} method.)
     * @param mapFactory a supplier providing a new empty {@code Map}
     *      into which the results will be inserted
     */
    <K, M extends Map<K, Can<T>>> Map<K, Can<T>> groupBy(
            @NonNull Function<? super T, ? extends K> classifier,
            @NonNull Supplier<M> mapFactory);

    // -- JOIN AS STRING

    /**
     * Semantically equivalent to {@link #map(Function) map(Object::toString)}
     * <br>{@code .collect(Collectors.joining(delimiter));}
     * <p>(Actual implementations might be optimized.)
     * @param delimiter
     * @apiNote the corner case, 
     *      when the {@code Object::toString} function returns {@code null} for some elements,
     *      results in those elements simply being ignored by the join 
     */
    String join(@NonNull String delimiter);

    /**
     * Semantically equivalent to {@link #map(Function) map(toStringFunction)}
     * <br>{@code .collect(Collectors.joining(delimiter));}
     * <p>(Actual implementations might be optimized.)
     * @param toStringFunction
     * @param delimiter
     * @apiNote the corner case, 
     *      when given {@code toStringFunction} function returns {@code null} for some elements,
     *      results in those elements simply being ignored by the join 
     */
    String join(@NonNull Function<? super T, String> toStringFunction, @NonNull String delimiter);

}
