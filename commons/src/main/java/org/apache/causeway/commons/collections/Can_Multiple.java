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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Objects;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(staticName="of")
final class Can_Multiple<T> implements Can<T> {

    private static final long serialVersionUID = 1L;

    private final List<T> elements;

    @Override
    public Optional<T> getFirst() {
        return Optional.of(elements.get(0));
    }

    @Override
    public Optional<T> getLast() {
        return Optional.of(elements.get(size()-1));
    }

    @Override
    public Cardinality getCardinality() {
        return Cardinality.MULTIPLE;
    }

    @Override
    public Stream<T> stream() {
        return elements.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return elements.parallelStream();
    }

    @Override
    public Optional<T> getSingleton() {
        return Optional.empty();
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean contains(final @Nullable T element) {
        if(element==null) {
            return false; // Can's dont't contain null
        }
        return elements.contains(element);
    }

    @Override
    public Optional<T> get(final int elementIndex) {
        // we do an index out of bounds check ourselves, in order to prevent any stack-traces,
        // that pollute the heap
        var size = size();
        if(size==0) {
            return Optional.empty();
        }
        var minIndex = 0;
        var maxIndex = size - 1;
        if(elementIndex < minIndex ||  elementIndex > maxIndex) {
            return Optional.empty();
        }
        return Optional.of(elements.get(elementIndex));
    }

    @Override
    public Can<T> sorted(@NonNull final Comparator<? super T> c) {
        var newElements = _Lists.<T>newArrayList(elements);
        newElements.sort(c);
        return Can_Multiple.of(newElements);
    }

    @Override
    public Can<T> distinct() {
        var set = new LinkedHashSet<T>(); // preserve order
        set.addAll(elements);
        return Can.ofCollection(set);
    }

    @Override
    public Can<T> distinct(@NonNull final BiPredicate<T, T> equality) {
        final int initialSize = Math.min(1024, elements.size());
        var uniqueElements = _Lists.<T>newArrayList(initialSize);
        elements
        .forEach(element->{
            if(!uniqueElements.stream().anyMatch(x->equality.test(x, element))) {
                uniqueElements.add(element);
            }
        });
        return _CanFactory.ofNonNullElements(uniqueElements);
    }

    @Override
    public Iterator<T> iterator(final int skip, final int limit) {
        return Collections.unmodifiableList(elements).stream()
            .skip(skip)
            .limit(limit)
            .iterator();
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }

    @Override
    public Iterator<T> reverseIterator() {
        return new Iterator<T>() {
            private int remainingCount = size();
            @Override public boolean hasNext() { return remainingCount>0; }
            @Override public T next() {
                if(!hasNext()) { throw _Exceptions.noSuchElement(); }
                return elements.get(--remainingCount);
            }
        };
    }

    @Override
    public Can<T> reverse() {
        var reverse = new ArrayList<T>(elements.size());
        for(int i=elements.size()-1; i>=0; --i) {
            reverse.add(elements.get(i));
        }
        return Can_Multiple.of(reverse);
    }

    @Override
    public Can<T> reduce(@NonNull final BinaryOperator<T> accumulator) {
        return this.stream().reduce(accumulator)
                .map(singleton->(Can<T>)Can_Singleton.of(singleton))
                .orElseGet(Can::empty);
    }

    @Override
    public void forEach(@NonNull final Consumer<? super T> action) {
        elements.forEach(action);
    }

    @Override
    public Can<T> filter(final @Nullable Predicate<? super T> predicate) {
        if(predicate==null) {
            return this; // identity
        }
        var filteredElements =
                stream()
                .filter(predicate)
                .collect(Collectors.toCollection(ArrayList::new));

        // optimization for the case when the filter accepted all
        if(filteredElements.size()==size()) {
            return this; // identity
        }
        return Can.ofCollection(filteredElements);
    }

    @Override
    public <R> void zip(@NonNull final Iterable<R> zippedIn, @NonNull final BiConsumer<? super T, ? super R> action) {
        var zippedInIterator = zippedIn.iterator();
        stream().forEach(t->{
            action.accept(t, zippedInIterator.next());
        });
    }

    @Override
    public <R, Z> Can<R> zipMap(@NonNull final Iterable<Z> zippedIn, @NonNull final BiFunction<? super T, ? super Z, R> mapper) {
        var zippedInIterator = zippedIn.iterator();
        return map(t->mapper.apply(t, zippedInIterator.next()));
    }

    @Override
    public <R, Z> Stream<R> zipStream(@NonNull final Iterable<Z> zippedIn, final BiFunction<? super T, ? super Z, R> mapper) {
        var zippedInIterator = zippedIn.iterator();
        return stream()
                .map(t->mapper.apply(t, zippedInIterator.next()))
                .filter(_NullSafe::isPresent);
    }

    @Override
    public Can<T> add(final @Nullable T element) {
        return element!=null
                ? Can.ofStream(Stream.concat(elements.stream(), Stream.of(element))) // append
                : this;
    }

    @Override
    public Can<T> addAll(final @Nullable Can<T> other) {
        if(other==null
                || other.isEmpty()) {
            return this;
        }
        var newElements = new ArrayList<T>(this.size() + other.size());
        newElements.addAll(elements);
        other.forEach(newElements::add);
        return Can_Multiple.of(newElements);
    }

    @Override
    public Can<T> add(final int index, final @Nullable T element) {
        if(element==null) {
            return this; // identity
        }
        var newElements = new ArrayList<T>(elements);
        newElements.add(index, element);
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> replace(final int index, final @Nullable T element) {
        if(element==null) {
            return remove(index);
        }
        var newElements = new ArrayList<T>(elements);
        newElements.set(index, element);
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> remove(final int index) {
        var newElements = new ArrayList<T>(elements);
        newElements.remove(index);
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> remove(final @Nullable T element) {
        if(element==null) {
            return this; // identity
        }
        var newElements = new ArrayList<T>(elements);
        newElements.remove(element);
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> pickByIndex(final @Nullable int... indices) {
        if(indices==null
                ||indices.length==0) {
            return Can.empty();
        }
        var newElements = new ArrayList<T>(indices.length);
        final int maxIndex = size()-1;
        for(int index:indices) {
            if(index>=0
                    && index<=maxIndex) {
                newElements.add(elements.get(index));
            }
        }
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> pickByIndex(final @Nullable IntStream intStream) {
        if(intStream==null) {
            return Can.empty();
        }
        var newElements = new ArrayList<T>();
        final int maxIndex = size()-1;
        intStream
        .filter(index->index>=0 && index<=maxIndex)
        .forEach(index->{
            newElements.add(elements.get(index));
        });
        return _CanFactory.ofNonNullElements(newElements);
    }

    @Override
    public Can<T> subCan(final int startInclusive) {
        return pickByIndex(IntStream.range(startInclusive, size()));
    }

    @Override
    public Can<T> subCan(final int startInclusive, final int endExclusive) {
        final int upperBoundExclusive = endExclusive < 0
                ? size() + endExclusive
                : endExclusive;
        if (startInclusive >= upperBoundExclusive) {
            return Can.empty();
        }
        return pickByIndex(IntStream.range(startInclusive, upperBoundExclusive));
    }

    @Override
    public Can<Can<T>> partitionInnerBound(final int maxInnerSize) {
        if(maxInnerSize<1) {
            throw _Exceptions.illegalArgument("maxInnerSize %d must be greater or equal to 1", maxInnerSize);
        }
        final int n = size();
        final int subCanCount = (n - 1)/maxInnerSize + 1;
        var newElements = new ArrayList<Can<T>>(subCanCount);
        for(int i=0; i<n; i+=maxInnerSize) {
            newElements.add(subCan(i, i + maxInnerSize)); // index overflow is ignored
        }
        return _CanFactory.ofNonNullElements(newElements);
    }

    @Override
    public Can<Can<T>> partitionOuterBound(final int outerSizeYield) {
        if(outerSizeYield<1) {
            throw _Exceptions.illegalArgument("outerSizeYield %d must be greater or equal to 1", outerSizeYield);
        }
        final int n = size();
        final int maxInnerSize = (n - 1)/outerSizeYield + 1;
        return partitionInnerBound(maxInnerSize);
    }

    @Override
    public int indexOf(final @Nullable T element) {
        return this.elements.indexOf(element);
    }

    @Override
    public String toString() {
        var literal = stream()
                .map(s->""+s)
                .collect(Collectors.joining(", "));
        return "Can["+literal+"]";
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
        return elements.hashCode();
    }

    @Override
    public int compareTo(final @Nullable Can<T> other) {
        // when returning
        // -1 ... this (multi-can) is before other
        // +1 ... this (multi-can) is after other
        if(other==null
                || other.isEmpty()) {
            return 1; // all empty Cans are same and come first
        }
        if(other.isCardinalityOne()) {
            final int firstElementComparison = _Objects.compareNonNull(
                    this.elements.get(0),
                    other.getSingletonOrFail());
            if(firstElementComparison!=0) {
                return firstElementComparison;
            }
        }
        // at this point firstElementComparison is 0 and other is a multi-can
        // XXX we already compared the first elements, could skip ahead for performance reasons
        if(this.size()>=other.size()) {
            var otherIterator = other.iterator();
            for(T left: this) {
                if(!otherIterator.hasNext()) {
                    return 1; // the other has fewer elements hence comes first
                }
                var right = otherIterator.next();
                int c = _Objects.compareNonNull(left, right);
                if(c!=0) {
                    return c;
                }
            }
        } else {
            var thisIterator = this.iterator();
            for(T right: other) {
                if(!thisIterator.hasNext()) {
                    return -1; // this has fewer elements hence comes first
                }
                var left = thisIterator.next();
                int c = _Objects.compareNonNull(left, right);
                if(c!=0) {
                    return c;
                }
            }
        }
        return 0; // we compared all elements and found no difference
    }

    @Override
    public List<T> toList() {
        return Collections.unmodifiableList(elements); // serializable and immutable
    }

    @Override
    public List<T> toArrayList() {
        return _Lists.newArrayList(elements);
    }

    @Override
    public Set<T> toSet() {
        var set = _Sets.<T>newHashSet(); // serializable
        set.addAll(elements);
        return Collections.unmodifiableSet(set); // serializable and immutable
    }

    @Override
    public Set<T> toSet(@NonNull final Consumer<T> onDuplicated) {
        var set = _Sets.<T>newHashSet(); // serializable
        elements
        .forEach(s->{
            if(!set.add(s)) {
                onDuplicated.accept(s);
            }
        });
        return Collections.unmodifiableSet(set); // serializable and immutable
    }

    @Override
    public T[] toArray(@NonNull final Class<T> elementType) {
        var array = _Casts.<T[]>uncheckedCast(Array.newInstance(elementType, size()));
        return elements.toArray(array);
    }

    @Override
    public <K> Map<K, T> toMap(
            @NonNull final Function<? super T, ? extends K> keyExtractor) {
        Map<K, T> map = collect(Collectors.toMap(keyExtractor, UnaryOperator.identity()));
        return Collections.unmodifiableMap(map);
    }
    @Override
    public <K, M extends Map<K, T>> Map<K, T> toMap(
            @NonNull final Function<? super T, ? extends K> keyExtractor,
            @NonNull final BinaryOperator<T> mergeFunction,
            @NonNull final Supplier<M> mapFactory) {
        Map<K, T> map = collect(Collectors.toMap(
                keyExtractor, UnaryOperator.identity(), mergeFunction, mapFactory));
        return Collections.unmodifiableMap(map);
    }

    @Override
    public <R, A> R collect(@NonNull final Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    @Override
    public <K> Map<K, Can<T>> groupBy(
            @NonNull final Function<? super T, ? extends K> classifier) {
        return groupBy(classifier, HashMap::new);
    }

    @Override
    public <K, M extends Map<K, Can<T>>> Map<K, Can<T>> groupBy(
            @NonNull final Function<? super T, ? extends K> classifier,
            @NonNull final Supplier<M> mapFactory) {
        var map = collect(Collectors.groupingBy(classifier, mapFactory, Can.toCan()));
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String join(@NonNull final String delimiter) {
        return join(Object::toString, delimiter);
    }

    @Override
    public String join(@NonNull final Function<? super T, String> toStringFunction, @NonNull final String delimiter) {
        return stream()
                .map(toStringFunction)
                .filter(_NullSafe::isPresent)
                .collect(Collectors.joining(delimiter));
    }

}
