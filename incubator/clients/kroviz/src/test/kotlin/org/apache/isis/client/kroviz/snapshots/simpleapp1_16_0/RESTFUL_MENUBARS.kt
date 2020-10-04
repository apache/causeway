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

object RESTFUL_MENUBARS : Response(){
    override val url = "http://localhost:8080/restful/menuBars"
    override val str = """{
    "primary": {
        "menu": [
            {
                "named": "Simple Objects",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "simple.SimpleObjectMenu",
                                "id": "create",
                                "named": "Create",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/create",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "simple.SimpleObjectMenu",
                                "id": "findByName",
                                "named": "Find By Name",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/findByName",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "simple.SimpleObjectMenu",
                                "id": "listAll",
                                "named": "List All",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/listAll",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Other",
                "cssClassFa": null,
                "unreferencedActions": true
            }
        ]
    },
    "secondary": {
        "menu": [
            {
                "named": "Prototyping",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.FixtureScriptsDefault",
                                "id": "runFixtureScript",
                                "named": "Run Fixture Script",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureScriptsDefault/1/actions/runFixtureScript",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.FixtureScriptsDefault",
                                "id": "runFixtureScriptWithAutoComplete",
                                "named": "Run Fixture Script",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureScriptsDefault/1/actions/runFixtureScriptWithAutoComplete",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.FixtureScriptsDefault",
                                "id": "recreateObjectsAndReturnFirst",
                                "named": "Recreate Objects And Return First",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureScriptsDefault/1/actions/recreateObjectsAndReturnFirst",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.LayoutServiceMenu",
                                "id": "downloadLayouts",
                                "named": "Download Object Layouts (ZIP)",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.LayoutServiceMenu/1/actions/downloadLayouts",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.LayoutServiceMenu",
                                "id": "downloadMenuBarsLayout",
                                "named": "Download Menu Bars Layout (XML)",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.LayoutServiceMenu/1/actions/downloadMenuBarsLayout",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.MetaModelServicesMenu",
                                "id": "downloadMetaModel",
                                "named": "Download Meta Model (CSV)",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.MetaModelServicesMenu/1/actions/downloadMetaModel",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.SwaggerServiceMenu",
                                "id": "openSwaggerUi",
                                "named": "Open Swagger Ui",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.SwaggerServiceMenu/1/actions/openSwaggerUi",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.SwaggerServiceMenu",
                                "id": "openRestApi",
                                "named": "Open Rest Api",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.SwaggerServiceMenu/1/actions/openRestApi",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.SwaggerServiceMenu",
                                "id": "downloadSwaggerSchemaDefinition",
                                "named": "Download Swagger Schema Definition",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.SwaggerServiceMenu/1/actions/downloadSwaggerSchemaDefinition",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.TranslationServicePoMenu",
                                "id": "downloadTranslations",
                                "named": "Download Translations",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/downloadTranslations",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.TranslationServicePoMenu",
                                "id": "resetTranslationCache",
                                "named": "Clear translation cache",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/resetTranslationCache",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.TranslationServicePoMenu",
                                "id": "switchToReadingTranslations",
                                "named": "Switch To Reading Translations",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/switchToReadingTranslations",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.TranslationServicePoMenu",
                                "id": "switchToWritingTranslations",
                                "named": "Switch To Writing Translations",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/switchToWritingTranslations",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.HsqlDbManagerMenu",
                                "id": "hsqlDbManager",
                                "named": "HSQL DB Manager",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.HsqlDbManagerMenu/1/actions/hsqlDbManager",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            }
        ]
    },
    "tertiary": {
        "menu": [
            {
                "named": "Configuration Service Menu",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.ConfigurationServiceMenu",
                                "id": "configuration",
                                "named": "Configuration",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.ConfigurationServiceMenu/1/actions/configuration",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            }
        ]
    },
    "metadataError": null
}"""
}
