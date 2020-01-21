package org.ro.layout

import org.ro.snapshots.simpleapp1_16_0.SO_LAYOUT_XML
import org.ro.to.bs3.XmlHelper
import org.ro.to.bs3.Grid
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LayoutXmlTest {

    @Test
    fun testParseXmlLayout() {
        console.log("[LayoutXmlTest.testParseXmlLayout]")
        //given
        val xmlStr = SO_LAYOUT_XML.str
        //when
        val doc = XmlHelper().parseXml(xmlStr)

        assertNotNull(doc)

        val grid = Grid(doc)
        assertEquals(2, grid.rows.size)

        val row1 = grid.rows.get(0)
        val cols1 = row1.cols
        assertEquals(1, cols1.size)
        val col1 = cols1.first()
        assertEquals("12", col1.span.toString().trim())
        val dom = col1.domainObject
        assertEquals("Simple Object", dom!!.named)
        assertEquals("Simple Objects", dom.plural)

        val row2 = grid.rows.get(1)
        val cols2 = row2.cols
        assertEquals(2, cols2.size)
        val col21 = cols2.first()
        assertEquals("6", col21.span.toString().trim())
        val tabGroup = col21.tabGroups.first()
        val tabs = tabGroup.tabs
        assertEquals(3, tabs.size)
        assertEquals("General", tabs.first().name)
        assertEquals("Metadata", tabs.get(1).name)
        assertEquals("Other", tabs.get(2).name)

        val general = tabs.first()
        val generalRows = general.rows
        assertEquals(1, generalRows.size)

        val theRow = generalRows.first()
        val theCol = theRow.cols.first()

        val fieldSet = theCol.fieldSet!!
        assertEquals("Name", fieldSet.name)
        assertEquals("name", fieldSet.id)
        assertEquals(1, fieldSet.actions.size)
        assertEquals(2, fieldSet.properties.size)

        val col22 = cols2.last()
        assertEquals("6", col22.span.toString().trim())
    }

}
