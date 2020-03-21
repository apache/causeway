package org.apache.isis.client.kroviz.core.aggregator

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.ACTION_SO_CREATE
import org.apache.isis.client.kroviz.to.ResultObject
import org.apache.isis.client.kroviz.to.ResultType
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ObjectAggregatorTest : IntegrationTest() {

    @Test
    fun testRestfulServices() {
        // given
        val aggregator = ObjectAggregator("object test")
        // when
        val logEntry = mockResponse(ACTION_SO_CREATE, aggregator)
        val ro = logEntry.getTransferObject() as ResultObject
        val type = ro.resulttype
        // then
        assertEquals(ResultType.DOMAINOBJECT.type, type)

        val links = ro.links
        assertEquals(0, links.size)

        val ror = ro.result!!

        val resLinks = ror.links
        assertEquals(4, resLinks.size)

        val title = ror.title
        assertEquals("Object: Beutlin", title)
    }
}
