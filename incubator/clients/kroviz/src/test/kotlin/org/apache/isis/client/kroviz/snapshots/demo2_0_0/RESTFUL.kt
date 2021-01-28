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

object RESTFUL : Response(){
    override val url = "http://localhost:8080/restful/"
    override val str = """
{
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/homepage\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/user",
    "href" : "http://localhost:8080/restful/user",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/user\""
  }, {
    "rel" : "urn:org.apache.isis.restfulobjects:rels/menuBars",
    "href" : "http://localhost:8080/restful/menuBars",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/layout-menubars\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/services",
    "href" : "http://localhost:8080/restful/services",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/list\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/version",
    "href" : "http://localhost:8080/restful/version",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/version\""
  }, {
    "rel" : "urn:org.restfulobjects:rels/domain-types",
    "href" : "http://localhost:8080/restful/domain-types",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/type-list\""
  } ],
  "extensions" : { }
}        
    """
}
