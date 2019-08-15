package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultListHandler
import org.ro.urls.SO_LIST_ALL_INVOKE
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ListTest {

    @Test
    fun testListAllInvoke() {
        val list = ResultListHandler().parse(SO_LIST_ALL_INVOKE.str) as ResultList
        val result = list.result!!
        val valueList = result.value
        assertEquals(10, valueList.size)
    }

}
