package org.ro.handler

import org.ro.core.event.ListObserver
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import org.ro.to.SO_LIST_ALL_INVOKE
import kotlin.test.Test
import kotlin.test.assertNotNull


class ListHandlerTest {

    @Test
    fun testService() {
        // given
        TestUtil().login()
        // when
        val le = LogEntry("", "GET", "")
        le.response = SO_LIST_ALL_INVOKE.str
        Dispatcher.handle(le)
        val lo: ListObserver = le.observer as ListObserver
        val t1: ObjectList = lo.getList()
        // then
        assertNotNull(t1)
//        assertTrue(Menu.uniqueMenuTitles().size > 2)
    }

}

