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

package org.apache.isis.commons.internal.assertions;

import java.util.Objects;

public final class _Assert {

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

    public static void assertTrue(final String message, final boolean flag) {
        assertTrue(message, null, flag);
    }

    public static void assertTrue(final String message, final Object target, final boolean flag) {
        if (!flag) {
            throw new IsisAssertException(message + (target == null ? "" : (": " + target)));
        }
    }

    public static void assertEquals(final String message, Object left, Object right) {
        if (!Objects.equals(left, right)) {
            throw new IsisAssertException(message + String.format(": '%s' != '%s' ", ""+left, ""+right));
        }
    }

}
