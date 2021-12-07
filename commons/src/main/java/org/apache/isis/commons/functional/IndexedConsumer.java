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
package org.apache.isis.commons.functional;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.isis.commons.internal.base._Refs;

import lombok.val;

/**
 * Similar to a {@link BiConsumer}, except that the first argument is an {@code int}.
 *
 * @since 2.x [@index}
 */
@FunctionalInterface
public interface IndexedConsumer<T> {

    // -- INTERFACE

    void accept(int index, T t);

    // -- UTILITY

    /**
     * Converts an {@link IndexedConsumer} into a {@link Consumer},
     * having its index start at {@code offset},
     * and incremented after each call.
     */
    static <T> Consumer<T> offset(final int offset, final IndexedConsumer<T> indexed){
        val indexRef = _Refs.intRef(offset);
        return t->indexed.accept(indexRef.getAndInc(), t);
    }

    /**
     * Converts an {@link IndexedConsumer} into a {@link Consumer},
     * having its index start at 0,
     * and incremented after each call.
     */
    static <T> Consumer<T> zeroBased(final IndexedConsumer<T> indexed){
        return offset(0, indexed);
    }

}
