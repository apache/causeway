package org.ro.to

import org.ro.handler.PropertyHandler
import org.ro.urls.FR_OBJECT_PROPERTY_
import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyTest {

    @Test
    fun testParse() {
        val to = PropertyHandler().parse(FR_OBJECT_PROPERTY_.str)
        val p = to as Property
        val actual = p.disabledReason!!
        val expected = "Non-cloneable view models are read-only; Immutable"
        assertEquals(expected, actual)
    }
    
}