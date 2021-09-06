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
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import lombok.NonNull;

/**
 *
 * package private mixin for utility class {@link _Collections}
 *
 * Collector for Collections.
 *
 */
class _Collections_Collector<T, C extends Collection<T>> implements Collector<T, C, C> {

    private final Supplier<C> supplier;
    private final Function<C, C> finisher;

    _Collections_Collector(final @NonNull Supplier<C> supplier, final @NonNull Function<C, C> finisher) {
        this.supplier = supplier;
        this.finisher = finisher;
    }

    @Override
    public Supplier<C> supplier() {
        return supplier;
    }

    @Override
    public BiConsumer<C, T> accumulator() {
        return Collection::add;
    }

    @Override
    public BinaryOperator<C> combiner() {
        return (left, right) -> { left.addAll(right); return left; };
    }

    @Override
    public Function<C, C> finisher() {
        return finisher;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Collections.emptySet();
    }


}





