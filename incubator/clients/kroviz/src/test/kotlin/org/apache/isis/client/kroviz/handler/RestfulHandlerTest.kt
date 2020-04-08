package org.apache.isis.client.kroviz.handler

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.RESTFUL
import org.apache.isis.client.kroviz.to.Restful
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class RestfulHandlerTest {

    @Test
    fun testParse() {
        val jsonStr = RESTFUL.str
        val ro =RestfulHandler().parse(jsonStr) as Restful
        assertNotNull(ro)

        assertEquals(6, ro.links.size)
        assertEquals("", ro.extensions.oid)
    }

}
