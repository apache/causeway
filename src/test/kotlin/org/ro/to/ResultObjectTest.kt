package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultObjectHandler
import org.ro.urls.ACTION_SO_CREATE
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ResultObjectTest {

    @Test
    fun testActionSimpleObjectsCreate() {
        val jsonStr = ACTION_SO_CREATE.str
        val ir = ResultObjectHandler().parse(jsonStr) as ResultObject
        val links = ir.links
        assertEquals(0, links.size)

        val result = ir.result!!

        val resLinks = result.links
        assertEquals(4, resLinks.size)

        val title = result.title
        assertEquals("Object: Beutlin", title)
    }

}
