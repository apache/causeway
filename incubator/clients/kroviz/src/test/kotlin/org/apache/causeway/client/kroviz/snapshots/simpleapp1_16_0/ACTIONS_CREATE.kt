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

object ACTIONS_CREATE : Response() {
    override val url = "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create"
    override val str = """{
    "id": "create",
    "memberType": "action",
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Simple Objects"
        },
        {
            "rel": "urn:org.restfulobjects:rels/invokeaction=\"create\"",
            "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create/invoke",
            "method": "POST",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
            "arguments": {
                "name": {
                    "value": null
                }
            }
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/actions/create",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
    ],
    "extensions": {
        "actionType": "user",
        "actionSemantics": "nonIdempotent"
    },
    "parameters": {
        "name": {
            "num": 0,
            "id": "name",
            "name": "Name",
            "description": ""
        }
    }
}"""
}
