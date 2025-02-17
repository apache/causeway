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
package org.apache.causeway.commons.internal.collections;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.causeway.commons.internal.base._Casts;

/**
 * package private helper for utility class {@link _Arrays}
 *
 * Collector for Arrays.
 */
record _Arrays_Collector<T>(
        Class<T> componentType,
        int size
        ) implements Collector<T, _Arrays_Collector.FastList<T>, T[]> {

    @Override
    public Supplier<FastList<T>> supplier() {
        return ()->new FastList<>(componentType, size);
    }

    @Override
    public BiConsumer<FastList<T>, T> accumulator() {
        return FastList::add;
    }

    @Override
    public BinaryOperator<FastList<T>> combiner() {
        return (a, b)->a.addAll(b);
    }

    @Override
    public Function<FastList<T>, T[]> finisher() {
        return list->list.buffer;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }

    // -- HELPER

    static final class FastList<T> {
        private final T[] buffer;
        private int offset=0;

        public FastList(final Class<T> componentType, final int size) {
            this.buffer = _Casts.uncheckedCast(Array.newInstance(componentType, size));
        }
        public void add(final T x){
            buffer[offset++]=x;
        }
        public FastList<T> addAll(final FastList<T> x){
            System.arraycopy(x.buffer, 0, buffer, offset, x.offset);
            return this;
        }
    }

}
