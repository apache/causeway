package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.InvocationResultHandler
import org.ro.urls.ACTIONS_OPEN_SWAGGER_UI
import org.ro.urls.SO_LIST_ALL_INVOKE
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class InvocationResultTest {

    @Test
    fun testListAllInvoke() {
        val jsonStr = SO_LIST_ALL_INVOKE.str
        val ir = InvocationResultHandler().parse(jsonStr) as InvocationResult
        val result = ir.result!!
        val valueList = result.value
        assertEquals(10, valueList.size)
    }

//FIXME    @Test
    fun testParseActionOpenSwaggerUI() {
        val jsonStr = ACTIONS_OPEN_SWAGGER_UI.str
        val ir = InvocationResultHandler().parse(jsonStr) as InvocationResult
        val links = ir.links
        assertEquals(1, links.size)

        val result = ir.result!!
        val valueList = result.value
        assertEquals(1, valueList.size)
    }

}
