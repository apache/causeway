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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import lombok.val;

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

    private _With() { }

    // -- OPTION IDIOMS

    /**
     * Equivalent to {@code Optional.ofNullable(obj).orElse(orElse);}
     * @param obj (nullable)
     * @param orElse (nullable)
     * @return {@code obj!=null ? obj : orElse}
     */
    public static <X> X ifPresentElse(@Nullable final X obj, @Nullable final X orElse) {
        return obj!=null ? obj : orElse;
    }

    /**
     * Equivalent to {@code Optional.ofNullable(obj).orElseGet(elseGet);}
     * @param obj (nullable)
     * @param elseGet
     * @return {@code obj!=null ? obj : elseGet.get()}
     */
    public static <X> X ifPresentElseGet(@Nullable final X obj, final Supplier<X> elseGet) {
        return obj!=null ? obj : requires(elseGet, "elseGet").get();
    }

    /**
     * Equivalent to {@code Optional.ofNullable(obj).orElseThrow(elseThrow);}
     * @param obj (nullable)
     * @param elseThrow
     * @return {@code obj!=null ? obj : throw( elseThrow.get() )}
     * @throws E
     */
    public static <X, E extends Exception> X ifPresentElseThrow(
            @Nullable final X obj,
            final Supplier<E> elseThrow)
                    throws E {
        if(obj!=null) {
            return obj;
        }
        throw requires(elseThrow, "elseThrow").get();
    }

    // -- CONSUMER IDIOMS

    /**
     * Unary identity operator that passes {@code obj} to {@code consumer}.
     * @param obj (nullable)
     * @param consumer
     * @return {@code obj}
     */
    public static <X> X accept(@Nullable final X obj, final Consumer<X> consumer) {
        requires(consumer, "consumer").accept(obj);
        return obj;
    }

    /**
     * Unary identity operator that passes {@code obj} to {@code ifPresent} if {@code obj} is present.
     * @param obj (nullable)
     * @param ifPresent
     * @return {@code obj}
     */
    public static <X> X acceptIfPresent(@Nullable final X obj, final Consumer<X> ifPresent) {
        if(obj!=null) {
            requires(ifPresent, "ifPresent").accept(obj);
        }
        return obj;
    }

    /**
     * Unary identity operator that passes {@code obj} to {@code ifPresent} if {@code obj} is present,
     * runs the specified {@code elseRun} otherwise.
     * @param obj (nullable)
     * @param ifPresent
     * @param elseRun
     * @return {@code obj}
     */
    public static <X> X acceptIfPresentElseRun(@Nullable final X obj, final Consumer<X> ifPresent, final Runnable elseRun) {
        if(obj!=null) {
            requires(ifPresent, "ifPresent").accept(obj);
        } else {
            requires(elseRun, "elseRun").run();
        }
        return obj;
    }

    /**
     * Unary identity operator that passes {@code obj} to {@code ifPresent} if {@code obj} is present,
     * throws the specified Exception provided by {@code elseThrow} otherwise.
     * @param obj (nullable)
     * @param ifPresent
     * @param elseThrow
     * @return {@code obj!=null ? obj : throw( elseThrow.get() ) }
     * @throws E
     */
    public static <X, E extends Exception> X acceptIfPresentElseThrow(
            @Nullable final X obj, final Consumer<X> ifPresent, final Supplier<E> elseThrow)
                    throws E {

        if(obj!=null) {
            requires(ifPresent, "ifPresent").accept(obj);
        } else {
            throw requires(elseThrow, "elseThrow").get();
        }
        return obj;
    }

    // -- SUPPLIER IDIOMS

    /**
     * @param obj (nullable)
     * @param supplier
     * @return {@code obj!=null ? obj : supplier.get()}
     */
    public static <X> X computeIfAbsent(@Nullable final X obj, final Supplier<X> supplier) {
        return obj!=null ? obj : requires(supplier, "supplier").get();
    }

    // -- MAPPING IDIOMS

    /**
     * Equivalent to {@code Optional.ofNullable(obj).map(mapper).orElse(orElse);}
     * @param obj (nullable)
     * @param mapper
     * @param orElse (nullable)
     * @return {@code obj!=null ? mapper.apply(obj) : orElse}
     */
    public static <X, R> R mapIfPresentElse(@Nullable final X obj, final Function<X, R> mapper, @Nullable final R orElse) {
        return obj!=null ? requires(mapper, "mapper").apply(obj) : orElse;
    }

    /**
     * Equivalent to {@code Optional.ofNullable(obj).map(mapper).orElseGet(elseGet);}
     * @param obj (nullable)
     * @param mapper
     * @param elseGet
     * @return {@code obj!=null ? mapper.apply(obj) : elseGet.get()}
     */
    public static <X, R> R mapIfPresentElseGet(@Nullable final X obj, final Function<X, R> mapper, final Supplier<R> elseGet) {
        return obj!=null ? requires(mapper, "mapper").apply(obj) : requires(elseGet, "elseGet").get();
    }

    /**
     * Equivalent to {@code Optional.ofNullable(obj).map(mapper).orElseThrow(elseThrow);}
     * @param obj (nullable)
     * @param mapper
     * @param elseThrow
     * @return {@code obj!=null ? mapper.apply(obj) : throw( elseThrow.get() )}
     * @throws E
     */
    public static <X, R, E extends Exception> R mapIfPresentElseThrow(
            @Nullable final X obj,
            final Function<X, R> mapper,
            final Supplier<E> elseThrow)
                    throws E {
        if(obj!=null) {
            return requires(mapper, "mapper").apply(obj);
        }
        throw requires(elseThrow, "elseThrow").get();
    }

    // -- PARAMETER NON-NULL CHECK

    /**
     * Allows for convenient named parameter non-null-check.
     * @param obj target for the non-null-check
     * @param paramName to use for the exception message, when the non-null-check fails
     * @return {@code obj!=null ? obj : throw NullPointerException}
     * @throws NullPointerException if {@code obj} is {@code null}
     * @deprecated instead use {@code @lombok.NonNull} on parameters
     * or {@link java.util.Objects#requireNonNull(Object, String)} on fields
     */
    @Deprecated
    public static <T> T requires(@Nullable final T obj, final String paramName) {
        if (obj == null) {
            val msg = String.format("Parameter/Field '%s' is required to be present (not null).", paramName);
            throw new IllegalArgumentException(msg);
        }
        return obj;
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
    public static String requiresNotEmpty(@Nullable final String obj, final String paramName) {
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

    /**
     * Allows for single line instantiation and initialization of an ArrayList.
     * @param initializer
     * @return a new ArrayList after calling the {@code initializer} on it
     */
    public static <X> ArrayList<X> arrayList(final Consumer<ArrayList<X>> initializer) {
        return create(ArrayList::new, initializer);
    }

    /**
     * Allows for single line instantiation and initialization of a HashSet.
     * @param initializer
     * @return a new HashSet after calling the {@code initializer} on it
     */
    public static <X> HashSet<X> hashSet(final Consumer<HashSet<X>> initializer) {
        return create(HashSet::new, initializer);
    }

    /**
     * Allows for single line instantiation and initialization of a TreeSet.
     * @param initializer
     * @return a new TreeSet after calling the {@code initializer} on it
     */
    public static <X> TreeSet<X> treeSet(final Consumer<TreeSet<X>> initializer) {
        return create(TreeSet::new, initializer);
    }

    /**
     * Allows for single line instantiation and initialization of a HashMap.
     * @param initializer
     * @return a new HashMap after calling the {@code initializer} on it
     */
    public static <K, V> HashMap<K, V> hashMap(final Consumer<HashMap<K, V>> initializer) {
        return accept(new HashMap<K, V>(), initializer);
    }

    /**
     * Allows for single line instantiation and initialization of a TreeMap.
     * @param initializer
     * @return a new TreeMap after calling the {@code initializer} on it
     */
    public static <K, V> TreeMap<K, V> treeMap(final Consumer<TreeMap<K, V>> initializer) {
        return accept(new TreeMap<K, V>(), initializer);
    }

    /**
     * Allows for single line instantiation and initialization of a StringBuilder.
     * @param initializer
     * @return a new StringBuilder after calling the {@code initializer} on it
     */
    public static StringBuilder stringBuilder(final Consumer<StringBuilder> initializer) {
        return create(StringBuilder::new, initializer);
    }

    // -- EXCEPTION SWALLOW IDIOMS

    /**
     * Returns Optional of the callable's result after invocation. Any exception during
     * invocation will result in an empty Optional.
     */
    public static <T> Optional<T> tryCall(final Callable<T> callable) {
        try {
            val result = callable.call();
            return Optional.ofNullable(result);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Returns the callable's result after invocation. Any exception during
     * invocation will result in the defaultValue being returned instead.
     */
    public static <T> T tryOrDefault(final Callable<T> callable, final T defaultValue) {
        try {
            return callable.call();
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
