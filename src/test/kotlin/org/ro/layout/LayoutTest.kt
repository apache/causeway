package org.ro.layout

import kotlinx.serialization.UnstableDefault
import org.ro.IntegrationTest
import org.ro.handler.LayoutHandler
import org.ro.snapshots.simpleapp1_16_0.FR_OBJECT_LAYOUT
import org.ro.snapshots.simpleapp1_16_0.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class LayoutTest : IntegrationTest() {

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

            // BUILD UI TEST
            // layout.rows[1].cols[1].col.tabGroup[0]
            //ensure tabgroup is TabNavigator
            assertEquals(2, lo.row.size) //4

     //       val view = lo.build()
       //     assertNotNull(view) // 5

/*            val kids = view.
            assertEquals(2, kids.size) // 6 row[0] is not to be rendered though

            val row1 = kids[1]
            assertEquals(2, row1.children.size)  // 7

            val h2 = row1.children[1]
            assertEquals(1, h2.children.size)   // 8  */
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
