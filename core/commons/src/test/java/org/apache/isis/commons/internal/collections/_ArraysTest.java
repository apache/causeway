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
package org.apache.isis.commons.internal.collections;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import lombok.val;

class _ArraysTest {

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
    
    
    
}
