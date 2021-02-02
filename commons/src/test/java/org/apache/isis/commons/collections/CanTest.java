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

import java.io.IOException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.SerializationTester;

class CanTest {

    @Test
    void tester_selftest() throws ClassNotFoundException, IOException {
        SerializationTester.selftest();
    }
    
    @Test
    void emptyCans_shouldBeEqual() {
        assertEquals(Can.empty(), Can.<String>of());
    }
    
    @Test
    void emptyCan_shouldBeSerializable() {
        SerializationTester.assertEqualsOnRoundtrip(Can.empty());
        SerializationTester.assertEqualsOnRoundtrip(Can.<String>of());
    }
    
    @Test
    void singletonCan_shouldBeSerializable() {
        SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi"));
    }
    
    @Test
    void multiCan_shouldBeSerializable() {
        SerializationTester.assertEqualsOnRoundtrip(Can.<String>of("hi", "there"));
    }
    
    // -- REVERTING
    
    @Test
    void multiCan_correctly_reverts() {
        assertEquals(Can.<String>of("c", "b", "a"), Can.<String>of("a", "b", "c").reverse());
    }
    
    // -- FILTERING
    
    @Test
    void emptyCanFilter_isIdentity() {
        assertEquals(Can.<String>empty(), Can.<String>empty().filter(x->false));
        assertEquals(Can.<String>empty(), Can.<String>empty().filter(null));
    }
    
    @Test
    void singletonCanFilter_whenAccept_isIdentity() {
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi").filter(x->true));
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi").filter(null));
    }
    
    @Test
    void canFilter_whenNotAccept_isEmpty() {
        assertEquals(Can.<String>empty(), Can.<String>of("hi").filter(x->false));
        assertEquals(Can.<String>empty(), Can.<String>of("hi", "there").filter(x->false));
    }
    
    @Test
    void multiCanFilter_whenAccept_isIdentity() {
        assertEquals(Can.<String>of("hi", "there"), Can.<String>of("hi", "there").filter(x->true));
        assertEquals(Can.<String>of("hi", "there"), Can.<String>of("hi", "there").filter(null));
    }
    
    @Test
    void multiCanFilter_whenAcceptOne_isDifferentCan() {
        assertEquals(Can.<String>of("there"), Can.<String>of("hi", "there").filter("there"::equals));
        assertEquals(Can.<String>of("hi"), Can.<String>of("hi", "there").filter("hi"::equals));
        assertEquals(Can.<String>of("hello"), Can.<String>of("hi", "hello", "there").filter("hello"::equals));
    }
    
    @Test
    void multiCanFilter_whenAcceptTwo_isDifferentCan() {
        assertEquals(Can.<String>of("hi", "hello"), Can.<String>of("hi", "hello", "there").filter(x->x.startsWith("h")));
    }
    
    

}
