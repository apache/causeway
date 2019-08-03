package org.ro.layout

import kotlinx.serialization.UnstableDefault
import org.ro.handler.IntegrationTest
import org.ro.handler.LayoutHandler
import org.ro.to.FR_OBJECT_LAYOUT
import org.ro.to.SO_OBJECT_LAYOUT
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

            val view = lo.build()
            assertNotNull(view) // 5

            val kids = view.children
            assertEquals(2, kids.size) // 6 row[0] is not to be rendered though

            val row1 = kids[1]
            assertEquals(2, row1.children.size)  // 7

            val h2 = row1.children[1]
            assertEquals(1, h2.children.size)   // 8
        }
    }

    @Test
    fun testparseFixtureScriptObjectLayout() {
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
            //TODO where do these labels come from?

            // (1) property.link.href "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/className"
            // (2) links[describedBy].href ->
            // (3) http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/className -> extensions.friendlyName
            // (4)
        }
    }

}
