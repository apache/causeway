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
package org.apache.causeway.client.kroviz.to

import org.apache.causeway.client.kroviz.IntegrationTest
import org.apache.causeway.client.kroviz.handler.CollectionHandler
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.OBJECT_COLLECTION
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionTest : IntegrationTest() {

    @Test
    fun testParse() {
        //given
        val jsonStr = OBJECT_COLLECTION.str
        //when
        val collection = CollectionHandler().parse(jsonStr) as Collection
        //then
        assertEquals("entities", collection.id)
        assertEquals("collection", collection.memberType)

        val linkList = collection.links
        assertEquals(3, linkList.size)

        assertEquals("list", collection.extensions.collectionSemantics)

        val valueList = collection.value
        assertEquals(2, valueList.size)
    }

}
