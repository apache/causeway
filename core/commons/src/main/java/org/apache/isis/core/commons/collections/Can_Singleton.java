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
package org.apache.isis.core.commons.collections;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
final class Can_Singleton<T> implements Can<T> {

    private static final long serialVersionUID = 1L;

    private final T element;

    @Getter(lazy=true, onMethod=@__({@Override})) 
    private final Optional<T> singleton = Optional.of(element);

    @Override
    public Cardinality getCardinality() {
        return Cardinality.ONE;
    }

    @Override
    public Stream<T> stream() {
        return Stream.of(element);
    }

    @Override
    public Optional<T> getFirst() {
        return getSingleton();
    }
    
    @Override
    public Optional<T> get(int elementIndex) {
        return getSingleton();
    }

    @Override
    public int size() {
        return 1;
    }
    
    @Override
    public boolean contains(T element) {
        return Objects.equals(this.element, element);
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.singletonList(element).iterator();
    }

    @Override
    public Can<T> add(@NonNull T element) {
        return Can.ofStream(Stream.of(this.element, element)); // append
    }
    
    @Override
    public Can<T> addAll(@NonNull Can<T> other) {
        if(other.isEmpty()) {
            return this;
        }
        if(other.isCardinalityOne()) {
            return add(other.getSingleton().orElseThrow(_Exceptions::unexpectedCodeReach));
        }
        val newElements = new ArrayList<T>(other.size()+1);
        newElements.add(element);
        other.forEach(newElements::add);
        return Can_Multiple.of(newElements);
    }
    
    @Override
    public Can<T> add(int index, @NonNull T element) {
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
    public Can<T> remove(int index) {
        if(index==0) {
            return Can.empty();    
        }
        throw new IndexOutOfBoundsException(
                "cannot remove from singleton with index other than 0; got " + index);
    }
    
    @Override
    public int indexOf(@NonNull T element) {
        return this.element.equals(element) ? 0 : -1;
    }
    
    @Override
    public String toString() {
        return "Can["+element+"]";
    }
    
    @Override
    public boolean equals(Object obj) {
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
    public List<T> toList() {
        return Collections.singletonList(element); // serializable and immutable
    }
    
    @Override
    public <C extends Collection<T>> C toCollection(@NonNull Supplier<C> collectionFactory) {
        val collection = collectionFactory.get();
        collection.add(element);
        return collection;
    }
    
    @Override
    public T[] toArray(@NonNull Class<T> elementType) {
        val array = _Casts.<T[]>uncheckedCast(Array.newInstance(elementType, 1));
        array[0] = element;
        return array;
    }
    
    
}
