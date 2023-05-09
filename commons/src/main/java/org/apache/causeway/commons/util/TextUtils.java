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
package org.apache.causeway.commons.util;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * Utilities for text processing.
 *
 * @since 2.0 {@index}
 */
@UtilityClass
public class TextUtils {

    // -- LINE READING

    /**
     * Converts given {@code text} into a {@link Stream} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param text - nullable
     * @return non-null
     * @apiNote Java 11+ provides {@code String.lines()}
     */
    public static Stream<String> streamLines(final @Nullable String text){
        return _Strings.splitThenStream(text, "\n")
                .map(s->s.replace("\r", ""));
    }

    /**
     * Converts given {@code text} into a {@link Can} of lines,
     * removing new line characters {@code \n,\r} in the process.
     * @param text - nullable
     * @return non-null
     */
    public static Can<String> readLines(final @Nullable String text){
        return Can.ofStream(streamLines(text));
    }

    // -- STRING CUTTER

    public StringCutter cutter(final @NonNull String value) {
        return StringCutter.of(value);
    }

    /**
     * Holder of immutable {@link String} value, that provides
     * dropping of characters before or after search and match for
     * a specific character sequence.
     */
    @AllArgsConstructor(staticName = "of", access = AccessLevel.PRIVATE)
    public final static class StringCutter {

        @Getter
        private final @NonNull String value;

        /**
         * Returns a new {@link StringCutter} holding a the string value as returned by given {@code mapper}.
         */
        public StringCutter map(final @NonNull UnaryOperator<String> mapper) {
            return StringCutter.of(mapper.apply(value));
        }

        /**
         * Whether the held string value contains the specified
         * sequence of char values.
         *
         * @param str the sequence to search for
         * @return true if the held string value contains {@code str},
         *  false otherwise (as well as when null or empty)
         */
        public boolean contains(final @Nullable CharSequence str) {
            return _Strings.isNullOrEmpty(str)
                    ? false
                    : value.contains(str);
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value and drops any characters that come before
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropBefore(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index));
            }
            return this;
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value and drops any characters that come after
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropAfter(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index + str.length()));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value and drops any characters that come before
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropBeforeLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.lastIndexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value and drops any characters that come after
         * (the matching part).
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter dropAfterLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.lastIndexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index + str.length()));
            }
            return this;
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value, keeps any characters that come before
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepBefore(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index));
            }
            return this;
        }

        /**
         * Searches for first occurrence of given {@code str} within
         * held string value, keeps any characters that come after
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepAfter(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index + str.length()));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value, keeps any characters that come before
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepBeforeLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(0, index));
            }
            return this;
        }

        /**
         * Searches for last occurrence of given {@code str} within
         * held string value, keeps any characters that come after
         * (the matching part) and drops the rest.
         * <p>
         * If no match, then acts as identity operator.
         */
        public StringCutter keepAfterLast(final @NonNull String str) {
            _Assert.assertNotEmpty(str, this::matcherNotEmptyMsg);
            final int index = value.indexOf(str);
            if(index>-1) {
                return StringCutter.of(value.substring(index + str.length()));
            }
            return this;
        }

        // -- HELPER

        private String matcherNotEmptyMsg() {
            return "can only match search and match for non-empty string";
        }

    }

}
