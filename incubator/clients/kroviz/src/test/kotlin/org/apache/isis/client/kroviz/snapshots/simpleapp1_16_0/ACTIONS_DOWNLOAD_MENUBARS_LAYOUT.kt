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

object ACTIONS_DOWNLOAD_MENUBARS_LAYOUT : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadLayouts"
    override val str = """{
  "id" : "downloadMenuBarsLayout",
  "memberType" : "action",
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadMenuBarsLayout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
  }, {
    "rel" : "up",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Prototyping"
  }, {
    "rel" : "urn:org.restfulobjects:rels/invoke;action=\"downloadMenuBarsLayout\"",
    "href" : "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu/actions/downloadMenuBarsLayout/invoke",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"",
    "arguments" : {
      "fileName" : {
        "value" : null
      },
      "type" : {
        "value" : null
      }
    }
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/isisApplib.LayoutServiceMenu/actions/downloadMenuBarsLayout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
  } ],
  "extensions" : {
    "actionType" : "prototype",
    "actionSemantics" : "safe"
  },
  "parameters" : {
    "fileName" : {
      "num" : 0,
      "id" : "fileName",
      "name" : "File name",
      "description" : "",
      "default" : "menubars.layout.xml"
    },
    "type" : {
      "num" : 1,
      "id" : "type",
      "name" : "Type",
      "description" : "",
      "choices" : [ "Default", "Fallback" ],
      "default" : "Default"
    }
  }
}"""
}
