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
package org.apache.isis.commons.collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.SerializationTester;

import lombok.val;

class CanVectorTest {

    @Test
    void emptyCanVectors_shouldBeEqual() {
        assertEquals(CanVector.empty(), new CanVector<String>(0));
    }
    
    @Test
    void emptyCanVector_shouldBeSerializable() {
        SerializationTester.assertEqualsOnRoundtrip(CanVector.empty());
        SerializationTester.assertEqualsOnRoundtrip(new CanVector<String>(0));
    }
    
    @Test
    void populatedCanVector_shouldBeSerializable() {
        val vector = new CanVector<String>(3);
        vector.set(0, Can.<String>of("hi"));
        vector.set(1, Can.<String>of("hi", "there"));
        SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi"));
    }

}
