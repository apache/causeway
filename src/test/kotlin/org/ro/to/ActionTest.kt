package org.ro.to

import org.ro.handler.ActionHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActionTest {

    @Test
    fun testParseActionGETArgument() {
        val jsonStr = ACTIONS_FIND_BY_NAME.str
        val action = ActionHandler().parse(jsonStr) as Action
        val linkList = action.links
        assertNotNull(linkList)
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
        assertNotNull(links)
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        assertNotNull(invokeLink)
        val argList = invokeLink.arguments
        assertNotNull(argList)
        assertEquals(2, argList.size)

        val paramList = action.parameters
        assertNotNull(paramList)
        assertEquals(2, paramList.size)

        val p = action.findParameterByName("script")
        assertEquals("script", p!!.id)

        val choiceList = p.choices
        assertNotNull(choiceList)
        assertEquals(1, choiceList.size)

        val defaultChoice = p.defaultChoice
        assertNotNull(defaultChoice)
        assertTrue(choiceList[0].href == defaultChoice.href)
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
        assertNotNull(links)
        assertEquals(4, links.size)
    }

}