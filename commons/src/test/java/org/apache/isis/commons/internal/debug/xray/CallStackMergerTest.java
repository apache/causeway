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
package org.apache.isis.commons.internal.debug.xray;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.commons.internal.debug.xray._CallStackMerger.IntTreeVisitor;

import lombok.val;

class CallStackMergerTest {

    private List<int[]> executionLanes;

    @BeforeEach
    void setUp() throws Exception {
        executionLanes = List.of(
                new int[] {1, 2, 3, 4, 5, 6},
                new int[] {1, 2, 3, 7, 8, 6, 9},
                new int[] {1, 2, 3, 4, 8}
                );
    }

    /**
     * expected ...<pre>
-1
└── 1
    └── 2
        └── 3
            ├── 4
            │   ├── 5
            │   │   └── 6
            │   └── 8
            └── 7
                └── 8
                    └── 6
                        └── 9
    </pre>
     */
    @Test
    void test() {
        val root = _CallStackMerger.merge(executionLanes);
        //System.err.printf("%s%n", root);

        val sb = new StringBuilder();
        root.visitDepthFirst(new IntTreeVisitor() {
            @Override
            public void accept(final int level, final int value) {
                sb.append(level).append(":").append(value).append(",");
            }
        });
        assertEquals("0:-1,1:1,2:2,3:3,4:4,5:5,6:6,5:8,4:7,5:8,6:6,7:9,", sb.toString());
    }

}
