package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ServiceHandler
import org.ro.urls.SO_MENU
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@UnstableDefault
class ServiceTest : ToTest() {

    @Test
    fun testSimpleObjectMenu() {
        val jsonStr = SO_MENU.str
        val service = ServiceHandler().parse(jsonStr) as Service
        assertEquals("Simple Objects", service.title)
        val actions: kotlin.collections.List<Member> = service.getMemberList()
        assertEquals(3, actions.size)

        assertTrue(service.containsMemberWith("listAll"))
        assertTrue(service.containsMemberWith("findByName"))
        assertTrue(service.containsMemberWith("create"))

        // jsonObj contains '"members": {}' not '"members": []'
        // in AS this results in an unordered list (Object{}),
        // but intended is an ordered list (Array[])
        // or is it a Map?
        //TODO use object-layout / menu layout instead
    }

 /*FIXME   private fun includesId(list: kotlin.collections.List<Member>, id: String): Boolean {
        for (m in list) {
            if (m.id == id) {
                return true
            }
        }
        return false
    }
   */
    fun Service.containsMemberWith(id: String): Boolean {
        for (m in getMemberList()) {
            if (m.id == id) {
                return true
            }
        }
        return false
    }

}
