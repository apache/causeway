package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.PropertyHandler
import org.ro.snapshots.demo2_0_0.DEMO_PROPERTY
import org.ro.snapshots.demo2_0_0.DEMO_PROPERTY_DESCRIPTION
import org.ro.snapshots.simpleapp1_16_0.FR_OBJECT_PROPERTY_
import org.ro.snapshots.simpleapp1_16_0.SO_PROPERTY
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@UnstableDefault
class PropertyTest {

    @Test
    fun testDemoPropertyDescription() {
        val jsonStr = DEMO_PROPERTY_DESCRIPTION.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertEquals("parity", p.id)
        assertEquals("The parity of this 'DemoItem'.", p.extensions!!.description)
    }

    @Test
    fun testDemoObjectProperty() {
        val jsonStr = DEMO_PROPERTY.str
        val p = PropertyHandler().parse(jsonStr) as Property
        assertEquals("string", p.id)
        assertEquals("string", p.extensions!!.xIsisFormat)
        assertEquals(5, p.links.size)

        val modifyLink = p.links[2]
        assertEquals("PUT", modifyLink.method)

        val arguments = modifyLink.arguments
        assertEquals(1, arguments.size)
        assertTrue(arguments.containsKey("value"))
    }

    @Test
    fun testSimpleObjectProperty() {
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

    @Test
    fun testFixtureResultObjectPropety() {
        val jsonStr = FR_OBJECT_PROPERTY_.str
        val p = PropertyHandler().parse(jsonStr) as Property
        val actual = p.disabledReason!!
        val expected = "Non-cloneable view models are read-only; Immutable"
        assertEquals(expected, actual)
    }

}
