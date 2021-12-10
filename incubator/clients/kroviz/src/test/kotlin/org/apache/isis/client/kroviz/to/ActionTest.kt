/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.to

import org.apache.isis.client.kroviz.IntegrationTest
import org.apache.isis.client.kroviz.core.aggregator.ActionDispatcher
import org.apache.isis.client.kroviz.core.event.ResourceProxy
import org.apache.isis.client.kroviz.core.event.ResourceSpecification
import org.apache.isis.client.kroviz.handler.ActionHandler
import org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0.*
import org.apache.isis.client.kroviz.ui.core.SessionManager
import org.apache.isis.client.kroviz.ui.core.UiManager
import org.apache.isis.client.kroviz.utils.StringUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActionTest : IntegrationTest() {

    // @Test
    // IntegrationTest, assumes a server is running, furthermore expects the fingerPrint (PD94*) to be valid
    fun testInvokeAction() {
        val url = "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript/invoke"
        val fingerPrint = "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4="
/*        val body = """{
                "script": {
                    "value": {
                        "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/$fingerPrint"}
                        },
                "parameters": {"value": ""}
            }  """   */
        val href = "\"href\": \"http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/$fingerPrint\"".trimIndent()
        //TODO construct link, invoke and check response
        if (isAppAvailable()) {
            console.log("[AT.testInvokeAction]")
            val action = ActionHandler().parse(ACTIONS_RUN_FIXTURE_SCRIPT.str) as Action

            val link = action.getInvokeLink()
            assertNotNull(link)
            //now pass on body in order to prepare everything to invoke
            val arguments = link.arguments as MutableMap
            val arg = Argument(href)
            arguments.put("script", arg)
            //ensure link arguments make up valid json body
            val body = StringUtils.argumentsAsBody(link)
            console.log(body)
            val json = JSON.parse<Argument>(body)
            console.log(json)
            ResourceProxy().fetch(link, ActionDispatcher())
            val urlSpec = ResourceSpecification(url)
            val es = SessionManager.getEventStore()
            val le = es.findBy(urlSpec)!!
            console.log(es.log)
            console.log(le)
            assertTrue(!le.isError())
        }
    }

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
        val argList = invokeLink.arguments
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
    fun testParseActionDownloadLayout() {
        val jsonStr = ACTIONS_DOWNLOAD_LAYOUTS.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val argList = invokeLink!!.arguments
        assertEquals(1, argList.size)  //2

        val paramList = action.parameters
        assertEquals(1, paramList.size)

        val p = action.findParameterByName("style")!!
        assertEquals("style", p.id)

        val choiceList = p.choices
        assertEquals(4, choiceList.size)

        val defaultChoice = p.defaultChoice!!.content as String
        assertEquals(choiceList[2].content, defaultChoice)
    }

    @Test
    fun testParseActionDownloadMenubarsLayout() {
        val jsonStr = ACTIONS_DOWNLOAD_MENUBARS_LAYOUT.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val argList = invokeLink!!.arguments
        assertEquals(2, argList.size)  //2

        val paramList = action.parameters
        assertEquals(2, paramList.size)

        val p = action.findParameterByName("type")
        assertEquals("type", p!!.id)

        val choiceList = p.choices
        assertEquals(2, choiceList.size)

        val defaultChoice = p.defaultChoice!!.content as String
        assertEquals(choiceList[0].content, defaultChoice)
    }

    @Test
    fun testParseActionDownloadSwaggerSchemaDefinition() {
        val jsonStr = ACTIONS_DOWNLOAD_SWAGGER_SCHEMA_DEFINITION.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val argList = invokeLink!!.arguments
        assertEquals(3, argList.size)  //2

        val paramList = action.parameters
        assertEquals(3, paramList.size)

        val p0 = action.findParameterByName("filename")
        assertEquals("filename", p0!!.id)
        val p1 = action.findParameterByName("visibility")
        assertEquals("visibility", p1!!.id)
        val p2 = action.findParameterByName("format")
        assertEquals("format", p2!!.id)

        val choiceList = p1.choices
        assertEquals(3, choiceList.size)

        val defaultChoice = p1.defaultChoice!!.content as String
        assertEquals("Private", defaultChoice)
    }

    //    @Test    //TODO-> empty key in arguments msg: Encountered unknown key csvFileName
    fun testParseActionDownloadMetaModel() {
        val jsonStr = ACTIONS_DOWNLOAD_META_MODEL.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val argList = invokeLink!!.arguments
        assertEquals(2, argList.size)  //2

        val paramList = action.parameters
        assertEquals(2, paramList.size)

        val p = action.findParameterByName(".csvFileName")
        assertEquals(".csvFileName", p!!.id)

//        val choiceList = p.choices
//        assertEquals(2, choiceList.size)

        val defaultChoice = p.defaultChoice!!.content as String
        assertEquals("metamodel.csv", defaultChoice)
    }

    //@Test    //TODO-> empty key in arguments msg: Encountered unknown key csvFileName
    fun testDownloadTranslations() {
        val jsonStr = ACTIONS_DOWNLOAD_TRANSLATIONS.str
        val action = ActionHandler().parse(jsonStr) as Action
        val links = action.links
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val argList = invokeLink!!.arguments
        assertEquals(1, argList.size)  //2

        val paramList = action.parameters
        assertEquals(1, paramList.size)

        val p = action.findParameterByName(".potFileName")
        assertEquals(".potFileName", p!!.id)

        val defaultChoice = p.defaultChoice!!.content as String
        assertEquals("translations.pot", defaultChoice)
    }

}
