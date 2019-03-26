package org.ro.handler

import org.ro.core.Menu
import org.ro.core.event.LogEntry
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertNotNull

class ServiceHandlerTest  () {

    @Test
    fun testService() {
        if (TestUtil().isSimpleAppAvailable()) {
            // this is an IntegrationTest, since urls referenced in jsonStr are
            // expected to loaded from a running backend, here SimpleApp localhost:8080/restful*
            // given
            TestUtil().login()
            val le = LogEntry("", "GET", "")
            le.response = RESTFUL_SERVICES.str
            // when
            Dispatcher.handle(le)

            val m1: Menu? = null //FIXME DisplayManager.getMenu() how to access RoMenuBar items?
            // then
            assertNotNull(m1)
            assertNotNull(m1.list)
        }
    }

}