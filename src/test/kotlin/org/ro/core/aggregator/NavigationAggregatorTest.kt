package org.ro.core.aggregator

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.to.ResultListResult
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class NavigationAggregatorTest : IntegrationTest() {

    @Test
    fun testRestfulServices() {
        // given
        val aggregator = NavigationAggregator()
        // when
        val logEntry = mockResponse(RESTFUL_SERVICES, aggregator)
        val result = logEntry.getTransferObject() as ResultListResult
        val actual = result.value.size
        // then
        assertEquals(8, actual)
    }

}
