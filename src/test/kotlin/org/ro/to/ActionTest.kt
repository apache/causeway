package org.ro.to

import kotlinx.serialization.UnstableDefault
import org.ro.handler.ActionHandler
import org.ro.urls.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@UnstableDefault
class ActionTest {

    @Test
    fun testParseActionGETArgument() {
        val jsonStr = ACTIONS_FIND_BY_NAME.str
        val action = ActionHandler().parse(jsonStr) as Action
        val linkList = action.links
        assertEquals(4, linkList.size)

        val invokeLink: Link = action.getInvokeLink()!!
        val args = invokeLink.arguments
        assertNotNull(args)
    }

    @Test
    fun testParseActionPOSTArgumentFS() {
        val jsonStr = ACTIONS_RUN_FIXTURE_SCRIPT.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)   //1

        val invokeLink = action.getInvokeLink()!!
        val argList = invokeLink.arguments!!.asMap()
        assertEquals(2, argList.size)  //2

        val paramList = action.parameters
        assertEquals(2, paramList.size)  //3

        val p = action.findParameterByName("script")!!
        assertEquals("script", p.id)   //4

        val choiceList = p.choices
        assertEquals(1, choiceList.size) //5

        val defaultChoice = p.defaultChoice!!.content as Link
        val l = choiceList.first().content as Link
        assertEquals(l.href, defaultChoice.href)   //6
    }

    @Test
    fun testParseActionPOSTArgument() {
        val jsonStr = ACTIONS_CREATE.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)
    }

    @Test
    fun testParseActionPOSTDelete() {
        val jsonStr = ACTIONS_DELETE.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)
    }

    @Test
    fun testParseActionDownloadLayouts() {
        val jsonStr = ACTIONS_DOWNLOAD_LAYOUTS.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val argList = invokeLink!!.arguments!!.asMap()
        assertEquals(1, argList.size)  //2

        val paramList = action.parameters
        assertEquals(1, paramList.size)

        val p = action.findParameterByName("style")
        assertEquals("style", p!!.id)

        val choiceList = p.choices
        assertEquals(4, choiceList.size)

        val defaultChoice = p.defaultChoice!!.content as String
        assertEquals(choiceList[2].content, defaultChoice)
    }

}
