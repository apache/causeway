package org.ro.to

import kotlinx.serialization.json.JSON
import org.ro.handler.MemberHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MemberTest() {

    @Test
    fun testParseString() {
        val jsonStr = """{
        "id": "className",
        "memberType": "property",
        "value": "Object: Foo"
    }"""
        val actual = JSON.parse(Member.serializer(), jsonStr)  as Member
        assertEquals("className", actual.id)
        console.log("[MT.testParseString] value ${actual.value}")
        assertEquals("Object: Foo", (actual.value!!.value.toString()))
    }

    @Test
    fun testParseLink() {
        val jsonStr = """{
        "id": "className",
        "memberType": "property",
        "value": {"rel": "R", "href": "H", "method": "GET", "type": "TY", "title": "TI"}
    }"""
        //
        val m = JSON.nonstrict.parse(Member.serializer(), jsonStr)  as Member
        assertEquals("className", m.id )
                console.log("[MT.testParseLink] value ${m.value}")
        val actual = m.value!!.value as Link
        console.log("[MT.testParseLink] actual $actual")
        val expected = Link()
        assertEquals(expected::class, actual::class)
    }

    @Test
    fun testParseNull() {
        val jsonStr = """{
        "id": "className",
        "memberType": "property",
        "value": null
    }"""
        val actual = JSON.parse(Member.serializer(), jsonStr)  as Member
        assertEquals("className", actual.id)
        assertEquals(null, actual.value)
    }

    @Test
    fun testParse() {
        val m = MemberHandler().parse(FR_PROPERTY_DESCRIPTION.str) as Member
        val extensions: Extensions? = m.extensions
        assertNotNull(extensions)
        assertEquals("Result class", extensions.friendlyName)
    }

}