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
package org.apache.isis.core.commons.internal.collections.snapshot;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;

class VersionedListTest {

    @Test
    void test() {
        
        val vList = new _VersionedList<String>();
        
        assertEquals(0, vList.size());
        assertTrue(vList.isEmpty());
        
        vList.add("foo");
        
        assertEquals(1, vList.size());
        assertFalse(vList.isEmpty());
        
        val snapshot1 = vList.snapshot();
        assertEquals(1, snapshot1.stream().count());
        
        vList.add("bar");
        
        val snapshot2 = vList.snapshot();
        assertEquals(2, snapshot2.stream().count());
        assertEquals("foo,bar", snapshot2.stream().collect(Collectors.joining(",")));
        
        val delta = vList.deltaSince(snapshot1);
        assertEquals(1, delta.stream().count());
        assertEquals("bar", delta.stream().collect(Collectors.joining(",")));
        
        vList.add("gru");
        
        val snapshot3 = vList.snapshot();
        assertEquals("foo,bar,gru", snapshot3.stream().collect(Collectors.joining(",")));
        
        val snapshot4 = vList.deltaSince(snapshot3);
        assertTrue(snapshot4.isEmpty());
        
    }

}
