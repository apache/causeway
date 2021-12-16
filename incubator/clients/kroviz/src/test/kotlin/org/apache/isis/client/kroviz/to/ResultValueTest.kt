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
package org.apache.isis.client.kroviz.to

import org.apache.isis.client.kroviz.IntegrationTest

import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.handler.ResultValueHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.ACTIONS_DOWNLOAD_VALUE
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.ACTIONS_OPEN_SWAGGER_UI
import org.apache.isis.client.kroviz.ui.core.SessionManager
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultValueTest : IntegrationTest() {

    @Test
    fun testParseActionOpenSwaggerUI() {
        val jsonStr = ACTIONS_OPEN_SWAGGER_UI.str
        val ir = ResultValueHandler().parse(jsonStr) as ResultValue
        val links = ir.links
        assertEquals(1, links.size)

        val result = ir.result!!
        val value = result.value!!.content as String
        assertEquals("http:/swagger-ui/index.html", value)
    }

    //@Test -> Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves.
    fun testDownload() {
        if (isAppAvailable()) {
            // given
            val aggregator = ObjectAggregator("object test")
            // when
            SessionManager.getEventStore().reset()
            val logEntry = mockResponse(ACTIONS_DOWNLOAD_VALUE, aggregator)
            val ro = logEntry.getTransferObject() as ResultValue
            val type = ro.resulttype
            // then
            assertEquals(ResultType.SCALARVALUE.type, type)
        }
    }

}
