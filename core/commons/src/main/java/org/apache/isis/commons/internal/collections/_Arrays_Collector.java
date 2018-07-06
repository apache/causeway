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

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import org.apache.isis.commons.internal.base._Casts;

/**
 *
 * package private mixin for utility class {@link _Arrays}
 *
 * Collector for Arrays.
 *
 */
class _Arrays_Collector<T> implements Collector<T, _Arrays_Collector.FastList<T>, T[]> {

    private final Class<T> componentType;
    private final int size;

    _Arrays_Collector(Class<T> componentType, int size) {
        this.componentType = componentType;
        this.size = size;
    }

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
        return (a,b)->a.addAll(b);
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

        public FastList(Class<T> componentType, int size) {
            this.buffer = _Casts.uncheckedCast(Array.newInstance(componentType, size));
        }
        public void add(T x){
            buffer[offset++]=x;
        }
        public FastList<T> addAll(FastList<T> x){
            System.arraycopy(x.buffer, 0, buffer, offset, x.offset);
            return this;
        }
    }


}





