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

object ACTIONS_RUN_FIXTURE_SCRIPT : Response() {
    override val url = "http://localhost:8080/restful/services/causewayApplib.FixtureScriptsDefault/actions/runFixtureScript"
    override val str = """{
    "id": "runFixtureScript",
    "memberType": "action",
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/causewayApplib.FixtureScriptsDefault/actions/runFixtureScript",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services/causewayApplib.FixtureScriptsDefault",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "urn:org.restfulobjects:rels/invokeaction=\"runFixtureScript\"",
            "href": "http://localhost:8080/restful/services/causewayApplib.FixtureScriptsDefault/actions/runFixtureScript/invoke",
            "method": "POST",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
            "arguments": {
                "script": {
                    "value": null
                },
                "parameters": {
                    "value": null
                }
            }
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/causewayApplib.FixtureScriptsDefault/actions/runFixtureScript",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
    ],
    "extensions": {
        "actionType": "prototype",
        "actionSemantics": "nonIdempotent"
    },
    "parameters": {
        "script": {
            "num": 0,
            "id": "script",
            "name": "Script",
            "description": "",
            "choices": [
                {
                    "rel": "urn:org.restfulobjects:rels/value",
                    "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4=",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Domain App Demo"
                }
            ],
            "default": {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/domainapp.application.fixture.scenarios.DomainAppDemo/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PHBhdGg-PC9wYXRoPjwvbWVtZW50bz4=",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Domain App Demo"
            }
        },
        "parameters": {
            "num": 1,
            "id": "parameters",
            "name": "Parameters",
            "description": "Script-specific parameters (if any).  The format depends on the script implementation (eg key=value, CSV, JSON, XML etc)"
        }
    }
}"""
}
