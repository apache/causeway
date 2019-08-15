package org.ro.urls

object SO_MENU: Response() {
    override val url = "http://localhost:8080/restful/services/simple.SimpleObjectMenu"
    override val str = """{
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/layout",
                "method": "GET",
                "type": "application/xmlprofile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/services",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
            }
        ],
        "extensions": {
            "oid": "simple.SimpleObjectMenu:1",
            "isService": true,
            "isPersistent": true,
            "menuBar": "PRIMARY"
        },
        "title": "Simple Objects",
        "serviceId": "simple.SimpleObjectMenu",
        "members": {
            "listAll": {
                "id": "listAll",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"listAll\"",
                        "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "findByName": {
                "id": "findByName",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"findByName\"",
                        "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/findByName",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "create": {
                "id": "create",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"create\"",
                        "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            }
        }
    }"""
}
