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
import org.apache.isis.client.kroviz.handler.MemberHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.COLLECTION_DESCRIPTION
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.FR_PROPERTY_DESCRIPTION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MemberTest() {

    @Test
    fun testParseLong() {
        val jsonStr = buildJsonWith(1234567890)
        val actual = parse(jsonStr)
        assertEquals(1234567890L, actual.value!!.content as Number)
    }

    @Test
    fun testParseInt() {
        val jsonStr = buildJsonWith(0)
        val actual = parse(jsonStr)
        assertEquals(0L, actual.value!!.content as Long)
    }

    @Test
    fun testParseString() {
        val jsonStr = buildJsonWith("\"Object: Foo\"")
        val actual = parse(jsonStr)
        assertEquals("className", actual.id)
        assertEquals("Object: Foo", (actual.value!!.content.toString()))
    }

    @Test
    fun testParseLink() {
        val jsonStr = buildJsonWith("""{"rel": "R", "href": "H", "method": "GET", "type": "TY", "title": "TI"}""")
        val m = parse(jsonStr)
        assertEquals("className", m.id)
        val actual = m.value!!.content as Link
        val expected = Link(href = "")
        assertEquals(expected::class, actual::class)
    }

    @Test
    fun testParseNull() {
        val jsonStr = buildJsonWith("null")
        val actual = parse(jsonStr)
        assertEquals("className", actual.id)
        assertEquals(null, actual.value)
    }

    @Test
    fun testParse() {
        val m = MemberHandler().parse(FR_PROPERTY_DESCRIPTION.str) as Member
        val extensions: Extensions? = m.extensions
        assertNotNull(extensions)
        assertEquals("ResultListResult class", extensions.getFriendlyName())
    }

    @Test
    fun testParseCollection() {
        val m = MemberHandler().parse(COLLECTION_DESCRIPTION.str) as Member
        val extensions: Extensions? = m.extensions
        assertNotNull(extensions)
        assertEquals("Entities", extensions.getFriendlyName())
    }

    private fun parse(jsonStr: String): Member {
        return Json.decodeFromString(Member.serializer(), jsonStr)
    }

    private fun buildJsonWith(value: Any): String {
        return """{
        "id": "className",
        "memberType": "property",
        "value": $value
    }"""
    }

}
