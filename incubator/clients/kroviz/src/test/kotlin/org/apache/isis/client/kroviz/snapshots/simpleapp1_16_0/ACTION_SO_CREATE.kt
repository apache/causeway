/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

object ACTION_SO_CREATE : Response() {
    override val url = "http://localhost:8080/restful/objects/simple.SimpleObject/20"
    override val str = """{
    "links": [],
    "resulttype": "domainobject",
    "result": {
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Beutlin"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/object-layout",
                "method": "GET",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Beutlin"
            },
            {
                "rel": "urn:org.restfulobjects:rels/update",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20",
                "method": "PUT",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
                "arguments": {}
            }
        ],
        "extensions": {
            "oid": "simple.SimpleObject:20",
            "isService": false,
            "isPersistent": true
        },
        "title": "Object: Beutlin",
        "domainType": "simple.SimpleObject",
        "instanceId": "20",
        "members": {
            "name": {
                "id": "name",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;property=\"name\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/properties/name",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": "Beutlin",
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Immutable"
            },
            "rebuildMetamodel": {
                "id": "rebuildMetamodel",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"rebuildMetamodel\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/rebuildMetamodel",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "downloadJdoMetadata": {
                "id": "downloadJdoMetadata",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"downloadJdoMetadata\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/downloadJdoMetadata",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "openRestApi": {
                "id": "openRestApi",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"openRestApi\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/openRestApi",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "downloadLayoutXml": {
                "id": "downloadLayoutXml",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"downloadLayoutXml\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/downloadLayoutXml",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "delete": {
                "id": "delete",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"delete\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/delete",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "updateName": {
                "id": "updateName",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"updateName\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/updateName",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "clearHints": {
                "id": "clearHints",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/20/actions/clearHints",
                        "method": "GET",
                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            }
        }
    }
}
"""
}
