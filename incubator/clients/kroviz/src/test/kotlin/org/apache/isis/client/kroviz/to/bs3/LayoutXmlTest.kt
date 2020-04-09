package org.apache.isis.client.kroviz.to.bs3

import org.apache.isis.client.kroviz.handler.LayoutXmlHandler
import org.apache.isis.client.kroviz.snapshots.demo2_0_0.DEMO_TAB_LAYOUT_XML
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_LAYOUT_XML
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutXmlTest {

    @Test
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
