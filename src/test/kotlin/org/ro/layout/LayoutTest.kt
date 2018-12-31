package org.ro.layout

import kotlinx.serialization.json.JsonObject
import org.ro.URLS
import kotlin.test.Test
import kotlin.test.assertEquals

class LayoutTest {

    @Test
    fun testObjectLayout() {
        //when
        val jsonObj = JSON.parse<JsonObject>(URLS.SO_OBJECT_LAYOUT)
        val lo = Layout(jsonObj)
        // then
        val properties = lo.properties
        assertEquals(2, properties.size)
        assertEquals("name", properties[0].id)
        assertEquals("notes", properties[1].id)
    }

    @Test
    fun testFsObjectLayout() {
        // given
        val jsonObj = JSON.parse<JsonObject>(URLS.FR_OBJECT_LAYOUT)
        val lo = Layout(jsonObj)
        // when
        val properties = lo.properties
        // then
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

    @Test
    fun testBuildObjectLayout() {
        //when
        val jsonObj = JSON.parse<JsonObject>(URLS.SO_OBJECT_LAYOUT)
        val lo = Layout(jsonObj)
        // layout.rows[1].cols[1].col.tabGroup[0]
        //ensure tabgroup is TabNavigator

        // then
        val ui = lo.build()
        assertEquals(2, ui.getChildren().size)

        //TODO IMPROVE expected values depend on 'debugInfo' applied in Layout classes 
        val row1 = ui.getChildren()[1]
        assertEquals(3, row1.getChildren().size)

        val h2 = row1.getChildren()[1]
        assertEquals(3, h2.getChildren().size)
    }

}