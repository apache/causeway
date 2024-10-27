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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Objects;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class Can_Singleton<T> implements Can<T> {

    private static final long serialVersionUID = 1L;

    private final T element;

    @Override
    public Optional<T> getSingleton() {
        return Optional.of(element);
    }

    @Override
    public Cardinality getCardinality() {
        return Cardinality.ONE;
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(element);
    }

    @Override
    public Stream<T> parallelStream() {
        return Stream.of(element);
    }

    @Override
    public Optional<T> getFirst() {
        return getSingleton();
    }

    @Override
    public Optional<T> getLast() {
        return getSingleton();
    }

    @Override
    public Optional<T> get(final int elementIndex) {
        return getSingleton();
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(final @Nullable T element) {
        return Objects.equals(this.element, element);
    }

    @Override
    public Iterator<T> iterator(final int skip, final int limit) {
        return skip<1
                && limit>0
                ? iterator()
                : Collections.<T>emptyList().iterator();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.singletonList(element).iterator();
    }

    @Override
    public Can<T> sorted(@NonNull final Comparator<? super T> c) {
        return this;
    }

    @Override
    public Can<T> distinct() {
        return this;
    }

    @Override
    public Can<T> distinct(@NonNull final BiPredicate<T, T> equality) {
        return this;
    }

    @Override
    public Can<T> reverse() {
        return this;
    }

    @Override
    public Iterator<T> reverseIterator() {
        return iterator();
    }

    @Override
    public Can<T> reduce(@NonNull final BinaryOperator<T> accumulator) {
        return this; // reduction of singleton acts as identity operation
    }

    @Override
    public void forEach(@NonNull final Consumer<? super T> action) {
        action.accept(this.element);
    }

    @Override
    public Can<T> filter(final @Nullable Predicate<? super T> predicate) {
        if(predicate==null) {
            return this; // identity
        }
        return predicate.test(element)
                ? this // identity
                : Can.empty();
    }

    @Override
    public <R> void zip(final Iterable<R> zippedIn, final BiConsumer<? super T, ? super R> action) {
        action.accept(element, zippedIn.iterator().next());
    }

    @Override
    public <R, Z> Can<R> zipMap(final Iterable<Z> zippedIn, final BiFunction<? super T, ? super Z, R> mapper) {
        var next = mapper.apply(element, zippedIn.iterator().next());
        return next!=null
                ? Can_Singleton.of(next)
                : Can.empty();
    }

    @Override
    public <R, Z> Stream<R> zipStream(@NonNull final Iterable<Z> zippedIn, final BiFunction<? super T, ? super Z, R> mapper) {
        var next = mapper.apply(element, zippedIn.iterator().next());
        return next!=null
                ? Stream.of(next)
                : Stream.empty();
    }

    @Override
    public Can<T> add(final @Nullable T element) {
        return element!=null
                ? Can.ofStream(Stream.of(this.element, element)) // append
                : this;
    }

    @Override
    public Can<T> addAll(final @Nullable Can<T> other) {
        if(other==null
                || other.isEmpty()) {
            return this;
        }
        if(other.isCardinalityOne()) {
            return add(other.getSingleton().orElseThrow(_Exceptions::unexpectedCodeReach));
        }
        var newElements = new ArrayList<T>(other.size()+1);
        newElements.add(element);
        other.forEach(newElements::add);
        return _CanFactory.ofNonNullElements(newElements);
    }

    @Override
    public Can<T> add(final int index, final @Nullable T element) {
        if(element==null) {
            return this; // no-op
        }
        if(index==0) {
            return Can.ofStream(Stream.of(element, this.element)); // insert before
        }
        if(index==1) {
            return Can.ofStream(Stream.of(this.element, element)); // append
        }
        throw new IndexOutOfBoundsException(
                "cannot add to singleton with index other than 0 or 1; got " + index);
    }

    @Override
    public Can<T> replace(final int index, final @Nullable T element) {
        if(index!=0) {
            throw new IndexOutOfBoundsException(
                "cannot replace on singleton with index other than 0; got " + index);
        }
        return element!=null
                ? Can.ofSingleton(element)
                : Can.empty();
    }

    @Override
    public Can<T> remove(final int index) {
        if(index==0) {
            return Can.empty();
        }
        throw new IndexOutOfBoundsException(
                "cannot remove from singleton with index other than 0; got " + index);
    }

    @Override
    public Can<T> remove(final @Nullable T element) {
        if(this.element.equals(element)) {
            return Can.empty();
        }
        return this;
    }

    @Override
    public Can<T> pickByIndex(final @Nullable int... indices) {
        if(indices==null
                ||indices.length==0) {
            return Can.empty();
        }
        int pickCount = 0; // actual size of the returned Can<T>
        for(int index:indices) {
            if(index==0) {
                ++pickCount;
            }
        }
        if(pickCount==0) {
            return Can.empty();
        }
        if(pickCount==1) {
            return this;
        }
        var newElements = new ArrayList<T>(pickCount);
        for(int i=0; i<pickCount; i++) {
            newElements.add(element);
        }
        return _CanFactory.ofNonNullElements(newElements);
    }

    @Override
    public Can<T> pickByIndex(final @Nullable IntStream intStream) {
        if(intStream==null) {
            return Can.empty();
        }
        final long pickCountL = intStream.filter(index->index==0).count();
        if(pickCountL==0L) {
            return Can.empty();
        }
        if(pickCountL==1L) {
            return this;
        }
        if(pickCountL>Integer.MAX_VALUE) {
            throw _Exceptions.illegalArgument("pickCount %d is too large to fit into an int", pickCountL);
        }
        final int pickCount = (int) pickCountL;
        var newElements = new ArrayList<T>(pickCount);
        for(int i=0; i<pickCount; i++) {
            newElements.add(element);
        }
        return _CanFactory.ofNonNullElements(newElements);
    }

    @Override
    public Can<T> subCan(final int startInclusive) {
        return startInclusive <= 0
                ? this
                : Can.empty();
    }

    @Override
    public Can<T> subCan(final int startInclusive, final int endExclusive) {
        if (startInclusive >= endExclusive) {
            return Can.empty();
        }
        return (startInclusive<=0
                    && endExclusive>0)
                ? this
                : Can.empty();
    }

    @Override
    public Can<Can<T>> partitionInnerBound(final int maxInnerSize) {
        if(maxInnerSize<1) {
            throw _Exceptions.illegalArgument("maxInnerSize %d must be greater or equal to 1", maxInnerSize);
        }
        // a singular always fits into a single slot
        return Can.of(this);
    }

    @Override
    public Can<Can<T>> partitionOuterBound(final int outerSizeYield) {
        if(outerSizeYield<1) {
            throw _Exceptions.illegalArgument("outerSizeYield %d must be greater or equal to 1", outerSizeYield);
        }
        // a singular always fits into a single slot
        return Can.of(this);
    }

    @Override
    public int indexOf(final @Nullable T element) {
        return this.element.equals(element) ? 0 : -1;
    }

    @Override
    public String toString() {
        return "Can["+element+"]";
    }

    @Override
    public boolean equals(final Object obj) {
        if(obj instanceof Can) {
            return ((Can<?>) obj).isEqualTo(this);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public int compareTo(final @Nullable Can<T> other) {
        // when returning
        // -1 ... this (singleton) is before other
        // +1 ... this (singleton) is after other
        if(other==null
                || other.isEmpty()) {
            return 1; // all empty Cans are same and come first
        }
        final int firstElementComparison = _Objects.compareNonNull(
                this.element,
                other.getFirstElseFail());
        if(firstElementComparison!=0
                || other.isCardinalityOne()) {
            return firstElementComparison; // when both Cans are singletons, just compare by their contained values
        }
        // at this point firstElementComparison is 0 and other is of cardinality MULTIPLE
        return -1; // singletons come before multi-cans
    }

    @Override
    public List<T> toList() {
        return Collections.singletonList(element); // serializable and immutable
    }

    @Override
    public List<T> toArrayList() {
        var list = _Lists.<T>newArrayList();
        list.add(element);
        return list;
    }

    @Override
    public Set<T> toSet() {
        return Collections.singleton(element); // serializable and immutable
    }

    @Override
    public Set<T> toSet(@NonNull final Consumer<T> onDuplicated) {
        return Collections.singleton(element); // serializable and immutable
    }

    @Override
    public T[] toArray(@NonNull final Class<T> elementType) {
        var array = _Casts.<T[]>uncheckedCast(Array.newInstance(elementType, 1));
        array[0] = element;
        return array;
    }

    @Override
    public <K> Map<K, T> toMap(
            @NonNull final Function<? super T, ? extends K> keyExtractor) {
        return Map.of(keyExtractor.apply(element), element);
    }
    @Override
    public <K, M extends Map<K, T>> Map<K, T> toMap(
            @NonNull final Function<? super T, ? extends K> keyExtractor,
            @NonNull final BinaryOperator<T> mergeFunction,
            @NonNull final Supplier<M> mapFactory) {
        return toMap(keyExtractor);
    }

    @Override
    public <R, A> R collect(@NonNull final Collector<? super T, A, R> collector) {
        var container = collector.supplier().get();
        collector.accumulator().accept(container, element);
        return collector.finisher().apply(container);
    }

    @Override
    public <K> Map<K, Can<T>> groupBy(
            @NonNull final Function<? super T, ? extends K> classifier) {
        return Map.of(classifier.apply(element), this);
    }

    @Override
    public <K, M extends Map<K, Can<T>>> Map<K, Can<T>> groupBy(
            @NonNull final Function<? super T, ? extends K> classifier,
            @NonNull final Supplier<M> mapFactory) {
        return groupBy(classifier);
    }

    @Override
    public String join(@NonNull final String delimiter) {
        var str = element.toString();
        return str!=null
                ? str
                : "";
    }

    @Override
    public String join(@NonNull final Function<? super T, String> toStringFunction, @NonNull final String delimiter) {
        var str = toStringFunction.apply(element);
        return str!=null
                ? str
                : "";
    }

}
