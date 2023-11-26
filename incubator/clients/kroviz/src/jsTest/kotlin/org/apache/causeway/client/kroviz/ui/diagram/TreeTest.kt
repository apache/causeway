/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.client.kroviz.ui.diagram

import kotlin.test.*

class TreeTest {

    @BeforeTest
    fun setup() {
    }

    @Test
    fun testAddChildToParent() {
        //given
        val url_0 = "root"
        val url_1 = "level_1"
        val url_1_1 = "level_1_1"
        val url_1_2 = "level_1_2"
        val root = Node(url_0, null)
        val tree = Tree(root)

        //when
        tree.addChildToParent(url_1, url_0)
        tree.addChildToParent(url_1_1, url_1)
        tree.addChildToParent(url_1_2, url_1)

        //then
        val r = tree.find(root, url_0, )!!
        assertEquals(1, r.children.size)
        assertNull(r.parent)

        val c = tree.find(root, url_1, )!!
        assertNotNull(c.parent)
        assertEquals(2, c.children.size)
        assertEquals(url_1_1, c.children.first().name)
        assertEquals(url_1_2, c.children.last().name)
    }

}
