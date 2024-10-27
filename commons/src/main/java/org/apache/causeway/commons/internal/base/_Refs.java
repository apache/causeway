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

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Not thread-safe, primitive and object references.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public final class _Refs {

    // -- FACTORIES

    public static BooleanReference booleanRef(final boolean value) {
        return new BooleanReference(value);
    }

    public static BooleanAtomicReference booleanAtomicRef(final boolean value) {
        return new BooleanAtomicReference(value);
    }

    public static IntReference intRef(final int value) {
        return new IntReference(value);
    }

    public static LongReference longRef(final int value) {
        return new LongReference(value);
    }

    public static <T> ObjectReference<T> objectRef(final @Nullable T value) {
        return new ObjectReference<>(value);
    }

    public static StringReference stringRef(final @NonNull String value) {
        return new StringReference(value);
    }

    // -- IMPLEMENTATIONS

    @FunctionalInterface
    public static interface BooleanUnaryOperator {
        boolean applyAsBoolean(boolean value);
    }

    /**
     * Holder of a mutable primitive {@code boolean} value.
     * @since 2.0
     */
    @Data @AllArgsConstructor
    public static final class BooleanReference {
        private boolean value;

        public boolean update(final @NonNull BooleanUnaryOperator operator) {
            return value = operator.applyAsBoolean(value);
        }

        public boolean isTrue() {
            return value;
        }

        public boolean isFalse() {
            return !value;
        }
    }

    /**
     * Serializable thread-safe boolean reference.
     * @apiNote unfortunately {@link AtomicBoolean} does not quite provide
     * conditional thread-safe value update
     */
    @AllArgsConstructor
    public static final class BooleanAtomicReference implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean value;
        private final Object $lock = new Object[0]; // serializable lock

        public boolean compute(final @NonNull BooleanUnaryOperator operator) {
            synchronized ($lock) {
                return value = operator.applyAsBoolean(value);
            }
        }

        public boolean computeIfFalse(final @NonNull BooleanSupplier supplier) {
            synchronized ($lock) {
                return value==false
                        ? value = supplier.getAsBoolean()
                        : true;
            }
        }

        public boolean computeIfTrue(final @NonNull BooleanSupplier supplier) {
            synchronized ($lock) {
                return value==true
                        ? value = supplier.getAsBoolean()
                        : false;
            }
        }

        public boolean isTrue() {
            synchronized ($lock) {
                return value;
            }
        }

        public boolean isFalse() {
            synchronized ($lock) {
                return !value;
            }
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof BooleanAtomicReference
                ? this.isTrue() == ((BooleanAtomicReference)obj).isTrue()
                : false;
        }

        @Override
        public int hashCode() {
            return isTrue() ? 1 : -1;
        }

    }

    /**
     * Holder of a mutable primitive {@code int} value.
     * @since 2.0
     */
    @Data @AllArgsConstructor
    public static final class IntReference {
        private int value;

        public int update(final @NonNull IntUnaryOperator operator) {
            return value = operator.applyAsInt(value);
        }

        public boolean isSet(final int other) {
            return value==other;
        }

        public int incAndGet() {
            return ++value;
        }

        public int decAndGet() {
            return --value;
        }

        public int getAndInc() {
            return value++;
        }

        public int getAndDec() {
            return value--;
        }
    }

    /**
     * Holder of a mutable primitive {@code long} value.
     * @since 2.0
     */
    @Data @AllArgsConstructor
    public static final class LongReference {
        private long value;

        public long update(final @NonNull LongUnaryOperator operator) {
            return value = operator.applyAsLong(value);
        }

        public boolean isSet(final long other) {
            return value==other;
        }

        public long inc() {
            return ++value;
        }

        public long dec() {
            return --value;
        }
    }

    /**
     * Holder of a nullable and mutable {@link Object} value.
     * @since 2.0
     */
    @Setter @ToString @EqualsAndHashCode @AllArgsConstructor
    public static final class ObjectReference<T> {
        private @Nullable T value;

        public T set(final @Nullable T value) {
            return this.value = value;
        }

        public T update(final @NonNull UnaryOperator<T> operator) {
            return value = operator.apply(value);
        }

        public T computeIfAbsent(final @NonNull Supplier<T> factory) {
            return value!=null
                    ? value
                    : set(factory.get());
        }

        public boolean isNull() {
            return value==null;
        }

        public boolean isNotNull() {
            return value!=null;
        }

        public boolean isSet(final @Nullable T other) {
            return value==other;
        }

        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }

        public T getValueElseGet(final Supplier<? extends T> other) {
            return getValue().orElseGet(other);
        }

        public T getValueElseDefault(final T defaultValue) {
            return getValue().orElse(defaultValue);
        }

        public T getValueElseFail() {
            return getValue().orElseThrow(_Exceptions::noSuchElement);
        }

    }

    /**
     * Holder of a non-null and mutable {@link String} value.
     * @since 2.0
     */
    @Setter @ToString @EqualsAndHashCode @AllArgsConstructor
    public static final class StringReference {
        private @NonNull String value;

        public StringReference update(final @NonNull UnaryOperator<String> operator) {
            value = Objects.requireNonNull(operator.apply(value));
            return this;
        }

        public boolean isSet(final String other) {
            return value.equals(other);
        }

        public String getValue() {
            return value;
        }

        /**
         * Returns true if and only if this string contains the specified
         * sequence of char values.
         *
         * @param s the sequence to search for
         * @return true if this string contains {@code s}, false otherwise
         */
        public boolean contains(final CharSequence s) {
            return value.contains(s);
        }

    }

}
