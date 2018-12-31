package org.ro.to

import kotlinx.serialization.json.JsonObject
import org.ro.URLS
import kotlin.test.Test
import kotlin.test.assertEquals

class ListTest {

    // http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke
    @Test
    fun testListAllInvoke() {
        val jsonObj = JSON.parse<JsonObject>(URLS.SO_LIST_ALL_INVOKE)
        val list = List(jsonObj)
        val linkList = list.getResult()!!.valueList
        assertEquals(10, linkList.size)
    }

}