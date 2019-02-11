package org.ro.handler

import kotlinx.serialization.ImplicitReflectionSerializer
import org.ro.core.Utils
import org.ro.core.event.EventLog
import org.ro.core.event.LogEntry
import org.ro.core.model.ObjectList
import org.ro.to.SO_0
import org.ro.to.SO_LIST_ALL
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ImplicitReflectionSerializer
class TObjectHandlerTest {

    @Test
    fun testService() {
        // given
        val ol = ObjectList()
        // when
        val le0: LogEntry = createLogEntry(SO_LIST_ALL.str)
        Dispatcher.handle(le0)
        val le1: LogEntry = createLogEntry(SO_0.str)
        Dispatcher.handle(le1)
        // then
        assertNotNull(ol)
        assertTrue(ol.length() == 0)
        //After SO_LIST_ALL is invoked, a couple of other URL's (3 top menu items) are invoked automatically.
        // therefore the expected number is 5 (for SimpleApp and not 2!
        assertTrue(5 == EventLog.log.size)
    }

    private fun createLogEntry(jsonStr: String): LogEntry {
        val url: String? = Utils().getSelfHref(jsonStr)
        val le: LogEntry = EventLog.start(url!!, "", "")
        le.response = jsonStr
        return le
    }

}

