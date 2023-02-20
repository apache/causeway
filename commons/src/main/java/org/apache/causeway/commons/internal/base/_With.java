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
package org.apache.causeway.commons.internal.base;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Provides shortcuts for common 'Optional' idioms.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _With<T> {

    // -- CONSUMER IDIOMS

    /**
     * Unary identity operator that passes {@code obj} to {@code consumer}.
     * @param obj (nullable)
     * @param consumer
     * @return {@code obj}
     */
    public static <X> X accept(final @Nullable X obj, final Consumer<X> consumer) {
        Objects.requireNonNull(consumer, "consumer").accept(obj);
        return obj;
    }

    // -- SUPPLIER IDIOMS

    /**
     * @param obj (nullable)
     * @param supplier
     * @return {@code obj!=null ? obj : supplier.get()}
     */
    public static <X> X computeIfAbsent(final @Nullable X obj, final Supplier<X> supplier) {
        return obj!=null ? obj : Objects.requireNonNull(supplier, "supplier").get();
    }

    // -- MAPPING IDIOMS

    /**
     * Equivalent to {@code Optional.ofNullable(obj).map(mapper).orElse(orElse);}
     * @param obj (nullable)
     * @param mapper
     * @param orElse (nullable)
     * @return {@code obj!=null ? mapper.apply(obj) : orElse}
     */
    public static <X, R> R mapIfPresentElse(final @Nullable X obj, final Function<X, R> mapper, final @Nullable R orElse) {
        return obj!=null ? Objects.requireNonNull(mapper, "mapper").apply(obj) : orElse;
    }

    // -- PARAMETER NON-EMPTY CHECK(S)

    /**
     * Allows for convenient named parameter non-empty-check.
     * @param obj target for the non-empty-check
     * @param paramName to use for the exception message, when the non-empty-check fails
     * @return {@code obj}
     * @throws NullPointerException if {@code obj} is {@code null}
     * @throws IllegalArgumentException if {@code obj} is 'empty'
     */
    public static String requiresNotEmpty(final @Nullable String obj, final String paramName) {
        if (obj == null) {
            throw new NullPointerException(String.format("Parameter/Field '%s' is required to be present (not null).", paramName));
        }
        if (obj.length()==0) {
            throw new IllegalArgumentException(String.format("Parameter/Field '%s' is required to be present and not empty.", paramName));
        }
        return obj;
    }

    // -- CONVENIENT CONSTRUCTORS

    /**
     * Allows for single line instantiation and initialization of an Object.
     * @param factory
     * @param initializer
     * @return a new Object as provided by {@code factory} after calling the {@code initializer} on it
     */
    public static <T> T create(final Supplier<T> factory, final Consumer<T> initializer) {
        return accept(factory.get(), initializer);
    }

}
