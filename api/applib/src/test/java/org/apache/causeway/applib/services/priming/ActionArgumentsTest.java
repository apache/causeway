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
package org.apache.causeway.applib.services.priming;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionArgumentsTest {

    @Test
    void preservesArityIncludingArrayAndNullValues() {
        final String[] arrayArgument = {"a", "b"};
        final ActionArguments arguments = ActionArguments.of(
                Arrays.asList(arrayArgument, null));

        assertEquals(2, arguments.size());
        assertSame(arrayArgument, arguments.get(0));
        assertSame(arrayArgument, arguments.get(0, String[].class));
        assertNull(arguments.get(1, String.class));
    }

    @Test
    void takesDefensiveCopyAndExposesUnmodifiableList() {
        final java.util.List<Object> source = new java.util.ArrayList<>();
        source.add("first");
        final ActionArguments arguments = ActionArguments.of(source);

        source.set(0, "changed");

        assertEquals("first", arguments.get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> arguments.asList().add("second"));
    }

    @Test
    void rejectsIncompatibleTypedAccessWithUsefulMessage() {
        final ActionArguments arguments = ActionArguments.of(
                java.util.Collections.singletonList("value"));

        final IllegalArgumentException failure = assertThrows(
                IllegalArgumentException.class,
                () -> arguments.get(0, Integer.class));

        assertTrue(failure.getMessage().contains("index 0"));
        assertTrue(failure.getMessage().contains(Integer.class.getName()));
    }
}
