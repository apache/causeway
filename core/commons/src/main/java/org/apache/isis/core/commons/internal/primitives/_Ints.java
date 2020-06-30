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
package org.apache.isis.core.commons.internal.primitives;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import static org.apache.isis.core.commons.internal.base._With.requires;

import lombok.NonNull;
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
        public static @NonNull Bound inclusive(int value) { return of(value, true); }
        public static @NonNull Bound exclusive(int value) { return of(value, true); }
    }
    
    @Value(staticConstructor = "of")
    public static class Range {
        @NonNull Bound lowerBound;
        @NonNull Bound upperBound;
        public boolean contains(int value) {
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
        public int bounded(int value) {
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
            return lowerBound.isInclusive() ? lowerBound.getValue() : lowerBound.getValue()+1;  
        }
        private int nearestToUpper() {
            return upperBound.isInclusive() ? upperBound.getValue() : upperBound.getValue()-1;
        }
        public @NonNull Optional<Range> intersect(@NonNull Range other) {
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
            return String.format("%s%d,%d%S", 
                    lowerBound.isInclusive() ? '[' : '(', lowerBound.getValue(),
                    upperBound.getValue(), upperBound.isInclusive() ? ']' : ')');
        }
        public IntStream stream() {
            return IntStream.rangeClosed(nearestToLower(), nearestToUpper());
        }
    }
    
    // -- RANGE FACTORIES
    
    public static Range rangeClosed(int a, int b) {
        if(a>b) {
            throw _Exceptions.illegalArgument("bounds must be ordered in [%d, %d]", a, b);
        }
        return Range.of(Bound.inclusive(a), Bound.inclusive(b));
    }
    
    public static Range rangeOpenEnded(int a, int b) {
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
    
    private static boolean isParseSuccess(long value) {
        return value!=Long.MAX_VALUE;
    }

    private static final Consumer<String> IGNORE_ERRORS = t->{};
    
    /**
     * @implNote Copied over from JDK's {@link Integer#parseInt(String)} to provide a variant 
     * with minimum potential heap pollution (does not produce stack-traces on parsing failures)
     */
    private static long parseIntElseLongMaxValue(
            @Nullable final String s, 
            final int radix, 
            final Consumer<String> onFailure) {
        
        requires(onFailure, "onFailure");
        
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
    
    
    
    
}
