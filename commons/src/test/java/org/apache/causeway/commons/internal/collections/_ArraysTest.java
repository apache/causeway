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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections.Samples.FiniteSpace2;

import lombok.val;

/**
 * @since Feb 22, 2020
 *
 */
class _ArraysTest {

    private List<Integer[]> inputSamples;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp() throws Exception {
        
        inputSamples = _Lists.of(
                null,
                new Integer[] {},
                new Integer[] {0, 1, 2, 3},
                new Integer[] {1, 2, 3, 4}
        ); 
        
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterEach
    void tearDown() throws Exception {
    }

    
    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#testAnyMatch(T[], T[], java.util.function.BiPredicate)}.
     */
    @Test
    void testTestAnyMatch() {
        
        FiniteSpace2.visit(inputSamples, inputSamples, (i, j, a, b)->{
            if(_NullSafe.size(a) != _NullSafe.size(b)) {
                
                assertThrows(IllegalArgumentException.class, 
                        ()->_Arrays.testAnyMatch(a, b, Objects::equals));
                
            } else if(_NullSafe.size(a) * _NullSafe.size(b) == 0
                    || i!=j) {
                assertFalse(_Arrays.testAnyMatch(a, b, Objects::equals));
            } else {
                // i==j
                assertEquals(i, j);
                assertTrue(_Arrays.testAnyMatch(a, b, Objects::equals));
            }
        });
        
        assertTrue(_Arrays.testAnyMatch(
                new Integer[] {0, 1, 2, 9}, 
                new Integer[] {1, 2, 3, 9}, 
                Objects::equals));
        
        assertFalse(_Arrays.testAnyMatch(
                new Integer[] {0, 1, 2, 3}, 
                new Integer[] {1, 2, 3, 4}, 
                Objects::equals));
        
        
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#testAllMatch(T[], T[], java.util.function.BiPredicate)}.
     */
    @Test
    void testTestAllMatch() {
        
        FiniteSpace2.visit(inputSamples, inputSamples, (i, j, a, b)->{
            if(_NullSafe.size(a) != _NullSafe.size(b)) {
                
                assertThrows(IllegalArgumentException.class, 
                        ()->_Arrays.testAllMatch(a, b, Objects::equals));
                
            } else if(_NullSafe.size(a) * _NullSafe.size(b) == 0) {
                assertTrue(_Arrays.testAllMatch(a, b, Objects::equals));
            } else if(i!=j) {
                assertFalse(_Arrays.testAllMatch(a, b, Objects::equals));
            } else {
                // i==j
                assertEquals(i, j);
                assertTrue(_Arrays.testAllMatch(a, b, Objects::equals));
            }
        });
        
        assertFalse(_Arrays.testAllMatch(
                new Integer[] {0, 1, 2, 9}, 
                new Integer[] {1, 2, 3, 9}, 
                Objects::equals));
        
        assertFalse(_Arrays.testAllMatch(
                new Integer[] {0, 1, 2, 3}, 
                new Integer[] {1, 2, 3, 4}, 
                Objects::equals));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#isArrayType(java.lang.Class)}.
     */
    @Test
    void testIsArrayType() {
        assertFalse(_Arrays.isArrayType(null));
        assertFalse(_Arrays.isArrayType(Integer.class));
        assertFalse(_Arrays.isArrayType(Enum.class));
        assertFalse(_Arrays.isArrayType(Collection.class));
        assertFalse(_Arrays.isArrayType(List.class));
        assertFalse(_Arrays.isArrayType(Set.class));
        
        assertTrue(_Arrays.isArrayType(int[].class));
        assertTrue(_Arrays.isArrayType(int[][].class));
        assertTrue(_Arrays.isArrayType(Object[][].class));
        
        IntStream.of(1,2,3)
        .mapToObj(inputSamples::get)
        .map(Object::getClass)
        .forEach(type->assertTrue(_Arrays.isArrayType(type)));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#isCollectionOrArrayType(java.lang.Class)}.
     */
    @Test
    void testIsCollectionOrArrayType() {
        assertFalse(_Arrays.isCollectionOrArrayType(null));
        assertFalse(_Arrays.isCollectionOrArrayType(Integer.class));
        assertFalse(_Arrays.isCollectionOrArrayType(Enum.class));
        
        assertTrue(_Arrays.isCollectionOrArrayType(Collection.class));
        assertTrue(_Arrays.isCollectionOrArrayType(List.class));
        assertTrue(_Arrays.isCollectionOrArrayType(Set.class));
        assertTrue(_Arrays.isCollectionOrArrayType(int[].class));
        assertTrue(_Arrays.isCollectionOrArrayType(int[][].class));
        assertTrue(_Arrays.isCollectionOrArrayType(Object[][].class));
        
        IntStream.of(1,2,3)
        .mapToObj(inputSamples::get)
        .map(Object::getClass)
        .forEach(type->assertTrue(_Arrays.isCollectionOrArrayType(type)));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#toArray(java.lang.Class, int)}.
     */
    @Test
    void testToArrayClassOfTInt() {
        
        assertArrayEquals( new Integer[] {1, 2, 3},
                Stream.of(1,2,3)
                .collect(_Arrays.toArray(Integer.class, 3)));
        
        assertArrayEquals( new Integer[] {1, 2, 3, null},
                Stream.of(1,2,3)
                .collect(_Arrays.toArray(Integer.class, 4)));
        
        assertThrows(Exception.class, ()->
            Stream.of(1,2,3)
                .collect(_Arrays.toArray(Integer.class, 2)));
        
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#toArray(java.lang.Class)}.
     */
    @Test
    void testToArrayClassOfT() {

        assertArrayEquals( new Integer[] {1, 2, 3},
                Stream.of(1,2,3)
                .collect(_Arrays.toArray(Integer.class)));
        
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#combine(java.lang.Object, T[])}.
     */
    @Test
    void testCombineTTArray() {
        
        assertArrayEquals(new Integer[]{1}, _Arrays.combine(1));
        assertArrayEquals(new Integer[]{1, 2}, _Arrays.combine(1, 2));
        assertArrayEquals(new Integer[]{1, 2, 3}, _Arrays.combine(1, 2, 3));
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, _Arrays.combine(1, 2, 3, 4));
        
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#combineWithExplicitType(java.lang.Class, java.lang.Object, Y[])}.
     */
    @Test
    void testCombineClassOfTXYArray() {
        assertArrayEquals(new Number[]{1L, 2L}, _Arrays.combineWithExplicitType(Number.class, 1L, 2L));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#combine(T[], T[])}.
     */
    @Test
    void testCombineTArrayTArray() {
        
        assertArrayEquals(new Integer[]{}, _Arrays.combine(new Integer[]{}));
        assertArrayEquals(new Integer[]{1, 2}, _Arrays.combine(new Integer[]{1, 2}));
        assertArrayEquals(new Integer[]{1, 2, 3}, _Arrays.combine(new Integer[]{1, 2}, 3));
        assertArrayEquals(new Integer[]{1, 2, 3, 4}, _Arrays.combine(new Integer[]{1, 2}, 3, 4));

    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#toArray(java.util.Collection, java.lang.Class)}.
     */
    @Test
    void testToArrayCollectionOfQextendsTClassOfT() {
        
        assertArrayEquals( new Integer[] {1, 2, 3},
                _Arrays.toArray(Arrays.asList(1,2,3), Integer.class) );

    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#toArray(java.lang.Iterable, java.lang.Class)}.
     */
    @Test
    void testToArrayIterableOfQextendsTClassOfT() {
        
        assertArrayEquals( new Integer[] {1, 2, 3},
                _Arrays.toArray(Samples.iterable(1, 2, 3), Integer.class) );
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#removeByIndex(T[], int)}.
     */
    @Test
    void removeByIndex_head() {
        val input = new Integer[] {0, 1, 2, 3};
        val output = _Arrays.removeByIndex(input, 0);
        assertArrayEquals(new Integer[] {1, 2, 3}, output);
    }

    @Test
    void removeByIndex_inBetween() {
        val input = new Integer[] {0, 1, 2, 3};
        val output = _Arrays.removeByIndex(input, 1);
        assertArrayEquals(new Integer[] {0, 2, 3}, output);
    }
    
    @Test
    void removeByIndex_tail() {
        val input = new Integer[] {0, 1, 2, 3};
        val output = _Arrays.removeByIndex(input, 3);
        assertArrayEquals(new Integer[] {0, 1, 2}, output);
    }
    
    @Test
    void removeByIndex_outOfBounds() {
        val input = new Integer[] {0, 1, 2, 3};
        assertThrows(IllegalArgumentException.class, 
                ()->_Arrays.removeByIndex(input, 4));
    }
    
    @Test
    void removeByIndex_empty() {
        val input = new Integer[] {};
        assertThrows(IllegalArgumentException.class, 
                ()->_Arrays.removeByIndex(input, 0));
    }
    
    @Test
    void removeByIndex_null() {
        val input = (Integer[])null;
        assertThrows(IllegalArgumentException.class, 
                ()->_Arrays.removeByIndex(input, 0));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#emptyToNull(T[])}.
     */
    @Test
    void testEmptyToNull() {
        assertEquals(null, _Arrays.emptyToNull(null));
        assertEquals(null, _Arrays.emptyToNull(new Integer[] {}));
        assertArrayEquals(new Integer[] {1}, _Arrays.emptyToNull(new Integer[] {1}));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#inferComponentTypeIfAny(java.lang.Class)}.
     */
    @Test
    void testInferComponentTypeIfAny() {
        assertEquals(null, _Arrays.inferComponentType(null).orElse(null));
        assertEquals(int.class, _Arrays.inferComponentType(int[].class).orElse(null));
        assertEquals(Integer.class, _Arrays.inferComponentType(Integer[].class).orElse(null));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#get(T[], int)}.
     */
    @Test
    void testGet() {
        
        assertEquals(Optional.empty(), _Arrays.get(null, 99));
        assertEquals(Optional.empty(), _Arrays.get(new Integer[] {}, 0));
        
        assertEquals(Optional.empty(), _Arrays.get(new Integer[] {1}, 99));
        assertEquals(Optional.of(1), _Arrays.get(new Integer[] {1}, 0));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#map(T[], java.lang.Class, java.util.function.Function)}.
     */
    @Test
    void testMapTArrayClassOfRFunctionOfTR() {
        assertEquals(null, _Arrays.map((Integer[])null, Long.class, Long::valueOf));
        assertArrayEquals(new Long[] {}, _Arrays.map(new Integer[] {}, Long.class, Long::valueOf));
        assertArrayEquals(new Long[] {1L, 2L, 3L}, _Arrays.map(new Integer[] {1, 2, 3}, Long.class, Long::valueOf));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#map(T[], java.util.function.Function)}.
     */
    @Test
    void testMapTArrayFunctionOfTQ() {
        assertEquals(null, _Arrays.map((Integer[])null, Long::valueOf));
        assertArrayEquals(new Long[] {}, _Arrays.map(new Integer[] {}, Long::valueOf));
        assertArrayEquals(new Long[] {1L, 2L, 3L}, _Arrays.map(new Integer[] {1, 2, 3}, Long::valueOf));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#mapCollection(java.util.Collection, java.lang.Class, java.util.function.Function)}.
     */
    @Test
    void testMapCollectionCollectionOfTClassOfRFunctionOfTR() {
        assertEquals(null, _Arrays.mapCollection((List<Integer>)null, Long.class, Long::valueOf));
        assertArrayEquals(new Long[] {}, _Arrays.mapCollection(Arrays.<Integer>asList(), Long.class, Long::valueOf));
        assertArrayEquals(new Long[] {1L, 2L, 3L}, _Arrays.mapCollection(Arrays.asList(1,2,3), Long.class, Long::valueOf));
    }

    /**
     * Test method for {@link org.apache.causeway.commons.internal.collections._Arrays#mapCollection(java.util.Collection, java.util.function.Function)}.
     */
    @Test
    void testMapCollectionCollectionOfTFunctionOfTQ() {
        assertEquals(null, _Arrays.mapCollection((List<Integer>)null, Long::valueOf));
        assertArrayEquals(new Long[] {}, _Arrays.mapCollection(Arrays.<Integer>asList(), Long::valueOf));
        assertArrayEquals(new Long[] {1L, 2L, 3L}, _Arrays.mapCollection(Arrays.asList(1,2,3), Long::valueOf));

    }

}
