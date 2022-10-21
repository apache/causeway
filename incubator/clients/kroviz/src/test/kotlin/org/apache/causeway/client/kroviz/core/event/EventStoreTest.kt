/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.causeway.client.kroviz.core.event

import io.kvision.panel.VPanel
import org.apache.causeway.client.kroviz.IntegrationTest
import org.apache.causeway.client.kroviz.core.aggregator.CollectionAggregator
import org.apache.causeway.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.causeway.client.kroviz.to.Method
import org.apache.causeway.client.kroviz.ui.core.Constants
import org.apache.causeway.client.kroviz.utils.XmlHelper
import kotlin.test.*

class EventStoreTest : IntegrationTest() {

    @Test
    fun testFindPlantuml() {
        // given
        val url1 = Constants.krokiUrl + "plantuml"
        val rs1 = ResourceSpecification(url1)
        val body1 = "oans"
        val url2 = Constants.krokiUrl + "plantuml"
        val rs2 = ResourceSpecification(url2)
        val body2 = "zwoa"
        val es = EventStore()

        // when
        es.reset()
        es.start(rs1, Method.POST.operation, body = body1)
        es.start(rs2, Method.POST.operation, body = body2)

        // then
        assertEquals(2, es.log.size) //1
        console.log("[EST.testFindPlantuml]")

        val le1 = es.findBy(rs1, body1)
        assertNotNull(le1)  //2
        console.log("le1: ${le1.toString()}")

        val le2 = es.findBy(rs2, body2)
        assertNotNull(le2)  //3
        console.log("le2: ${le2.toString()}")

        assertFalse(le1 === le2)  //4
    }

    //@Test
    // sometimes fails with:
    // Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves.
    fun testLayout() {
        if (isAppAvailable()) {
            // given
            val es = EventStore()
            es.reset()
            val obs = CollectionAggregator("test")

            //when
            val soList = ResourceSpecification(SO_LIST_ALL.url)
            mockResponse(SO_LIST_ALL, obs)

            val rsJson = ResourceSpecification(SO_LAYOUT_JSON.url)
            mockResponse(SO_LAYOUT_JSON, obs)

            val rsXml = ResourceSpecification(SO_LAYOUT_XML.url, Constants.subTypeXml)
            mockResponse(SO_LAYOUT_XML, obs)

            // then
            val soListLe = es.findBy(soList)!!
            assertEquals(Constants.subTypeJson, soListLe.subType) // 1

            val leJson = es.findBy(rsJson)!!
            assertEquals(Constants.subTypeJson, leJson.subType) // 2

            val leXml = es.findBy(rsXml)!!
            assertEquals(Constants.subTypeXml, leXml.subType) // 3
            assertTrue(XmlHelper.isXml(leXml.response)) // 4

            assertTrue(es.log.size > 3)
        }
    }

    @Test
    fun testSecondEntry() {
        // given
        val es = EventStore()
        es.reset()
        val initialSize: Int = es.log.size
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
        es.start(selfSpec, method, body = myFirst)
        es.start(upSpec, method, body = myFirst)
        es.end(selfSpec, selfStr)
        es.end(upSpec, upStr)
        es.start(selfSpec, method, body = myLast)
        es.start(upSpec, method, body = myLast)
        // then
        val currentSize: Int = es.log.size
        assertEquals(4 + initialSize, currentSize)  //1

        // Entries with the same key can be written, but when updated or retrieved the first (oldest) entry should be used
        //when
        val le2 = es.findBy(selfSpec)!!
        //then
        assertEquals(myFirst, le2.request)  //2
        assertEquals(selfStr.length, le2.response.length)  //3
        //when
        val leU = es.findBy(upSpec)!!
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
        val es = EventStore()

        // construct list with urls
        es.add(h1Spec)
        es.addView(i1, agg, VPanel())
        es.add(h2Spec)
        es.addView(i2, agg, VPanel())

        val le1 = es.findBy(h1Spec)!!
        assertEquals(h1, le1.url)   //1

        val le2 = es.findBy(h2Spec)!!
        assertEquals(h2, le2.url)   //2

        val le3 = es.findView(i2)
        assertNotNull(le3)                  //3
        val le4 = es.findView(i1)
        assertNotNull(le4)                 //4

        es.closeView(i1)
        assertTrue(le4.isClosedView())
    }

