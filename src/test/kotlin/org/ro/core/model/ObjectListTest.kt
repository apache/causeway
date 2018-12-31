package org.ro.core.model

import kotlinx.serialization.json.JsonObject
import org.ro.layout.Layout
import org.ro.to.TObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ObjectListTest {

    @Test
    fun testListAllInvoke() {
        val js0 = JSON.parse<JsonObject>(json0)
        val ro0 = TObject(js0)
        ro0.addMembersAsProperties()

        val js1 = JSON.parse<JsonObject>(json1)
        val ro1 = TObject(js1)
        ro1.addMembersAsProperties()

        val jsl = JSON.parse<JsonObject>(jsonLayout)
        val lyt = Layout(jsl)

        val ol = ObjectList()
        ol.initSize(2)

        val oa0 = ObjectAdapter(ro0)
        ol.add(oa0)

        val oa1 = ObjectAdapter(ro1)
        ol.add(oa1)

        ol.setLayout(lyt)
        assertEquals(2, ol.length())

        assertNotNull(ol.getLayout()!!.properties)
    }

    // http://localhost:8080/restful/objects/simple.SimpleObject/0
    private val json0 = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Foo"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/object-layout",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Foo"
        },
        {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }
        ],
        "extensions": {
        "oid": "simple.SimpleObject:0",
        "isService": false,
        "isPersistent": true
    },
        "title": "Object: Foo",
        "domainType": "simple.SimpleObject",
        "instanceId": "0",
        "members": {
        "name": {
        "id": "name",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"name\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/name",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": "Foo",
        "extensions": {
        "x-isis-format": "string"
    },
        "disabledReason": "Immutable"
    },
        "notes": {
        "id": "notes",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": null,
        "extensions": {
        "x-isis-format": "string"
    }
    },
        "datanucleusIdLong": {
        "id": "datanucleusIdLong",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"datanucleusIdLong\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/datanucleusIdLong",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": 0,
        "format": "int",
        "extensions": {
        "x-isis-format": "long"
    },
        "disabledReason": "Contributed property"
    },
        "datanucleusVersionTimestamp": {
        "id": "datanucleusVersionTimestamp",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"datanucleusVersionTimestamp\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/properties/datanucleusVersionTimestamp",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": 1518074491759,
        "format": "utc-millisec",
        "extensions": {
        "x-isis-format": "javasqltimestamp"
    },
        "disabledReason": "Contributed property"
    },
        "downloadJdoMetadata": {
        "id": "downloadJdoMetadata",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadJdoMetadata\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/downloadJdoMetadata",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "rebuildMetamodel": {
        "id": "rebuildMetamodel",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"rebuildMetamodel\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/rebuildMetamodel",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "openRestApi": {
        "id": "openRestApi",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"openRestApi\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/openRestApi",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "downloadLayoutXml": {
        "id": "downloadLayoutXml",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadLayoutXml\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/downloadLayoutXml",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "delete": {
        "id": "delete",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"delete\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/delete",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "updateName": {
        "id": "updateName",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"updateName\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/updateName",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "clearHints": {
        "id": "clearHints",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"clearHints\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/0/actions/clearHints",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""

    // http://localhost:8080/restful/objects/simple.SimpleObject/1
    private val json1 = """{
        "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Bar"
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        },
        {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/object-layout",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Bar"
        },
        {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }
        ],
        "extensions": {
        "oid": "simple.SimpleObject:1",
        "isService": false,
        "isPersistent": true
    },
        "title": "Object: Bar",
        "domainType": "simple.SimpleObject",
        "instanceId": "1",
        "members": {
        "name": {
        "id": "name",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"name\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/name",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": "Bar",
        "extensions": {
        "x-isis-format": "string"
    },
        "disabledReason": "Immutable"
    },
        "notes": {
        "id": "notes",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": null,
        "extensions": {
        "x-isis-format": "string"
    }
    },
        "datanucleusIdLong": {
        "id": "datanucleusIdLong",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"datanucleusIdLong\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/datanucleusIdLong",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": 1,
        "format": "int",
        "extensions": {
        "x-isis-format": "long"
    },
        "disabledReason": "Contributed property"
    },
        "datanucleusVersionTimestamp": {
        "id": "datanucleusVersionTimestamp",
        "memberType": "property",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsproperty=\"datanucleusVersionTimestamp\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/properties/datanucleusVersionTimestamp",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }
        ],
        "value": 1518074491868,
        "format": "utc-millisec",
        "extensions": {
        "x-isis-format": "javasqltimestamp"
    },
        "disabledReason": "Contributed property"
    },
        "downloadJdoMetadata": {
        "id": "downloadJdoMetadata",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadJdoMetadata\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/downloadJdoMetadata",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "rebuildMetamodel": {
        "id": "rebuildMetamodel",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"rebuildMetamodel\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/rebuildMetamodel",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "openRestApi": {
        "id": "openRestApi",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"openRestApi\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/openRestApi",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "downloadLayoutXml": {
        "id": "downloadLayoutXml",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadLayoutXml\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/downloadLayoutXml",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "delete": {
        "id": "delete",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"delete\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/delete",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "updateName": {
        "id": "updateName",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"updateName\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/updateName",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    },
        "clearHints": {
        "id": "clearHints",
        "memberType": "action",
        "links": [
        {
            "rel": "urn:org.restfulobjects:rels/detailsaction=\"clearHints\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/1/actions/clearHints",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
        }
        ]
    }
    }
    }"""

    //TODO implement domainObject
    private val jsonLayout = """{
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
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\""
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
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
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
                                            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
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
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
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
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
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
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
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
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
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
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
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
    }"""

}