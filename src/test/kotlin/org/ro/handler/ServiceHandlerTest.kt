package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.core.event.LogEntry
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertNotNull

@ImplicitReflectionSerializer
class ServiceHandlerTest  () {

    @Test
    fun testService() {
        if (TestUtil().isSimpleAppAvailable()) {
            // this is an IntegrationTest, since urls referenced in jsonStr are
            // expected to loaded from a running backend, here SimpleApp localhost:8080/restful*
            // given
            val le = LogEntry("", "GET", "")
            le.response = RESTFUL_SERVICES.str
            // when
            Dispatcher.handle(le)

            val m1: Menu? = DisplayManager.getMenu()
            // then
            assertNotNull(m1)
            assertNotNull(m1.menuItems)
        }
    }

}