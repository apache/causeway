package org.ro.core.event

import org.ro.to.Method
import org.ro.to.RESTFUL
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.*

class EventStoreTest {

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
        assertEquals(4 + initialSize, currentSize)  //1

        // Entries with the same key can be written, but when updated or retrieved the first (oldest) entry should be used
        //when
        val le2 = EventStore.find(selfUrl)!!
        //then
        assertEquals(le2.request, myFirst)  //2
        assertEquals(le2.response.length, selfStr.length)  //3
        //when
        val leU = EventStore.find(upUrl)!!
        //then
        assertEquals(leU.request, myFirst)  //4
        assertEquals(leU.response.length, upStr.length)  //5
    }

    @Test
    fun testFindView() {
        val h1 = "http://localhost:8080/restful/objects/simple.SimpleObject/51/object-layout"
        val h2 = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"
        val i1 = "Test (1)"
        val i2 = "Test (2)"

        // construct list with urls
        EventStore.add(h1)
        EventStore.addView(i1)
        EventStore.add(h2)
        EventStore.addView(i2)

        val le1 = EventStore.find(h1)!!
        assertEquals(h1, le1.url)   //1

        val le2 = EventStore.find(h2)!!
        assertEquals(h2, le2.url)   //2

        val le3 = EventStore.findView(i2)
        assertNotNull(le3)                  //3
        val le4 = EventStore.findView(i1)
        assertNotNull(le4)                 //4

        EventStore.close(i1)
        console.log("[EventLogTest.testFindView] $i1")

        assertTrue(le4.isClosedView())
    }

    @Test
    fun testFind() {
        EventStore.reset()
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
        assertNotNull(le1)  //1

        val le2 = EventStore.findExact(ol9)
        assertEquals(null, le2)     //2

        val le3 = EventStore.findEquivalent(ol9)
        assertNotNull(le3)  //3
        assertEquals(ol1, le3.url)  //4

        val le4 = EventStore.find(ol9)
        assertEquals(le3, le4)      //5

        val le5 = EventStore.findEquivalent(olx)
        assertNull(le5)             //6

        val p1 = "http://localhost:8080/restful/objects/simple.SimpleObject/11/properties/name"
        val p2 = "http://localhost:8080/restful/objects/simple.SimpleObject/12/properties/name"
        val p3 = "http://localhost:8080/restful/objects/simple.SimpleObject/13/properties/name"
        EventStore.add(p1)
        EventStore.add(p2)
        EventStore.add(p3)
        val le6 = EventStore.find(p3)
        assertNotNull(le6)          //7
        assertEquals(le6.url, p1)   //8

        val pName = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/name"
        val pNotes = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes"
        EventStore.add(pName)
        EventStore.add(pNotes)
        val le7 = EventStore.find(pNotes)
        assertNotNull(le7)            //9
        assertEquals(le7.url, pNotes) //10
    }
}