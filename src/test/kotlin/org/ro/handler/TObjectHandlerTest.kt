package org.ro.handler

import org.ro.core.event.ListObserver
import org.ro.to.SO_0
import org.ro.to.SO_LIST_ALL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TObjectHandlerTest : IntegrationTest() {

    @Test
    fun testService() {
        if (isSimpleAppAvailable()) {
            // given
            val obs = ListObserver()
            // when
            val le0 = mockResponse(SO_LIST_ALL, obs)
            val le1 = mockResponse(SO_0, obs)
            // then
            val ol = obs.list
            assertNotNull(ol)
            assertEquals(0, ol.list.size)

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

