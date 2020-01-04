package org.ro.to.mb

import kotlinx.serialization.UnstableDefault
import org.ro.handler.MenuBarsHandler
import org.ro.snapshots.ai1_16_0.RESTFUL_MENUBARS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class MenubarsTest {

    @Test
    fun testParseMenubars() {
        //given
        val jsonStr = RESTFUL_MENUBARS.str
        //when
        val menuBars = MenuBarsHandler().parse(jsonStr) as Menubars

        //then
        assertNotNull(menuBars.primary)
        assertNotNull(menuBars.secondary)
        assertNotNull(menuBars.tertiary)

        val primary = menuBars.primary
        assertEquals("Simple Objects", primary.menu.first().named)

        val section = primary.menu.first().section
        val serviceActions = section.first().serviceAction
        assertEquals(3, serviceActions.size)

        val sa1 = serviceActions.first()
        assertEquals("simple.SimpleObjectMenu", sa1.objectType)
        assertEquals("create", sa1.id)
        assertEquals("Create", sa1.named)

        val l1 = sa1.link!!
        assertEquals("urn:org.restfulobjects:rels/action", l1.rel)
        assertEquals("GET", l1.method)
        assertEquals("http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/create", l1.href)
        assertEquals("application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"", l1.type)

        val saN = serviceActions.last()
        assertEquals("listAll", saN.id)
        assertEquals("List All", saN.named)
    }

}
