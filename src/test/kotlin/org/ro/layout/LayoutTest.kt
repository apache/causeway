package org.ro.layout

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.handler.LayoutHandler
import org.ro.snapshots.demo2_0_0.DEMO_OBJECT_LAYOUT
import org.ro.snapshots.simpleapp1_16_0.FR_OBJECT_LAYOUT
import org.ro.snapshots.simpleapp1_16_0.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class LayoutTest : IntegrationTest() {

    @Test
    fun testDemoObjectLayout() {
        if (isSimpleAppAvailable()) {
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
    }

    @Test
    fun testParseSimpleObjectLayout() {
        if (isSimpleAppAvailable()) {
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
    }

    @Test
    fun testParseFixtureScriptObjectLayout() {
        if (isSimpleAppAvailable()) {
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

}
