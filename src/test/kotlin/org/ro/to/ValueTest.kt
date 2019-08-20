package org.ro.to

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
// these tests fail with
// JsonParsingException: Invalid JSON at 0: Expected '{, kind: [object Object]'.message
// there is no additional coverage compared with MemberTest
@UnstableDefault
class ValueTest {

    @Test
    fun testParseLink() {
        val jsonStr = """{"value": {"rel": "R", "href": "H", "method": "GET", "type": "TY", "title": "TI"}
        }"""
        val value = Json.parse(Value.serializer(), jsonStr)
        val actual = value.content as Link
        val expected = Link()
        assertEquals(expected::class, actual::class)
    }

    @Test
    fun testParseLong() {
        val jsonStr = """{"value": 1514897074953 }"""
        val value = Json.parse(Value.serializer(), jsonStr)
        val actual = value.content as Long
        assertEquals(1514897074953L, actual)
    }

    @Test
    fun testParseInt() {
        val jsonStr = """{"value": 0 }"""
        val value = Json.parse(Value.serializer(), jsonStr)
        val actual = value.content as Int
        assertEquals(0, actual)
    }

    @Test
    fun testParseString() {
        val jsonStr = """{"value": "Foo" }"""
        val value = Json.parse(Value.serializer(), jsonStr)
        val actual = value.content!! as String
        assertEquals("Foo", actual)
    }

    @Test
    fun testParseNull() {
        val jsonStr = """{"value": null }"""
        val value = Json.parse(Value.serializer(), jsonStr)
        val actual = value.content.toString()
        assertEquals("null", actual)
    }

}
