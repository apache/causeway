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
package org.apache.isis.client.kroviz.snapshots.demo2_0_0

import org.apache.isis.client.kroviz.snapshots.Response

object DEMO_FILE_NODE : Response() {
    override val url = "http://localhost:8080/restful/domain-types/demo.FileNode"
    override val str = """
{
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/layout",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
        }
    ],
    "canonicalName": "demoapp.dom.tree.FileNode",
    "members": [
        {
            "rel": "urn:org.restfulobjects:rels/property",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/parent",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/property",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/path",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/property",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/type",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/rebuildMetamodel",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/openRestApi",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/downloadMetaModelXml",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/downloadLayoutXml",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        },
        {
            "rel": "urn:org.restfulobjects:rels/action",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/clearHints",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
    ],
    "typeActions": [
        {
            "rel": "urn:org.restfulobjects:rels/invoke;typeaction=\"isSubtypeOf\"",
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/type-actions/isSubtypeOf/invoke",
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
            "href": "http://localhost:8080/restful/domain-types/demo.FileNode/type-actions/isSupertypeOf/invoke",
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
        "friendlyName": "File Node",
        "pluralName": "File Nodes",
        "isService": false
    }
}
"""
}
