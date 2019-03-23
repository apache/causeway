package org.ro.to

import org.ro.handler.ListHandler
import kotlin.test.Test
import kotlin.test.assertEquals

class ListTest {

    @Test
    fun testListAllInvoke() {
        val list = ListHandler().parse(SO_LIST_ALL_INVOKE.str)
        val result = list.result!!
        val valueList = result.value
        assertEquals(10, valueList.size)
    }

}