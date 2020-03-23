package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

object SO : Response() {
    override val url = "http://localhost:8080/restful/objects/simple.SimpleObject"
    override val str = """
{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/layout",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
        }
    ],
    "canonicalName": "domainapp.modules.simple.dom.impl.SimpleObject",
    "members": [
        {
            "rel": "urn:org.restfulobjects:rels/property",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/name",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/property",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/rebuildMetamodel",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/downloadJdoMetadata",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/openRestApi",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/downloadLayoutXml",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/delete",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/updateName",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/actions/clearHints",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
    ],
    "typeActions": [
        {
            "rel": "urn:org.restfulobjects:rels/invoke;typeaction=\"isSubtypeOf\"",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/type-actions/isSubtypeOf/invoke",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/type-action-result\"",
            "arguments": {
                "supertype": {
                    "href": null
                }
            }
        },
        {
            "rel": "urn:org.restfulobjects:rels/invoke;typeaction=\"isSupertypeOf\"",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/type-actions/isSupertypeOf/invoke",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/type-action-result\"",
            "arguments": {
                "subtype": {
                    "href": null
                }
            }
        }
    ],
    "extensions": {
        "friendlyName": "Simple Object",
        "pluralName": "Simple Objects",
        "isService": false
    }
}
"""
}
