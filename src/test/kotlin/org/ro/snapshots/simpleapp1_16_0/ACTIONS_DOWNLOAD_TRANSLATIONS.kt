package org.ro.snapshots.simpleapp1_16_0

import org.ro.snapshots.Response

object ACTIONS_DOWNLOAD_TRANSLATIONS  : Response() {
    override val url = "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu/actions/downloadTranslations"
    override val str = """{
    "id": "downloadTranslations",
    "memberType": "action",
    "links": [
        {
            "rel": "self",
            "href": "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu/actions/downloadTranslations",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
        },
        {
            "rel": "up",
            "href": "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Prototyping"
        },
        {
            "rel": "urn:org.restfulobjects:rels/invoke;action=\"downloadTranslations\"",
            "href": "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu/actions/downloadTranslations/invoke",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\"",
            "arguments": {
                "": {
                    "potFileName": {
                        "value": null
                    }
                }
            }
        },
        {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.TranslationServicePoMenu/actions/downloadTranslations",
            "method": "GET",
            "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/action-description\""
        }
    ],
    "extensions": {
        "actionType": "prototype",
        "actionSemantics": "safe"
    },
    "parameters": {
        ".potFileName": {
            "num": 0,
            "id": ".potFileName",
            "name": ".pot file name",
            "description": "",
            "default": "translations.pot"
        }
    }
}
"""
}
