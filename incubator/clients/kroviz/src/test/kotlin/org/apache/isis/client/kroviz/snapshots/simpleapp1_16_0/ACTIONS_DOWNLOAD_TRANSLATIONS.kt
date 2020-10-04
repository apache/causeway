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
package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

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
