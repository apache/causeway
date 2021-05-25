/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.isis.client.kroviz.ui.table

import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.LogEntry
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.handler.*
import org.apache.isis.client.kroviz.snapshots.Response
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.ui.diagram.SequenceDiagram
import kotlin.test.*

class SequenceDiagramTest {

    @BeforeTest
    fun setup() {
        val user = "sven"
        val pw = "pass"
        val url = "http://${user}:${pw}@localhost:8080/restful/"
        UiManager.login(url, user, pw)
    }

    //@Test   //TODO IntegrationTest ?
    fun testSequenceDiagram() {
        //given
        val rootLe = fillEventStoreWith(RESTFUL)
        fillEventStoreWith(RESTFUL_SERVICES)
        fillEventStoreWith(RESTFUL_MENUBARS)
        //when
        val actual = SequenceDiagram.with(rootLe)
        //then
        assertEquals(3, EventStore.log.size)
        console.log("[PBT.testSequenceDiagram]")
        console.log(actual)
        assertTrue(actual.startsWith("\"@startuml"))
        assertTrue(actual.endsWith("@enduml\""))
        assertTrue(actual.contains("-> restful_services"))
    }

    private fun fillEventStoreWith(r: Response): LogEntry {
        val rs = ResourceSpecification(r.url)
        EventStore.start(rs, Method.GET.operation)
        val logEntry = EventStore.end(rs, r.str)!!
        ResponseHandler.handle(logEntry)
        return logEntry
    }

    @Test
    fun testEventDiagram() {
        //given
        load(RESTFUL, RestfulHandler())
        load(RESTFUL_SERVICES, ServiceHandler())
        load(RESTFUL_USER, UserHandler())
        load(RESTFUL_MENUBARS, MenuBarsHandler())
        load(RESTFUL_VERSION, VersionHandler())
        load(RESTFUL_DOMAIN_TYPES, DomainTypesHandler())

        val rootRs = ResourceSpecification(RESTFUL.url)

        // when
        val rootLogEntry = EventStore.findBy(rootRs)
        // then
        assertNotNull(rootLogEntry)  //1

        // when
        val code = SequenceDiagram.with(rootLogEntry)
        // then
        console.log("[PumlBuilderTest.testEventDiagram]")
        console.log(code)
        val wrong = "@startuml\n@enduml"
        assertNotEquals(wrong, code)
    }

    private fun load(response: Response, handler: BaseHandler) {
        val rs = ResourceSpecification(response.url)
        EventStore.start(rs, Method.GET.operation)
        val le = EventStore.end(rs, response.str)!!
        val tObj = handler.parse(response.str)!!
        le.setTransferObject(tObj)
    }

}
