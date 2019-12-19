package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.core.aggregator.ObjectAggregator
import org.ro.handler.ResultValueHandler
import org.ro.snapshots.ai1_16_0.ACTIONS_DOWNLOAD_VALUE
import org.ro.snapshots.ai1_16_0.ACTIONS_OPEN_SWAGGER_UI
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ResultValueTest : IntegrationTest() {

    @Test
    fun testParseActionOpenSwaggerUI() {
        val jsonStr = ACTIONS_OPEN_SWAGGER_UI.str
        val ir = ResultValueHandler().parse(jsonStr) as ResultValue
        val links = ir.links
        assertEquals(1, links.size)

        val result = ir.result!!
        val value = result.value!!.content as String
        assertEquals("http:/swagger-ui/index.html", value)
    }

    @Test
    fun testDownload() {
        // given
        val aggregator = ObjectAggregator("object test")
        // when
        val logEntry = mockResponse(ACTIONS_DOWNLOAD_VALUE, aggregator)
        val ro = logEntry.getTransferObject() as ResultValue
        val type = ro.resulttype
        // then
        assertEquals(ResultType.SCALARVALUE.type, type)

    }

}
