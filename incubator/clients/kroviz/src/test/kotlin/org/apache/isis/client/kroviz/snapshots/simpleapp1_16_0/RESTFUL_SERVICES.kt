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
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.FixtureScriptsDefault\"",
                "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.LayoutServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.MetaModelServicesMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.SwaggerServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.TranslationServicePoMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.HsqlDbManagerMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.HsqlDbManagerMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.ConfigurationServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.ConfigurationServiceMenu",
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
