package org.ro.layout

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.handler.LayoutHandler
import org.ro.snapshots.demo2_0_0.DEMO_OBJECT_LAYOUT
import org.ro.snapshots.demo2_0_0.DEMO_TAB_OBJECT_LAYOUT
import org.ro.snapshots.demo2_0_0.DEMO_TEXT_LAYOUT
import org.ro.snapshots.demo2_0_0.DEMO_TOOLTIP_OBJECT_LAYOUT
import org.ro.snapshots.simpleapp1_16_0.FR_OBJECT_LAYOUT
import org.ro.snapshots.simpleapp1_16_0.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class LayoutTest : IntegrationTest() {

    @Test
    fun testDemoTextLayout() {
        if (isAppAvailable()) {
            //given
            val jsonStr = DEMO_TEXT_LAYOUT.str
            //when
            val lo = LayoutHandler().parse(jsonStr) as Layout
            val rows = lo.row
            // then
            assertEquals(2, rows.size)    //1
            val rl1 = rows[0]
            val csl1List = rl1.cols
            assertEquals(1, csl1List.size) //2

            // row1(0) ^= DomainObject
            val cslDomainObject = csl1List[0]
            val clDomainObject = cslDomainObject.col
            assertNotNull(clDomainObject)  // 3
            assertNotNull(clDomainObject.domainObject) //4
            assertEquals(5, clDomainObject.action.size) //5
            assertEquals(12, clDomainObject.span)  //6

            // row2(1) ^= Body (Editable, ReadOnly, Description)
            val rl2 = rows[1]
            val csl2List = rl2.cols
            assertEquals(1, csl1List.size) //7
            val cslBody = csl2List[0]
            val clBody = cslBody.col
            assertNotNull(clBody)  //8
            assertEquals(6, clBody.span as Int)  //9
            val fslList = clBody.fieldSet
            assertEquals(2, fslList.size) //10
            // Editable
            val fslEditable = fslList[0]
            assertEquals("Editable", fslEditable.name)
            val editableProperties = fslEditable.property
            assertEquals(2, editableProperties.size) //11
            // Readonly
            val fslReadonly = fslList[1]
            assertEquals("Readonly", fslReadonly.name) //12
            val readonlyProperties = fslReadonly.property
            assertEquals(2, readonlyProperties.size) //13

            // Description
            val descCslBody = csl2List[1]
            val descClBody = descCslBody.col
            assertNotNull(descClBody)  //14
            assertEquals(6, descClBody.span as Int)  //15
            val descFslList = descClBody.fieldSet
            assertEquals(1, descFslList.size) //16
            val fslDescription = descFslList[0]
            assertEquals("Description", fslDescription.name) //17
            val descriptionProperties = fslDescription.property
            assertEquals(1, descriptionProperties.size) //18
        }
    }

    @Test
    fun testDemoToolTipObjectLayout() {
        //given
        val jsonStr = DEMO_TOOLTIP_OBJECT_LAYOUT.str
        //when
        val lo = LayoutHandler().parse(jsonStr) as Layout
        val fieldSet = lo.properties
        // then
        assertEquals(1, fieldSet.size)    //1
        assertEquals("text1", fieldSet[0].id)  //2
    }

    @Test
    fun testDemoTabObjectLayout() {
        //given
        val jsonStr = DEMO_TAB_OBJECT_LAYOUT.str
        //when
        val lo = LayoutHandler().parse(jsonStr) as Layout
        val fieldSet = lo.properties
        // then
        assertEquals(1, fieldSet.size)    //1
        assertEquals("field1", fieldSet[0].id)  //2
    }

    @Test
    fun testDemoObjectLayout() {
        //given
        val jsonStr = DEMO_OBJECT_LAYOUT.str
        //when
        val lo = LayoutHandler().parse(jsonStr) as Layout
        val fieldSet = lo.properties
        // then
        assertEquals(2, fieldSet.size)    //1
        assertEquals("string", fieldSet[0].id)  //2
        assertEquals("stringMultiline", fieldSet[1].id)  //3
        assertEquals(2, lo.row.size) //4
    }

    @Test
    fun testParseSimpleObjectLayout() {
        //given
        val jsonStr = SO_OBJECT_LAYOUT.str
        //when
        val lo = LayoutHandler().parse(jsonStr) as Layout
        val properties = lo.properties
        // then
        assertEquals(2, properties.size)    //1
        assertEquals("name", properties[0].id)  //2
        assertEquals("notes", properties[1].id)  //3
        assertEquals(2, lo.row.size) //4
    }

    @Test
    fun testParseFixtureScriptObjectLayout() {
        // given
        val jsonStr = FR_OBJECT_LAYOUT.str
        val lo = LayoutHandler().parse(jsonStr) as Layout
        // when
        val properties = lo.properties
        // then
        assertNotNull(properties)  // (1)
        assertEquals(4, properties.size)
        assertEquals("className", properties[0].id)
        assertEquals("fixtureScriptClassName", properties[1].id)
        assertEquals("key", properties[2].id)
        assertEquals("object", properties[3].id)
    }

}
