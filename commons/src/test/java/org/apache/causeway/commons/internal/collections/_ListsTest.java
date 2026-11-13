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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class _ListsTest {

    @Test
    void varargListFactory() {
        var nullList = _Lists.ofNullable(null);
        assertNotNull(nullList);
        assertEquals(1, nullList.size());

        var otherList = _Lists.ofNullable(4, 5);
        assertNotNull(otherList);
        assertEquals(2, otherList.size());

        var biNullList = _Lists.ofNullable(null, null);
        assertNotNull(biNullList);
        assertEquals(2, biNullList.size());
    }

    @Test
    void singletonListFactory() {
        var emptyList = _Lists.singletonOrElseEmpty(null);
        assertNotNull(emptyList);
        assertEquals(0, emptyList.size());

        var otherList = _Lists.singletonOrElseEmpty(6);
        assertNotNull(otherList);
        assertEquals(1, otherList.size());
    }

    @Test
    void ofArray() {
        var emptyList = _Lists.ofArray(null);
        assertNotNull(emptyList);
        assertEquals(0, emptyList.size());

        var emptyList2 = _Lists.ofArray(new Integer[] {});
        assertNotNull(emptyList2);
        assertEquals(0, emptyList2.size());

        // verify elements are actually copied
        var array = new Integer[] {1, 2, 3};

        assertEquals(_Lists.ofArray(new Integer[] {1, 2, 3}), _Lists.ofArray(array));

        // when
        array[1] = 9;

        // then
        assertEquals(_Lists.ofArray(new Integer[] {1, 9, 3}), _Lists.ofArray(array));
    }

}
