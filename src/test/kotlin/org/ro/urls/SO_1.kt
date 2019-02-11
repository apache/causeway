package org.ro.to

object SO_1 {
    val str = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Bar"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/object-layout",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Bar"
        },
        {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }
        ],
        "extensions": {
        "oid": "simple.SimpleObject:1",
        "isService": false,
        "isPersistent": true
    },
        "title": "Object: Bar",
        "domainType": "simple.SimpleObject",
        "instanceId": "1",
        "members": {
        "name": {
        "id": "name",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"name\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/name",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": "Bar",
        "extensions": {
        "x-isis-format": "string"
    },
        "disabledReason": "Immutable"
    },
        "notes": {
        "id": "notes",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": null,
        "extensions": {
        "x-isis-format": "string"
    }
    },
        "datanucleusIdLong": {
        "id": "datanucleusIdLong",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"datanucleusIdLong\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/datanucleusIdLong",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": 1,
        "format": "int",
        "extensions": {
        "x-isis-format": "long"
    },
        "disabledReason": "Contributed property"
    },
        "datanucleusVersionTimestamp": {
        "id": "datanucleusVersionTimestamp",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"datanucleusVersionTimestamp\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/datanucleusVersionTimestamp",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": 1518074491868,
        "format": "utc-millisec",
        "extensions": {
        "x-isis-format": "javasqltimestamp"
    },
        "disabledReason": "Contributed property"
    },
        "downloadJdoMetadata": {
        "id": "downloadJdoMetadata",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadJdoMetadata\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/downloadJdoMetadata",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "rebuildMetamodel": {
        "id": "rebuildMetamodel",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"rebuildMetamodel\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/rebuildMetamodel",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "openRestApi": {
        "id": "openRestApi",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"openRestApi\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/openRestApi",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "downloadLayoutXml": {
        "id": "downloadLayoutXml",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadLayoutXml\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/downloadLayoutXml",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "delete": {
        "id": "delete",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"delete\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/delete",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "updateName": {
        "id": "updateName",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"updateName\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/updateName",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "clearHints": {
        "id": "clearHints",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"clearHints\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/clearHints",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""
}