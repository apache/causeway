package org.ro.handler

import kotlinx.serialization.UnstableDefault
import org.ro.core.event.EventStore
import org.ro.core.event.ListObserver
import org.ro.to.SO_0
import org.ro.to.SO_LIST_ALL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class TObjectHandlerTest : IntegrationTest() {

    @Test
    fun testService() {
        if (isSimpleAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListObserver()
            // when
            mockResponse(SO_LIST_ALL, obs)
            mockResponse(SO_0, obs)
            // then
            val ol = obs.list
            assertNotNull(ol)
            assertEquals(1, ol.list.size)

            // WHEN
            //      SimpleApp is available at http://localhost:8080/restful
            // AND
            //      a couple of other URL's (3 top menu items) are invoked automatically from SO_LIST_ALL.
            // THEN
            //      the expected number is 5 (for SimpleApp and not 2!
            //assertTrue(5 == EventLog.log.size)
            // }
        }
    }

}

