package org.ro.to

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.urls.FR_OBJECT_PROPERTY_
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class PropertyTest {

    @Test
    fun testParse() {
        val p = Json.parse(Property.serializer(), FR_OBJECT_PROPERTY_.str)
        val actual = p.disabledReason!!
        val expected = "Non-cloneable view models are read-only; Immutable"
        assertEquals(expected, actual)
    }

}
