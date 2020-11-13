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
package org.apache.isis.client.kroviz.to.bs3

import org.apache.isis.client.kroviz.handler.LayoutXmlHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.DEMO_TAB_LAYOUT_XML
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_LAYOUT_XML
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutXmlTest {

//FIXME    @Test
    fun testDemoTabGrid() {
        //given
        val xmlStr = DEMO_TAB_LAYOUT_XML.str
        //when
        val grid = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        console.log("[LXT.testDemoTabGrid]")
        console.log(grid)
        assertEquals(2, grid.rows.size)    //1
    }

    @Test
    fun testParseXmlLayout() {
        //given
        val xmlStr = SO_LAYOUT_XML.str
        //when
        val grid = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        assertEquals(2, grid.rows.size)    //1

        val row1 = grid.rows.get(0)
        val cols1 = row1.colList
        assertEquals(1, cols1.size)       //2
        val col1 = cols1.first()
        assertEquals("12", col1.span.toString().trim())    //3
        val dom = col1.domainObject
        assertEquals("Simple Object", dom!!.named)        // 4
        assertEquals("Simple Objects", dom.plural)        // 5

        val row2 = grid.rows.get(1)
        val cols2 = row2.colList
        assertEquals(2, cols2.size)                       //6
        val col21 = cols2.first()
        assertEquals("6", col21.span.toString().trim())   //7
        val tabGroup = col21.tabGroupList.first()
        val tabs = tabGroup.tabList
        assertEquals(3, tabs.size)                        // 8
        assertEquals("General", tabs.first().name)        // 9
        assertEquals("Metadata", tabs.get(1).name)        // 10
        assertEquals("Other", tabs.get(2).name)           // 11

        val general = tabs.first()
        val generalRows = general.rowList
        assertEquals(1, generalRows.size)                 // 12

        val theRow = generalRows.first()
        val theCol = theRow.colList.first()

        val fieldSet = theCol.fieldSetList.first()
        assertEquals(1, fieldSet.actionList.size)         // 13
        assertEquals(2, fieldSet.propertyList.size)       // 14

        val col22 = cols2.last()
        assertEquals("6", col22.span.toString().trim())   // 15
    }

}
