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

object ACTIONS_STRINGS_INVOKE : Response() {
    override val url = "http://localhost:8080/restful/objects/demo.JavaLangTypesMenu/1/actions/strings/invoke"
    override val str = """
{
  "links": [
    {
      "rel": "self",
      "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "title": "String data type"
    },
    {
      "rel": "describedby",
      "href": "https://demo-wicket.isis.incode.work/restful/domain-types/demo.JavaLangStrings",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
    },
    {
      "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
      "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/object-layout",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
    },
    {
      "rel": "urn:org.apache.isis.restfulobjects:rels/object-icon",
      "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/image",
      "method": "GET",
      "type": "image/png"
    },
    {
      "rel": "urn:org.restfulobjects:rels/update",
      "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings:PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
      "method": "PUT",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
      "arguments": {}
    }
  ],
  "extensions": {
    "oid": "demo.JavaLangStrings:PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
    "isService": false,
    "isPersistent": true
  },
  "title": "String data type",
  "domainType": "demo.JavaLangStrings",
  "instanceId": "PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=",
  "members": {
    "description": {
      "id": "description",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"description\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/properties/description",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "<div class=\"paragraph\">\n<p>The framework has built-in support for the <code>String</code> data type.</p>\n</div>\n<div class=\"paragraph\">\n<p>From here you can:</p>\n</div>\n<div class=\"ulist\">\n<ul>\n<li>\n<p>navigate to an entity that uses the <code>String</code> datatype</p>\n</li>\n<li>\n<p>open a view model that uses the <code>String</code> datatype</p>\n</li>\n</ul>\n</div>\n<div class=\"paragraph\">\n<p>Some properties on these domain objects are mandatory, some optional.</p>\n</div>\n<div class=\"sect1\">\n<h2 id=\"_common_interfaces\">Common interfaces</h2>\n<div class=\"sectionbody\">\n<div class=\"paragraph\">\n<p>The entity and view model types both implement some common interfaces.</p>\n</div>\n<div class=\"sect2\">\n<h3 id=\"_javalangstringholder\">JavaLangStringHolder</h3>\n<div class=\"paragraph\">\n<p>The <code>JavaLangStringHolder</code> interface is used to contribute a number of mixins to both types:</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre class=\"highlight\"><code class=\"language-java\" data-lang=\"java\">public interface JavaLangStringHolder {\n\n    String getReadOnlyProperty();\n    void setReadOnlyProperty(String c);\n\n    String getReadWriteProperty();\n    void setReadWriteProperty(String c);\n\n    String getReadOnlyOptionalProperty();\n    void setReadOnlyOptionalProperty(String c);\n\n    String getReadWriteOptionalProperty();\n    void setReadWriteOptionalProperty(String c);\n\n}</code></pre>\n</div>\n</div>\n</div>\n<div class=\"sect2\">\n<h3 id=\"_javalangstringholder2\">JavaLangStringHolder2</h3>\n<div class=\"paragraph\">\n<p>The <code>JavaLangStringHolder2</code> interface is used to demonstrate support for label positions using <code>@PropertyLayout(labelPosition=&#8230;&#8203;)</code>.</p>\n</div>\n<div class=\"paragraph\">\n<p>Further details, along with the effect of this annotation, can be seen on the entity and view model object pages.</p>\n</div>\n</div>\n</div>\n</div>",
      "format": "string",
      "extensions": {
        "x-isis-format": "string"
      },
      "disabledReason": "Contributed property"
    },
    "objectType": {
      "id": "objectType",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"objectType\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/properties/objectType",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "demo.JavaLangStrings",
      "extensions": {
        "x-isis-format": "string"
      },
      "disabledReason": "Contributed property"
    },
    "objectIdentifier": {
      "id": "objectIdentifier",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"objectIdentifier\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/properties/objectIdentifier",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "»1a9012b0",
      "extensions": {
        "x-isis-format": "string"
      },
      "disabledReason": "Contributed property"
    },
    "sources": {
      "id": "sources",
      "memberType": "property",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;property=\"sources\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/properties/sources",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
      ],
      "value": "<div class=\"paragraph\">\n<p><a href=\"https://github.com/apache/isis/tree/master/examples/demo/domain/src/main/java/demoapp/dom/types/javalang/strings\">Sources</a> for this demo</p>\n</div>",
      "format": "string",
      "extensions": {
        "x-isis-format": "string"
      },
      "disabledReason": "Contributed property"
    },
    "entities": {
      "id": "entities",
      "memberType": "collection",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;collection=\"entities\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/collections/entities",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
        }
      ]
    },
    "openViewModel": {
      "id": "openViewModel",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"openViewModel\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/openViewModel",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "clearHints": {
      "id": "clearHints",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/clearHints",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "downloadMetamodelXml": {
      "id": "downloadMetamodelXml",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"downloadMetamodelXml\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/downloadMetamodelXml",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "rebuildMetamodel": {
      "id": "rebuildMetamodel",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"rebuildMetamodel\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/rebuildMetamodel",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "downloadLayoutXml": {
      "id": "downloadLayoutXml",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"downloadLayoutXml\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/downloadLayoutXml",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "openRestApi": {
      "id": "openRestApi",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"openRestApi\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/openRestApi",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "inspectMetamodel": {
      "id": "inspectMetamodel",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"inspectMetamodel\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/inspectMetamodel",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    },
    "recentCommands": {
      "id": "recentCommands",
      "memberType": "action",
      "links": [
        {
          "rel": "urn:org.restfulobjects:rels/details;action=\"recentCommands\"",
          "href": "https://demo-wicket.isis.incode.work/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/recentCommands",
          "method": "GET",
          "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
      ]
    }
  }
}
"""
}
