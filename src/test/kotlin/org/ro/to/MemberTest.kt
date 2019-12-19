package org.ro.to

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.handler.MemberHandler
import org.ro.snapshots.ai1_16_0.FR_PROPERTY_DESCRIPTION
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
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
        val expected = Link()
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
        assertEquals("ResultListResult class", extensions.friendlyName)
    }

    private fun parse(jsonStr: String): Member {
        return Json.parse(Member.serializer(), jsonStr)
    }

    private fun buildJsonWith(value: Any): String {
        return """{
        "id": "className",
        "memberType": "property",
        "value": $value
    }"""
    }

}
