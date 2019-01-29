package org.ro.handler

import org.ro.URLS
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import kotlin.test.Test
import kotlin.test.assertNotNull

class ListHandlerTest {

    @Test
    fun testService() {
        // given
        val m: Menu = Menu(0)
        DisplayManager.setMenu(m)
        // when
        val le: LogEntry = LogEntry("", "GET", "")
        le.response = JSON.stringify(URLS.SO_LIST_ALL_INVOKE)
        Dispatcher.handle(le)
        val lo: ListObserver = le.observer as ListObserver
        val t1: ObjectList = lo.getList()
        // then
        assertNotNull(t1)
    }

}

