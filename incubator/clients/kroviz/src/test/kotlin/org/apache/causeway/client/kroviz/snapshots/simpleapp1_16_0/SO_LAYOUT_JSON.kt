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

object SO_LAYOUT_JSON: Response() {
    override val url = "http://localhost:8080/restful/domain-types/simple.SimpleObject/layout"
    override val str = """
{
  "row": [
    {
      "cols": [
        {
          "col": {
            "domainObject": {
              "named": null,
              "describedAs": null,
              "plural": null,
              "metadataError": null,
              "link": {
                "rel": "urn:org.restfulobjects:rels/element",
                "method": "GET",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\""
              },
              "bookmarking": "AS_ROOT",
              "cssClass": null,
              "cssClassFa": null,
              "cssClassFaPosition": null,
              "namedEscaped": null
            },
            "metadataError": null,
            "cssClass": null,
            "size": null,
            "id": null,
            "span": 12,
            "unreferencedActions": true,
            "unreferencedCollections": null
          }
        }
      ],
      "metadataError": null,
      "cssClass": null,
      "id": null
    },
    {
      "cols": [
        {
          "col": {
            "domainObject": null,
            "tabGroup": [
              {
                "tab": [
                  {
                    "name": "General",
                    "row": [
                      {
                        "cols": [
                          {
                            "col": {
                              "domainObject": null,
                              "fieldSet": [
                                {
                                  "name": "Name",
                                  "action": [
                                    {
                                      "named": null,
                                      "describedAs": "Deletes this object from the persistent datastore",
                                      "metadataError": null,
                                      "link": {
                                        "rel": "urn:org.restfulobjects:rels/action",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/delete",
                                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                      },
                                      "id": "delete",
                                      "bookmarking": null,
                                      "cssClass": null,
                                      "cssClassFa": null,
                                      "cssClassFaPosition": null,
                                      "hidden": null,
                                      "namedEscaped": null,
                                      "position": "PANEL",
                                      "promptStyle": null
                                    }
                                  ],
                                  "property": [
                                    {
                                      "named": null,
                                      "describedAs": null,
                                      "action": [
                                        {
                                          "named": null,
                                          "describedAs": "Updates the object's name",
                                          "metadataError": null,
                                          "link": {
                                            "rel": "urn:org.restfulobjects:rels/action",
                                            "method": "GET",
                                            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/updateName",
                                            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                          },
                                          "id": "updateName",
                                          "bookmarking": null,
                                          "cssClass": null,
                                          "cssClassFa": null,
                                          "cssClassFaPosition": null,
                                          "hidden": null,
                                          "namedEscaped": null,
                                          "position": "BELOW",
                                          "promptStyle": null
                                        }
                                      ],
                                      "metadataError": null,
                                      "link": {
                                        "rel": "urn:org.restfulobjects:rels/property",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/name",
                                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                      },
                                      "id": "name",
                                      "cssClass": null,
                                      "hidden": null,
                                      "labelPosition": null,
                                      "multiLine": null,
                                      "namedEscaped": true,
                                      "promptStyle": null,
                                      "renderedAsDayBefore": null,
                                      "typicalLength": null,
                                      "unchanging": null
                                    },
                                    {
                                      "named": null,
                                      "describedAs": null,
                                      "metadataError": null,
                                      "link": {
                                        "rel": "urn:org.restfulobjects:rels/property",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/notes",
                                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                      },
                                      "id": "notes",
                                      "cssClass": null,
                                      "hidden": "ALL_TABLES",
                                      "labelPosition": null,
                                      "multiLine": 10,
                                      "namedEscaped": true,
                                      "promptStyle": null,
                                      "renderedAsDayBefore": null,
                                      "typicalLength": null,
                                      "unchanging": null
                                    }
                                  ],
                                  "metadataError": null,
                                  "id": "name",
                                  "unreferencedActions": null,
                                  "unreferencedProperties": null
                                }
                              ],
                              "metadataError": null,
                              "cssClass": null,
                              "size": null,
                              "id": null,
                              "span": 12,
                              "unreferencedActions": null,
                              "unreferencedCollections": null
                            }
                          }
                        ],
                        "metadataError": null,
                        "cssClass": null,
                        "id": null
                      }
                    ],
                    "cssClass": null
                  },
                  {
                    "name": "Metadata",
                    "row": [
                      {
                        "cols": [
                          {
                            "col": {
                              "domainObject": null,
                              "fieldSet": [
                                {
                                  "name": "Metadata",
                                  "property": [
                                    {
                                      "named": null,
                                      "describedAs": null,
                                      "metadataError": null,
                                      "link": {
                                        "rel": "urn:org.restfulobjects:rels/property",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/datanucleusIdLong",
                                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                      },
                                      "id": "datanucleusIdLong",
                                      "cssClass": null,
                                      "hidden": null,
                                      "labelPosition": null,
                                      "multiLine": null,
                                      "namedEscaped": null,
                                      "promptStyle": null,
                                      "renderedAsDayBefore": null,
                                      "typicalLength": null,
                                      "unchanging": null
                                    },
                                    {
                                      "named": null,
                                      "describedAs": null,
                                      "metadataError": null,
                                      "link": {
                                        "rel": "urn:org.restfulobjects:rels/property",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/datanucleusVersionLong",
                                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                      },
                                      "id": "datanucleusVersionLong",
                                      "cssClass": null,
                                      "hidden": null,
                                      "labelPosition": null,
                                      "multiLine": null,
                                      "namedEscaped": null,
                                      "promptStyle": null,
                                      "renderedAsDayBefore": null,
                                      "typicalLength": null,
                                      "unchanging": null
                                    },
                                    {
                                      "named": null,
                                      "describedAs": null,
                                      "metadataError": null,
                                      "link": {
                                        "rel": "urn:org.restfulobjects:rels/property",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/datanucleusVersionTimestamp",
                                        "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                      },
                                      "id": "datanucleusVersionTimestamp",
                                      "cssClass": null,
                                      "hidden": null,
                                      "labelPosition": null,
                                      "multiLine": null,
                                      "namedEscaped": null,
                                      "promptStyle": null,
                                      "renderedAsDayBefore": null,
                                      "typicalLength": null,
                                      "unchanging": null
                                    }
                                  ],
                                  "metadataError": null,
                                  "id": "metadata",
                                  "unreferencedActions": null,
                                  "unreferencedProperties": null
                                }
                              ],
                              "metadataError": null,
                              "cssClass": null,
                              "size": null,
                              "id": null,
                              "span": 12,
                              "unreferencedActions": null,
                              "unreferencedCollections": null
                            }
                          }
                        ],
                        "metadataError": null,
                        "cssClass": null,
                        "id": null
                      }
                    ],
                    "cssClass": null
                  },
                  {
                    "name": "Other",
                    "row": [
                      {
                        "cols": [
                          {
                            "col": {
                              "domainObject": null,
                              "fieldSet": [
                                {
                                  "name": "Other",
                                  "metadataError": null,
                                  "id": "other",
                                  "unreferencedActions": null,
                                  "unreferencedProperties": true
                                }
                              ],
                              "metadataError": null,
                              "cssClass": null,
                              "size": null,
                              "id": null,
                              "span": 12,
                              "unreferencedActions": null,
                              "unreferencedCollections": null
                            }
                          }
                        ],
                        "metadataError": null,
                        "cssClass": null,
                        "id": null
                      }
                    ],
                    "cssClass": null
                  }
                ],
                "metadataError": null,
                "cssClass": null,
                "unreferencedCollections": null
              },
              {
                "metadataError": null,
                "cssClass": null,
                "unreferencedCollections": null
              }
            ],
            "metadataError": null,
            "cssClass": null,
            "size": null,
            "id": null,
            "span": 6,
            "unreferencedActions": null,
            "unreferencedCollections": null
          }
        },
        {
          "col": {
            "domainObject": null,
            "tabGroup": [
              {
                "metadataError": null,
                "cssClass": null,
                "unreferencedCollections": true
              }
            ],
            "metadataError": null,
            "cssClass": null,
            "size": null,
            "id": null,
            "span": 6,
            "unreferencedActions": null,
            "unreferencedCollections": null
          }
        }
      ],
      "metadataError": null,
      "cssClass": null,
      "id": null
    }
  ],
  "cssClass": null
}
"""
}
