package org.ro.layout

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import org.ro.IntegrationTest
import org.ro.snapshots.demo2_0_0.*
import org.ro.snapshots.simpleapp1_16_0.FR_OBJECT_LAYOUT
import org.ro.snapshots.simpleapp1_16_0.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class LayoutTest : IntegrationTest() {

//    @Test
    fun testDemoTextLayout() {
        //given
        val jsonStr = DEMO_TEXT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val rows = lo.row
        // then
        assertEquals(2, rows.size)    //1
        val row0 = rows[0]
        val cols0List = row0.cols
        assertEquals(1, cols0List.size) //2

        // row0 ^= DomainObject
        val cols00 = cols0List[0]
        val col00 = cols00.getCol()
        assertNotNull(col00)  // 3
        val do00 = col00.domainObject
        assertNotNull(do00) //4
        assertEquals(5, col00.action.size) //5
        assertEquals(12, col00.span)  //6

        // row1 ^= Body (Editable, ReadOnly, Description)
        val row1 = rows[1]
        val cols1List = row1.cols
        assertEquals(2, cols1List.size) //7
        val cslBody = cols1List[0]
        val clBody = cslBody.getCol()
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
        val descCslBody = cols1List[1]
        val descClBody = descCslBody.getCol()
        assertNotNull(descClBody)  //14
        assertEquals(6, descClBody.span as Int)  //15
        val descFslList = descClBody.fieldSet
        assertEquals(1, descFslList.size) //16
        val fslDescription = descFslList[0]
        assertEquals("Description", fslDescription.name) //17
        val descriptionProperties = fslDescription.property
        assertEquals(1, descriptionProperties.size) //18
    }

//    @Test
    fun testDemoTupleObjectLayout() {
        //given
        val jsonStr = DEMO_TUPLE_OBJECT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val rowLayoutList = lo.row
        // then
        assertEquals(2, rowLayoutList.size)    //1
    }

//    @Test
    fun testDemoToolTipObjectLayout() {
        //given
        val jsonStr = DEMO_TOOLTIP_OBJECT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val fieldSet = lo.properties
        // then
        assertEquals(1, fieldSet.size)    //1
        assertEquals("text1", fieldSet[0].id)  //2
    }

//    @Test
    fun testDemoAssociatedActionObjectLayout() {
        //given
        val jsonStr = DEMO_ASSOCIATED_ACTION_OBJECT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val row0 = lo.row[0]
        val cols00 = row0.cols[0]
        val col000 = cols00.getCol()
        val action = col000.action
        // then
        assertEquals(5, action.size)    //1
        //assertEquals("field1", fieldSet[0].id)  //2
    }

//    @Test
    fun testDemoTabObjectLayout() {
        //given
        val jsonStr = DEMO_TAB_OBJECT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val fieldSet = lo.properties
        // then
        assertEquals(1, fieldSet.size)    //1
        assertEquals("field1", fieldSet[0].id)  //2
    }

//    @Test
    fun testDemoObjectLayout() {
        //given
        val jsonStr = DEMO_OBJECT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val fieldSet = lo.properties
        // then
        assertEquals(2, fieldSet.size)    //1
        assertEquals("string", fieldSet[0].id)  //2
        assertEquals("stringMultiline", fieldSet[1].id)  //3
        assertEquals(2, lo.row.size) //4
    }

//    @Test
    fun testParseSimpleObjectLayout() {
        //given
        val jsonStr = SO_OBJECT_LAYOUT.str
        //when
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val properties = lo.properties
        // then
        assertEquals(2, properties.size)    //1
        assertEquals("name", properties[0].id)  //2
        assertEquals("notes", properties[1].id)  //3
        assertEquals(2, lo.row.size) //4
    }

//    @Test
    fun testParseFixtureScriptObjectLayout() {
        // given
        val jsonStr = FR_OBJECT_LAYOUT.str
        val lo = Json.parse(Layout.serializer(), jsonStr)
        val cols = lo.row[1].cols.first()
        assertEquals(2, cols.colList.size)  // (1)   //TODO is 1 expected or 2???
        // when
        val properties = lo.properties
        // then
        assertNotNull(properties)  // (2)
        assertEquals(4, properties.size)
        assertEquals("className", properties[0].id)
        assertEquals("fixtureScriptClassName", properties[1].id)
        assertEquals("key", properties[2].id)
        assertEquals("object", properties[3].id)
    }

}
