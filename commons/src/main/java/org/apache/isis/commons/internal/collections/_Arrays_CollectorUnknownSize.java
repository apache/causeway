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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
class _Arrays_CollectorUnknownSize<T> implements Collector<T, List<T>, T[]> {

    private final Class<T> componentType;

    _Arrays_CollectorUnknownSize(Class<T> componentType) {
        this.componentType = componentType;
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (left, right) -> { left.addAll(right); return left; };
    }

    @Override
    public Function<List<T>, T[]> finisher() {
        final T[] arg = _Casts.uncheckedCast(Array.newInstance(componentType, 0));
        return list->list.toArray(arg);
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }


}





