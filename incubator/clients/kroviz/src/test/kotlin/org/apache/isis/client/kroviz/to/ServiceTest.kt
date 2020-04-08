package org.apache.isis.client.kroviz.to

import kotlinx.serialization.UnstableDefault
import org.apache.isis.client.kroviz.handler.ServiceHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.SO_MENU
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
        val actions: List<Member> = service.getMemberList()
        assertEquals(3, actions.size)

        assertTrue(service.containsMemberWith("listAll"))
        assertTrue(service.containsMemberWith("findByName"))
        assertTrue(service.containsMemberWith("create"))

        // jsonObj contains '"members": {}' not '"members": []'
        // this results in an unordered list (Map),
        // but intended is an ordered list (Array[])
        //TODO use object-layout / menu layout instead
    }

    fun Service.containsMemberWith(id: String): Boolean {
        for (m in getMemberList()) {
            if (m.id == id) {
                return true
            }
        }
        return false
    }

}
