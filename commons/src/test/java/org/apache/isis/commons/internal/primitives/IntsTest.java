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
package org.apache.isis.commons.internal.primitives;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.isis.commons.internal.primitives._Ints;

import lombok.val;

class IntsTest {

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }

    @Test
    void emptyRange() {
        val range = _Ints.rangeOpenEnded(3, 3);
        assertEquals("[]", range.toString());
    }
    
    @Test
    void rangeIntersection() {
        
        val intersection = _Ints.rangeClosed(3, 7)
                .intersect(_Ints.rangeClosed(5, 10))
                .get();
        
        assertEquals("[5,7]", intersection.toString());
    }
    
    @Test
    void rangeIntersectionEmpty() {
        
        val intersection = _Ints.rangeClosed(3, 7)
                .intersect(_Ints.rangeClosed(8, 10));
        
        assertFalse(intersection.isPresent());
    }
    
    @Test
    void rangeIntersectionOnBoundary() {
        
        val intersection = _Ints.rangeClosed(3, 7)
                .intersect(_Ints.rangeClosed(7, 10))
                .get();
        
        assertEquals("[7,7]", intersection.toString());
    }
    
    @Test
    void rangeIntersectionContainment() {
        
        val intersection = _Ints.rangeClosed(3, 10)
                .intersect(_Ints.rangeClosed(5, 7))
                .get();
        
        assertEquals("[5,7]", intersection.toString());
    }
    
    @Test
    void rangeIterator() {

        val sb = new StringBuilder();
        
        val rangeIterator1 = _Ints.rangeClosed(3, 7).iterator();
        while(rangeIterator1.hasNext()) {
            sb.append(rangeIterator1.nextInt()).append(',');
        }
        assertEquals("3,4,5,6,7,", sb.toString());
        sb.setLength(0);
        
        val rangeIterator2 = _Ints.rangeOpenEnded(3, 3).iterator();
        while(rangeIterator2.hasNext()) {
            sb.append(rangeIterator2.nextInt()).append(',');
        }
        assertEquals("", sb.toString());
        sb.setLength(0);
        
    }

}
