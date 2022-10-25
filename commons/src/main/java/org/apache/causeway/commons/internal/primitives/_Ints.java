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

import java.util.Optional;
import java.util.OptionalInt;
import java.util.PrimitiveIterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import lombok.experimental.UtilityClass;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Integer Utility
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
public class _Ints {

    // -- RANGE

    @Value(staticConstructor = "of")
    public static class Bound {
        int value;
        boolean inclusive;
        public static @NonNull Bound inclusive(final int value) { return of(value, true); }
        public static @NonNull Bound exclusive(final int value) { return of(value, true); }
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Range {

        public static Range empty() {
            return new Range(null, null, true);
        }

        public static Range of(
                final @NonNull Bound lowerBound,
                final @NonNull Bound upperBound) {
            return new Range(lowerBound, upperBound, false);
        }

        private final Bound lowerBound;
        private final Bound upperBound;
        private final boolean empty;
        public boolean contains(final int value) {
            if(empty) return false;
            val isBelowLower = lowerBound.isInclusive()
                    ? value < lowerBound.getValue()
                    : value <= lowerBound.getValue();
            if(isBelowLower) {
                return false;
            }
            val isAboveUpper = upperBound.isInclusive()
                    ? value > upperBound.getValue()
                    : value >= upperBound.getValue();
            if(isAboveUpper) {
                return false;
            }
            return true;
        }
        /**
         * @param value
         * @return the value or if not within range, the nearest integer to the value, that is within range
         */
        public int bounded(final int value) {
            if(empty) return value; // noop
            if(contains(value)) {
                return value;
            }
            final int nearestToLower = nearestToLower();
            final int nearestToUpper = nearestToUpper();
            final int distanceToLower = value - nearestToLower;
            final int distanceToUpper = value - nearestToUpper;
            return (distanceToLower <= distanceToUpper)
                    ? nearestToLower
                    : nearestToUpper;
        }
        private int nearestToLower() {
            if(empty) throw _Exceptions.unsupportedOperation();
            return lowerBound.isInclusive() ? lowerBound.getValue() : lowerBound.getValue()+1;
        }
        private int nearestToUpper() {
            if(empty) throw _Exceptions.unsupportedOperation();
            return upperBound.isInclusive() ? upperBound.getValue() : upperBound.getValue()-1;
        }
        public @NonNull Optional<Range> intersect(final @NonNull Range other) {
            if(empty) return Optional.empty();
            final int s1 = this.nearestToLower();
            final int e1 = this.nearestToUpper();
            final int s2 = other.nearestToLower();
            final int e2 = other.nearestToUpper();
            if(s2>e1 || s1>e2) {
                return Optional.empty();
            }
            return Optional.of(of(
                    Bound.inclusive(Math.max(s1, s2)),
                    Bound.inclusive(Math.min(e1, e2))));
        }
        @Override
        public String toString() {
            if(empty) return "[]";
            return String.format("%s%d,%d%S",
                    lowerBound.isInclusive() ? '[' : '(', lowerBound.getValue(),
                    upperBound.getValue(), upperBound.isInclusive() ? ']' : ')');
        }
        public IntStream stream() {
            if(empty) return IntStream.empty();
            return IntStream.rangeClosed(nearestToLower(), nearestToUpper());
        }
        public PrimitiveIterator.OfInt iterator() {
            if(empty) return IntStream.empty().iterator();
            return new PrimitiveIterator.OfInt() {
                int next = nearestToLower();
                final int upperIncluded = nearestToUpper();

                @Override
                public int nextInt() {
                    if(!hasNext()) {
                        throw _Exceptions.noSuchElement();
                    }
                    int result = next;
                    next++;
                    return result;
                }

                @Override
                public boolean hasNext() {
                    return next <= upperIncluded;
                }
            };
        }
    }

    // -- RANGE FACTORIES

    /**
     * Range includes a and b.
     */
    public static Range rangeClosed(final int a, final int b) {
        if(a>b) {
            throw _Exceptions.illegalArgument("bounds must be ordered in [%d, %d]", a, b);
        }
        return Range.of(Bound.inclusive(a), Bound.inclusive(b));
    }

