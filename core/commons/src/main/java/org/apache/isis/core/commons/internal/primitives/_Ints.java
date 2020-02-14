package org.apache.isis.core.commons.internal.primitives;

import java.util.OptionalInt;
import java.util.function.Consumer;

import javax.annotation.Nullable;

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
     * <p>An exception of type {@code NumberFormatException} is
     * thrown if any of the following situations occurs:
     * <ul>
     * <li>The first argument is {@code null} or is a string of
     * length zero.
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

    private final static Consumer<String> IGNORE_ERRORS = t->{};
    
    /**
     * @implNote Copied over from JDK's {@link Integer#parseInt(String)} to provide a variant 
     * with minimum potential heap pollution (does not produce stack-traces on parsing failures)
     */
    private static long parseIntElseLongMaxValue(
            @Nullable final String s, 
            final int radix, 
            @Nullable final Consumer<String> onFailure) {
        
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
