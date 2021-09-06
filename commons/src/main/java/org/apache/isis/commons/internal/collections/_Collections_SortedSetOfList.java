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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 *
 * package private mixin for utility class {@link _Collections}
 *
 */
class _Collections_SortedSetOfList<T> implements SortedSet<T> {

    private static final String JUST_AN_ADAPTER =
            "this set is just an adapter, it has no information about the intended comparator";

    static <T> _Collections_SortedSetOfList<T> of(List<T> list){
        return new _Collections_SortedSetOfList<>(list);
    }

    private final List<T> list;

    private _Collections_SortedSetOfList(List<T> list) {
        this.list = list;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException(JUST_AN_ADAPTER);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <X> X[] toArray(X[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T e) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException(JUST_AN_ADAPTER);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("unmodifiable");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("unmodifiable");
    }

    @Override
    public Comparator<? super T> comparator() {
        return null;
        // this set was created from a list, we have no
        // info about the intended comparator,
        // but a least let stream traverse the elements
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException(JUST_AN_ADAPTER);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException(JUST_AN_ADAPTER);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException(JUST_AN_ADAPTER);
    }

    @Override
    public T first() {
        if(size()==0) {
            throw new NoSuchElementException("set is empty");
        }
        return list.get(0);
    }

    @Override
    public T last() {
        if(size()==0) {
            throw new NoSuchElementException("set is empty");
        }
        return list.get(size()-1);
    }
}
