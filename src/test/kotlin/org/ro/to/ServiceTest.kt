package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.URLS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceTest {

    @Test
    fun testSimpleObjectMenu() {
        val jsonObj = JSON.parse<JsonObject>(URLS.SO_MENU)
        val service = Service(jsonObj)
        assertEquals("Simple Objects", service.title)
        val actions = service.getMembers()
        assertEquals(3, actions.size)

        assertTrue(includesId(actions, "listAll"))
        assertTrue(includesId(actions, "findByName"))
        assertTrue(includesId(actions, "create"))

        // jsonObj contains '"members": {}' not '"members": []' 
        // in AS this results in an unordered list (Object{}), 
        // but intended is an ordered list (Array[])
        //TODO use object-layout / menu layout instead
    }

    private fun includesId(list: MutableList<Invokeable>, id: String): Boolean {
        for (m in list) {
            if ((m as Member).id == id) {
                return true
            }
        }
        return false
    }

    @Test
    fun test_() {
        val jsonObj = JSON.parse<JsonObject>(URLS.RESTFUL_SERVICES)
        val service = Service(jsonObj)
        val values = service.valueList
        assertEquals(8, values.size)
    }

}