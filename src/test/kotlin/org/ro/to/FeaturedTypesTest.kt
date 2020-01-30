package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.TObjectHandler
import org.ro.snapshots.demo2_0_0.DEMO_PRIMITIVES
import org.ro.snapshots.demo2_0_0.DEMO_TEMPORALS
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class FeaturedTypesTest {

    @Test
    fun testTemporals() {
        //given
        val jsonStr = DEMO_TEMPORALS.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val properties = tObject.getProperties()
        // then
        assertEquals(7, properties.size) //1

        val javaSqlDate = properties.firstOrNull { it.id == "javaSqlDate" }!!
        assertEquals("date", javaSqlDate.format) // 2
        assertEquals("javasqldate", javaSqlDate.extensions!!.xIsisFormat) //3
        assertEquals(true, javaSqlDate.type == ValueType.DATE.type) //4
        val dt = javaSqlDate.value?.content
        assertEquals("2020-01-24", dt as String) //5

        val javaSqlTimestamp = properties.firstOrNull { it.id == "javaSqlTimestamp" }!!
        assertEquals("utc-millisec", javaSqlTimestamp.format) // 6
        assertEquals("javasqltimestamp", javaSqlTimestamp.extensions!!.xIsisFormat) //7
        assertEquals(true, javaSqlTimestamp.type == ValueType.TIME.type) //8
        console.log("[FTT.testTemporals]")
        console.log(javaSqlTimestamp.value!!)
        console.log(javaSqlTimestamp.value!!.content)
        val ts = javaSqlTimestamp.value?.content
        assertEquals(1579957625356, ts) //9
    }

    @Test
    fun testPrimitives() {
        //given
        val jsonStr = DEMO_PRIMITIVES.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val properties = tObject.getProperties()
        val actions = tObject.getActions()
        // then
        assertEquals(17, properties.size) //1
        assertEquals(18, actions.size) //2

        val description = properties.firstOrNull { it.id == "description" }!!
        assertEquals("string", description.format) //3
        assertEquals("string", description.extensions!!.xIsisFormat) //4
        assertEquals(true, description.type == ValueType.HTML.type) //5

        val javaLangBoolean = properties.firstOrNull { it.id == "javaLangBoolean" }!!
        assertEquals("boolean", javaLangBoolean.extensions!!.xIsisFormat) //6
        assertEquals(true, javaLangBoolean.type == ValueType.BOOLEAN.type) //7

        val javaLangByte = properties.firstOrNull { it.id == "javaLangByte" }!!
        assertEquals("int", javaLangByte.format)
        assertEquals("byte", javaLangByte.extensions!!.xIsisFormat)
        assertEquals(true, javaLangByte.type == ValueType.NUMERIC.type)
        val jlb = javaLangByte.value?.content as Long
        assertEquals(127.toLong(), jlb)

        val primitiveByte = properties.firstOrNull { it.id == "primitiveByte" }!!
        assertEquals("int", primitiveByte.format)
        assertEquals("byte", primitiveByte.extensions!!.xIsisFormat)
        assertEquals(true, primitiveByte.type == ValueType.NUMERIC.type)
        val pb = primitiveByte.value?.content as Long
        assertEquals(-128.toLong(), pb)
    }

}
