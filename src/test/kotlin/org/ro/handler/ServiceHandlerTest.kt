package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.core.event.LogEntry
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertNotNull

@ImplicitReflectionSerializer
class ServiceHandlerTest {

    @Test
    fun testService() {
        // given
        // when
        val le = LogEntry("", "GET", "")
        le.response = JSON.stringify(RESTFUL_SERVICES.str)
        Dispatcher.handle(le)
        val m1: Menu? = DisplayManager.getMenu()
        // then 
        assertNotNull(m1)
        assertNotNull(m1.menuItems)
    }

}