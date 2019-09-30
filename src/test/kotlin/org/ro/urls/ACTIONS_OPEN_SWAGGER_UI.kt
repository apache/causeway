package org.ro.urls

object ACTIONS_OPEN_SWAGGER_UI : Response(){
    override val url = "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu/actions/openSwaggerUi/invoke"
    override val str = """{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu/actions/openSwaggerUi/invoke",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-result\"",
            "args": {}
        }
    ],
    "resulttype": "scalarvalue",
    "result": {
        "value": "http:/swagger-ui/index.html",
        "links": [
            {
                "rel": "urn:org.restfulobjects:rels/return-type",
                "href": "http://localhost:8080/restful/domain-types/java.net.URL",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
            }
        ],
        "extensions": {}
    }
}"""
}
