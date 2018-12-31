package org.ro.handler

import org.ro.URLS
import org.ro.core.DisplayManager
import org.ro.core.Globals
import org.ro.core.Menu
import org.ro.core.event.LogEntry
import kotlin.test.Test
import kotlin.test.assertNotNull

class ServiceHandlerTest {

    @Test
    public fun testService() {
        // given
        // when
        var le: LogEntry = LogEntry("", "GET", null)
        le.response = JSON.stringify(URLS.RESTFUL_SERVICES)
        Globals.dispatcher.handle(le)
        var m1: Menu? = DisplayManager.getMenu()
        // then 
        assertNotNull(m1)
        assertNotNull(m1.menuItems)
    }

}