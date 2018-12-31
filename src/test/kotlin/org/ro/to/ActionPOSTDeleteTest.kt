package org.ro.to

import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals

class ActionPOSTDeleteTest {
    
    @Test
    fun testParseService() {
        val jsonObj = JSON.parse<JsonObject>(jsonStr)
        val actual = Action(jsonObj)
        //var actual = Action(json)
        val links = actual.linkList
        assertEquals(4, links.size)
    }

    // http://localhost:8080/restful/objects/simple.SimpleObject/40/actions/delete
    val jsonStr = """{
        "id": "delete",
        "memberType": "action",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/40/actions/delete",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/40",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Foo"
            },
            {
                "rel": "urn:org.restfulobjects:rels/invokeaction=\"delete\"",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/40/actions/delete/invoke",
                "method": "POST",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
                "arguments": {}
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/delete",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
            }
        ],
        "extensions": {
            "actionType": "user",
            "actionSemantics": "nonIdempotentAreYouSure"
        },
        "parameters": {}
    }"""

}