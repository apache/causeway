package org.ro.to


import kotlinx.serialization.json.JSON
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class MemberTest() {

    @Test
    fun testParse() {
        val m = JSON.parse(Member.serializer(), FR_PROPERTY_DESCRIPTION.str)
        val extensions: Extensions? = m.extensions
        assertNotNull(extensions)
        assertEquals("Result class", extensions.friendlyName)
    }

}