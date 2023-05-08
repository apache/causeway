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

import java.util.function.UnaryOperator;

import org.apache.causeway.commons.internal.assertions._Assert;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * String cutting utility.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@AllArgsConstructor(staticName = "of")
public final class _StringCutter {

    @Getter
    private final @NonNull String value;

    /**
     * Returns a new {@link _StringCutter} holding a the string value as returned by given {@code mapper}.
     */
    public _StringCutter map(final @NonNull UnaryOperator<String> mapper) {
        return _StringCutter.of(mapper.apply(value));
    }

    /**
     * Whether the held string value contains the specified
     * sequence of char values.
     *
     * @param s the sequence to search for
     * @return true if the held string value contains {@code s}, false otherwise
     */
    public boolean contains(final CharSequence s) {
        return value.contains(s);
    }

    /**
     * Searches for first occurrence of given {@code str} within
     * held string value and drops any characters that come before
     * (the matching part).
     * <p>
     * If no match acts as identity operator.
     */
    public _StringCutter dropBefore(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.indexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(index));
        }
        return this;
    }

    /**
     * Searches for first occurrence of given {@code str} within
     * held string value and drops any characters that come after
     * (the matching part).
     */
    public _StringCutter dropAfter(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.indexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(0, index + str.length()));
        }
        return this;
    }

    /**
     * Searches for last occurrence of given {@code str} within
     * held string value and drops any characters that come before
     * (the matching part).
     */
    public _StringCutter dropBeforeLast(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.lastIndexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(index));
        }
        return this;
    }

    /**
     * Searches for last occurrence of given {@code str} within
     * held string value and drops any characters that come after
     * (the matching part).
     */
    public _StringCutter dropAfterLast(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.lastIndexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(0, index + str.length()));
        }
        return this;
    }

    /**
     * Searches for first occurrence of given {@code str} within
     * held string value, keeps any characters that come before
     * (the matching part) and drops the rest.
     * <p>
     * If no match acts as identity operator.
     */
    public _StringCutter keepBefore(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.indexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(0, index));
        }
        return this;
    }

    /**
     * Searches for first occurrence of given {@code str} within
     * held string value, keeps any characters that come after
     * (the matching part) and drops the rest.
     */
    public _StringCutter keepAfter(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.indexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(index + str.length()));
        }
        return this;
    }

    /**
     * Searches for last occurrence of given {@code str} within
     * held string value, keeps any characters that come before
     * (the matching part) and drops the rest.
     * <p>
     * If no match acts as identity operator.
     */
    public _StringCutter keepBeforeLast(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.indexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(0, index));
        }
        return this;
    }

    /**
     * Searches for last occurrence of given {@code str} within
     * held string value, keeps any characters that come after
     * (the matching part) and drops the rest.
     */
    public _StringCutter keepAfterLast(final @NonNull String str) {
        _Assert.assertNotEmpty(str, ()->"can only matches against non-empty string");
        final int index = value.indexOf(str);
        if(index>-1) {
            return _StringCutter.of(value.substring(index + str.length()));
        }
        return this;
    }


}
