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
package org.apache.isis.client.kroviz.layout

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.handler.LayoutHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_LAYOUT_JSON
import kotlin.test.Test
import kotlin.test.assertNotNull

@UnstableDefault
class LayoutTest : IntegrationTest() {

    @Test
    fun testDemoTextLayout() {
        //given
        val jsonStr = SO_LAYOUT_JSON.str
        //when
        val layout = LayoutHandler().parse(jsonStr) as Layout
        //val linkList = layout.propertyList
        // then
        assertNotNull(layout)    //1
    }

}
