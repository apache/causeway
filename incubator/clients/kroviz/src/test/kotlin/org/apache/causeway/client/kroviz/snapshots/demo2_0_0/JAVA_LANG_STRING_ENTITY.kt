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

object JAVA_LANG_STRING_ENTITY : Response() {
    override val url = "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity"
    override val str = """
{
  "links": [
    {
      "rel": "self",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
    },
    {
      "rel": "urn:org.apache.causeway.restfulobjects:rels/layout",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/layout",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
    }
  ],
  "canonicalName": "demoapp.dom.types.javalang.strings.jdo.JavaLangStringJdo",
  "members": {
    "mixinProperty": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/mixinProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "description": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/description",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readWriteProperty": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readWriteProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readOnlyPropertyDerivedLabelPositionLeft": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readOnlyPropertyDerivedLabelPositionLeft",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readOnlyPropertyDerivedLabelPositionTop": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readOnlyPropertyDerivedLabelPositionTop",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readOnlyPropertyDerivedLabelPositionRight": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readOnlyPropertyDerivedLabelPositionRight",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readOnlyPropertyDerivedLabelPositionNone": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readOnlyPropertyDerivedLabelPositionNone",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "logicalTypeName": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/logicalTypeName",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "objectIdentifier": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/objectIdentifier",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "datanucleusVersionLong": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/datanucleusVersionLong",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "datanucleusVersionTimestamp": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/datanucleusVersionTimestamp",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readOnlyOptionalProperty": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readOnlyOptionalProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readWriteOptionalProperty": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readWriteOptionalProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "readOnlyProperty": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/readOnlyProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "sources": {
      "rel": "urn:org.restfulobjects:rels/property",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/properties/sources",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/property-description\""
    },
    "actionReturningCollection": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/actionReturningCollection",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "updateReadOnlyOptionalProperty": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/updateReadOnlyOptionalProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "clearHints": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/clearHints",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "updateReadOnlyProperty": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/updateReadOnlyProperty",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "updateReadOnlyPropertyWithChoices": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/updateReadOnlyPropertyWithChoices",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "actionReturning": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/actionReturning",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "inspectMetamodel": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/inspectMetamodel",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "stopImpersonating": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/stopImpersonating",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "downloadLayoutXml": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/downloadLayoutXml",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "impersonateWithRoles": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/impersonateWithRoles",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "downloadMetamodelXml": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/downloadMetamodelXml",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "downloadJdoMetadata": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/downloadJdoMetadata",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "impersonate": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/impersonate",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "recentCommands": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/recentCommands",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "openRestApi": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/openRestApi",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    },
    "rebuildMetamodel": {
      "rel": "urn:org.restfulobjects:rels/action",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/actions/rebuildMetamodel",
      "method": "GET",
      "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
    }
  },
  "typeActions": {
    "isSubtypeOf": {
      "rel": "urn:org.restfulobjects:rels/invoke;typeaction=\"isSubtypeOf\"",
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/type-actions/isSubtypeOf/invoke",
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
      "href": "http://localhost:8080/restful/domain-types/demo.JavaLangStringEntity/type-actions/isSupertypeOf/invoke",
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
    "friendlyName": "Java Lang String Jdo",
    "pluralName": "Java Lang String Jdos",
    "isService": false
  }
}
"""
}

