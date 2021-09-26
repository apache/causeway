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
package org.apache.isis.commons.functional;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompositionTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @Test
    void pseudoComposition() {
        final var comp = Composition.ofElement("a");

        assertEquals(List.of("a"), comp.flatten());
        assertEquals(1, comp.size());
    }

    @Test
    void composingElements() {
        final var comp = Composition.ofElements("a", "b");

        assertEquals(List.of("a", "b"), comp.flatten());
        assertEquals(2, comp.size());
    }

    @Test
    void composingCompositions() {

        final var comp = Composition.of(
                Composition.ofElements("a", "b"),
                Composition.ofElements("c", "d"));

        assertEquals(List.of("a", "b", "c", "d"), comp.flatten());
        assertEquals(4, comp.size());
    }

    @Test
    void composingElementAndComposition() {

        final var comp = Composition.ofLeftElement(
                "a",
                Composition.ofElements("c", "d"));

        assertEquals(List.of("a", "c", "d"), comp.flatten());
        assertEquals(3, comp.size());
    }

    @Test
    void composingCompositionAndElement() {

        final var comp = Composition.ofRightElement(
                Composition.ofElements("a", "b"),
                "d");

        assertEquals(List.of("a", "b", "d"), comp.flatten());
        assertEquals(3, comp.size());
    }

    @Test
    void compositionNesting() {

        final var comp = Composition.of(
                Composition.of(
                        Composition.ofElements("a", "b"),
                        Composition.ofElements("c", "d")),
                Composition.of(
                        Composition.ofElements("e", "f"),
                        Composition.ofElements("g", "h")));

        assertEquals(List.of("a", "b", "c", "d", "e", "f", "g", "h"), comp.flatten());
        assertEquals(8, comp.size());
    }



}
