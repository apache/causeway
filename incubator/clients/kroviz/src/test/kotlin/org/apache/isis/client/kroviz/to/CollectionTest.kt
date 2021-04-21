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
package org.apache.isis.client.kroviz.to

import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.handler.CollectionHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.COLLECTIONS_ENTITIES
import kotlin.test.Test
import kotlin.test.assertEquals

class CollectionTest : IntegrationTest() {

    @Test
    fun testParse() {
        //given
        val jsonStr = COLLECTIONS_ENTITIES.str
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

        assertEquals("Immutable", collection.disabledReason)
    }

}
