package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.TObjectHandler
import org.ro.snapshots.demo2_0_0.DEMO_PRIMITIVES
import kotlin.test.Test
import kotlin.test.assertEquals

class FeaturedTypesTest {

    @UnstableDefault
    @Test
    fun testPropertiesChanged() {
        //given
        val jsonStr = DEMO_PRIMITIVES.str
        // when
        val tObject = TObjectHandler().parse(jsonStr) as TObject
        val properties = tObject.getProperties()
        val actions = tObject.getActions()
        // then
        assertEquals(17, properties.size)
        assertEquals(18, actions.size)

        val description = properties.firstOrNull { it.id == "description" }!!
        assertEquals("string", description.format)
        assertEquals("string", description.extensions!!.xIsisFormat)
        assertEquals(true, description.isHtml())

        val javaLangBoolean = properties.firstOrNull { it.id == "javaLangBoolean" }!!
        assertEquals("boolean", javaLangBoolean.extensions!!.xIsisFormat)
        assertEquals(true, javaLangBoolean.isBoolean())

        val javaLangByte = properties.firstOrNull { it.id == "javaLangByte" }!!
        assertEquals("int", javaLangByte.format)
        assertEquals("byte", javaLangByte.extensions!!.xIsisFormat)
        assertEquals(true, javaLangByte.isNumeric())
     }

}
