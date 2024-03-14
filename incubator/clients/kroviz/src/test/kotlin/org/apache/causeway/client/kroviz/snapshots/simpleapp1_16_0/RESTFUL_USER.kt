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

object RESTFUL_USER : Response(){
    override val url = "http://localhost:8080/restful/user"
    override val str = """{
        "userName": "sven",
        "roles": ["iniRealm:admin_role"],
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/user",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/user\""
        }, {
            "rel": "up",
            "href": "http://localhost:8080/restful/",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        }, {
            "rel": "urn:org.apache.causeway.restfulobjects:rels/logout",
            "href": "http://localhost:8080/restful/user/logout",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        }],
        "extensions": {}
    }"""
}
