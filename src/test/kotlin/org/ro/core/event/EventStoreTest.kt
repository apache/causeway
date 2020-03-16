package org.ro.core.event

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.core.aggregator.ListAggregator
import org.ro.core.aggregator.ObjectAggregator
import org.ro.snapshots.simpleapp1_16_0.*
import org.ro.to.Method
import org.ro.utils.XmlHelper
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

            //when
            val soList = ResourceSpecification(SO_LIST_ALL.url)
            mockResponse(SO_LIST_ALL, obs)

            val rsJson = ResourceSpecification(SO_LAYOUT_JSON.url)
            mockResponse(SO_LAYOUT_JSON, obs)

            val rsXml = ResourceSpecification(SO_LAYOUT_XML.url, "xml")
            mockResponse(SO_LAYOUT_XML, obs)

            // then
            val soListLe= EventStore.find(soList)!!
            assertEquals("json", soListLe.subType) // 1

            val leJson = EventStore.find(rsJson)!!
            assertEquals("json", leJson.subType) // 2

            val leXml = EventStore.find(rsXml)!!
            assertEquals("xml", leXml.subType) // 3
            assertTrue(XmlHelper.isXml(leXml.response)) // 4

            assertTrue(EventStore.log.size > 3)
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
        EventStore.start(selfSpec, method, body = myFirst)
        EventStore.start(upSpec, method, body = myFirst)
        EventStore.end(selfSpec, selfStr)
        EventStore.end(upSpec, upStr)
        EventStore.start(selfSpec, method, body = myLast)
        EventStore.start(upSpec, method, body = myLast)
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
        val h1Spec = ResourceSpecification(h1)
        val h2 = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"
        val h2Spec = ResourceSpecification(h2)
        val i1 = "Test (1)"
        val i2 = "Test (2)"
        val agg = ObjectAggregator("testFindView")

        // construct list with urls
        EventStore.add(h1Spec)
        EventStore.addView(i1, agg, VPanel())
        EventStore.add(h2Spec)
        EventStore.addView(i2, agg, VPanel())

        val le1 = EventStore.find(h1Spec)!!
        assertEquals(h1, le1.url)   //1

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
        val ol1Spec = ResourceSpecification(ol1)
        val ol2 = "http://localhost:8080/restful/objects/simple.SimpleObject/52/object-layout"
        val ol3 = "http://localhost:8080/restful/objects/simple.SimpleObject/53/object-layout"
        val ol9 = "http://localhost:8080/restful/objects/simple.SimpleObject/59/object-layout"
        val ol9Spec = ResourceSpecification(ol9)
        val olx = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"

        // construct list with urls
        EventStore.add(ol1Spec)
        EventStore.add(ResourceSpecification(ol2))
        EventStore.add(ResourceSpecification(ol3))

        val le1 = EventStore.find(ol1Spec)
        assertNotNull(le1)  //1

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
        val p1Spec = ResourceSpecification(p1)
        val p2 = "http://localhost:8080/restful/objects/simple.SimpleObject/12/properties/name"
        val p2Spec = ResourceSpecification(p2)
        val p3 = "http://localhost:8080/restful/objects/simple.SimpleObject/13/properties/name"
        val p3Spec = ResourceSpecification(p3)
        EventStore.add(p1Spec)
        EventStore.add(p2Spec)
        EventStore.add(p3Spec)
        val le6 = EventStore.find(p3Spec)
        assertNotNull(le6)          //7
        assertEquals(le6.url, p1)   //8

        val pName = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/name"
        val pNameSpec = ResourceSpecification(pName)
        val pNotes = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes"
        val pNotesSpec = ResourceSpecification(pNotes)
        EventStore.add(pNameSpec)
        EventStore.add(pNotesSpec)
        val le7 = EventStore.find(pNotesSpec)
        assertNotNull(le7)            //9
        assertEquals(le7.url, pNotes) //10
    }

    @Test
    fun testFindEquivalent_ConfigurationLayout() {
        EventStore.reset()
        val ol1 = "http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLnBlcnNpc3Rvci5kYXRhbnVjbGV1cy5pbXBsLmRhdGFudWNsZXVzLmNhY2hlLmxldmVsMi50eXBlPC9rZXk-CiAgICA8dmFsdWU-bm9uZTwvdmFsdWU-CjwvY29uZmlndXJhdGlvblByb3BlcnR5Pgo=/object-layout"
        val ol1Spec = ResourceSpecification(ol1)
        val ol2 = "http://localhost:8080/restful/objects/isisApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLnJlZmxlY3Rvci52YWxpZGF0b3Iuc2VydmljZUFjdGlvbnNPbmx5PC9rZXk-CiAgICA8dmFsdWU-dHJ1ZTwvdmFsdWU-CjwvY29uZmlndXJhdGlvblByb3BlcnR5Pgo=/object-layout"
        val ol2Spec = ResourceSpecification(ol2)

        // construct list with urls
        EventStore.add(ol1Spec)
        EventStore.add(ol2Spec)

        val le1 = EventStore.find(ol1Spec)
        assertNotNull(le1)  //1

        val le2 = EventStore.findEquivalent(ol2Spec)
        assertNotNull(le2)  //2
        assertEquals(ol1, le2.url)  //3
    }
}
