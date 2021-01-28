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

package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.core.aggregator.ObjectAggregator
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.ACTIONS_STRINGS_INVOKE
import kotlin.test.assertTrue

class ResponseHandlerTest : IntegrationTest() {

    //@Test
    fun findHandlerTest() {
        if (isAppAvailable()) {
            //given
            val logEntry = mockResponse(ACTIONS_STRINGS_INVOKE, null)
            //when
            ResponseHandler.handle(logEntry)
            wait(1000)
            val actual = logEntry.getAggregator()
            //then
            assertTrue(actual is ObjectAggregator)
        }
    }
}
