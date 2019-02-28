package org.ro.to

object FR_PROPERTY_DESCRIPTION {
    val url = "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/className"
    val str = """{
        "id": "className",
        "memberType": "property",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/className",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/property-description\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.restfulobjects:rels/return-type",
                "href": "http://localhost:8080/restful/domain-types/java.lang.String",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            }
        ],
        "optional": false,
        "extensions": {
            "friendlyName": "Result class"
        }
    }"""
}