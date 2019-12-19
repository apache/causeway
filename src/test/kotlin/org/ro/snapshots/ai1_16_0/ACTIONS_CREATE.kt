package org.ro.snapshots.ai1_16_0

object ACTIONS_CREATE : Response() {
    override val url = "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create"
    override val str = """{
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
