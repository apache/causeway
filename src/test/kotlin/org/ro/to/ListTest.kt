package org.ro.to

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlin.test.Test
import kotlin.test.assertEquals

@ImplicitReflectionSerializer
class ListTest {

    @Test
    fun testListAllInvoke() {
        val list = JSON.parse(ResultList.serializer(), SO_LIST_ALL_INVOKE.str)
        val result = list.result!!
        val valueList = result.value
        assertEquals(10, valueList.size)
    }

}