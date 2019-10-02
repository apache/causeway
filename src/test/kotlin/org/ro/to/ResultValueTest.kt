package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ResultValueHandler
import org.ro.urls.ACTIONS_OPEN_SWAGGER_UI
import kotlin.test.Test
import kotlin.test.assertEquals

@UnstableDefault
class ResultValueTest{

    @Test
    fun testParseActionOpenSwaggerUI() {
        val jsonStr = ACTIONS_OPEN_SWAGGER_UI.str
        val ir = ResultValueHandler().parse(jsonStr) as ResultValue
        val links = ir.links
        assertEquals(1, links.size)

        val result = ir.result!!
        val value = result.value!!.content as String
        assertEquals("http:/swagger-ui/index.html", value)
    }

}
