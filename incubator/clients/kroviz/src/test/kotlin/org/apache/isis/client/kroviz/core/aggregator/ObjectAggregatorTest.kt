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
package org.apache.isis.client.kroviz.core.aggregator

import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.ACTION_SO_CREATE
import org.apache.isis.client.kroviz.to.ResultObject
import org.apache.isis.client.kroviz.to.ResultType
import kotlin.test.Test
import kotlin.test.assertEquals

class ObjectAggregatorTest : IntegrationTest() {

    @Test
    fun testRestfulServices() {
        // given
        val aggregator = ObjectAggregator("object test")
        // when
        val logEntry = mockResponse(ACTION_SO_CREATE, aggregator)
        val ro = logEntry.getTransferObject() as ResultObject
        val type = ro.resulttype
        // then
        assertEquals(ResultType.DOMAINOBJECT.type, type)

        val links = ro.links
        assertEquals(0, links.size)

        val ror = ro.result!!

        val resLinks = ror.links
        assertEquals(4, resLinks.size)

        val title = ror.title
        assertEquals("Object: Beutlin", title)
    }
}
