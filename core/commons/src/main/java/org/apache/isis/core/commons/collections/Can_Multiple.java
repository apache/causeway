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
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.core.commons.internal.base._Casts;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor(staticName="of")
final class Can_Multiple<T> implements Can<T> {

    private static final long serialVersionUID = 1L;

    private final List<T> elements;

    @Override
    public Optional<T> getFirst() {
        return Optional.of(elements.get(0));
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
    public Optional<T> getSingleton() {
        return Optional.empty();
    }

    @Override
    public int size() {
        return elements.size();
    }
    
    @Override
    public boolean contains(T element) {
        if(element==null) {
            return false; // an optimization: Can's dont't contain null
        }
        return elements.contains(element);
    }
    
    @Override
    public Optional<T> get(int elementIndex) {
        // we do an index out of bounds check ourselves, in order to prevent any stack-traces, 
        // that pollute the heap
        val size = size();
        if(size==0) {
            return Optional.empty();
        }
        val minIndex = 0;
        val maxIndex = size - 1;
        if(elementIndex < minIndex ||  elementIndex > maxIndex) {
            return Optional.empty();
        }
        return Optional.of(elements.get(elementIndex));
    }

    @Override
    public Iterator<T> iterator() {
        return Collections.unmodifiableList(elements).iterator();
    }

    @Override
    public Can<T> add(@NonNull T element) {
        val elementStream = Stream.concat(elements.stream(), Stream.of(element)); // append
        return Can.ofStream(elementStream); 
    }
    
    @Override
    public Can<T> addAll(@NonNull Can<T> other) {
        if(other.isEmpty()) {
            return this;
        }
        val newElements = new ArrayList<T>(this.size() + other.size());
        newElements.addAll(elements);
        other.forEach(newElements::add);
        return Can_Multiple.of(newElements);
    }
    
    @Override
    public Can<T> add(int index, @NonNull T element) {
        val newElements = new ArrayList<T>(elements);
        newElements.add(index, element);
        return Can.ofCollection(newElements);
    }

    @Override
    public Can<T> replace(int index, T element) {
        val newElements = new ArrayList<T>(elements);
        newElements.set(index, element);
        return Can.ofCollection(newElements);
    }
    
    @Override
    public Can<T> remove(int index) {
        val newElements = new ArrayList<T>(elements);
        newElements.remove(index);
        return Can.ofCollection(newElements);
    }
    
    @Override
    public Can<T> remove(T element) {
        val newElements = new ArrayList<T>(elements);
        newElements.remove(element);
        return Can.ofCollection(newElements);
    }
    
    @Override
    public int indexOf(@NonNull T element) {
        return this.elements.indexOf(element);
    }
    
    @Override
    public String toString() {
        val literal = stream()
                .map(s->""+s)
                .collect(Collectors.joining(", "));
        return "Can["+literal+"]";
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
        return elements.hashCode();
    }
    
    @Override
    public List<T> toList() {
        return Collections.unmodifiableList(elements); // serializable and immutable
    }
    
    @Override
    public <C extends Collection<T>> C toCollection(@NonNull Supplier<C> collectionFactory) {
        val collection = collectionFactory.get();
        collection.addAll(elements);
        return collection;
    }
    
    @Override
    public T[] toArray(@NonNull Class<T> elementType) {
        val array = _Casts.<T[]>uncheckedCast(Array.newInstance(elementType, size()));
        return elements.toArray(array);
    }

}
