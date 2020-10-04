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

object RESTFUL_VERSION : Response(){
    override val url = "http://localhost:8080/restful/version"
    override val str = """ 
            {
        "links" : [ {
            "rel" : "self",
            "href" : "http://localhost:8080/restful/version",
            "method" : "GET",
            "type" : "application/jsonprofile=\"urn:org.restfulobjects:repr-types/version\""
        }, {
            "rel" : "up",
            "href" : "http://localhost:8080/restful/",
            "method" : "GET",
            "type" : "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        } ],
        "specVersion" : "1.0.0",
        "implVersion" : "UNKNOWN",
        "optionalCapabilities" : {
            "blobsClobs" : "yes",
            "deleteObjects" : "yes",
            "domainModel" : "formal",
            "validateOnly" : "yes",
            "protoPersistentObjects" : "yes"
        },
        "extensions" : { }
    }"""
}
