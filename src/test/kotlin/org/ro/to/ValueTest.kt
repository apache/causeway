package org.ro.to

import kotlinx.serialization.json.Json
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
// This test fails due to an unkown reason.
// Passing the same strings indirectly via Member(Test) works, so there is no additional coverage.
// Left in here for further inspection.
// IIRC ValueTest used to work when Value.content type was JsonObject.
class ValueTest {

    @Test
    fun testParseLink() {
        val jsonStr = """{
                "value": 
                    {"rel": "R", "href": "H", "method": "GET", "type": "TY", "title": "TI"}
        }"""
        val v = parse(jsonStr)
        val raw = v.content.toString()
        val actual = Json.nonstrict.parse(Link.serializer(), raw)
        console.log("[VT.testParseLink] actual $actual")
        val expected = Link()
        assertEquals(expected::class, actual::class)
        assertEquals("R", actual.rel)
    }

    @Test
    fun testParseLong() {
        val jsonStr = """{ "value": 1514897074953 }"""
        val v = parse(jsonStr)
        val actual = v.content as Long
        val expected = 1514897074953L
        assertEquals(expected, actual)
    }

    @Test
    fun testParseInt() {
        val jsonStr = """{ "value": 0 }"""
        val v = parse(jsonStr)
        val actual = v.content as Int
        val expected = 0
        assertEquals(expected, actual)
    }

    @Test
    fun testParseString() {
        val jsonStr = """{ "value": "Foo" }"""
        val v = parse(jsonStr)
        val actual = v.content!! as String
        val expected = "Foo"
        assertEquals(expected, actual)
    }

    @Test
    fun testParseNull() {
        val jsonStr = """{ "value": null }"""
        val v = parse(jsonStr)
        val expected = "null"
        val actual = v.content.toString()
        assertEquals(expected, actual)
    }

    private fun parseNew(jsonStr: String): Value {
//        val obj =  jsonStr.asJsObject()
//        console.log("[${this::class.simpleName}.parse] obj ${obj}")
//        val dyn =  obj.asDynamic()
//        console.log("[${this::class.simpleName}.parse] dyn ${dyn}")
//        console.log("[${this::class.simpleName}.parse] dyn ${dyn::class}")
//        val str = dyn as String
//        console.log("[${this::class.simpleName}.parse] string ${str}")
//        return JSON.nonstrict.parse(Value.serializer(), str)

        val newStr = jsonStr.replace("value", "content")

        val obj: Any? = kotlin.js.JSON.parse(newStr)
        console.log("[${this::class.simpleName}.parse] obj ${obj}")
        return obj as Value

    }

    private fun parse(jsonStr: String): Value {
        return Json.nonstrict.parse(Value.serializer(), jsonStr)
    }

}