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

// ResultListList.kt
object RESTFUL_SERVICES : Response() {
    override val url = "http://localhost:8080/restful/services"
    override val str = """
        {
        "value": [
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"simple.SimpleObjectMenu\"",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.FixtureScriptsDefault\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.FixtureScriptsDefault",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.LayoutServiceMenu\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.LayoutServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.MetaModelServicesMenu\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.MetaModelServicesMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.SwaggerServiceMenu\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.SwaggerServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.TranslationServicePoMenu\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.TranslationServicePoMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.HsqlDbManagerMenu\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.HsqlDbManagerMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"causewayApplib.ConfigurationServiceMenu\"",
                "href": "http://localhost:8080/restful/services/causewayApplib.ConfigurationServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Configuration Service Menu"
            }
        ],
        "extensions": {},
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
            }
        ]
    }"""
}
