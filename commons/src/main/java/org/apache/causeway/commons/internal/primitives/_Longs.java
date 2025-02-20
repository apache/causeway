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
package org.apache.causeway.commons.internal.primitives;

import java.util.OptionalLong;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.primitives._Ints.BiIntConsumer;
import org.apache.causeway.commons.internal.primitives._Ints.BiIntFunction;

import org.jspecify.annotations.NonNull;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Long Utility
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
public record _Longs() {

    // -- RANGE

    public record Bound(
            long value,
            boolean isInclusive) {
        public static @NonNull Bound inclusive(final long value) { return new Bound(value, true); }
        public static @NonNull Bound exclusive(final long value) { return new Bound(value, true); }
    }

    public record Range(
            @NonNull Bound lowerBound,
            @NonNull Bound upperBound) {

        public boolean contains(final long value) {
            var isBelowLower = lowerBound.isInclusive()
                    ? value < lowerBound.value()
                    : value <= lowerBound.value();
            if(isBelowLower) return false;

            var isAboveUpper = upperBound.isInclusive()
                    ? value > upperBound.value()
                    : value >= upperBound.value();
            if(isAboveUpper) return false;

            return true;
        }
        /**
         * @param value
         * @return the value or if not within range, the nearest integer to the value, that is within range
         */
        public long bounded(final long value) {
            //if(empty) return value; // noop
            if(contains(value)) {
                return value;
            }
            final long nearestToLower = nearestToLower();
            final long nearestToUpper = nearestToUpper();
            final long distanceToLower = value - nearestToLower;
            final long distanceToUpper = value - nearestToUpper;
            return (distanceToLower <= distanceToUpper)
                    ? nearestToLower
                    : nearestToUpper;
        }
        private long nearestToLower() {
            //if(empty) throw _Exceptions.unsupportedOperation();
            return lowerBound.isInclusive() ? lowerBound.value() : lowerBound.value()+1;
        }
        private long nearestToUpper() {
            //if(empty) throw _Exceptions.unsupportedOperation();
            return upperBound.isInclusive() ? upperBound.value() : upperBound.value()-1;
        }
        @Override
        public String toString() {
            return String.format("%s%d,%d%S",
                    lowerBound.isInclusive() ? '[' : '(', lowerBound.value(),
                    upperBound.value(), upperBound.isInclusive() ? ']' : ')');
        }
    }

    // -- RANGE FACTORIES

    /**
     * Range includes a and b.
     */
    public static Range rangeClosed(final long a, final long b) {
        if(a>b) {
            throw _Exceptions.illegalArgument("bounds must be ordered in [%d, %d]", a, b);
        }
        return new Range(Bound.inclusive(a), Bound.inclusive(b));
    }

    /**
     * Range includes a but not b.
     */
    public static Range rangeOpenEnded(final long a, final long b) {
        if(a==b) {
            throw _Exceptions.unsupportedOperation("empty range not implemented");
            //return Range.empty();
        }
        if(a>=b) {
            throw _Exceptions.illegalArgument("bounds must be ordered in [%d, %d]", a, b);
        }
        return new Range(Bound.inclusive(a), Bound.exclusive(b));
    }

    // -- PARSING

    /**
     * Parses the string argument as a signed integer in the radix
     * specified by the second argument. The characters in the string
     * must all be digits of the specified radix (as determined by
     * whether {@link java.lang.Character#digit(char, int)} returns a
     * nonnegative value), except that the first character may be an
     * ASCII minus sign {@code '-'} ({@code '\u005Cu002D'}) to
     * indicate a negative value or an ASCII plus sign {@code '+'}
     * ({@code '\u005Cu002B'}) to indicate a positive value. The
     * resulting integer value is returned.
     *
     *
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or
     * larger than {@link java.lang.Character#MAX_RADIX}.
     *
     * <li>Any character of the string is not a digit of the specified
     * radix, except that the first character may be a minus sign
     * {@code '-'} ({@code '\u005Cu002D'}) or plus sign
     * {@code '+'} ({@code '\u005Cu002B'}) provided that the
     * string is longer than length 1.
     *
     * <li>The value represented by the string is not a value of type
     * {@code int}.
     * </ul>
     *
     * @param      s   the {@code String} containing the integer
     *                  representation to be parsed
     * @param      radix   the radix to be used while parsing {@code s}.
     * @param      onFailure on parsing failure consumes the failure message
     * @return optionally the long represented by the string argument in the specified radix
     * @implNote Copied over from JDK's {@link Integer#parseInt(String)} to provide a variant
     * with minimum potential heap pollution (does not produce stack-traces on parsing failures)
     */
    public static OptionalLong parseLong(final @Nullable String s, final int radix, final @NonNull Consumer<String> onFailure) {

        if (s == null) {
            onFailure.accept("null");
            return OptionalLong.empty();
        }

        if (radix < Character.MIN_RADIX) {
            onFailure.accept("radix " + radix + " less than Character.MIN_RADIX");
            return OptionalLong.empty();
        }
        if (radix > Character.MAX_RADIX) {
            onFailure.accept("radix " + radix + " greater than Character.MAX_RADIX");
            return OptionalLong.empty();
        }

        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+') {
                    onFailure.accept(s);
                    return OptionalLong.empty();
                }
                if (len == 1) {// Cannot have lone "+" or "-"
                    onFailure.accept(s);
                    return OptionalLong.empty();
                }
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    onFailure.accept(s);
                    return OptionalLong.empty();
                }
                if (result < multmin) {
                    onFailure.accept(s);
                    return OptionalLong.empty();
                }
                result *= radix;
                if (result < limit + digit) {
                    onFailure.accept(s);
                    return OptionalLong.empty();
                }
                result -= digit;
            }
        } else {
            onFailure.accept(s);
            return OptionalLong.empty();
        }
        return OptionalLong.of(negative ? result : -result);
    }

    // -- INT PACKING

    /**
     * For reference see <a href="http://stackoverflow.com/questions/12772939/java-storing-two-ints-in-a-long">link</a>.
     * @param lower
     * @param upper
     */
    public static long pack(final int lower, final int upper) {
        return (((long)upper) << 32) | (lower & 0xffffffffL);
    }

    public static void unpackAndAccept(final long x, final BiIntConsumer consumer) {
        consumer.accept((int)x,(int)(x>>>32));
    }

    public static <X> X unpackAndApply(final long x, final BiIntFunction<X> function) {
        return function.apply((int)x,(int)(x>>>32));
    }

    // -- SHORTCUTS

    public static OptionalLong parseLong(final String s, final int radix) {
        return parseLong(s, radix, IGNORE_ERRORS);
    }

    // -- HELPER

    private static final Consumer<String> IGNORE_ERRORS = t->{};

}
