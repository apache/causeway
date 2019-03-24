package org.ro.layout

import org.ro.handler.LayoutHandler
import org.ro.handler.TestUtil
import org.ro.to.FR_OBJECT_LAYOUT
import org.ro.to.SO_OBJECT_LAYOUT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LayoutTest {

    @Test
    fun testParseSimpleObjectLayout() {
        if (TestUtil().isSimpleAppAvailable()) {
            //given
            val jsonStr = SO_OBJECT_LAYOUT.str
            //when
            val lo = LayoutHandler().parse(jsonStr)
            val properties = lo.properties
            // then
            assertNotNull(properties)
            assertEquals(2, properties.size)
            assertEquals("name", properties[0].id)
            assertEquals("notes", properties[1].id)

            // BUILD UI TEST
            // layout.rows[1].cols[1].col.tabGroup[0]
            //ensure tabgroup is TabNavigator
            //FIXME
            // then (5)
            assertEquals(2, lo.row.size)

            val view = lo.build()
            assertNotNull(view) // (6)

            val kids = view.children
            assertEquals(2, kids.size) // (7) row[0] is not to be rendered though

            val row1 = kids[1]
            assertEquals(2, row1.children.size)  // (8)

            val h2 = row1.children[1]
            assertEquals(1, h2.children.size)
        }
    }

    @Test
    fun testparseFixtureScriptObjectLayout() {
        if (TestUtil().isSimpleAppAvailable()) {
            // given
            val jsonStr = FR_OBJECT_LAYOUT.str
            val lo = LayoutHandler().parse(jsonStr)
            // when
            val properties = lo.properties
            // then
            assertNotNull(properties)
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