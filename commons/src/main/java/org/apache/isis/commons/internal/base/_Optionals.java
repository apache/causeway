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
package org.apache.isis.commons.internal.base;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import lombok.experimental.UtilityClass;

@UtilityClass
public class _Optionals {

    public static <T> Optional<T> orNullable(
            final Optional<? extends T> a,
            final Supplier<? extends T> b) {
        return a.isPresent()
                ? _Casts.uncheckedCast(a)
                : Optional.ofNullable(b.get());
    }

    public static <T> Optional<T> orNullable(
            final Optional<? extends T> a,
            final Supplier<? extends T> b,
            final Supplier<? extends T> c) {
        return orNullable(orNullable(a, b), c);
    }


    public <T> OptionalInt toInt(
            final Optional<T> optional,
            final ToIntFunction<? super T> mapper) {
        return optional.isPresent()
            ? OptionalInt.of(mapper.applyAsInt(optional.get()))
            : OptionalInt.empty();
    }

}
