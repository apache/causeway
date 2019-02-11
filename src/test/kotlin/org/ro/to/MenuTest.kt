package org.ro.to

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.core.Menu
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ImplicitReflectionSerializer
class MenuTest {

    @Test
    fun testUnique() {
        //given:
        val jsonStr =  SO_MENU.str
        val s1 = JSON.parse(Service.serializer(), jsonStr)
        val m1 = s1.members
        val s2 = JSON.parse(Service.serializer(), jsonStr)
        val m2 = s2.members
        //when
        val menu = Menu(2)
        menu.init(s1, m1)
        menu.init(s2, m2)
        //then
        val size: Int = menu.uniqueMenuTitles().size
        assertTrue(1 == size)
    }

    @Test
    fun testParse() {
        val jsonStr = SO_MENU.str
        val service = JSON.parse(Service.serializer(), jsonStr)
        val members = service.members
        val menu = Menu(1)
        menu.init(service, members)
        assertNotNull(menu)
    }

}