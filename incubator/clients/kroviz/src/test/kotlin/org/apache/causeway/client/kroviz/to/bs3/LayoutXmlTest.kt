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

class LayoutXmlTest {

    @Test
    fun testDemoStringGrid() {
        //given
        val xmlStr = STRINGS_LAYOUT_XML.str
        //when
        val grid = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        assertEquals(3, grid.rows.size, message = "grid.rows.size")    //1

        val primaryRow = grid.rows[1]
        assertEquals(2, primaryRow.colList.size, message = "primaryRow.colList.size")    //2

        val primaryCol = primaryRow.colList[0]
        assertEquals(1, primaryCol.rowList.size, message = "primaryCol.rowList.size")    //3

        val secondaryRow = primaryCol.rowList[0]
        assertEquals(2, secondaryRow.colList.size, message = "secondaryRow.colList.size") //4

        val secondaryCol = secondaryRow.colList[0]
        assertEquals("12", secondaryCol.span.toString(), message = "secondaryCol.span") //5

        val collectionList = secondaryCol.collectionList
        assertEquals(1, collectionList.size, message = "collectionList.size") //6

        val collection = collectionList[0]
        assertEquals("entities", collection.id, message = "collection.id")
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
