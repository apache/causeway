package org.ro.to

import org.ro.core.Menu
import org.ro.handler.ServiceHandler
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MenuTest {

    @Test
    fun testUnique() {
        //given:
        val jsonStr = SO_MENU.str
        val s1 = ServiceHandler().parse(jsonStr)
        val s2 = ServiceHandler().parse(jsonStr)
        //when
        Menu.add(s1)
        Menu.add(s2)
        //then
        val size: Int = Menu.filterUniqueMenuTitles().size
        console.log("[Menu.uniqueMenuTitles().size: $size]")
        assertTrue(1 == size)
    }

    @Test
    fun testParse() {
        val jsonStr = SO_MENU.str
        val service = ServiceHandler().parse(jsonStr)
        assertTrue(service.members.size > 0)
        assertTrue(service.getMemberList().size > 0)
        assertTrue(service.getActionList().size > 0)
        Menu.add(service)
        assertNotNull(Menu.list)
        assertTrue(Menu.list.size > 0)
    }

}