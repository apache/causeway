package org.ro.snapshots.simpleapp1_16_0

import org.ro.snapshots.Response

object SO_LIST_ALL_OBJECTS : Response() {
    override val url = "http://localhost:8080/restful/objects/isisApplib.DomainObjectList/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8bGlzdCB4bWxuczpjb209Imh0dHA6Ly9pc2lzLmFwYWNoZS5vcmcvc2NoZW1hL2NvbW1vbiI-CiAgICA8dGl0bGU-MTAgU2ltcGxlIE9iamVjdHM8L3RpdGxlPgogICAgPGFjdGlvbk93bmluZ1R5cGU-c2ltcGxlLlNpbXBsZU9iamVjdE1lbnU8L2FjdGlvbk93bmluZ1R5cGU-CiAgICA8YWN0aW9uSWQ-bGlzdEFsbDwvYWN0aW9uSWQ-CiAgICA8YWN0aW9uQXJndW1lbnRzPjwvYWN0aW9uQXJndW1lbnRzPgogICAgPGVsZW1lbnRPYmplY3RUeXBlPnNpbXBsZS5TaW1wbGVPYmplY3Q8L2VsZW1lbnRPYmplY3RUeXBlPgogICAgPG9iamVjdHM-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYwIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYxIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYyIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYzIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY0Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY1Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY2Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY3Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY4Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY5Ii8-CiAgICA8L29iamVjdHM-CjwvbGlzdD4K/collections/objects"
    override val str = """
        {
        "id": "objects",
        "memberType": "collection",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/isisApplib.DomainObjectList/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8bGlzdCB4bWxuczpjb209Imh0dHA6Ly9pc2lzLmFwYWNoZS5vcmcvc2NoZW1hL2NvbW1vbiI-CiAgICA8dGl0bGU-MTAgU2ltcGxlIE9iamVjdHM8L3RpdGxlPgogICAgPGFjdGlvbk93bmluZ1R5cGU-c2ltcGxlLlNpbXBsZU9iamVjdE1lbnU8L2FjdGlvbk93bmluZ1R5cGU-CiAgICA8YWN0aW9uSWQ-bGlzdEFsbDwvYWN0aW9uSWQ-CiAgICA8YWN0aW9uQXJndW1lbnRzPjwvYWN0aW9uQXJndW1lbnRzPgogICAgPGVsZW1lbnRPYmplY3RUeXBlPnNpbXBsZS5TaW1wbGVPYmplY3Q8L2VsZW1lbnRPYmplY3RUeXBlPgogICAgPG9iamVjdHM-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYwIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYxIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYyIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYzIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY0Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY1Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY2Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY3Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY4Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY5Ii8-CiAgICA8L29iamVjdHM-CjwvbGlzdD4K/collections/objects",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-collection\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/objects/isisApplib.DomainObjectList/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiIHN0YW5kYWxvbmU9InllcyI_Pgo8bGlzdCB4bWxuczpjb209Imh0dHA6Ly9pc2lzLmFwYWNoZS5vcmcvc2NoZW1hL2NvbW1vbiI-CiAgICA8dGl0bGU-MTAgU2ltcGxlIE9iamVjdHM8L3RpdGxlPgogICAgPGFjdGlvbk93bmluZ1R5cGU-c2ltcGxlLlNpbXBsZU9iamVjdE1lbnU8L2FjdGlvbk93bmluZ1R5cGU-CiAgICA8YWN0aW9uSWQ-bGlzdEFsbDwvYWN0aW9uSWQ-CiAgICA8YWN0aW9uQXJndW1lbnRzPjwvYWN0aW9uQXJndW1lbnRzPgogICAgPGVsZW1lbnRPYmplY3RUeXBlPnNpbXBsZS5TaW1wbGVPYmplY3Q8L2VsZW1lbnRPYmplY3RUeXBlPgogICAgPG9iamVjdHM-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYwIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYxIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYyIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjYzIi8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY0Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY1Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY2Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY3Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY4Ii8-CiAgICAgICAgPGNvbTpvaWQgdHlwZT0ic2ltcGxlLlNpbXBsZU9iamVjdCIgaWQ9IjY5Ii8-CiAgICA8L29iamVjdHM-CjwvbGlzdD4K",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "10 Simple Objects"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.DomainObjectList/collections/objects",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/collection-description\""
            }
        ],
        "extensions": {
            "collectionSemantics": "list"
        },
        "value": [
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/60",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Foo"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/61",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Bar"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/62",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Baz"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/63",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Frodo"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/64",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Froyo"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/65",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Fizz"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/66",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Bip"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/67",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Bop"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/68",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Bang"
            },
            {
                "rel": "urn:org.restfulobjects:rels/value",
                "href": "http://localhost:8080/restful/objects/simple.SimpleObject/69",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Object: Boo"
            }
        ],
        "disabledReason": "Immutable"

    }"""
}
