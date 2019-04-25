package org.ro.to

import org.ro.urls.Response

object SO_LIST_ALL_INVOKE : Response() {
    override val url = "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke"
    override val str = """{
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-result\"",
                "args": {}
            }
        ],
        "resulttype": "list",
        "result": {
            "value": [
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Foo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bar"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/2",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Baz"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/3",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Frodo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/4",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Froyo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/5",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Fizz"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/6",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bip"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/7",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bop"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/8",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bang"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/9",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Boo"
                }
            ],
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/return-type",
                    "href": "http://localhost:8080/restful/domain-types/java.util.List",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
                }
            ],
            "extensions": {}
        }
    }"""
}