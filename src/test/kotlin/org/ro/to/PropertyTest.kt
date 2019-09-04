package org.ro.to

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.urls.FR_OBJECT_PROPERTY_
import org.ro.urls.SO_PROPERTY
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class PropertyTest {

    @Test
    fun testFixtureResultObjectPropety() {
        val p = Json.parse(Property.serializer(), FR_OBJECT_PROPERTY_.str)
        val actual = p.disabledReason!!
        val expected = "Non-cloneable view models are read-only; Immutable"
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleObjectPropety() {
        val p = Json.parse(Property.serializer(), SO_PROPERTY.str)
        assertEquals("notes", p.id)
        assertEquals("string", p.extensions!!.xIsisFormat)
        assertEquals(5, p.links.size)

        val modifyLink = p.links[2]
        assertEquals("PUT", modifyLink.method)

        //FIXME expected is an entry for "arguments": {
        //                "value": null
        //            }
        val arguments = modifyLink.arguments!!.asMap()
  //      assertEquals(1, arguments.size)
    }

}
