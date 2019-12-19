package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultHandler
import org.ro.snapshots.ai1_16_0.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class ListResultTest {

    @Test
    fun testParseServices() {
        val jsonStr = RESTFUL_SERVICES.str
        val services = ResultHandler().parse(jsonStr) as ResultListResult
        val values = services.value
        assertNotNull(values)
        assertEquals(8, values.size)
    }

}
