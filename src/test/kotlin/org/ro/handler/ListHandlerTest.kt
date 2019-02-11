package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.DisplayManager
import org.ro.core.Menu
import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import org.ro.to.SO_LIST_ALL_INVOKE
import kotlin.test.Test
import kotlin.test.assertNotNull

@ImplicitReflectionSerializer
class ListHandlerTest {

    @Test
    fun testService() {
        // given
        val m = Menu(0)
        DisplayManager.setMenu(m)
        // when
        val le = LogEntry("", "GET", "")
        le.response = SO_LIST_ALL_INVOKE.str
        Dispatcher.handle(le)
        val lo: ListObserver = le.observer as ListObserver
        val t1: ObjectList = lo.getList()
        // then
        assertNotNull(t1)
    }

}

