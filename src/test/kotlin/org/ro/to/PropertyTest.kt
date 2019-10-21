package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.PropertyHandler
import org.ro.urls.FR_OBJECT_PROPERTY_
import org.ro.urls.SO_PROPERTY
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@UnstableDefault
class PropertyTest {

    @Test
    fun testFixtureResultObjectPropety() {
        val jsonStr = FR_OBJECT_PROPERTY_.str
        val p = PropertyHandler().parse(jsonStr) as Property
        val actual = p.disabledReason!!
        val expected = "Non-cloneable view models are read-only; Immutable"
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleObjectPropety() {
        val jsonStr = SO_PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertEquals("notes", p.id)
        assertEquals("string", p.extensions!!.xIsisFormat)
        assertEquals(5, p.links.size)

        val modifyLink = p.links[2]
        assertEquals("PUT", modifyLink.method)

        val arguments = modifyLink.arguments
        assertEquals(1, arguments.size)
        assertTrue(arguments.containsKey("value"))
    }

}
