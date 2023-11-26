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

object SO_LIST_ALL_INVOKE : Response() {
    override val url = "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke"
    override val str = """{
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-result\"",
                "args": {}
            }
        ],
        "resulttype": "list",
        "result": {
            "value": [
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Foo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bar"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/2",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Baz"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/3",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Frodo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/4",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Froyo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/5",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Fizz"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/6",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bip"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/7",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bop"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/8",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bang"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/9",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Boo"
                }
            ],
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/return-type",
                    "href": "http://localhost:8080/restful/domain-types/java.util.List",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
                }
            ],
            "extensions": {}
        }
    }"""
}
