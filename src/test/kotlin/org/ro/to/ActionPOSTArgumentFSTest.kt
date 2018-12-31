package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ActionPOSTArgumentFSTest {

    @Test
    fun testParseService() {
        val jsonObj = JSON.parse<JsonObject>(jsonStr)
        val action = Action(jsonObj)
        val links = action.linkList
        assertEquals(4, links.size)

        val invokeLink = action.getInvokeLink()
        val args = invokeLink!!.argumentList
        assertNotNull(args)
        assertEquals(2, args.size)

        val params = action.parameterList
        assertNotNull(params)
        assertEquals(2, params.size)

        val p = action.findParameterByName("script")
        assertEquals("script", p!!.id)

        val choiceList = p.choiceList
        assertEquals(1, choiceList.size)

        val defaultChoice = p.getDefaultChoice()
        assertTrue(choiceList[0].href == defaultChoice!!.href)
    }

    // http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript
    private val jsonStr = """{
        "id": "runFixtureScript",
        "memberType": "action",
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "urn:org.restfulobjects:rels/invokeaction=\"runFixtureScript\"",
            "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault/actions/runFixtureScript/invoke",
            "method": "POST",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
            "arguments": {
            "script": {
            "value": null
        },
            "parameters": {
            "value": null
        }
        }
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureScriptsDefault/actions/runFixtureScript",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
        ],
        "extensions": {
        "actionType": "prototype",
        "actionSemantics": "nonIdempotent"
    },
        "parameters": {
        "script": {
        "num": 0,
        "id": "script",
        "name": "Script",
        "description": "",
        "choices": [
        {
            "rel": "urn:org.restfulobjects:rels/value",
            "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4=",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Domain App Demo"
        }
        ],
        "default": {
        "rel": "urn:org.restfulobjects:rels/value",
        "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4=",
        "method": "GET",
        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
        "title": "Domain App Demo"
    }
    },
        "parameters": {
        "num": 1,
        "id": "parameters",
        "name": "Parameters",
        "description": "Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)"
    }
    }
    }"""

}