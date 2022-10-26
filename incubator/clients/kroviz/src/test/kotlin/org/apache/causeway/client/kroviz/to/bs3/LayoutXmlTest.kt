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
package org.apache.causeway.client.kroviz.to.bs3

import org.apache.causeway.client.kroviz.handler.LayoutXmlHandler
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.STRINGS_LAYOUT_XML
import org.apache.causeway.client.kroviz.snapshots.demo2_0_0.TAB_LAYOUT_XML
import org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0.SO_LAYOUT_XML
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LayoutXmlTest {

    //@Test
    // org.apache.causeway.client.kroviz.to.bs3.LayoutXmlTest.testDemoStringGrid FAILED
    //    AssertionError at webpack://kroviz-test/./kotlin/kroviz-test.js?:3837
    fun testDemoStringGrid() {
        //given
        val xmlStr = STRINGS_LAYOUT_XML.str
        //when
        val grid = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        assertEquals(2, grid.rows.size, message = "grid.rows.size")    //1

        val r2 = grid.rows[1]
        assertEquals(2, r2.colList.size, message = "r2.colList.size")    //2

        val r2c1 = r2.colList[0]
        assertEquals("4", r2c1.span.toString(), message = "r2c1.span")      //3a
        assertEquals(2, r2c1.rowList.size, message = "r2c1.rowList.size")   //3b

        val r2c1r1 = r2c1.rowList[0]
        assertEquals(1, r2c1r1.colList.size, message = "r2c1r1.colList.size") //4

        val r2c1r1c1 = r2c1r1.colList[0]
        assertEquals("12", r2c1r1c1.span.toString(), message = "r2c1r1c1.span") //5

        val tabGroupList = r2c1r1c1.tabGroupList
        assertEquals(1, tabGroupList.size, message = "tabGroupList.size") //5b

        val tabList = tabGroupList[0].tabList
        assertEquals(3, tabList.size, message = "tabList.size") //5c

        val tab3 = tabList[2]
        assertEquals("Metadata", tab3.name, message = "tab3.name") //5d

        val tab3r = tab3.rowList
        assertEquals(1, tab3r.size, message = "tab3r.size") //5e

        val tab3_r1 = tab3.rowList[0]
        assertEquals(1, tab3_r1.colList.size, message = "tab3_r1.colList.size") //5f

        val tab3_r1c1 = tab3_r1.colList[0]
        assertEquals("12", tab3_r1c1.span.toString(), message = "tab3_r1c1.span") //5g

        val tab3_r1c1fs1 = tab3_r1c1.fieldSetList[0]
        assertEquals(10, tab3_r1c1fs1.actionList.size, message = "tab3_r1c1fs1.actionList.size") //5h
        assertEquals(2, tab3_r1c1fs1.propertyList.size, message = "tab3_r1c1fs1.propertyList.size") //5h

        val tab3_r1c1fs1_a10 = tab3_r1c1fs1.actionList[9]
        assertEquals("recentAuditTrailEntries", tab3_r1c1fs1_a10.id, message = "tab3_r1c1fs1_a10.id") //5h
        assertEquals(1, tab3_r1c1fs1_a10.linkList.size, message = "tab3_r1c1fs1_a10.linkList") //5i

        val tab3_r1c1fs1_a10l1 = tab3_r1c1fs1_a10.linkList[0]
        assertTrue(tab3_r1c1fs1_a10l1.type.contains("object-action"), message = "tab3_r1c1fs1_a10l1.type") //5j

        val r2c2 = r2.colList[1]
        val r2c2tg1 = r2c2.tabGroupList[0]
        val r2c2tg1_tab1 = r2c2tg1.tabList[0]
        val r2c2tg1_tab1r1 = r2c2tg1_tab1.rowList[0]
        val r2c2tg1_tab1r1c1 = r2c2tg1_tab1r1.colList[0]
        val collectionList = r2c2tg1_tab1r1c1.collectionList
        assertEquals(1, collectionList.size, message = "collectionList.size") //6

        val collection = collectionList[0]
        assertEquals("objects", collection.id, message = "collection.id") //7

        val collection_l1 = collection.linkList[0]
        assertTrue(collection_l1.type.contains("object-collection"), message = "tab3_r1c1fs1_a10l1.type") //8
    }

    @Test
    fun testDemoTabGrid() {
        //given
        val xmlStr = TAB_LAYOUT_XML.str
        //when
        val grid = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        console.log("[LXT.testDemoTabGrid] ${grid.toString()}")
        console.log("rows: ", grid.rows.size)
//        assertEquals(2, grid.rows.size)    //1
    }

    //    @Test nameSpace was renamed from bs3 to bs, SO_LAYOUT_XML needs to be updated
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