    @Test
    fun testFind() {
        val es = EventStore()
        es.reset()
        val ol1 = "http://localhost:8080/restful/objects/simple.SimpleObject/51/object-layout"
        val ol1Spec = ResourceSpecification(ol1)
        val ol2 = "http://localhost:8080/restful/objects/simple.SimpleObject/52/object-layout"
        val ol3 = "http://localhost:8080/restful/objects/simple.SimpleObject/53/object-layout"
        val ol9 = "http://localhost:8080/restful/objects/simple.SimpleObject/59/object-layout"
        val ol9Spec = ResourceSpecification(ol9)
        val olx = "http://localhost:8080/restful/objects/simple.SimpleObject/object-layout"

        // construct list with urls
        es.add(ol1Spec)
        es.add(ResourceSpecification(ol2))
        es.add(ResourceSpecification(ol3))

        val le1 = es.findBy(ol1Spec)
        assertNotNull(le1)  //1

        val le2 = es.findExact(ol9Spec)
        assertEquals(null, le2)     //2

        val le3 = es.findEquivalent(ol9Spec)
        assertNotNull(le3)  //3
        assertEquals(ol1, le3.url)  //4

        val le4 = es.findBy(ol9Spec)
        assertEquals(le3, le4)      //5

        val olxSpec = ResourceSpecification(olx)
        val le5 = es.findEquivalent(olxSpec)
        assertNull(le5)             //6

        val p1 = "http://localhost:8080/restful/objects/simple.SimpleObject/11/properties/name"
        val p1Spec = ResourceSpecification(p1)
        val p2 = "http://localhost:8080/restful/objects/simple.SimpleObject/12/properties/name"
        val p2Spec = ResourceSpecification(p2)
        val p3 = "http://localhost:8080/restful/objects/simple.SimpleObject/13/properties/name"
        val p3Spec = ResourceSpecification(p3)
        es.add(p1Spec)
        es.add(p2Spec)
        es.add(p3Spec)
        val le6 = es.findBy(p3Spec)
        assertNotNull(le6)          //7
        assertEquals(le6.url, p1)   //8

        val pName = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/name"
        val pNameSpec = ResourceSpecification(pName)
        val pNotes = "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes"
        val pNotesSpec = ResourceSpecification(pNotes)
        es.add(pNameSpec)
        es.add(pNotesSpec)
        val le7 = es.findBy(pNotesSpec)
        assertNotNull(le7)            //9
        assertEquals(le7.url, pNotes) //10
    }

    @Test
    fun testFindEquivalent_ConfigurationLayout() {
        val es = EventStore()
        es.reset()
        val ol1 = "http://localhost:8080/restful/objects/causewayApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLnBlcnNpc3Rvci5kYXRhbnVjbGV1cy5pbXBsLmRhdGFudWNsZXVzLmNhY2hlLmxldmVsMi50eXBlPC9rZXk-CiAgICA8dmFsdWU-bm9uZTwvdmFsdWU-CjwvY29uZmlndXJhdGlvblByb3BlcnR5Pgo=/object-layout"
        val ol1Spec = ResourceSpecification(ol1)
        val ol2 = "http://localhost:8080/restful/objects/causewayApplib.ConfigurationProperty/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8Y29uZmlndXJhdGlvblByb3BlcnR5PgogICAgPGtleT5pc2lzLnJlZmxlY3Rvci52YWxpZGF0b3Iuc2VydmljZUFjdGlvbnNPbmx5PC9rZXk-CiAgICA8dmFsdWU-dHJ1ZTwvdmFsdWU-CjwvY29uZmlndXJhdGlvblByb3BlcnR5Pgo=/object-layout"
        val ol2Spec = ResourceSpecification(ol2)

        // construct list with urls
        es.add(ol1Spec)
        es.add(ol2Spec)

        val le1 = es.findBy(ol1Spec)
        assertNotNull(le1)  //1

        val le2 = es.findEquivalent(ol2Spec)
        assertNotNull(le2)  //2
        assertEquals(ol1, le2.url)  //3
    }

    @Test
    fun testAnonymousEntry() {
        // given
        val es = EventStore()
        es.reset()
        val method = Method.POST.operation
        val url = Constants.krokiUrl
        val rs = ResourceSpecification(url)
        // when
        es.start(rs, method, body = "first invocation")
        es.start(rs, method, body = "second invocation")
        es.end(rs, "first response")
        // then
        val currentSize: Int = es.log.size
        assertEquals(2, currentSize)  //1
        val le = es.findBy(rs)!!
        assertEquals("first response", le.response)
    }

    @Test
    fun testFindActionsStrings() {
        //given
        val url1 = "http://localhost:8080/restful/objects/demo.JavaLangTypesMenu/1/actions/strings"
        val rs1 = ResourceSpecification(url1)
        val url2 = "http://localhost:8080/restful/objects/demo.JavaLangTypesMenu/1/actions/strings/invoke"
        val rs2 = ResourceSpecification(url2)

        //when
        val es = EventStore()
        es.reset()
        es.add(rs1)
        es.add(rs2)
        val le1 = es.findBy(rs1)
        val le2 = es.findBy(rs2)

        //then
        assertEquals(2, es.log.size)
        assertNotEquals(le1, le2)

        //when
        val le1eq = es.findEquivalent(rs1)
        val le2eq = es.findEquivalent(rs2)

        //then
        assertNotEquals(le1eq, le2eq)
    }

}
