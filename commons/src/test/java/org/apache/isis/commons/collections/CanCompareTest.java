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

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.internal.collections._Lists;

class CanCompareTest {

    // in natural order
    private List<Can<String>> samples = _Lists.of(
            null,
            Can.empty(),
            Can.<String>of("hi"),
            Can.<String>of("hi", "there"),
            Can.<String>of("hi", "there", "world"),
            Can.<String>of("hi", "there", "zzz"),
            Can.<String>of("hi", "zzz", "world")
            );
    
    private final int x = 99; // n/a
    
    //  left arg in row
    // right arg in col
    private final int[][] expectationMatrix = {
            {x, x, x, x, x, x, x},
            {0, 0,-1,-1,-1,-1,-1},
            {1, 1, 0,-1,-1,-1,-1},
            {1, 1, 1, 0,-1,-1,-1},
            {1, 1, 1, 1, 0,-1,-1},
            {1, 1, 1, 1, 1, 0,-1},
            {1, 1, 1, 1, 1, 1, 0},
    };
    
    @Test
    void cans_shouldCompareCorrectly() {
        
        int row = 0;
        for(Can<String> left : samples) {
            int col = 0;
            for(Can<String> right : samples) {
                // skip first row // as first argument is null and cannot be used
                if(row>0) {
                    
                    int c = left.compareTo(right);
                    if(c>0) {
                        c = 1;
                    } else if(c<0) {
                        c = -1;
                    }
                    assertEquals(expectationMatrix[row][col], c,
                            String.format("failed in (row, col) (%d, %d) with (%s, %s)",
                                    row, col, left, right));    
                }
                ++col;
            }
            ++row;
        }
    }

}
