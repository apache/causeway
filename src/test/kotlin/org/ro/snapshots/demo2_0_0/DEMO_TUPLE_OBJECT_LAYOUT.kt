package org.ro.snapshots.demo2_0_0

import org.ro.snapshots.Response

object DEMO_TUPLE_OBJECT_LAYOUT : Response() {
    override val url = "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/object-layout"
    override val str = """{
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
                "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=",
                "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\""
              },
              "bookmarking": "AS_ROOT",
              "cssClass": null,
              "cssClassFa": null,
              "cssClassFaPosition": null,
              "namedEscaped": null
            },
            "action": [
              {
                "named": null,
                "describedAs": null,
                "metadataError": null,
                "link": {
                  "rel": "urn:org.restfulobjects:rels/action",
                  "method": "GET",
                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/actions/clearHints",
                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                },
                "id": "clearHints",
                "bookmarking": null,
                "cssClass": null,
                "cssClassFa": null,
                "cssClassFaPosition": null,
                "hidden": null,
                "namedEscaped": null,
                "position": null,
                "promptStyle": null,
                "redirect": null
              }
            ],
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
            "row": [
              {
                "cols": [
                  {
                    "col": {
                      "domainObject": null,
                      "tabGroup": [
                        {
                          "tab": [
                            {
                              "name": "Identity",
                              "row": [
                                {
                                  "cols": [
                                    {
                                      "col": {
                                        "domainObject": null,
                                        "fieldSet": [
                                          {
                                            "name": "Identity",
                                            "metadataError": null,
                                            "id": "identity",
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
                                            "property": [
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/property",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/properties/description",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                                },
                                                "id": "description",
                                                "cssClass": null,
                                                "hidden": null,
                                                "labelPosition": null,
                                                "multiLine": null,
                                                "namedEscaped": null,
                                                "promptStyle": null,
                                                "renderDay": null,
                                                "typicalLength": null,
                                                "repainting": null,
                                                "unchanging": null
                                              }
                                            ],
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
                                            "action": [
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/action",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/actions/downloadLayoutXml",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                                },
                                                "id": "downloadLayoutXml",
                                                "bookmarking": null,
                                                "cssClass": null,
                                                "cssClassFa": null,
                                                "cssClassFaPosition": null,
                                                "hidden": null,
                                                "namedEscaped": null,
                                                "position": "PANEL_DROPDOWN",
                                                "promptStyle": null,
                                                "redirect": null
                                              },
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/action",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/actions/downloadMetaModelXml",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                                },
                                                "id": "downloadMetaModelXml",
                                                "bookmarking": null,
                                                "cssClass": null,
                                                "cssClassFa": null,
                                                "cssClassFaPosition": null,
                                                "hidden": null,
                                                "namedEscaped": null,
                                                "position": "PANEL_DROPDOWN",
                                                "promptStyle": null,
                                                "redirect": null
                                              },
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/action",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/actions/openRestApi",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                                },
                                                "id": "openRestApi",
                                                "bookmarking": null,
                                                "cssClass": null,
                                                "cssClassFa": null,
                                                "cssClassFaPosition": null,
                                                "hidden": null,
                                                "namedEscaped": null,
                                                "position": "PANEL_DROPDOWN",
                                                "promptStyle": null,
                                                "redirect": null
                                              },
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/action",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/actions/rebuildMetamodel",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                                },
                                                "id": "rebuildMetamodel",
                                                "bookmarking": null,
                                                "cssClass": null,
                                                "cssClassFa": null,
                                                "cssClassFaPosition": null,
                                                "hidden": null,
                                                "namedEscaped": null,
                                                "position": "PANEL_DROPDOWN",
                                                "promptStyle": null,
                                                "redirect": null
                                              }
                                            ],
                                            "property": [
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/property",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/properties/objectType",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                                },
                                                "id": "objectType",
                                                "cssClass": null,
                                                "hidden": null,
                                                "labelPosition": null,
                                                "multiLine": null,
                                                "namedEscaped": null,
                                                "promptStyle": null,
                                                "renderDay": null,
                                                "typicalLength": null,
                                                "repainting": null,
                                                "unchanging": null
                                              },
                                              {
                                                "named": null,
                                                "describedAs": null,
                                                "metadataError": null,
                                                "link": {
                                                  "rel": "urn:org.restfulobjects:rels/property",
                                                  "method": "GET",
                                                  "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/properties/objectIdentifier",
                                                  "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-property\""
                                                },
                                                "id": "objectIdentifier",
                                                "cssClass": null,
                                                "hidden": null,
                                                "labelPosition": null,
                                                "multiLine": null,
                                                "namedEscaped": null,
                                                "promptStyle": null,
                                                "renderDay": null,
                                                "typicalLength": null,
                                                "repainting": null,
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
                            }
                          ],
                          "metadataError": null,
                          "cssClass": null,
                          "unreferencedCollections": null,
                          "collapseIfOne": null
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
              },
              {
                "cols": [
                  {
                    "col": {
                      "domainObject": null,
                      "fieldSet": [
                        {
                          "name": "Details",
                          "metadataError": null,
                          "id": "details",
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
            "metadataError": null,
            "cssClass": null,
            "size": null,
            "id": null,
            "span": 4,
            "unreferencedActions": null,
            "unreferencedCollections": null
          }
        },
        {
          "col": {
            "domainObject": null,
            "tabGroup": [
              {
                "tab": [
                  {
                    "name": "All Constants",
                    "row": [
                      {
                        "cols": [
                          {
                            "col": {
                              "domainObject": null,
                              "collection": [
                                {
                                  "named": null,
                                  "describedAs": null,
                                  "sortedBy": null,
                                  "metadataError": null,
                                  "link": {
                                    "rel": "urn:org.restfulobjects:rels/collection",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.TupleDemo/AKztAAVzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHg=/collections/allConstants",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-collection\""
                                  },
                                  "id": "allConstants",
                                  "cssClass": null,
                                  "defaultView": "table",
                                  "hidden": null,
                                  "namedEscaped": null,
                                  "paged": null
                                }
                              ],
                              "metadataError": null,
                              "cssClass": null,
                              "size": "MD",
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
                "unreferencedCollections": true,
                "collapseIfOne": null
              }
            ],
            "metadataError": null,
            "cssClass": null,
            "size": null,
            "id": null,
            "span": 8,
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
}"""
}
