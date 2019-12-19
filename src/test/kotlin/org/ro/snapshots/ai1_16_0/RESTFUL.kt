package org.ro.snapshots.ai1_16_0

object RESTFUL : Response(){
    override val url = "http://localhost:8080/restful/"
    override val str = """{
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        }, {
            "rel": "urn:org.restfulobjects:rels/user",
            "href": "http://localhost:8080/restful/user",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/user\""
        }, {
            "rel": "urn:org.apache.isis.restfulobjects:rels/menuBars",
            "href": "http://localhost:8080/restful/menuBars",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/layout-menubars\""
        }, {
            "rel": "urn:org.restfulobjects:rels/services",
            "href": "http://localhost:8080/restful/services",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
        }, {
            "rel": "urn:org.restfulobjects:rels/version",
            "href": "http://localhost:8080/restful/version",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/version\""
        }, {
            "rel": "urn:org.restfulobjects:rels/domain-types",
            "href": "http://localhost:8080/restful/domain-types",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/type-list\""
        }],
        "extensions": {}
    }"""
}
