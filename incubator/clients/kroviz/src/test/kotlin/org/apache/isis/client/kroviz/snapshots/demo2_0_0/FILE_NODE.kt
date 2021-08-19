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

object FILE_NODE : Response() {
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
  "canonicalName": "demoapp.dom.domain.properties.PropertyLayout.navigable.FileNodeVm",
  "members": {
    "description": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/description",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "parent": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/parent",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "path": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/path",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "type": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/type",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "objectType": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/objectType",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "objectIdentifier": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/objectIdentifier",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "sources": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/sources",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "tree": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/properties/tree",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "clearHints": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/clearHints",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "returnsTree": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/returnsTree",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "rebuildMetamodel": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/rebuildMetamodel",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "downloadLayoutXml": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/downloadLayoutXml",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "downloadMetamodelXml": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/downloadMetamodelXml",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "impersonate": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/impersonate",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "stopImpersonating": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/stopImpersonating",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "recentCommands": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/recentCommands",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "openRestApi": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/openRestApi",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "impersonateWithRoles": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/impersonateWithRoles",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "inspectMetamodel": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.FileNode/actions/inspectMetamodel",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    }
  },
  "typeActions": {
    "isSubtypeOf": {
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
    "isSupertypeOf": {
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
  },
  "extensions": {
    "friendlyName": "File Node Vm",
    "pluralName": "File Node Vms",
    "isService": false
  }
}
"""
}
