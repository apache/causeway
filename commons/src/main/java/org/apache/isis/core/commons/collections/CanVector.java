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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

import lombok.NonNull;

/** 
 * Represents a mutable, but fixed size vector of Can<T>.
 * <p>
 * Mutable meaning, that the vector elements can be replaced.
 * 
 * @since Jun 30, 2020
 */
public final class CanVector<T> implements Iterable<Can<T>>, Serializable {

    private static final long serialVersionUID = 1L;
    
    private final int size;
    private final List<Can<T>> cans;
    
    public CanVector(int size) {
        this.size = size;
        this.cans = new ArrayList<>(size);
        // init vector with empty cans
        IntStream.range(0, size).forEach(__->cans.add(Can.empty()));
    }

    public int size() {
        return size;
    }
    
    @Override
    public Iterator<Can<T>> iterator() {
        return cans.iterator();
    }

    /**
     * Returns the element at the specified position in this CanVector.
     * @param index
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public Can<T> get(int index) {
        return cans.get(index);
    }
    
    /**
     * Replaces the element at the specified position in this CanVector with the
     * specified element.
     * 
     * @param index
     * @param can
     * @return self
     * @throws IndexOutOfBoundsException if the index is out of range
     *         (<tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public CanVector<T> set(int index, @NonNull Can<T> can) {
        cans.set(index, can);
        return this;
    }

    public static <T> CanVector<T> empty() {
        return new CanVector<>(0);
    }
    
    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true; // optimization not strictly necessary
        }
        return (obj instanceof CanVector)
                ? cans.equals(((CanVector<?>)obj).cans)
                : false;
    }
    
    @Override
    public int hashCode() {
        return cans.hashCode();
    }
    
    
}
