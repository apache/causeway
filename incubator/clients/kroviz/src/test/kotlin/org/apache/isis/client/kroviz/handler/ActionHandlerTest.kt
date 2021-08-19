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

import org.apache.isis.client.kroviz.snapshots.demo2_0_0.ACTIONS_STRINGS
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.COLLECTION_DESCRIPTION
import org.apache.isis.client.kroviz.to.Action
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

class ActionHandlerTest {

    @Test
    fun testParse() {
        val json = ACTIONS_STRINGS.str
        val action = ActionHandler().parse(json)
        assertTrue(action is Action)
    }

    @Test
    fun testParseSadCase() {
        val json = COLLECTION_DESCRIPTION.str
        try {
            ActionHandler().parse(json)
            fail("Exception expected")
        } catch (re: RuntimeException) {
            assertTrue(true)
        }
    }

}
