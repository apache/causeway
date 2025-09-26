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
package org.apache.causeway.commons.internal.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.apache.causeway.commons.internal.testing._SerializationTester;

class StableValueTest {

    @Test
    void equality_whenEmpty() {
        assertEquals(new _StableValue<>(), new _StableValue<>());
    }

    @Test
    void equality_whenNotEmpty() {
        assertEquals(new _StableValue<>("test"), new _StableValue<>("test"));
    }

    @Test
    void nonEquality_whenDiffer() {
        assertNotEquals(new _StableValue<>("a"), new _StableValue<>("b"));
    }

    @Test
    void shouldRountrip_whenEmpty() {
        _SerializationTester.assertEqualsOnRoundtrip(new _StableValue<>());
    }

    @Test
    void shouldBeEmpty_whenRoundtrip() {
        assertEquals(
            new _StableValue<>(),
            _SerializationTester.roundtrip(new _StableValue<String>("test")));
    }

}
