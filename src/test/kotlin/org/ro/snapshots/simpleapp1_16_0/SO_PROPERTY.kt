package org.ro.snapshots.simpleapp1_16_0

import org.ro.snapshots.Response

object SO_PROPERTY : Response(){
    override val url = "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes"
    override val str = """{
        "id": "notes",
        "memberType": "property",
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }, {
            "rel": "up",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Boo"
        }, {
            "rel": "urn:org.restfulobjects:rels/modifyproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\"",
            "arguments": {
                "value": null
            }
        }, {
            "rel": "urn:org.restfulobjects:rels/clearproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "DELETE",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }, {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/property-description\""
        }],
        "value": null,
        "extensions": {
            "x-isis-format": "string"
        }
    }"""
}
