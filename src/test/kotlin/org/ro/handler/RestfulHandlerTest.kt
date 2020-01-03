package org.ro.handler

import kotlinx.serialization.UnstableDefault
import org.ro.snapshots.ai1_16_0.RESTFUL
import org.ro.to.Restful
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
public class RestfulHandlerTest {

    @Test
    fun testParse() {
        val jsonStr = RESTFUL.str
        val ro = RestfulHandler().parse(jsonStr) as Restful
        assertNotNull(ro)

        assertEquals(6, ro.links.size)
        assertEquals("", ro.extensions.oid)
    }

}
