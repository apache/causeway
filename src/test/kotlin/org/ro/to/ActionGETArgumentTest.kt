package org.ro.to

import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.parse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ActionGETArgumentTest {

    @ImplicitReflectionSerializer
    @Test
    fun testParseService(): Unit {
        val jsonObj = JSON.parse<JsonObject>(jsonStr)
        val action: Action = Action(jsonObj)
        val links = action.linkList
        assertEquals(4, links.size)

        val invokeLink: Link? = action.getInvokeLink()
        val args = invokeLink!!.arguments
        assertNotNull(args)
    }

    val jsonStr = """{
        "id": "findByName",
        "memberType": "action",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/findByName",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "urn:org.restfulobjects:rels/invokeaction=\"findByName\"",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/findByName/invoke",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
                "arguments": {
                    "name": {
                        "value": null
                    }
                }
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/actions/findByName",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
            }
        ],
        "extensions": {
            "actionType": "user",
            "actionSemantics": "safe"
        },
        "parameters": {
            "name": {
                "num": 0,
                "id": "name",
                "name": "Name",
                "description": ""
            }
        }
    }"""

}