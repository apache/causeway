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

object JAVA_LANG_STRING_JDO : Response() {
    override val url = "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1"
    override val str = """
        {
  "links" : [ {
    "rel" : "self",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "title" : "StringJDO entity: Hello"
  }, {
    "rel" : "describedby",
    "href" : "http://localhost:8080/restful/domain-types/demo.JavaLangStringJdo",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/domain-type\""
  }, {
    "rel" : "urn:org.apache.isis.restfulobjects:rels/object-layout",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/object-layout",
    "method" : "GET",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-layout-bs3\""
  }, {
    "rel" : "urn:org.apache.isis.restfulobjects:rels/object-icon",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/image",
    "method" : "GET",
    "type" : "image/png"
  }, {
    "rel" : "urn:org.restfulobjects:rels/update",
    "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo:1",
    "method" : "PUT",
    "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
    "arguments" : { }
  } ],
  "extensions" : {
    "oid" : "demo.JavaLangStringJdo:1",
    "isService" : false,
    "isPersistent" : true
  },
  "title" : "StringJDO entity: Hello",
  "domainType" : "demo.JavaLangStringJdo",
  "instanceId" : "1",
  "members" : {
    "mixinProperty" : {
      "id" : "mixinProperty",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"mixinProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/mixinProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Contributed property"
    },
    "description" : {
      "id" : "description",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"description\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/description",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "<div class=\"paragraph\">\n<p>JDO supports <code>String</code> <a href=\"http://www.datanucleus.org:15080/products/accessplatform_5_2/jdo/mapping.html#_primitive_and_java_lang_types\">out-of-the-box</a>, so no special annotations are required.</p>\n</div>\n<div class=\"listingblock\">\n<div class=\"content\">\n<pre class=\"highlight\"><code class=\"language-java\" data-lang=\"java\">@PersistenceCapable(identityType = IdentityType.DATASTORE, schema = \"demo\")\n@DatastoreIdentity(strategy = IdGeneratorStrategy.IDENTITY, column = \"id\")\n@DomainObject(\n        objectType = \"demo.JavaLangStringJdo\"\n)\npublic class JavaLangStringJdo                                                  <i class=\"conum\" data-value=\"1\"></i><b>(1)</b>\n        implements HasAsciiDocDescription, JavaLangStringHolder2 {\n\n    @Title(prepend = \"StringJDO entity: \")\n    @PropertyLayout(fieldSetId = \"read-only-properties\", sequence = \"1\")\n    @Column(allowsNull = \"false\")                                               <i class=\"conum\" data-value=\"2\"></i><b>(2)</b>\n    @Getter @Setter\n    private String readOnlyProperty;\n\n    @Property(editing = Editing.ENABLED)                                        <i class=\"conum\" data-value=\"3\"></i><b>(3)</b>\n    @PropertyLayout(fieldSetId = \"editable-properties\", sequence = \"1\")\n    @Column(allowsNull = \"false\")\n    @Getter @Setter\n    private String readWriteProperty;\n\n    @Property(optionality = Optionality.OPTIONAL)                               <i class=\"conum\" data-value=\"4\"></i><b>(4)</b>\n    @PropertyLayout(fieldSetId = \"optional-properties\", sequence = \"1\")\n    @Column(allowsNull = \"true\")                                                <i class=\"conum\" data-value=\"5\"></i><b>(5)</b>\n    @Getter @Setter\n    private String readOnlyOptionalProperty;\n\n    @Property(editing = Editing.ENABLED, optionality = Optionality.OPTIONAL)\n    @PropertyLayout(fieldSetId = \"optional-properties\", sequence = \"2\")\n    @Column(allowsNull = \"true\")\n    @Getter @Setter\n    private String readWriteOptionalProperty;\n\n}</code></pre>\n</div>\n</div>\n<div class=\"colist arabic\">\n<table>\n<tr>\n<td><i class=\"conum\" data-value=\"1\"></i><b>1</b></td>\n<td>a no-arg constructor is introduced by JDO enhancer</td>\n</tr>\n<tr>\n<td><i class=\"conum\" data-value=\"2\"></i><b>2</b></td>\n<td>required property as defined to JDO/DataNucleus.\n<div class=\"paragraph\">\n<p>Apache Isis assumes properties are mandatory, so no additional annotation is required.</p>\n</div></td>\n</tr>\n<tr>\n<td><i class=\"conum\" data-value=\"3\"></i><b>3</b></td>\n<td>directly editable property as defined to Apache Isis</td>\n</tr>\n<tr>\n<td><i class=\"conum\" data-value=\"4\"></i><b>4</b></td>\n<td>optional property as defined to Apache Isis</td>\n</tr>\n<tr>\n<td><i class=\"conum\" data-value=\"5\"></i><b>5</b></td>\n<td>optional property as defined to JDO/DataNucleus</td>\n</tr>\n</table>\n</div>\n<div class=\"paragraph\">\n<p>Resource '../JavaLangStrings-common.adoc' not found.</p>\n</div>",
      "format" : "string",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Contributed property"
    },
    "readWriteProperty" : {
      "id" : "readWriteProperty",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readWriteProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readWriteProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      }
    },
    "readOnlyPropertyDerivedLabelPositionLeft" : {
      "id" : "readOnlyPropertyDerivedLabelPositionLeft",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readOnlyPropertyDerivedLabelPositionLeft\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readOnlyPropertyDerivedLabelPositionLeft",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Always disabled"
    },
    "readOnlyPropertyDerivedLabelPositionTop" : {
      "id" : "readOnlyPropertyDerivedLabelPositionTop",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readOnlyPropertyDerivedLabelPositionTop\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readOnlyPropertyDerivedLabelPositionTop",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Always disabled"
    },
    "readOnlyPropertyDerivedLabelPositionRight" : {
      "id" : "readOnlyPropertyDerivedLabelPositionRight",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readOnlyPropertyDerivedLabelPositionRight\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readOnlyPropertyDerivedLabelPositionRight",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Always disabled"
    },
    "readOnlyPropertyDerivedLabelPositionNone" : {
      "id" : "readOnlyPropertyDerivedLabelPositionNone",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readOnlyPropertyDerivedLabelPositionNone\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readOnlyPropertyDerivedLabelPositionNone",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Always disabled"
    },
    "objectType" : {
      "id" : "objectType",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"objectType\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/objectType",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "demo.JavaLangStringJdo",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Contributed property"
    },
    "objectIdentifier" : {
      "id" : "objectIdentifier",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"objectIdentifier\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/objectIdentifier",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "1",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Contributed property"
    },
    "readOnlyOptionalProperty" : {
      "id" : "readOnlyOptionalProperty",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readOnlyOptionalProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readOnlyOptionalProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : null,
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Disabled"
    },
    "readWriteOptionalProperty" : {
      "id" : "readWriteOptionalProperty",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readWriteOptionalProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readWriteOptionalProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : null,
      "extensions" : {
        "x-isis-format" : "string"
      }
    },
    "readOnlyProperty" : {
      "id" : "readOnlyProperty",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"readOnlyProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/readOnlyProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "Hello",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Disabled"
    },
    "sources" : {
      "id" : "sources",
      "memberType" : "property",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;property=\"sources\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/properties/sources",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
      } ],
      "value" : "<div class=\"paragraph\">\n<p><a href=\"https://github.com/apache/isis/tree/master/examples/demo/domain/src/main/java/demoapp/dom/types/javalang/strings/jdo\">Sources</a> for this demo</p>\n</div>",
      "format" : "string",
      "extensions" : {
        "x-isis-format" : "string"
      },
      "disabledReason" : "Contributed property"
    },
    "updateReadOnlyOptionalProperty" : {
      "id" : "updateReadOnlyOptionalProperty",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"updateReadOnlyOptionalProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/updateReadOnlyOptionalProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "updateReadOnlyProperty" : {
      "id" : "updateReadOnlyProperty",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"updateReadOnlyProperty\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/updateReadOnlyProperty",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "updateReadOnlyPropertyWithChoices" : {
      "id" : "updateReadOnlyPropertyWithChoices",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"updateReadOnlyPropertyWithChoices\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/updateReadOnlyPropertyWithChoices",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "actionReturning" : {
      "id" : "actionReturning",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"actionReturning\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/actionReturning",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "actionReturningCollection" : {
      "id" : "actionReturningCollection",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"actionReturningCollection\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/actionReturningCollection",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "clearHints" : {
      "id" : "clearHints",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"clearHints\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/clearHints",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "recentCommands" : {
      "id" : "recentCommands",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"recentCommands\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/recentCommands",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadLayoutXml" : {
      "id" : "downloadLayoutXml",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadLayoutXml\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/downloadLayoutXml",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadJdoMetadata" : {
      "id" : "downloadJdoMetadata",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadJdoMetadata\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/downloadJdoMetadata",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "inspectMetamodel" : {
      "id" : "inspectMetamodel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"inspectMetamodel\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/inspectMetamodel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "downloadMetamodelXml" : {
      "id" : "downloadMetamodelXml",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"downloadMetamodelXml\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/downloadMetamodelXml",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "openRestApi" : {
      "id" : "openRestApi",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"openRestApi\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/openRestApi",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    },
    "rebuildMetamodel" : {
      "id" : "rebuildMetamodel",
      "memberType" : "action",
      "links" : [ {
        "rel" : "urn:org.restfulobjects:rels/details;action=\"rebuildMetamodel\"",
        "href" : "http://localhost:8080/restful/objects/demo.JavaLangStringJdo/1/actions/rebuildMetamodel",
        "method" : "GET",
        "type" : "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
      } ]
    }
  }
}
"""
}

