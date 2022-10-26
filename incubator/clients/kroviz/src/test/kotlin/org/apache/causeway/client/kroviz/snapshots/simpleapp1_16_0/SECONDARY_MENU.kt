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
package org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.causeway.client.kroviz.snapshots.Response

object SECONDARY_MENU : Response(){
    override val url = "http://localhost:8080/restful/services/causewayApplib.MetaModelServicesMenu"
    override val str = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/causewayApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/causewayApplib.MetaModelServicesMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.causeway.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/causewayApplib.MetaModelServicesMenu/layout",
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
        "oid": "causewayApplib.MetaModelServicesMenu:1",
        "isService": true,
        "isPersistent": true,
        "menuBar": "SECONDARY"
    },
        "title": "Prototyping",
        "serviceId": "causewayApplib.MetaModelServicesMenu",
        "members": {
        "downloadMetaModel": {
        "id": "downloadMetaModel",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadMetaModel\"",
            "href": "http://localhost:8080/restful/services/causewayApplib.MetaModelServicesMenu/actions/downloadMetaModel",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""
}
