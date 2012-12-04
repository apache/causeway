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

package org.apache.isis.core.commons.ensure;

public final class Assert {

    public static void assertEquals(final Object expected, final Object actual) {
        assertEquals("", expected, actual);
    }

    public static void assertEquals(final String message, final int expected, final int value) {
        if (expected != value) {
            throw new IsisAssertException(message + " expected " + expected + "; but was " + value);
        }
    }

    public static void assertEquals(final String message, final Object expected, final Object actual) {
        assertTrue(message + ": expected " + expected + " but was " + actual, (expected == null && actual == null) || (expected != null && expected.equals(actual)));
    }

    public static void assertFalse(final boolean flag) {
        assertFalse("expected false", flag);
    }

    public static void assertFalse(final String message, final boolean flag) {
        assertTrue(message, !flag);
    }

    public static void assertFalse(final String message, final Object target, final boolean flag) {
        assertTrue(message, target, !flag);
    }

    public static void assertNotNull(final Object object) {
        assertNotNull("", object);
    }

    public static void assertNotNull(final String message, final Object object) {
        assertTrue("unexpected null: " + message, object != null);
    }

    public static void assertNotNull(final String message, final Object target, final Object object) {
        assertTrue(message, target, object != null);
    }

    public static void assertNull(final Object object) {
        assertTrue("unexpected reference; should be null", object == null);
    }

    public static void assertNull(final String message, final Object object) {
        assertTrue(message, object == null);
    }

    public static void assertSame(final Object expected, final Object actual) {
        assertSame("", expected, actual);
    }

    public static void assertSame(final String message, final Object expected, final Object actual) {
        assertTrue(message + ": expected " + expected + " but was " + actual, expected == actual);
    }

    public static void assertTrue(final boolean flag) {
        assertTrue("expected true", flag);
    }

    public static void assertTrue(final String message, final boolean flag) {
        assertTrue(message, null, flag);
    }

    public static void assertTrue(final String message, final Object target, final boolean flag) {
        if (!flag) {
            throw new IsisAssertException(message + (target == null ? "" : (": " + target)));
        }
    }

}
