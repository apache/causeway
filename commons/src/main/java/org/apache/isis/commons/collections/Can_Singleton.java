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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._Objects;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

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
    public Can<T> sorted(final @NonNull Comparator<? super T> c) {
        return this;
    }

    @Override
    public Can<T> distinct() {
        return this;
    }

    @Override
    public Can<T> distinct(final @NonNull BiPredicate<T, T> equality) {
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
    public void forEach(final @NonNull Consumer<? super T> action) {
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
        return Can_Singleton.of(mapper.apply(element, zippedIn.iterator().next()));
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
        val newElements = new ArrayList<T>(other.size()+1);
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
        val newElements = new ArrayList<T>(pickCount);
        for(int i=0; i<pickCount; i++) {
            newElements.add(element);
        }
        return _CanFactory.ofNonNullElements(newElements);
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
                other.getFirstOrFail());
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
    public Set<T> toSet() {
        return Collections.singleton(element); // serializable and immutable
    }

    @Override
    public Set<T> toSet(final @NonNull Consumer<T> onDuplicated) {
        return Collections.singleton(element); // serializable and immutable
    }

    @Override
    public <C extends Collection<T>> C toCollection(final @NonNull Supplier<C> collectionFactory) {
        val collection = collectionFactory.get();
        collection.add(element);
        return collection;
    }

    @Override
    public T[] toArray(final @NonNull Class<T> elementType) {
        val array = _Casts.<T[]>uncheckedCast(Array.newInstance(elementType, 1));
        array[0] = element;
        return array;
    }




}