    /**
     * Range includes a but not b.
     */
    public static Range rangeOpenEnded(final int a, final int b) {
        if(a==b) {
            return Range.empty();
        }
        if(a>=b) {
            throw _Exceptions.illegalArgument("bounds must be ordered in [%d, %d]", a, b);
        }
        return Range.of(Bound.inclusive(a), Bound.exclusive(b));
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
     * @return optionally the integer represented by the string argument in the specified radix
     *
     */
    public OptionalInt parseInt(final String s, final int radix, final Consumer<String> onFailure) {
        final long parseResult = parseIntElseLongMaxValue(s, radix, onFailure);
        if(isParseSuccess(parseResult)) {
            return OptionalInt.of(Math.toIntExact(parseResult));
        }
        return OptionalInt.empty();
    }

    // -- SHORTCUTS

    public OptionalInt parseInt(final String s, final int radix) {
        return parseInt(s, radix, IGNORE_ERRORS);
    }

    // -- LOW LEVEL HELPER

    private static boolean isParseSuccess(final long value) {
        return value!=Long.MAX_VALUE;
    }

    private static final Consumer<String> IGNORE_ERRORS = t->{};

    /**
     * @implNote Copied over from JDK's {@link Integer#parseInt(String)} to provide a variant
     * with minimum potential heap pollution (does not produce stack-traces on parsing failures)
     */
    private static long parseIntElseLongMaxValue(
            final @Nullable String s,
            final int radix,
            final @NonNull Consumer<String> onFailure) {

        if (s == null) {
            onFailure.accept("null");
            return Long.MAX_VALUE;
        }

        if (radix < Character.MIN_RADIX) {
            onFailure.accept("radix " + radix +
                                            " less than Character.MIN_RADIX");
            return Long.MAX_VALUE;
        }

        if (radix > Character.MAX_RADIX) {
            onFailure.accept("radix " + radix +
                                            " greater than Character.MAX_RADIX");
            return Long.MAX_VALUE;
        }

        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+') {
                    onFailure.accept(s);
                    return Long.MAX_VALUE;
                }

                if (len == 1) { // Cannot have lone "+" or "-"
                    onFailure.accept(s);
                    return Long.MAX_VALUE;
                }
                i++;
            }
            int multmin = limit / radix;
            int result = 0;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                int digit = Character.digit(s.charAt(i++), radix);
                if (digit < 0 || result < multmin) {
                    onFailure.accept(s);
                    return Long.MAX_VALUE;
                }
                result *= radix;
                if (result < limit + digit) {
                    onFailure.accept(s);
                    return Long.MAX_VALUE;
                }
                result -= digit;
            }
            val value = negative ? result : -result;
            return value;
        } else {
            onFailure.accept(s);
            return Long.MAX_VALUE;
        }
    }

    // -- ARRAY FLATTEN

    public static int[] flatten(final @NonNull int[][] nested) {
        final int n = nested.length;
        final int stride = nested[0].length;
        val flattened = new int[n*stride];
        for(int i=0; i<n; ++i) {
            System.arraycopy(nested[i], 0, flattened, i*stride, stride);
        }
        return flattened;
    }

    // -- ARRAY PARTITION

    public static int[][] partition(final @NonNull int[] flattened, final int stride) {
        final int n = flattened.length/stride;
        _Assert.assertEquals(flattened.length, n*stride, ()->"flattened.length must be divisible by stride");
        val nested = new int[n][stride];
        for(int i=0; i<n; ++i) {
            System.arraycopy(flattened, i*stride, nested[i], 0, stride);
        }
        return nested;
    }

    // -- PRINTING

    public static String rowForm(
            final @NonNull int[] array) {
        return rowForm(array, 8, Integer::toString);
    }

    public static String rowForm(
            final @NonNull int[] array,
            final int columnWidth,
            final @NonNull IntFunction<String> cellFormatter) {

        final int m = array.length;
        val sb = new StringBuilder();

        for(int j=0; j<m; ++j) {
            final int cellValue = array[j];
            val cellStringFull = cellFormatter.apply(cellValue);
            val cellStringTrimmed = _Strings.ellipsifyAtEnd(cellStringFull, columnWidth, "..");

            // right align, at column end marker
            final int spacesCount = columnWidth - cellStringTrimmed.length();
            for(int k=0; k<spacesCount; ++k) {
                sb.append(' ');
            }
            sb.append(cellStringTrimmed);
        }
        sb.append("\n");

        return sb.toString();
    }


    public static String tableForm(
            final @NonNull int[][] nested) {
        return tableForm(nested, 8, Integer::toString);
    }

    public static String tableForm(
            final @NonNull int[][] nested,
            final int columnWidth,
            final @NonNull IntFunction<String> cellFormatter) {

        final int n = nested.length;
        val sb = new StringBuilder();
        for(int i=0; i<n; ++i) {
            sb.append(rowForm(nested[i], columnWidth, cellFormatter));
        }
        return sb.toString();
    }

}
