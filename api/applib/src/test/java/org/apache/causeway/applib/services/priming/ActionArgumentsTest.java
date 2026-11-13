/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.applib.services.priming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ActionArgumentsTest {
    @Test void snapshotPreservesNullsArraysAndOriginalPositions() {
        var array = new String[] { "first", "second" };
        var source = new ArrayList<Object>(Arrays.asList("value", null, array));
        var args = ActionArguments.of(source);
        source.clear();
        assertEquals(3, args.size());
        assertEquals("value", args.get(0, String.class));
        assertNull(args.get(1, Integer.class));
        assertSame(array, args.get(2, String[].class));
    }

    @Test void listViewIsUnmodifiable() {
        var args = ActionArguments.of(List.of("value"));
        assertThrows(UnsupportedOperationException.class, () -> args.asList().add("new"));
        assertThrows(UnsupportedOperationException.class, () -> args.asList().set(0, "new"));
        assertThrows(UnsupportedOperationException.class, () -> args.asList().clear());
    }

    @Test void typedAccessReportsIndexAndExpectedType() {
        var args = ActionArguments.of(List.of("value"));
        var failure = assertThrows(IllegalArgumentException.class, () -> args.get(0, Integer.class));
        assertTrue(failure.getMessage().contains("index 0"));
        assertTrue(failure.getMessage().contains(Integer.class.getName()));
        assertThrows(IndexOutOfBoundsException.class, () -> args.get(1));
        assertThrows(NullPointerException.class, () -> args.get(0, null));
        assertThrows(NullPointerException.class, () -> ActionArguments.of(null));
        assertEquals(0, ActionArguments.of(List.of()).size());
    }
}
