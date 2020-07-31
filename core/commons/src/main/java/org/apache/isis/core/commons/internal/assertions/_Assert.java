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

package org.apache.isis.core.commons.internal.assertions;

import java.util.Objects;
import java.util.function.Supplier;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.commons.internal.primitives._Ints;
import org.apache.isis.core.commons.internal.primitives._Longs;

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

    public static void assertTrue(boolean condition) {
        assertTrue(condition, (String) null);
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            fail(message, true, false);
        }
    }

    public static void assertTrue(boolean condition, Supplier<String> lazyMessage) {
        if (!condition) {
            fail(lazyMessage.get(), true, false);
        }
    }

    // -- FALSE

    public static void assertFalse(boolean condition) {
        assertFalse(condition, (String) null);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            fail(message, false, true);
        }
    }

    public static void assertFalse(boolean condition, Supplier<String> lazyMessage) {
        if (condition) {
            fail(lazyMessage.get(), true, false);
        }
    }

    // -- NOT NULL

    public static void assertNotNull(Object object) {
        assertNotNull(object, (String) null);
    }

    public static void assertNotNull(Object object, String message) {
        if (object==null) {
            fail(message, "not null", "null");
        }
    }

    public static void assertNotNull(Object object, Supplier<String> lazyMessage) {
        if (object==null) {
            fail(lazyMessage.get(), "not null", "null");
        }
    }

    /**
     * <em>Assert</em> that {@code expected} and {@code actual} are equal.
     * <p>If both are {@code null}, they are considered equal.
     *
     * @see Object#equals(Object)
     */
    public static void assertEquals(Object left, Object right) {
        assertEquals(left, right, (String) null);
    }

    /**
     * <em>Assert</em> that {@code expected} and {@code actual} are equal.
     * <p>If both are {@code null}, they are considered equal.
     * <p>Fails with the supplied failure {@code message}.
     *
     * @see Object#equals(Object)
     */
    public static void assertEquals(Object left, Object right, String message) {
        if (!Objects.equals(left, right)) {
            fail(message, left, right);
        }
    }

    public static void assertEquals(Object left, Object right, Supplier<String> lazyMessage) {
        if (!Objects.equals(left, right)) {
            fail(lazyMessage.get(), left, right);
        }
    }

    // -- RANGE CHECKS

    public static void assertRangeContains(_Ints.Range range, int value, String message) {
        if(!range.contains(value)) {
            fail(message, range.toString(), value);
        }
    }
    
    public static void assertRangeContains(_Ints.Range range, int value, Supplier<String> lazyMessage) {
        if(!range.contains(value)) {
            fail(lazyMessage.get(), range.toString(), value);
        }
    }

    public static void assertRangeContains(_Longs.Range range, long value, String message) {
        if(!range.contains(value)) {
            fail(message, range.toString(), value);
        }
    }
    
    public static void assertRangeContains(_Longs.Range range, long value, Supplier<String> lazyMessage) {
        if(!range.contains(value)) {
            fail(lazyMessage.get(), range.toString(), value);
        }
    }

    // -- TYPE INSTANCE OF

    public static void assertTypeIsInstanceOf(Class<?> type, Class<?> requiredType) {
        if(!requiredType.isAssignableFrom(type)) {
            throw _Exceptions.assertionError(String.format(
                    "unexpected type: <%s> is not an instance of <%s> ", ""+type, ""+requiredType));
        }
    }

    // -- HELPER

    static String buildPrefix(String message) {
        return _Strings.isNotEmpty(message) ? message + " ==> " : "";
    }

    private static void fail(String message, Object expected, Object actual) {
        val error = _Exceptions.assertionError(
                buildPrefix(message) 
                + String.format("expected: <%s> but was: <%s>", ""+expected, ""+actual));
        log.error(error); // in case exceptions get swallowed, make sure errors at least get logged
        throw error;
    }


}
