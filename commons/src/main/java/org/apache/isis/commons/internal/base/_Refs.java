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
    
    public static <T> ObjectReference<T> objectRef(final T value) {
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
    
    @Data @AllArgsConstructor
    public static final class IntReference {
        private int value;
        
        public int update(final @NonNull IntUnaryOperator operator) {
            return value = operator.applyAsInt(value);
        }
        
        public boolean isSet(int other) {
            return value==other;
        }

        public int inc() {
            return ++value;
        }
        
        public int dec() {
            return --value;
        }
        
    }
    
    @Data @AllArgsConstructor
    public static final class LongReference {
        private long value;
        
        public long update(final @NonNull LongUnaryOperator operator) {
            return value = operator.applyAsLong(value);
        }
        
        public boolean isSet(long other) {
            return value==other;
        }
        
        public long inc() {
            return ++value;
        }
        
        public long dec() {
            return --value;
        }
    }
    
    @Setter @ToString @EqualsAndHashCode @AllArgsConstructor
    public static final class ObjectReference<T> {
        private T value;
        
        public T update(final @NonNull UnaryOperator<T> operator) {
            return value = operator.apply(value);
        }
        
        public boolean isSet(T other) {
            return value==other;
        }
        
        public Optional<T> getValue() {
            return Optional.ofNullable(value);
        }
        
        public T getValueElseGet(Supplier<? extends T> other) {
            return getValue().orElseGet(other);
        }
        
        public T getValueElseDefault(T defaultValue) {
            return getValue().orElse(defaultValue);
        }
        
        public T getValueElseFail() {
            return getValue().orElseThrow(_Exceptions::noSuchElement);
        }
        
    }
    
    @Setter @ToString @EqualsAndHashCode @AllArgsConstructor
    public static final class StringReference {
        private String value;
        
        public String update(final @NonNull UnaryOperator<String> operator) {
            return value = Objects.requireNonNull(operator.apply(value));
        }
        
        public boolean isSet(String other) {
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
        public boolean contains(CharSequence s) {
            return value.contains(s);
        }
        
        public String cutAtIndex(int index) {
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
        
        public String cutAtIndexOf(String s) {
            return cutAtIndex(value.indexOf(s));
        }
        
        public String cutAtLastIndexOf(String s) {
            return cutAtIndex(value.lastIndexOf(s));
        }
        
        public String cutAtIndexOfAndDrop(String s) {
            if(!value.contains(s)) {
                return "";
            }
            val left = cutAtIndex(value.indexOf(s));
            cutAtIndex(s.length());
            return left;
        }
        
        public String cutAtLastIndexOfAndDrop(String s) {
            if(!value.contains(s)) {
                return "";
            }
            val left = cutAtIndex(value.lastIndexOf(s));
            cutAtIndex(s.length());
            return left;
        }
        
    }
    
}
