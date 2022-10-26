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
package org.apache.causeway.commons.internal.assertions;

import java.util.Objects;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.commons.internal.primitives._Longs;

import lombok.val;
import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * A collection of commonly used constants.
 * </p>
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 * </p>
 *
 * @since 2.0
 */
@UtilityClass
@Log4j2
public final class _Assert {


    // -- TRUE

    public static void assertTrue(final boolean condition) {
        assertTrue(condition, (String) null);
    }

    public static void assertTrue(final boolean condition, final String message) {
        if (!condition) {
            fail(message, true, false);
        }
    }

    public static void assertTrue(final boolean condition, final Supplier<String> lazyMessage) {
        if (!condition) {
            fail(lazyMessage.get(), true, false);
        }
    }

    // -- FALSE

    public static void assertFalse(final boolean condition) {
        assertFalse(condition, (String) null);
    }

    public static void assertFalse(final boolean condition, final String message) {
        if (condition) {
            fail(message, false, true);
        }
    }

    public static void assertFalse(final boolean condition, final Supplier<String> lazyMessage) {
        if (condition) {
            fail(lazyMessage.get(), true, false);
        }
    }

    // -- NULL

    public static void assertNull(final Object object) {
        assertNull(object, (String) null);
    }

    public static void assertNull(final Object object, final String message) {
        if (object!=null) {
            fail(message, "null", "not null");
        }
    }

    public static void assertNull(final Object object, final Supplier<String> lazyMessage) {
        if (object!=null) {
            fail(lazyMessage.get(), "null", "not null");
        }
    }

    // -- NOT NULL

    public static void assertNotNull(final Object object) {
        assertNotNull(object, (String) null);
    }

    public static void assertNotNull(final Object object, final String message) {
        if (object==null) {
            fail(message, "not null", "null");
        }
    }

    public static void assertNotNull(final Object object, final Supplier<String> lazyMessage) {
        if (object==null) {
            fail(lazyMessage.get(), "not null", "null");
        }
    }

    // -- SAMENESS

    /**
     * <em>Assert</em> that {@code left} == {@code right}.
     * @return {@code left} if assertion holds
     */
    public static <T> T assertSameObject(final T left, final Object right) {
        assertTrue(left == right);
        return left;
    }

    /**
     * <em>Assert</em> that {@code left} == {@code right}.
     * @return {@code left} if assertion holds
     */
    public static <T> T assertSameObject(final T left, final Object right, final String msg) {
        assertTrue(left == right, msg);
        return left;
    }

    /**
     * <em>Assert</em> that {@code left} == {@code right}.
     * @return {@code left} if assertion holds
     */
    public static <T> T assertSameObject(final T left, final Object right, final Supplier<String> lazyMessage) {
        assertTrue(left == right, lazyMessage);
        return left;
    }

    // -- EQUALITY

    /**
     * <em>Assert</em> that {@code expected} and {@code actual} are equal.
     * <p>If both are {@code null}, they are considered equal.
     *
     * @see Object#equals(Object)
     */
    public static void assertEquals(final Object left, final Object right) {
        assertEquals(left, right, (String) null);
    }

    /**
     * <em>Assert</em> that {@code expected} and {@code actual} are equal.
     * <p>If both are {@code null}, they are considered equal.
     * <p>Fails with the supplied failure {@code message}.
     *
     * @see Object#equals(Object)
     */
    public static void assertEquals(final Object left, final Object right, final String message) {
        if (!Objects.equals(left, right)) {
            fail(message, left, right);
        }
    }

    public static void assertEquals(final Object left, final Object right, final Supplier<String> lazyMessage) {
        if (!Objects.equals(left, right)) {
            fail(lazyMessage.get(), left, right);
        }
    }

    // -- RANGE CHECKS

    public static void assertRangeContains(final _Ints.Range range, final int value, final String message) {
        if(!range.contains(value)) {
            fail(message, range.toString(), value);
        }
    }

    public static void assertRangeContains(final _Ints.Range range, final int value, final Supplier<String> lazyMessage) {
        if(!range.contains(value)) {
            fail(lazyMessage.get(), range.toString(), value);
        }
    }

    public static void assertRangeContains(final _Longs.Range range, final long value, final String message) {
        if(!range.contains(value)) {
            fail(message, range.toString(), value);
        }
    }

    public static void assertRangeContains(final _Longs.Range range, final long value, final Supplier<String> lazyMessage) {
        if(!range.contains(value)) {
            fail(lazyMessage.get(), range.toString(), value);
        }
    }

    // -- TYPE INSTANCE OF

    public static void assertTypeIsInstanceOf(final Class<?> type, final Class<?> requiredType) {
        if(!requiredType.isAssignableFrom(type)) {
            throw _Exceptions.assertionError(String.format(
                    "unexpected type: <%s> is not an instance of <%s> ", ""+type, ""+requiredType));
        }
    }

    // -- OPEN/CLOSE STATE

    public enum OpenCloseState {

        NOT_INITIALIZED,
        OPEN,
        CLOSED
        ;

        public boolean isNotInitialized() { return this == NOT_INITIALIZED; }
        public boolean isOpen() { return this == OPEN; }
        public boolean isClosed() { return this == CLOSED; }

        /**
         * @param expected - nullable
         * @throws IllegalArgumentException if {@code this} not equal to {@code expected}
         */
        public void assertEquals(
                final @Nullable OpenCloseState expected) {
            if (expected != this) {
                throw new IllegalStateException("State is: " + this + "; should be: " + expected);
            }
        }

    }

    // -- HELPER

    static String buildPrefix(final String message) {
        return _Strings.isNotEmpty(message) ? message + " ==> " : "";
    }

    private static void fail(final String message, final Object expected, final Object actual) {
        val error = _Exceptions.assertionError(
                buildPrefix(message)
                + String.format("expected: <%s> but was: <%s>", ""+expected, ""+actual));
        log.error(error); // in case exceptions get swallowed, make sure errors at least get logged
        throw error;
    }


}
