package org.ro.to.bs3

import org.ro.handler.LayoutXmlHandler
import org.ro.layout.Layout
import org.ro.snapshots.simpleapp1_16_0.SO_LAYOUT_XML
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutXmlTest {

    @Test
    fun testParseXmlLayout() {
        //given
        val xmlStr = SO_LAYOUT_XML.str
        //when
        val grid = LayoutXmlHandler().parse(xmlStr) as Grid
        // then
        assertEquals(2, grid.rows.size)

        val row1 = grid.rows.get(0)
        val cols1 = row1.colList
        assertEquals(1, cols1.size)
        val col1 = cols1.first()
        assertEquals("12", col1.span.toString().trim())
        val dom = col1.domainObject
        assertEquals("Simple Object", dom!!.named)
        assertEquals("Simple Objects", dom.plural)

        val row2 = grid.rows.get(1)
        val cols2 = row2.colList
        assertEquals(2, cols2.size)
        val col21 = cols2.first()
        assertEquals("6", col21.span.toString().trim())
        val tabGroup = col21.tabGroupList.first()
        val tabs = tabGroup.tabList
        assertEquals(3, tabs.size)
        assertEquals("General", tabs.first().name)
        assertEquals("Metadata", tabs.get(1).name)
        assertEquals("Other", tabs.get(2).name)

        val general = tabs.first()
        val generalRows = general.rowList
        assertEquals(1, generalRows.size)

        val theRow = generalRows.first()
        val theCol = theRow.colList.first()

        val fieldSet = theCol.fieldSetList.first()
        assertEquals("Name", fieldSet.name)
        assertEquals("name", fieldSet.id)
        assertEquals(1, fieldSet.actionList.size)
        assertEquals(2, fieldSet.propertyList.size)

        val col22 = cols2.last()
        assertEquals("6", col22.span.toString().trim())
    }

}
