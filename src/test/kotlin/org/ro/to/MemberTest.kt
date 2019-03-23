package org.ro.to

import org.ro.handler.MemberHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class MemberTest() {

    @Test
    fun testParse() {
        val m = MemberHandler().parse(FR_PROPERTY_DESCRIPTION.str)
        val extensions: Extensions? = m.extensions
        assertNotNull(extensions)
        assertEquals("Result class", extensions.friendlyName)
    }

}