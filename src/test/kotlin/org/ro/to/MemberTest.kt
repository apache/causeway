package org.ro.to

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.handler.MemberHandler
import org.ro.urls.FR_PROPERTY_DESCRIPTION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class MemberTest() {

    @Test
    fun testParseLong() {
        val jsonStr = getJsonWith(1234567890)
        val actual = parse(jsonStr)
        //FIXME using Longs eg. 123456789012 results in a NumberFormatException - is this due to Kotlin/KotlinJS differences?
        assertEquals(1234567890, actual.value!!.content as Number)
    }

    @Test
    fun testParseInt() {
        val jsonStr = getJsonWith(0)
        val actual = parse(jsonStr)
        assertEquals(0, actual.value!!.content as Int)
    }

    @Test
    fun testParseString() {
        val jsonStr = getJsonWith("\"Object: Foo\"")
        val actual = parse(jsonStr)
        assertEquals("className", actual.id)
        assertEquals("Object: Foo", (actual.value!!.content.toString()))
    }

    @Test
    fun testParseLink() {
        val jsonStr = getJsonWith("""{"rel": "R", "href": "H", "method": "GET", "type": "TY", "title": "TI"}""")
        val m = parse(jsonStr)
        assertEquals("className", m.id)
        val actual = m.value!!.content as Link
        val expected = Link()
        assertEquals(expected::class, actual::class)
    }

    @Test
    fun testParseNull() {
        val jsonStr = getJsonWith("null")
        val actual = parse(jsonStr)
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

    private fun parse(jsonStr: String): Member {
        return Json.nonstrict.parse(Member.serializer(), jsonStr)
    }

    private fun getJsonWith(value: Any): String {
        return """{
        "id": "className",
        "memberType": "property",
        "value": $value
    }"""

    }
}
