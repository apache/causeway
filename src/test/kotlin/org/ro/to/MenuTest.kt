package org.ro.to

import kotlinx.serialization.json.JSON
import org.ro.core.Menu
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MenuTest {

    @Test
    fun testUnique() {
        //given:
        val jsonStr = SO_MENU.str
        val s1 = JSON.parse(Service.serializer(), jsonStr)
        val s2 = JSON.parse(Service.serializer(), jsonStr)
        //when
        Menu.add(s1)
        Menu.add(s2)
        //then
        val size: Int = Menu.uniqueMenuTitles().size
        console.log("[Menu.uniqueMenuTitles().size: $size]")
        assertTrue(1 == size)
    }

    @Test
    fun testParse() {
        val jsonStr = SO_MENU.str
        val service = JSON.parse(Service.serializer(), jsonStr)
        assertTrue(service.members.size > 0)
        assertTrue(service.getMemberList().size > 0)
        assertTrue(service.getActionList().size > 0)
        Menu.add(service)
        assertNotNull(Menu.menuItems)
        assertTrue(Menu.menuItems.size > 0)
    }

}