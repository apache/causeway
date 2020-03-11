package org.ro.core.event

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.core.aggregator.ListAggregator
import org.ro.core.aggregator.ObjectAggregator
import org.ro.snapshots.simpleapp1_16_0.RESTFUL
import org.ro.snapshots.simpleapp1_16_0.RESTFUL_SERVICES
import org.ro.snapshots.simpleapp1_16_0.SO_LAYOUT_JSON
import org.ro.snapshots.simpleapp1_16_0.SO_LAYOUT_XML
import org.ro.to.Method
import pl.treksoft.kvision.panel.VPanel
import kotlin.test.*

@UnstableDefault
class EventStoreTest : IntegrationTest() {

    @Test
    fun testLayout() {
        if (isAppAvailable()) {
            // given
            EventStore.reset()
            val obs = ListAggregator("test")
            //val rsJson = ResourceSpecification(SO_LAYOUT_JSON.url)
            val leJson = mockResponse(SO_LAYOUT_JSON, obs)
            //val rsXml = ResourceSpecification(SO_LAYOUT_XML.url)
            val leXml = mockResponse(SO_LAYOUT_XML, obs)
            assertEquals(2, EventStore.log.size)
            console.log("[EST.testLayout]")
            console.log(EventStore.log)
        }
    }

    @Test
    fun testSecondEntry() {
        // given
        EventStore.reset()
        val initialSize: Int = EventStore.log.size
        val myFirst = "1"
        val myLast = "n"
        val method = Method.GET.operation

        val selfStr = RESTFUL_SERVICES.str
        val selfUrl = "http://localhost:8080/restful/services"
        val upStr = RESTFUL.str
        val upUrl = "http://localhost:8080/restful/"

        val selfSpec = ResourceSpecification(selfUrl)
        val upSpec = ResourceSpecification(upUrl)
        // when
        EventStore.start(selfSpec, method, myFirst)
        EventStore.start(upSpec, method, myFirst)
        EventStore.end(selfSpec, selfStr)
        EventStore.end(upSpec, upStr)
        EventStore.start(selfSpec, method, myLast)
        EventStore.start(upSpec, method, myLast)
        // then
        val currentSize: Int = EventStore.log.size
        assertEquals(4 + initialSize, currentSize)  //1

        // Entries with the same key can be written, but when updated or retrieved the first (oldest) entry should be used
        //when
        val le2 = EventStore.find(selfSpec)!!
        //then
        assertEquals(myFirst, le2.request)  //2
        assertEquals(selfStr.length, le2.response.length)  //3
        //when
        val leU = EventStore.find(upSpec)!!
        //then
        assertEquals(myFirst, leU.request)  //4
        assertEquals(upStr.length, leU.response.length)  //5
    }

    @Test
    fun testFindView() {
        val h1 = "http://localhost:8080/restful/objects/simple.SimpleObject/51/object-layout"
        val h2 = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"
        val i1 = "Test (1)"
        val i2 = "Test (2)"
        val agg = ObjectAggregator("testFindView")

        // construct list with urls
        EventStore.add(h1)
        EventStore.addView(i1, agg, VPanel())
        EventStore.add(h2)
        EventStore.addView(i2, agg, VPanel())

        val h1Spec = ResourceSpecification(h1)
        val le1 = EventStore.find(h1Spec)!!
        assertEquals(h1, le1.url)   //1

        val h2Spec = ResourceSpecification(h2)
        val le2 = EventStore.find(h2Spec)!!
        assertEquals(h2, le2.url)   //2

        val le3 = EventStore.findView(i2)
        assertNotNull(le3)                  //3
        val le4 = EventStore.findView(i1)
        assertNotNull(le4)                 //4

        EventStore.closeView(i1)
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

        val ol1Spec = ResourceSpecification(ol1)
        val le1 = EventStore.find(ol1Spec)
        assertNotNull(le1)  //1

        val ol9Spec = ResourceSpecification(ol9)
        val le2 = EventStore.findExact(ol9Spec)
        assertEquals(null, le2)     //2

        val le3 = EventStore.findEquivalent(ol9Spec)
        assertNotNull(le3)  //3
        assertEquals(ol1, le3.url)  //4

        val le4 = EventStore.find(ol9Spec)
        assertEquals(le3, le4)      //5

        val olxSpec = ResourceSpecification(olx)
        val le5 = EventStore.findEquivalent(olxSpec)
        assertNull(le5)             //6

        val p1 = "http://localhost:8080/restful/objects/simple.SimpleObject/11/properties/name"
        val p2 = "http://localhost:8080/restful/objects/simple.SimpleObject/12/properties/name"
        val p3 = "http://localhost:8080/restful/objects/simple.SimpleObject/13/properties/name"
        EventStore.add(p1)
        EventStore.add(p2)
        EventStore.add(p3)
        val p3Spec = ResourceSpecification(p3)
        val le6 = EventStore.find(p3Spec)
        assertNotNull(le6)          //7
        assertEquals(le6.url, p1)   //8

        val pName = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/name"
        val pNotes = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes"
        EventStore.add(pName)
        EventStore.add(pNotes)
        val pNotesSpec = ResourceSpecification(pNotes)
        val le7 = EventStore.find(pNotesSpec)
        assertNotNull(le7)            //9
        assertEquals(le7.url, pNotes) //10
    }

    @Test
    fun testFindEquivalent_ConfigurationLayout() {
        EventStore.reset()
        val ol1 = "http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLnBlcnNpc3Rvci5kYXRhbnVjbGV1cy5pbXBsLmRhdGFudWNsZXVzLmNhY2hlLmxldmVsMi50eXBlPC9rZXk-CiAgICA8dmFsdWU-bm9uZTwvdmFsdWU-CjwvY29uZmlndXJhdGlvblByb3BlcnR5Pgo=/object-layout"
        val ol2 = "http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLnJlZmxlY3Rvci52YWxpZGF0b3Iuc2VydmljZUFjdGlvbnNPbmx5PC9rZXk-CiAgICA8dmFsdWU-dHJ1ZTwvdmFsdWU-CjwvY29uZmlndXJhdGlvblByb3BlcnR5Pgo=/object-layout"

        // construct list with urls
        EventStore.add(ol1)
        EventStore.add(ol2)

        val ol1Spec = ResourceSpecification(ol1)
        val le1 = EventStore.find(ol1Spec)
        assertNotNull(le1)  //1

        val ol2Spec = ResourceSpecification(ol2)
        val le2 = EventStore.findEquivalent(ol2Spec)
        assertNotNull(le2)  //2
        assertEquals(ol1, le2.url)  //3
    }
}
