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
package org.apache.isis.client.kroviz.to.mb

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.handler.MenuBarsHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.DEMO_MENUBARS
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.RESTFUL_MENUBARS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class MenubarsTest {

    @Test
    fun testDemoMenubars() {
        //given
        val jsonStr = DEMO_MENUBARS.str
        //when
        val menuBars = MenuBarsHandler().parse(jsonStr) as Menubars

        //then
        assertNotNull(menuBars.primary)
        assertNotNull(menuBars.secondary)
        assertNotNull(menuBars.tertiary)

        val primary = menuBars.primary
        assertEquals("Featured Types", primary.menu.first().named)
        assertEquals("Other", primary.menu.last().named)
        assertEquals(8, primary.menu.size)

        val section = primary.menu.first().section
        val serviceActions = section.first().serviceAction
        assertEquals(4, serviceActions.size)

        val sa1 = serviceActions.first()
        assertEquals("demo.FeaturedTypesMenu", sa1.objectType)
        assertEquals("text", sa1.id)
        assertEquals("Text", sa1.named)

        val l1 = sa1.link!!
        assertEquals("urn:org.restfulobjects:rels/action", l1.rel)
        assertEquals("GET", l1.method)
        assertEquals("http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/text", l1.href)
        assertEquals("application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"", l1.type)

        val saN = serviceActions.last()
        assertEquals("blobs", saN.id)
        assertEquals("Blob Type", saN.named)
    }

    @Test
    fun testParseMenubars() {
        //given
        val jsonStr = RESTFUL_MENUBARS.str
        //when
        val menuBars = MenuBarsHandler().parse(jsonStr) as Menubars

        //then
        assertNotNull(menuBars.primary)
        assertNotNull(menuBars.secondary)
        assertNotNull(menuBars.tertiary)

        val primary = menuBars.primary
        assertEquals("Simple Objects", primary.menu.first().named)

        val section = primary.menu.first().section
        val serviceActions = section.first().serviceAction
        assertEquals(3, serviceActions.size)

        val sa1 = serviceActions.first()
        assertEquals("simple.SimpleObjectMenu", sa1.objectType)
        assertEquals("create", sa1.id)
        assertEquals("Create", sa1.named)

        val l1 = sa1.link!!
        assertEquals("urn:org.restfulobjects:rels/action", l1.rel)
        assertEquals("GET", l1.method)
        assertEquals("http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/create", l1.href)
        assertEquals("application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"", l1.type)

        val saN = serviceActions.last()
        assertEquals("listAll", saN.id)
        assertEquals("List All", saN.named)
    }

}
