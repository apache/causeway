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

object ACTIONS_WHEREINTHEWORLD_INVOKE : Response() {
    override val url = "http://localhost:8080/restful/objects/demo.WhereInTheWorldMenu/1/actions/whereInTheWorld/invoke?address=Malvern,%20UK&zoom=14"
    override val str = """
{
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "Malvern, UK"
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/demo.CustomUiVm",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.apache.isis.restfulobjects:rels/object-layout",
    "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/object-layout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
  }, {
    "rel" : "urn:org.apache.isis.restfulobjects:rels/object-icon",
    "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/image",
    "method" : "GET",
    "type" : "image/png"
  }, {
    "rel" : "urn:org.restfulobjects:rels/update",
    "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm:PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K",
    "method" : "PUT",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "arguments" : { }
  } ],
  "extensions" : {
    "oid" : "demo.CustomUiVm:PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K",
    "isService" : false,
    "isPersistent" : true
  },
  "title" : "Malvern, UK",
  "domainType" : "demo.CustomUiVm",
  "instanceId" : "PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K",
  "members" : {
    "address" : {
      "id" : "address",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"address\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/properties/address",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Malvern, UK",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Disabled"
    },
    "latitude" : {
      "id" : "latitude",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"latitude\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/properties/latitude",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "52.198944",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Disabled"
    },
    "longitude" : {
      "id" : "longitude",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"longitude\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/properties/longitude",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "-2.2426571079130886",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Disabled"
    },
    "zoom" : {
      "id" : "zoom",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"zoom\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/properties/zoom",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : 14,
      "format" : "int",
      "extensions" : {
        "x-isis-format" : "int"
      },
      "disabledReason" : "Disabled"
    },
    "clearHints" : {
      "id" : "clearHints",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/clearHints",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "inspectMetamodel" : {
      "id" : "inspectMetamodel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"inspectMetamodel\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/inspectMetamodel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadLayoutXml" : {
      "id" : "downloadLayoutXml",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadLayoutXml\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/downloadLayoutXml",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "recentCommands" : {
      "id" : "recentCommands",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"recentCommands\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/recentCommands",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "rebuildMetamodel" : {
      "id" : "rebuildMetamodel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"rebuildMetamodel\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/rebuildMetamodel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadMetamodelXml" : {
      "id" : "downloadMetamodelXml",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadMetamodelXml\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/downloadMetamodelXml",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "openRestApi" : {
      "id" : "openRestApi",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"openRestApi\"",
        "href" : "http://localhost:8080/restful/objects/demo.CustomUiVm/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPGRlbW8uQ3VzdG9tVWlWbT4KICAgIDxhZGRyZXNzPk1hbHZlcm4sIFVLPC9hZGRyZXNzPgogICAgPGxhdGl0dWRlPjUyLjE5ODk0NDwvbGF0aXR1ZGU-CiAgICA8bG9uZ2l0dWRlPi0yLjI0MjY1NzEwNzkxMzA4ODY8L2xvbmdpdHVkZT4KICAgIDx6b29tPjE0PC96b29tPgo8L2RlbW8uQ3VzdG9tVWlWbT4K/actions/openRestApi",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    }
  }
}
"""
}
