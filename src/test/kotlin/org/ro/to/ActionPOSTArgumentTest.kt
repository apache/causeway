package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlin.test.assertEquals

class ActionPOSTArgumentTest {

    //[Test(description="parse result of invoking http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create")]
    fun testParseService() {
        val jsonObj = JSON.parse<JsonObject>(jsonStr)
        val actual: Action = Action(jsonObj)
        val links = actual.linkList
        assertEquals(4, links.size)
    }

    private var jsonStr = """{
        "id": "create",
        "memberType": "action",
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create",
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
            "rel": "urn:org.restfulobjects:rels/invokeaction=\"create\"",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create/invoke",
            "method": "POST",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
            "arguments": {
            "name": {
            "value": null
        }
        }
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/actions/create",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
        ],
        "extensions": {
        "actionType": "user",
        "actionSemantics": "nonIdempotent"
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