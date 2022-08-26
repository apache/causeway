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

import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.handler.LayoutXmlHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.STRINGS_LAYOUT_XML_BS3
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.STRINGS_LAYOUT_XML_BS
import org.apache.isis.client.kroviz.to.bs3.Grid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LayoutXmlTest : IntegrationTest() {

    @Test
    fun testStringsLayout_BS() {
        //given
        val xmlStr = STRINGS_LAYOUT_XML_BS.str
        //when
        val layout = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        assertNotNull(layout)    //
        assertEquals(2, layout.rows.size)

        val r1 = layout.rows[0]
        assertEquals(1, r1.colList.size)

        val r2 = layout.rows[1]
    }

}
