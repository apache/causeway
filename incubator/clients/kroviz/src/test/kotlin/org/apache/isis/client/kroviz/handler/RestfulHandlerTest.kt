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
package org.apache.isis.client.kroviz.handler

import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.RESTFUL
import org.apache.isis.client.kroviz.to.Restful
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RestfulHandlerTest {

    @Test
    fun testParse() {
        val jsonStr = RESTFUL.str
        val ro = RestfulHandler().parse(jsonStr) as Restful
        assertNotNull(ro)

        assertEquals(6, ro.links.size)
        assertEquals("", ro.extensions.oid)
    }

}
