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

object SO_PROPERTY : Response(){
    override val url = "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes"
    override val str = """{
        "id": "notes",
        "memberType": "property",
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }, {
            "rel": "up",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Boo"
        }, {
            "rel": "urn:org.restfulobjects:rels/modifyproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\"",
            "arguments": {
                "value": null
            }
        }, {
            "rel": "urn:org.restfulobjects:rels/clearproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "DELETE",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }, {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/property-description\""
        }],
        "value": null,
        "extensions": {
            "x-isis-format": "string"
        }
    }"""
}
