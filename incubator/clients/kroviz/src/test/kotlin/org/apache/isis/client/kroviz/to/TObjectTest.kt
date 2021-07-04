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

import org.apache.isis.client.kroviz.handler.TObjectHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.ACTIONS_STRINGS_INVOKE
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.ACTIONS_TEXT_INVOKE
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.ISIS_SECURITY_ME_SERVICE
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_0
import org.apache.isis.client.kroviz.utils.Utils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TObjectTest {

    @Test
    fun testIsisSecurityMe() {
        //given
        val jsonStr = ISIS_SECURITY_ME_SERVICE.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val members = tObject.members
        // then
        assertEquals(27, members.size)
    }

    @Test
    fun testPropertiesChanged() {
        //given
        val jsonStr = SO_0.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val properties = tObject.getProperties()
        // then
        val mutable = properties.filter { it.isReadWrite() }
        assertEquals(1, mutable.size)

        //when
        mutable.first().value!!.content = "l on the hill"
        //then
        val putBody = Utils.propertiesAsBody(tObject)
        assertTrue(putBody.contains("notes"))
        assertTrue(putBody.contains("value"))
        assertTrue(putBody.contains("l on the hill"))
    }

    @Test
    fun testLinksMembersProperties() {
        //given
        val jsonStr = SO_0.str
        // when
        val to = TObjectHandler().parse(jsonStr) as TObject
        val members = to.members
        val properties = to.getProperties()
        // then
        assertNotNull(to.links)
        assertEquals("Object: Foo", to.links[0].title)
        assertEquals(10, members.size)
        assertEquals(4, properties.size)

        val namedMembers = properties.filter { it.id == "name" }
        assertEquals(1, namedMembers.size)

        val nameMember = namedMembers.first()
        val content = nameMember.value!!.content as String
        assertEquals("Foo", content)
    }

    @Test
    fun testTextDemo() {
        //given
        val jsonStr = ACTIONS_TEXT_INVOKE.str
        // when
        val to = TObjectHandler().parse(jsonStr) as TObject
        val members = to.members
        val properties = to.getProperties()
        // then
        assertNotNull(to.links)
        assertEquals("TextDemo", to.links[0].title)
        assertEquals(6, members.size)
        assertEquals(5, properties.size)

        val filteredProperties = properties.filter { it.id == "description" }
        assertEquals(1, filteredProperties.size)

        val description = filteredProperties.first()
        val content = description.value!!.content as String
        assertTrue(content.startsWith("<div") && content.endsWith("div>"))
    }

    @Test
    fun testActionsStringsInvoke() {
        //given
        val jsonStr = ACTIONS_STRINGS_INVOKE.str
        // when
        val to = TObjectHandler().parse(jsonStr) as TObject
        // then
        assertEquals("String data type", to.links[0].title)
        assertEquals(9, to.members.size)
        assertEquals(1, to.getCollections().size)
        assertEquals(8, to.getActions().size)
        assertEquals(0, to.getProperties().size)

        val filteredProperties = to.getProperties().filter { it.id == "description" }
        assertEquals(0, filteredProperties.size)
    }

}
