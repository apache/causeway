package org.ro.to.mb3

import org.ro.urls.RESTFUL_MENUBARS
import org.w3c.dom.parsing.DOMParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class MenubarsXmlTest {

    @Test
    fun testParseXmlMenubars() {
        //given
        val xmlStr = RESTFUL_MENUBARS.str
        //when
        val p = DOMParser()
        val doc = p.parseFromString(xmlStr, "application/xml")

        //then
        assertNotNull(doc)

        val menuBars = Menubars(doc)
        assertNotNull(menuBars.primary)
        assertNotNull(menuBars.secondary)
        assertNotNull(menuBars.tertiary)

        val primary = menuBars.primary
        assertEquals("Simple Objects", primary.named)

        val section = primary.section
        val serviceActions = section.serviceActions
        assertEquals(3, serviceActions.size)

        val sa1 = serviceActions.first()
        assertEquals("simple.SimpleObjectMenu", sa1.objectType)
        assertEquals("create", sa1.id)
        assertEquals("Create", sa1.named)

        val l1 = sa1.link
        assertEquals("urn:org.restfulobjects:rels/action", l1.rel)
        assertEquals("GET", l1.method)
        assertEquals("http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/create", l1.href)
        assertEquals("application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"", l1.type)

        val saN = serviceActions.last()
        assertEquals("listAll", saN.id)
        assertEquals("List All", saN.named)
    }

}
