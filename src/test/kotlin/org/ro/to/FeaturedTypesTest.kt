package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.TObjectHandler
import org.ro.snapshots.demo2_0_0.DEMO_PRIMITIVES
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class FeaturedTypesTest {

    @Test
    fun testPropertiesChanged() {
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
        assertEquals(true, description.type == "Html") //5

        val javaLangBoolean = properties.firstOrNull { it.id == "javaLangBoolean" }!!
        assertEquals("boolean", javaLangBoolean.extensions!!.xIsisFormat) //6
        assertEquals(true, javaLangBoolean.type == "Boolean") //7

        val javaLangByte = properties.firstOrNull { it.id == "javaLangByte" }!!
        assertEquals("int", javaLangByte.format)
        assertEquals("byte", javaLangByte.extensions!!.xIsisFormat)
        assertEquals(true, javaLangByte.type == "Numeric")
        val jlb = javaLangByte.value?.content as Long
        assertEquals(127.toLong(), jlb)

        val primitiveByte = properties.firstOrNull { it.id == "primitiveByte" }!!
        assertEquals("int", primitiveByte.format)
        assertEquals("byte", primitiveByte.extensions!!.xIsisFormat)
        assertEquals(true, primitiveByte.type == "Numeric")
        val pb = primitiveByte.value?.content as Long
        assertEquals(-128.toLong(), pb)
    }

}
