package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

object SECONDARY_MENU : Response(){
    override val url = "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu"
    override val str = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.MetaModelServicesMenu/layout",
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
        "oid": "isisApplib.MetaModelServicesMenu:1",
        "isService": true,
        "isPersistent": true,
        "menuBar": "SECONDARY"
    },
        "title": "Prototyping",
        "serviceId": "isisApplib.MetaModelServicesMenu",
        "members": {
        "downloadMetaModel": {
        "id": "downloadMetaModel",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadMetaModel\"",
            "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu/actions/downloadMetaModel",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""
}
