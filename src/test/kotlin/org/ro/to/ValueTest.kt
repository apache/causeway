package org.ro.to

import kotlinx.serialization.json.JSON
import kotlin.test.Test
import kotlin.test.assertEquals

class ValueTest {

    @Test
    fun testParseLink() {
        val jsonStr = """{"value": 
                            {"rel": "R", "href": "H", "method": "GET", "type": "TY", "title": "TI"}
                    }"""
        val v = parse(jsonStr)
        val raw = v.value.toString()
        val actual = JSON.parse(Link.serializer(), raw)
        console.log("[VT.testParseLink] actual $actual")
        val expected = Link()
        assertEquals(expected::class, actual::class)
        assertEquals("R", actual.rel)
    }

    @Test
    fun testParseLong() {
        val jsonStr = """{"value": 1514897074953}"""
        val v = parse(jsonStr)
        val actual = v.value as Long
        val expected = 1514897074953L
        assertEquals(expected, actual)
    }

    @Test
    fun testParseInt() {
        val jsonStr = """{"value": 0}"""
        val v = parse(jsonStr)
        val actual = v.value as Int
        val expected = 0
        assertEquals(expected, actual)
    }

    @Test
    fun testParseString() {
        val jsonStr = """{"value": "Foo"}"""
        val v = parse(jsonStr)
        val actual = v.value as String
        val expected = "Foo"
        assertEquals(expected, actual)
    }

    @Test
    fun testParseNull() {
        val jsonStr = """{"value": null}"""
        val v = parse(jsonStr)
        val expected = "null"
        val actual = v.value.toString()
        assertEquals(expected, actual)
    }

    private fun parse(jsonStr: String): Value {
        return JSON.parse(Value.serializer(), jsonStr)
    }

}