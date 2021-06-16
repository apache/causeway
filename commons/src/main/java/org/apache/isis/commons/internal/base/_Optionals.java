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
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;

@UtilityClass
public class _Optionals {

    // can be replaced by Java 9 firstOptional.or(() -> secondOptional);
    public static <T> Optional<T> or(
            final Optional<T> a,
            final Supplier<Optional<? extends T>> b) {
        return a.isPresent()
                ? a
                : b.get().map(_Casts::uncheckedCast);
    }

    public static <T> Optional<T> or(
            final Optional<T> a,
            final Supplier<Optional<? extends T>> b,
            final Supplier<Optional<? extends T>> c) {
        return or(or(a, b), c);
    }

    public static <T> Optional<T> orNullable(
            final Optional<T> a,
            final Supplier<T> b) {
        return a.isPresent() ? a : Optional.ofNullable(b.get());
    }

    public static <T> Optional<T> orNullable(
            final Optional<T> a,
            final Supplier<T> b,
            final Supplier<T> c) {
        return orNullable(orNullable(a, b), c);
    }

}
