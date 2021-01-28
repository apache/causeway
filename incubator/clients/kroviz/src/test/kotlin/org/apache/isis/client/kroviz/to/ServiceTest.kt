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

import org.apache.isis.client.kroviz.handler.ServiceHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_MENU
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceTest : ToTest() {

    @Test
    fun testSimpleObjectMenu() {
        val jsonStr = SO_MENU.str
        val service = ServiceHandler().parse(jsonStr) as Service
        assertEquals("Simple Objects", service.title)
        val actions: List<Member> = service.getMemberList()
        assertEquals(3, actions.size)

        assertTrue(service.containsMemberWith("listAll"))
        assertTrue(service.containsMemberWith("findByName"))
        assertTrue(service.containsMemberWith("create"))

        // jsonObj contains '"members": {}' not '"members": []'
        // this results in an unordered list (Map),
        // but intended is an ordered list (Array[])
        //TODO use object-layout / menu layout instead
    }

    fun Service.containsMemberWith(id: String): Boolean {
        for (m in getMemberList()) {
            if (m.id == id) {
                return true
            }
        }
        return false
    }

}
