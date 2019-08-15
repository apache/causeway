package org.ro.urls

object ACTIONS_FIND_BY_NAME : Response(){
    override val url = "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/findByName"
    override val str = """{
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
