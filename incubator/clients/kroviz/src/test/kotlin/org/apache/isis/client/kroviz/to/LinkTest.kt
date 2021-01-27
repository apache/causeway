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

import kotlinx.serialization.json.Json
import org.apache.isis.client.kroviz.handler.*
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.*
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.RESTFUL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LinkTest {

    @Test
    fun testParse() {
        //given
        val jsonStr = """{
            "rel": "R",
            "href": "H",
            "method": "GET",
            "type": "TY",
            "title": "TI"
        }"""

        // when
        val link = Json.decodeFromString(Link.serializer(), jsonStr)

        // then
        assertEquals("R", link.rel)
    }

    @Test
    fun testArgumentsCanHaveEmptyKeys() {
        val href = "href"
        val arg = Argument(href)
        val args = mutableMapOf<String, Argument?>()
        args.put("", arg)
        val l = Link(arguments = args, href = href)
        // then
        val arguments = l.argMap()!!
        val a = arguments[""]
        assertEquals("href", a!!.key)
    }

    @Test
    fun testFindRelation() {
        //given
        var rel: Relation?
        //when
        rel = Relation.find("menuBars")
        //then
        assertEquals(Relation.MENU_BARS, rel)

        //when
        rel = Relation.find("self")
        //then
        assertEquals(Relation.SELF, rel)

        //when
        rel = Relation.find("services")
        //then
        assertEquals(Relation.SERVICES, rel)
    }

    @Test
    fun testFindParsedLinkEnums() {
        //given
        val map = Response2Handler.map
        //when
        map.forEach { rh ->
            val jsonStr = rh.key.str
            val ro = rh.value.parse(jsonStr)
            if (ro is HasLinks) {
                val links = ro.links
                links.forEach { l ->
                    console.log("[LT.testFindParsedLinkENums]")
                    console.log(l)
                    console.log(l.relation())
                    console.log(l.representation())
                }
            }
        }
        //then
        assertTrue(true, "no exception in loop")
    }

}
