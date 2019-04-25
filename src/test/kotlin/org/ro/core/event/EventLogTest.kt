package org.ro.core.event

import org.ro.to.Method
import org.ro.to.RESTFUL
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.*

class EventLogTest {

    @Test
    fun testSecondEntry() {
        // given
        val initialSize: Int = EventStore.log.size
        val myFirst = "1"
        val myLast = "n"
        val method = Method.GET.operation
        //val myEveryThing: String = ".."

        val selfStr = RESTFUL_SERVICES.str
        val selfUrl = "http://localhost:8080/restful/services"
        val upStr: String = RESTFUL.str
        val upUrl: String = "http://localhost:8080/restful/"

        // when
        EventStore.start(selfUrl, method, myFirst)
        EventStore.start(upUrl, method, myFirst)
        EventStore.end(selfUrl, selfStr)
        EventStore.end(upUrl, upStr)
        EventStore.start(selfUrl, method, myLast)
        EventStore.start(upUrl, method, myLast)
        // then
        val currentSize: Int = EventStore.log.size
        assertEquals(4 + initialSize, currentSize)

        // Entries with the same key can be written, but when updated or retrieved the first (oldest) entry should be used
        //when
        val le2: LogEntry? = EventStore.find(selfUrl)
        //then
        assertNotNull(le2)
        assertEquals(le2.request, myFirst)
        assertEquals(le2.response.length, selfStr.length)
        //when
        val leU: LogEntry? = EventStore.find(upUrl)
        //then
        assertEquals(leU!!.request, myFirst)
        assertEquals(leU.response.length, upStr.length)
    }

    @Test
    fun testFindView() {
        val h1 = "http://localhost:8080/restful/objects/simple.SimpleObject/51/object-layout"
        val h2 = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"
        val i1 = "Test (1)"
        val i2 = "Test (2)"

        // construct list with urls
        EventStore.add(h1)
        EventStore.add(i1)
        EventStore.add(h2)
        EventStore.add(i2)

        val le1 = EventStore.findView(h1)
        assertEquals(null, le1)

        val le2 = EventStore.findView(h2)
        assertEquals(null, le2)

        val le3 = EventStore.findView(i2)
        assertNotNull(le3)
        val le4 = EventStore.findView(i1)
        assertNotNull(le4)

        EventStore.close(i1)
        console.log("[EventLogTest.testFindView] $i1")

        assertTrue(le4.isClosedView())
    }

    @Test
    fun testFind() {
        val ol1 = "http://localhost:8080/restful/objects/simple.SimpleObject/51/object-layout"
        val ol2 = "http://localhost:8080/restful/objects/simple.SimpleObject/52/object-layout"
        val ol3 = "http://localhost:8080/restful/objects/simple.SimpleObject/53/object-layout"
        val ol9 = "http://localhost:8080/restful/objects/simple.SimpleObject/59/object-layout"
        val olx = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"

        // construct list with urls
        EventStore.add(ol1)
        EventStore.add(ol2)
        EventStore.add(ol3)

        val le1 = EventStore.find(ol1)
        assertNotNull(le1)

        val le2 = EventStore.findExact(ol9)
        assertNull(le2)

        val le3 = EventStore.findEquivalent(ol9)
        // FIXME 
        /*  assertNotNull(le3)
       assertEquals(ol1, le3.url)

        val le4 = EventLog.find(ol9)
        assertEquals(le3, le4)

        val le5 = EventLog.findSimilar(olx)
        assertNull(le5)

        val p1 = "http://localhost:8080/restful/objects/simple.SimpleObject/11/properties/name"
        val p2 = "http://localhost:8080/restful/objects/simple.SimpleObject/12/properties/name"
        val p3 = "http://localhost:8080/restful/objects/simple.SimpleObject/13/properties/name"
        EventLog.add(p1)
        EventLog.add(p2)
        EventLog.add(p3)
        val le6 = EventLog.find(p3)
        assertNotNull(le6)
        assertEquals(le6.url, p1)

        val pName = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/name"
        val pNotes = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes"
        EventLog.add(pName)
        EventLog.add(pNotes)
        val le7 = EventLog.find(pNotes)
        assertNotNull(le7)
        assertEquals(le7.url, pNotes)  */
    }
}