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

object DEMO_MENUBARS : Response(){
    override val url = "http://localhost:8080/restful/menuBars"
    override val str = """{
    "primary": {
        "menu": [
            {
                "named": "Featured Types",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.FeaturedTypesMenu",
                                "id": "text",
                                "named": "Text",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/text",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "demo.FeaturedTypesMenu",
                                "id": "primitives",
                                "named": "Primitives",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/primitives",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "demo.FeaturedTypesMenu",
                                "id": "temporals",
                                "named": "Temporal Types",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/temporals",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "demo.FeaturedTypesMenu",
                                "id": "blobs",
                                "named": "Blob Type",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.FeaturedTypesMenu/1/actions/blobs",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Tooltips",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.TooltipMenu",
                                "id": "tooltipDemo",
                                "named": "Tooltip Demo",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.TooltipMenu/1/actions/tooltipDemo",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Trees",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.TreeDemoMenu",
                                "id": "fileSystemTree",
                                "named": "File System Tree",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.TreeDemoMenu/1/actions/fileSystemTree",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Actions",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.AssociatedActionMenu",
                                "id": "associatedActions",
                                "named": "Associated Actions",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.AssociatedActionMenu/1/actions/associatedActions",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "demo.AsyncActionMenu",
                                "id": "asyncActions",
                                "named": "Asynchronous Actions",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.AsyncActionMenu/1/actions/asyncActions",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "demo.DependentArgsActionMenu",
                                "id": "dependentArgsActions",
                                "named": "Actions w/ dependent Arguments",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.DependentArgsActionMenu/1/actions/dependentArgsActions",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Events",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.EventsDemoMenu",
                                "id": "eventsDemo",
                                "named": null,
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.EventsDemoMenu/1/actions/eventsDemo",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Error Handling",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.ErrorMenu",
                                "id": "errorHandling",
                                "named": null,
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.ErrorMenu/1/actions/errorHandling",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "JEE/CDI",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "demo.JeeMenu",
                                "id": "jeeInjectDemo",
                                "named": null,
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/demo.JeeMenu/1/actions/jeeInjectDemo",
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
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "tabMenu",
                                "id": "tabDemo",
                                "named": "Tab Demo",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/tabMenu/1/actions/tabDemo",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
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
                                "objectType": "isisExtFixtures.FixtureScripts",
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
                                    "href": "http://localhost:8080/restful/objects/isisExtFixtures.FixtureScripts/1/actions/runFixtureScript",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisExtFixtures.FixtureScripts",
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
                                    "href": "http://localhost:8080/restful/objects/isisExtFixtures.FixtureScripts/1/actions/recreateObjectsAndReturnFirst",
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
                                "id": "downloadMetaModelXml",
                                "named": "Download Meta Model (XML)",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.MetaModelServicesMenu/1/actions/downloadMetaModelXml",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isisApplib.MetaModelServicesMenu",
                                "id": "downloadMetaModelCsv",
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
                                    "href": "http://localhost:8080/restful/objects/isisApplib.MetaModelServicesMenu/1/actions/downloadMetaModelCsv",
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
                            },
                            {
                                "objectType": "isisApplib.H2ManagerMenu",
                                "id": "openH2Console",
                                "named": "H2 Console",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isisApplib.H2ManagerMenu/1/actions/openH2Console",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Security",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "isissecurity.ApplicationPermissionMenu",
                                "id": "findOrphanedPermissions",
                                "named": "Find Orphaned Permissions",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationPermissionMenu/1/actions/findOrphanedPermissions",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationPermissionMenu",
                                "id": "allPermissions",
                                "named": "All Permissions",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationPermissionMenu/1/actions/allPermissions",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isissecurity.ApplicationUserMenu",
                                "id": "findUsers",
                                "named": "Find Users",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUserMenu/1/actions/findUsers",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationUserMenu",
                                "id": "newDelegateUser",
                                "named": "New Delegate User",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUserMenu/1/actions/newDelegateUser",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationUserMenu",
                                "id": "newLocalUser",
                                "named": "New Local User",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUserMenu/1/actions/newLocalUser",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationUserMenu",
                                "id": "allUsers",
                                "named": "All Users",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationUserMenu/1/actions/allUsers",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isissecurity.ApplicationTenancyMenu",
                                "id": "findTenancies",
                                "named": "Find Tenancies",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationTenancyMenu/1/actions/findTenancies",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationTenancyMenu",
                                "id": "newTenancy",
                                "named": "New Tenancy",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationTenancyMenu/1/actions/newTenancy",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationTenancyMenu",
                                "id": "allTenancies",
                                "named": "All Tenancies",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationTenancyMenu/1/actions/allTenancies",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isissecurity.ApplicationFeatureViewModels",
                                "id": "allPackages",
                                "named": "All Packages",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationFeatureViewModels/1/actions/allPackages",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationFeatureViewModels",
                                "id": "allClasses",
                                "named": "All Classes",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationFeatureViewModels/1/actions/allClasses",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationFeatureViewModels",
                                "id": "allActions",
                                "named": "All Actions",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationFeatureViewModels/1/actions/allActions",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationFeatureViewModels",
                                "id": "allProperties",
                                "named": "All Properties",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationFeatureViewModels/1/actions/allProperties",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationFeatureViewModels",
                                "id": "allCollections",
                                "named": "All Collections",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationFeatureViewModels/1/actions/allCollections",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    },
                    {
                        "serviceAction": [
                            {
                                "objectType": "isissecurity.ApplicationRoleMenu",
                                "id": "findRoles",
                                "named": "Find Roles",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationRoleMenu/1/actions/findRoles",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationRoleMenu",
                                "id": "newRole",
                                "named": "New Role",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationRoleMenu/1/actions/newRole",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            },
                            {
                                "objectType": "isissecurity.ApplicationRoleMenu",
                                "id": "allRoles",
                                "named": "All Roles",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.ApplicationRoleMenu/1/actions/allRoles",
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
                "named": "Security",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "isissecurity.MeService",
                                "id": "me",
                                "named": "Me",
                                "namedEscaped": null,
                                "bookmarking": null,
                                "cssClass": null,
                                "cssClassFa": null,
                                "describedAs": null,
                                "metadataError": null,
                                "link": {
                                    "rel": "urn:org.restfulobjects:rels/action",
                                    "method": "GET",
                                    "href": "http://localhost:8080/restful/objects/isissecurity.MeService/1/actions/me",
                                    "type": "application/json;profile=\"urn:org.restfulobjects:repr-types/object-action\""
                                }
                            }
                        ]
                    }
                ],
                "unreferencedActions": null
            },
            {
                "named": "Configuration Menu",
                "cssClassFa": null,
                "section": [
                    {
                        "serviceAction": [
                            {
                                "objectType": "isisApplib.ConfigurationMenu",
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
                                    "href": "http://localhost:8080/restful/objects/isisApplib.ConfigurationMenu/1/actions/configuration",
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
}
"""
}
