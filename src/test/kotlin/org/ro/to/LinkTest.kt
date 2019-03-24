package org.ro.to

import com.github.snabbdom._set
import org.ro.core.event.EventLog
import org.ro.handler.ActionHandler
import org.ro.handler.TestUtil
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LinkTest {

    @Test
    // IntegrationTest, assumes a server is running, furthermore expects the fingerPrint (PD94*) to be valid
    fun testInvokeAction() {
        val url = "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript/invoke"
        val fingerPrint = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4="
        val body = """{
                "script": {
                    "value": {
                        "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/$fingerPrint"}
                        },
                "parameters": {"value": ""}
            }  """
        val href = """"href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/$fingerPrint""""
        //TODO construct link, invoke and check response
        if (TestUtil().isSimpleAppAvailable()) {
            val action = ActionHandler().parse(ACTIONS_RUN_FIXTURE_SCRIPT.str)

            val link = action.getInvokeLink()
            assertNotNull(link)
            //now pass on body in order to prepare everything to invoke
            val arguments = link.arguments
            val arg = Argument(href)
            arguments._set("script", arg)
            console.log("[LinkTest.testInvokeAction] $link")
            link.invoke()
            val le = EventLog.find(url)
            assertNotNull(le)
            assertTrue(!le.isError())
         //   assertEquals(body, le.request)
        }
    }
}