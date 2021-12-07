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

import java.util.Objects;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;
import lombok.val;

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
            return value=operator.applyAsBoolean(value);
        }

        public boolean isTrue() {
            return value;
        }

        public boolean isFalse() {
            return !value;
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

        public int gatAndDec() {
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

        /**
         * At given {@code index} cuts the held {@link String} value into <i>left</i> and <i>right</i>
         * parts, returns the <i>left</i> part and replaces the held string value with the <i>right</i>
         * part.
         * <ul>
         * <li>Index underflow returns an empty string and leaves the held value unmodified.</li>
         * <li>Index overflow returns the currently held value 'then' assigns the held value an empty string.</li>
         * </ul>
         * @param index - zero based cutting point
         * @return left - cut off - part of held value (non-null)
         */
        public String cutAtIndex(final int index) {
            if(index<=0) {
                return "";
            }
            if(index>=value.length()) {
                val left = value;
                value = "";
                return left;
            }
            val left = value.substring(0, index);
            value = value.substring(index);
            return left;
        }

        /**
         * Shortcut to {@code cutAtIndex(value.indexOf(s))}.
         * <p>
         * At calculated {@code value.indexOf(s)} cuts the held {@link String} value into
         * <i>left</i> and <i>right</i> parts, returns the <i>left</i> part and replaces the held
         * string value with the <i>right</i> part.
         * <p>
         * When the substring is not found, returns an empty string and leaves the held value unmodified.
         *
         * @param   s   the substring to search for.
         * @return  left - cut off - part of held value (non-null) at the index of the first
         *          occurrence of the specified substring,
         *          or an empty string if there is no such occurrence.
         * @see #cutAtIndex(int)
         * @see String#indexOf(String)
         */
        public String cutAtIndexOf(final String s) {
            return cutAtIndex(value.indexOf(s));
        }

        /**
         * Shortcut to {@code cutAtIndex(value.lastIndexOf(s))}.
         * <p>
         * At calculated {@code value.lastIndexOf(s)} cuts the held {@link String} value into
         * <i>left</i> and <i>right</i> parts, returns the <i>left</i> part and replaces the held
         * string value with the <i>right</i> part.
         * <p>
         * When the substring is not found, returns an empty string and leaves the held value unmodified.
         *
         * @param   s   the substring to search for.
         * @return  left - cut off - part of held value (non-null) at the index of the last
         *          occurrence of the specified substring,
         *          or an empty string if there is no such occurrence.
         * @see #cutAtIndex(int)
         * @see String#lastIndexOf(String)
         */
        public String cutAtLastIndexOf(final String s) {
            return cutAtIndex(value.lastIndexOf(s));
        }

        /**
         * Variant of to {@link #cutAtIndexOf(String)}, that drops the specified substring {@code s}.
         * <p>
         * At calculated {@code value.indexOf(s)} cuts the held {@link String} value into
         * <i>left</i>, <i>dropped</i> and <i>right</i> parts, returns the <i>left</i> part and
         * replaces the held string value with the <i>right</i> part. Where the <i>dropped</i> part
         * identifies as the matching part, that equals the specified substring {@code s}.
         * <p>
         * When the substring is not found, returns an empty string and leaves the held value unmodified.
         *
         * @param   s   the substring to search for.
         * @return  left - cut off - part of held value (non-null) at the index of the first
         *          occurrence of the specified substring,
         *          or an empty string if there is no such occurrence.
         * @see #cutAtIndex(int)
         * @see String#indexOf(String)
         */
        public String cutAtIndexOfAndDrop(final String s) {
            if(!value.contains(s)) {
                return "";
            }
            val left = cutAtIndex(value.indexOf(s));
            cutAtIndex(s.length());
            return left;
        }

        /**
         * Variant of to {@link #cutAtLastIndexOf(String)}, that drops the specified substring {@code s}.
         * <p>
         * At calculated {@code value.lastIndexOf(s)} cuts the held {@link String} value into
         * <i>left</i>, <i>dropped</i> and <i>right</i> parts, returns the <i>left</i> part and
         * replaces the held string value with the <i>right</i> part. Where the <i>dropped</i> part
         * identifies as the matching part, that equals the specified substring {@code s}.
         * <p>
         * When the substring is not found, returns an empty string and leaves the held value unmodified.
         *
         * @param   s   the substring to search for.
         * @return  left - cut off - part of held value (non-null) at the index of the last
         *          occurrence of the specified substring,
         *          or an empty string if there is no such occurrence.
         * @see #cutAtIndex(int)
         * @see String#lastIndexOf(String)
         */
        public String cutAtLastIndexOfAndDrop(final String s) {
            if(!value.contains(s)) {
                return "";
            }
            val left = cutAtIndex(value.lastIndexOf(s));
            cutAtIndex(s.length());
            return left;
        }

    }

}
