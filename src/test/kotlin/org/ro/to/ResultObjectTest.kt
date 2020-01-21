package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultObjectHandler
import org.ro.snapshots.simpleapp1_16_0.ACTION_SO_CREATE
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ResultObjectTest {

    @Test
    fun testActionSimpleObjectsCreate() {
        val jsonStr = ACTION_SO_CREATE.str
        val ro = ResultObjectHandler().parse(jsonStr) as ResultObject
        val links = ro.links
        assertEquals(0, links.size)

        val ror = ro.result!!

        val resLinks = ror.links
        assertEquals(4, resLinks.size)

        val title = ror.title
        assertEquals("Object: Beutlin", title)
    }

}
