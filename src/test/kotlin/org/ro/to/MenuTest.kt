package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.core.Menu
import org.ro.handler.ServiceHandler
import org.ro.urls.SO_MENU
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@UnstableDefault
class MenuTest : ToTest() {

    @Test
    fun testUnique() {
        //given:
        val jsonStr = SO_MENU.str
        val s1 = ServiceHandler().parse(jsonStr) as Service
        val s2 = ServiceHandler().parse(jsonStr) as Service
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
        val service = ServiceHandler().parse(jsonStr) as Service
        assertTrue(service.members.size > 0)
        val actionCount =  service.getMemberList().size
        assertTrue( actionCount > 0)
        Menu.add(service)
        assertNotNull(Menu.list)
        val menuEntryCount = Menu.list.size
        assertTrue( menuEntryCount> 0)
//        assertEquals(actionCount, menuEntryCount)
    }

}
