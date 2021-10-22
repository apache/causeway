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

import org.apache.isis.client.kroviz.core.aggregator.SystemAggregator
import org.apache.isis.client.kroviz.core.event.EventStore
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.handler.*
import org.apache.isis.client.kroviz.snapshots.Response
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.isis.client.kroviz.to.Method
import org.apache.isis.client.kroviz.ui.diagram.LinkTreeDiagram
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LinkTreeDiagramTest {

    //@Test         //TODO rework test to use AggregatorWithLayout
    fun testLinkTreeDiagram() {
        // given
        val es = EventStore()
        //when
        val aggregator = SystemAggregator()
        load(RESTFUL, "", RestfulHandler(), aggregator, es)
        val referer = RESTFUL.url
        load(RESTFUL_SERVICES, referer, ServiceHandler(), aggregator, es)
        load(RESTFUL_USER, referer, UserHandler(), aggregator, es)
        load(RESTFUL_MENUBARS, referer, MenuBarsHandler(), aggregator, es)
        load(RESTFUL_VERSION, referer, VersionHandler(), aggregator, es)
        load(RESTFUL_DOMAIN_TYPES, referer, DomainTypesHandler(), aggregator, es)
        // then
        assertTrue(es.log.size >= 6)
        val rootRs = ResourceSpecification(RESTFUL.url)
        val rootLogEntry = es.findBy(rootRs)
        assertNotNull(rootLogEntry)  //1

        // when
        val code = LinkTreeDiagram.build(aggregator).trim()
        // then
        console.log("[LTDT.testLinkTreeDiagram] ${code}")
        assertTrue(code.startsWith("@startmindmap"))
        assertTrue(code.endsWith("@endmindmap"))
        assertTrue(code.contains("http://localhost:8080/restful/"))
        assertTrue(code.contains("http://localhost:8080/restful/version"))
        assertTrue(code.contains("http://localhost:8080/restful/menuBars"))
    }

    private fun load(response: Response, referer: String, handler: BaseHandler, aggregator: SystemAggregator, es: EventStore) {
        val rs = ResourceSpecification(response.url, referrerUrl = referer)
        es.start(rs, Method.GET.operation, aggregator = aggregator)
        val le = es.end(rs, response.str)!!
        val tObj = handler.parse(response.str)!!
        le.setTransferObject(tObj)
    }

}
