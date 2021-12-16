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
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.core.model.CollectionDM
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.isis.client.kroviz.to.Property
import org.apache.isis.client.kroviz.to.Relation
import org.apache.isis.client.kroviz.ui.core.SessionManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CollectionAggregatorTest : IntegrationTest() {

    //@Test
    // sometimes fails with:
    // Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves.
    fun testFixtureResult() {
        if (isAppAvailable()) {
            // given
            val es = SessionManager.getEventStore()
            es.reset()
            val obs = CollectionAggregator("test")
            // when
            mockResponse(FR_OBJECT, obs)
            mockResponse(FR_OBJECT_LAYOUT, obs)
            mockResponse(FR_OBJECT_PROPERTY, obs)
            val reSpec = ResourceSpecification(FR_OBJECT_PROPERTY.url)
            val pLe = es.findBy(reSpec)!!
            val pdLe = mockResponse(FR_PROPERTY_DESCRIPTION, obs)
            val layoutLe = mockResponse(FR_OBJECT_LAYOUT, obs)

            // then
            val actObs = pLe.getAggregator() as CollectionAggregator
            assertEquals(obs, actObs)  // 1
            assertEquals(pdLe.getAggregator(), layoutLe.getAggregator()) // 2 - trivial?
            // seems they are equal but not identical - changes on obs are not reflected in actObs !!!
            // assertNotNull(obs.dsp.layout)  // 3  // does not work - due to async?

            //then
            val p = pLe.getTransferObject() as Property
            assertEquals("className", p.id)  // 3
            val links = p.links
            val descLink = links.find {
                it.rel == Relation.DESCRIBED_BY.type
            }
            assertNotNull(descLink)  // 4

            // then
            val dl = obs.dpm as CollectionDM
            val propertyLabels = dl.properties.propertyDescriptionList
            val property = pdLe.getTransferObject() as Property
            assertTrue(propertyLabels.size > 0)  // 5
            val lbl = dl.properties.find(property.id)!!.friendlyName
            val expected = "ResultListResult class"
            assertEquals(expected, lbl)  // 6
        }
    }

    //@Test
    // sometimes fails with:
    // Error: Timeout of 2000ms exceeded. For async tests and hooks, ensure "done()" is called; if returning a Promise, ensure it resolves.
    fun testService() {
        if (isAppAvailable()) {
            // given
            SessionManager.getEventStore().reset()
            val obs = CollectionAggregator("test")
            // when
            mockResponse(SO_LIST_ALL, obs)
            mockResponse(SO_0, obs)
            // then
            val ol = obs.dpm
            assertNotNull(ol)
            assertEquals(1, (ol as CollectionDM).data.size)
        }
    }

}
