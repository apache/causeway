package org.ro.to

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import org.ro.urls.RESTFUL_SERVICES
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ImplicitReflectionSerializer
class ServiceTest {

    @Test
    fun testSimpleObjectMenu() {
        val jsonStr = SO_MENU.str
        val service = JSON.parse(Service.serializer(), jsonStr)
        assertEquals("Simple Objects", service.title)
        val actions: kotlin.collections.List<Member> = service.members
        assertEquals(3, actions.size)

        assertTrue(includesId(actions, "listAll"))
        assertTrue(includesId(actions, "findByName"))
        assertTrue(includesId(actions, "create"))

        // jsonObj contains '"members": {}' not '"members": []' 
        // in AS this results in an unordered list (Object{}), 
        // but intended is an ordered list (Array[])
        //TODO use object-layout / menu layout instead
    }

    private fun includesId(list: kotlin.collections.List<Member>, id: String): Boolean {
        for (m in list) {
            if (m.id == id) {
                return true
            }
        }
        return false
    }

    @Test
    fun testParseServices() {
        val jsonStr = RESTFUL_SERVICES.str
        val services = JSON.parse(Services.serializer(), jsonStr)
        val values = services.valueList()
        assertNotNull(values)
        assertEquals(8, values.size)
    }

}