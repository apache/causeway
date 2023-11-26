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
package org.apache.causeway.client.kroviz.snapshots.demo2_0_0

import org.apache.causeway.client.kroviz.snapshots.Response

object ACTIONS_STRINGS_INVOKE : Response() {
    override val url = "http://localhost:8080/restful/objects/demo.JavaLangTypesMenu/1/actions/strings/invoke"
    override val str = """
{
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "String data type"
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaLangStrings",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.apache.causeway.restfulobjects:rels/object-layout",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/object-layout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
  }, {
    "rel" : "urn:org.apache.causeway.restfulobjects:rels/object-icon",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/object-icon",
    "method" : "GET",
    "type" : "image/*"
  }, {
    "rel" : "urn:org.restfulobjects:rels/update",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings:PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
    "method" : "PUT",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "arguments" : { }
  } ],
  "extensions" : {
    "oid" : "demo.JavaLangStrings:PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
    "isService" : false,
    "isPersistent" : true
  },
  "title" : "String data type",
  "domainType" : "demo.JavaLangStrings",
  "instanceId" : "PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
  "members" : {
    "entities" : {
      "id" : "entities",
      "memberType" : "collection",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;collection=\"entities\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/collections/entities",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
      } ]
    },
    "openViewModel" : {
      "id" : "openViewModel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"openViewModel\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/openViewModel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "clearHints" : {
      "id" : "clearHints",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/clearHints",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadMetamodelXml" : {
      "id" : "downloadMetamodelXml",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadMetamodelXml\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/downloadMetamodelXml",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "recentCommands" : {
      "id" : "recentCommands",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"recentCommands\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/recentCommands",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "rebuildMetamodel" : {
      "id" : "rebuildMetamodel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"rebuildMetamodel\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/rebuildMetamodel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "openRestApi" : {
      "id" : "openRestApi",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"openRestApi\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/openRestApi",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "inspectMetamodel" : {
      "id" : "inspectMetamodel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"inspectMetamodel\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/inspectMetamodel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadLayoutXml" : {
      "id" : "downloadLayoutXml",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadLayoutXml\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/downloadLayoutXml",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    }
  }
}
"""
}
