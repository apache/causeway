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
package org.apache.causeway.commons.internal.collections;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.collections._PrimitiveCollections.IntList;

class _PrimitiveCollectionsTest {

    @Test
    void emptyList() {

        var intList = new IntList();
        assertEquals(0, intList.size());
        assertTrue(intList.isEmpty());
        assertNotNull(intList.toArray());
        assertEquals(0, intList.toArray().length);
        assertThrows(IndexOutOfBoundsException.class, ()->intList.get(0));
        assertEquals(0L, intList.stream().count());
    }

    @Test
    void oneElementList() {
        var intList = new IntList();
        intList.add(5);

        assertEquals(1, intList.size());
        assertFalse(intList.isEmpty());
        assertNotNull(intList.toArray());
        assertEquals(1, intList.toArray().length);
        assertEquals(5, intList.get(0));
        assertThrows(IndexOutOfBoundsException.class, ()->intList.get(1));
        assertEquals(1L, intList.stream().count());
        assertEquals(5, intList.stream().sum());
    }

    @Test
    void manyElementList() {
        var intList = new IntList();
        IntStream.range(0, 100)
            .forEach(i->intList.add(i + 100));

        assertEquals(100, intList.size());
        assertFalse(intList.isEmpty());
        assertNotNull(intList.toArray());
        assertEquals(100, intList.toArray().length);
        assertEquals(100, intList.get(0));
        assertEquals(199, intList.get(99));
        assertEquals(99, intList.indexOf(199).orElse(-1));
        assertThrows(IndexOutOfBoundsException.class, ()->intList.get(100));
        assertEquals(100L, intList.stream().count());
        assertEquals(14950, intList.stream().sum());
    }

    @Test
    void setSemantics() {
        var intList = new IntList();
        intList.addUnique(5);
        intList.addUnique(4);
        intList.addUnique(7);
        intList.addUnique(4);

        assertEquals(3, intList.size());
        assertFalse(intList.isEmpty());
        assertNotNull(intList.toArray());
        assertEquals(3, intList.toArray().length);
        assertEquals(5, intList.get(0));
        assertThrows(IndexOutOfBoundsException.class, ()->intList.get(3));
        assertEquals(3L, intList.stream().count());
        assertEquals(5+4+7, intList.stream().sum());

        assertTrue(intList.contains(4));
        assertTrue(intList.contains(7));
        assertFalse(intList.contains(2));

        assertEquals(2, intList.indexOf(7).orElse(-1));
    }

    @Test
    void wrapping() {
        var array = new int[]{
                1, 3, 2, 5,
                1, 3, 2, 5,
                1, 3, 2, 5,
                1, 3, 2, 5};
        var intList = new IntList(array);
        assertEquals(4*4, intList.size());
        assertFalse(intList.isEmpty());
        assertEquals(44, intList.stream().sum());
    }

    @Test
    void addAll() {
        var array = new int[]{1, 3, 2, 5, 1, 3, 2, 5};
        var intList = new IntList(array).addAll(array);
        assertEquals(4*4, intList.size());
        assertFalse(intList.isEmpty());
        assertEquals(44, intList.stream().sum());
    }

    @Test
    void pickByIndex() {
        var array = new int[]{1, 3, 2, 5};
        var intList = new IntList(array);
        assertArrayEquals(new int[]{1, 1, 5}, intList.toArrayPickByIndex(0, 0, 9, -1, 3));
    }

}
