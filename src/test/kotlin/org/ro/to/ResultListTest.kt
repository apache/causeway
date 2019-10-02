package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultListHandler
import org.ro.urls.SO_LIST_ALL_INVOKE
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ResultListTest {

    @Test
    fun testListAllInvoke() {
        val jsonStr = SO_LIST_ALL_INVOKE.str
        val ir = ResultListHandler().parse(jsonStr) as ResultList
        val result = ir.result!!
        val valueList = result.value
        assertEquals(10, valueList.size)
    }

}
