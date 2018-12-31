package org.ro

object URLS {

    val RESTFUL = """{
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        }, {
            "rel": "urn:org.restfulobjects:rels/user",
            "href": "http://localhost:8080/restful/user",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/user\""
        }, {
            "rel": "urn:org.apache.isis.restfulobjects:rels/menuBars",
            "href": "http://localhost:8080/restful/menuBars",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/layout-menubars\""
        }, {
            "rel": "urn:org.restfulobjects:rels/services",
            "href": "http://localhost:8080/restful/services",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
        }, {
            "rel": "urn:org.restfulobjects:rels/version",
            "href": "http://localhost:8080/restful/version",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/version\""
        }, {
            "rel": "urn:org.restfulobjects:rels/domain-types",
            "href": "http://localhost:8080/restful/domain-types",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/type-list\""
        }],
        "extensions": {}
    }"""

     val RESTFUL_USER = """{
        "userName": "sven",
        "roles": ["iniRealm:admin_role"],
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/user",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/user\""
        }, {
            "rel": "up",
            "href": "http://localhost:8080/restful/",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        }, {
            "rel": "urn:org.apache.isis.restfulobjects:rels/logout",
            "href": "http://localhost:8080/restful/user/logout",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        }],
        "extensions": {}
    }"""

    val RESTFUL_MENUBARS =  """
            <mb3:menuBars xmlns:cpt="http://isis.apache.org/applib/layout/component"
                          xmlns:mb3="http://isis.apache.org/applib/layout/menubars/bootstrap3"
                          xmlns:lnk="http://isis.apache.org/applib/layout/links">
                <mb3:primary>
                    <mb3:menu>
                        <mb3:named>Simple Objects</mb3:named>
                        <mb3:section>
                            <mb3:serviceAction objectType="simple.SimpleObjectMenu" id="create">
                                <cpt:named>Create</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/create
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="simple.SimpleObjectMenu" id="findByName">
                                <cpt:named>Find By Name</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/findByName
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="simple.SimpleObjectMenu" id="listAll">
                                <cpt:named>List All</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/simple.SimpleObjectMenu/1/actions/listAll
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                    </mb3:menu>
                    <mb3:menu unreferencedActions="true">
                        <mb3:named>Other</mb3:named>
                    </mb3:menu>
                </mb3:primary>
                <mb3:secondary>
                    <mb3:menu>
                        <mb3:named>Prototyping</mb3:named>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.FixtureScriptsDefault" id="runFixtureScript">
                                <cpt:named>Run Fixture Script</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.FixtureScriptsDefault/1/actions/runFixtureScript
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.FixtureScriptsDefault"
                                               id="runFixtureScriptWithAutoComplete">
                                <cpt:named>Run Fixture Script</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.FixtureScriptsDefault/1/actions/runFixtureScriptWithAutoComplete
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.FixtureScriptsDefault"
                                               id="recreateObjectsAndReturnFirst">
                                <cpt:named>Recreate Objects And Return First</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.FixtureScriptsDefault/1/actions/recreateObjectsAndReturnFirst
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.LayoutServiceMenu" id="downloadLayouts">
                                <cpt:named>Download Object Layouts (ZIP)</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.LayoutServiceMenu/1/actions/downloadLayouts
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.LayoutServiceMenu" id="downloadMenuBarsLayout">
                                <cpt:named>Download Menu Bars Layout (XML)</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.LayoutServiceMenu/1/actions/downloadMenuBarsLayout
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.MetaModelServicesMenu" id="downloadMetaModel">
                                <cpt:named>Download Meta Model (CSV)</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.MetaModelServicesMenu/1/actions/downloadMetaModel
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.SwaggerServiceMenu" id="openSwaggerUi">
                                <cpt:named>Open Swagger Ui</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.SwaggerServiceMenu/1/actions/openSwaggerUi
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.SwaggerServiceMenu" id="openRestApi">
                                <cpt:named>Open Rest Api</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.SwaggerServiceMenu/1/actions/openRestApi
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.SwaggerServiceMenu"
                                               id="downloadSwaggerSchemaDefinition">
                                <cpt:named>Download Swagger Schema Definition</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.SwaggerServiceMenu/1/actions/downloadSwaggerSchemaDefinition
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.TranslationServicePoMenu"
                                               id="downloadTranslations">
                                <cpt:named>Download Translations</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/downloadTranslations
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.TranslationServicePoMenu"
                                               id="resetTranslationCache">
                                <cpt:named>Clear translation cache</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/resetTranslationCache
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.TranslationServicePoMenu"
                                               id="switchToReadingTranslations">
                                <cpt:named>Switch To Reading Translations</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/switchToReadingTranslations
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                            <mb3:serviceAction objectType="isisApplib.TranslationServicePoMenu"
                                               id="switchToWritingTranslations">
                                <cpt:named>Switch To Writing Translations</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.TranslationServicePoMenu/1/actions/switchToWritingTranslations
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.HsqlDbManagerMenu" id="hsqlDbManager">
                                <cpt:named>HSQL DB Manager</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.HsqlDbManagerMenu/1/actions/hsqlDbManager
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                    </mb3:menu>
                </mb3:secondary>
                <mb3:tertiary>
                    <mb3:menu>
                        <mb3:named>Configuration Service Menu</mb3:named>
                        <mb3:section>
                            <mb3:serviceAction objectType="isisApplib.ConfigurationServiceMenu" id="configuration">
                                <cpt:named>Configuration</cpt:named>
                                <cpt:link>
                                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                                    <lnk:method>GET</lnk:method>
                                    <lnk:href>
                                        http://localhost:8080/restful/objects/isisApplib.ConfigurationServiceMenu/1/actions/configuration
                                    </lnk:href>
                                    <lnk:type>
                                        application/jsonprofile="urn:org.restfulobjects:repr-types/object-action"
                                    </lnk:type>
                                </cpt:link>
                            </mb3:serviceAction>
                        </mb3:section>
                    </mb3:menu>
                </mb3:tertiary>
            </mb3:menuBars>"""
    
    val RESTFUL_SERVICES = """
        {
        "value": [
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"simple.SimpleObjectMenu\"",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.FixtureScriptsDefault\"",
                "href": "http://localhost:8080/restful/services/isisApplib.FixtureScriptsDefault",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.LayoutServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.LayoutServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.MetaModelServicesMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.MetaModelServicesMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.SwaggerServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.SwaggerServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.TranslationServicePoMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.HsqlDbManagerMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.HsqlDbManagerMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Prototyping"
            },
            {
                "rel": "urn:org.restfulobjects:rels/serviceserviceId=\"isisApplib.ConfigurationServiceMenu\"",
                "href": "http://localhost:8080/restful/services/isisApplib.ConfigurationServiceMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Configuration Service Menu"
            }
        ],
        "extensions": {},
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
            }
        ]
    }"""

    val RESTFUL_VERSION = """ 
            {
        "links" : [ {
            "rel" : "self",
            "href" : "http://localhost:8080/restful/version",
            "method" : "GET",
            "type" : "application/jsonprofile=\"urn:org.restfulobjects:repr-types/version\""
        }, {
            "rel" : "up",
            "href" : "http://localhost:8080/restful/",
            "method" : "GET",
            "type" : "application/jsonprofile=\"urn:org.restfulobjects:repr-types/homepage\""
        } ],
        "specVersion" : "1.0.0",
        "implVersion" : "UNKNOWN",
        "optionalCapabilities" : {
            "blobsClobs" : "yes",
            "deleteObjects" : "yes",
            "domainModel" : "formal",
            "validateOnly" : "yes",
            "protoPersistentObjects" : "yes"
        },
        "extensions" : { }
    }"""

    val SO_MENU = """ 
            {
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/layout",
                "method": "GET",
                "type": "application/xmlprofile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/services",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/list\""
            }
        ],
        "extensions": {
            "oid": "simple.SimpleObjectMenu:1",
            "isService": true,
            "isPersistent": true,
            "menuBar": "PRIMARY"
        },
        "title": "Simple Objects",
        "serviceId": "simple.SimpleObjectMenu",
        "members": {
            "listAll": {
                "id": "listAll",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"listAll\"",
                        "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "findByName": {
                "id": "findByName",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"findByName\"",
                        "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/findByName",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            },
            "create": {
                "id": "create",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"create\"",
                        "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/create",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            }
        }
    }"""

    val SO_LIST_ALL = """
        {
        "id": "listAll",
        "memberType": "action",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "Simple Objects"
            },
            {
                "rel": "urn:org.restfulobjects:rels/invokeaction=\"listAll\"",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\"",
                "arguments": {}
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObjectMenu/actions/listAll",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-description\""
            }
        ],
        "extensions": {
            "actionType": "user",
            "actionSemantics": "safe"
        },
        "parameters": {}
    }"""

    val SO_LIST_ALL_INVOKE = """{
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/services/simple.SimpleObjectMenu/actions/listAll/invoke",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/action-result\"",
                "args": {}
            }
        ],
        "resulttype": "list",
        "result": {
            "value": [
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/10",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Foo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/11",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bar"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/12",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Baz"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/13",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Frodo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/14",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Froyo"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/15",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Fizz"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/16",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bip"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/17",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bop"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/18",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Bang"
                },
                {
                    "rel": "urn:org.restfulobjects:rels/element",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/19",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Boo"
                }
            ],
            "links": [
                {
                    "rel": "urn:org.restfulobjects:rels/return-type",
                    "href": "http://localhost:8080/restful/domain-types/java.util.List",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
                }
            ],
            "extensions": {}
        }
    }"""

    val SO_LIST_ALL_OBJECTS = """
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

    val SO_0 = """{
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
                "rel": "urn:org.apache.isis.restfulobjects:rels/layout",
                "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/layout",
                "method": "GET",
                "type": "application/xmlprofile=\"urn:org.restfulobjects:repr-types/layout-bs3\""
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
                "value": 1514897074953,
                "format": "utc-millisec",
                "extensions": {
                    "x-isis-format": "javasqltimestamp"
                },
                "disabledReason": "Contributed property"
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

    val SO_OBJECT_LAYOUT = """{
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

    val FR_OBJECT_LAYOUT = """{
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
                                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=",
                                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\""
                                },
                                "bookmarking": null,
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
                                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/clearHints",
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                                    },
                                    "id": "clearHints",
                                    "bookmarking": null,
                                    "cssClass": null,
                                    "cssClassFa": null,
                                    "cssClassFaPosition": null,
                                    "hidden": null,
                                    "namedEscaped": null,
                                    "position": null,
                                    "promptStyle": null
                                },
                                {
                                    "named": null,
                                    "describedAs": null,
                                    "metadataError": null,
                                    "link": {
                                        "rel": "urn:org.restfulobjects:rels/action",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/downloadLayoutXml",
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                                    },
                                    "id": "downloadLayoutXml",
                                    "bookmarking": null,
                                    "cssClass": null,
                                    "cssClassFa": null,
                                    "cssClassFaPosition": null,
                                    "hidden": null,
                                    "namedEscaped": null,
                                    "position": null,
                                    "promptStyle": null
                                },
                                {
                                    "named": null,
                                    "describedAs": null,
                                    "metadataError": null,
                                    "link": {
                                        "rel": "urn:org.restfulobjects:rels/action",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/rebuildMetamodel",
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                                    },
                                    "id": "rebuildMetamodel",
                                    "bookmarking": null,
                                    "cssClass": null,
                                    "cssClassFa": null,
                                    "cssClassFaPosition": null,
                                    "hidden": null,
                                    "namedEscaped": null,
                                    "position": null,
                                    "promptStyle": null
                                },
                                {
                                    "named": null,
                                    "describedAs": null,
                                    "metadataError": null,
                                    "link": {
                                        "rel": "urn:org.restfulobjects:rels/action",
                                        "method": "GET",
                                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/openRestApi",
                                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                                    },
                                    "id": "openRestApi",
                                    "bookmarking": null,
                                    "cssClass": null,
                                    "cssClassFa": null,
                                    "cssClassFaPosition": null,
                                    "hidden": null,
                                    "namedEscaped": null,
                                    "position": null,
                                    "promptStyle": null
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
                            "fieldSet": [
                                {
                                    "name": "General",
                                    "property": [
                                        {
                                            "named": null,
                                            "describedAs": null,
                                            "metadataError": null,
                                            "link": {
                                                "rel": "urn:org.restfulobjects:rels/property",
                                                "method": "GET",
                                                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/className",
                                                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                                            },
                                            "id": "className",
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
                                                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/fixtureScriptClassName",
                                                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                                            },
                                            "id": "fixtureScriptClassName",
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
                                                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/key",
                                                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                                            },
                                            "id": "key",
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
                                                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/object",
                                                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                                            },
                                            "id": "object",
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
                                    "id": "general",
                                    "unreferencedActions": null,
                                    "unreferencedProperties": true
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
                            "metadataError": null,
                            "cssClass": null,
                            "size": null,
                            "id": null,
                            "span": 8,
                            "unreferencedActions": null,
                            "unreferencedCollections": true
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

    val FR_OBJECT = """{
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "domain-app-demo/persist-all/item-5:"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/object-layout",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "domain-app-demo/persist-all/item-5:"
            },
            {
                "rel": "urn:org.restfulobjects:rels/update",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=",
                "method": "PUT",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "arguments": {}
            }
        ],
        "extensions": {
            "oid": "*isisApplib.FixtureResult:PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=",
            "isService": false,
            "isPersistent": true
        },
        "title": "domain-app-demo/persist-all/item-5:",
        "domainType": "isisApplib.FixtureResult",
        "instanceId": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=",
        "members": {
            "fixtureScriptClassName": {
                "id": "fixtureScriptClassName",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"fixtureScriptClassName\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/fixtureScriptClassName",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": null,
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Immutable Non-cloneable view models are read-only"
            },
            "key": {
                "id": "key",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"key\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/key",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": "domain-app-demo/persist-all/item-5",
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "object": {
                "id": "object",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"object\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/object",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": null,
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "className": {
                "id": "className",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"className\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/className",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": null,
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "rebuildMetamodel": {
                "id": "rebuildMetamodel",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"rebuildMetamodel\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/rebuildMetamodel",
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
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/openRestApi",
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
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/downloadLayoutXml",
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
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/actions/clearHints",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            }
        }
    }"""

    val FR_OBJECT_PROPERTY = """{
        "id": "className",
        "memberType": "property",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=/properties/className",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS01PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjExNDwvb2JqZWN0LmJvb2ttYXJrPjwvbWVtZW50bz4=",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "domain-app-demo/persist-all/item-5:"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/className",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/property-description\""
            }
        ],
        "value": null,
        "extensions": {
            "x-isis-format": "string"
        },
        "disabledReason": "Non-cloneable view models are read-only Immutable"
    }"""

    val FR_PROPERTY_DESCRIPTION = """{
        "id": "className",
        "memberType": "property",
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult/properties/className",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/property-description\""
            },
            {
                "rel": "up",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.restfulobjects:rels/return-type",
                "href": "http://localhost:8080/restful/domain-types/java.lang.String",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            }
        ],
        "optional": false,
        "extensions": {
            "friendlyName": "Result class"
        }
    }"""

    val FR_OBJECT_BAZ = """{
        "links": [
            {
                "rel": "self",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "domain-app-demo/persist-all/item-3:  Object: Baz"
            },
            {
                "rel": "describedby",
                "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
            },
            {
                "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/object-layout",
                "method": "GET",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "title": "domain-app-demo/persist-all/item-3:  Object: Baz"
            },
            {
                "rel": "urn:org.restfulobjects:rels/update",
                "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==",
                "method": "PUT",
                "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                "arguments": {}
            }
        ],
        "extensions": {
            "oid": "*isisApplib.FixtureResult:PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==",
            "isService": false,
            "isPersistent": true
        },
        "title": "domain-app-demo/persist-all/item-3:  Object: Baz",
        "domainType": "isisApplib.FixtureResult",
        "instanceId": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==",
        "members": {
            "fixtureScriptClassName": {
                "id": "fixtureScriptClassName",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"fixtureScriptClassName\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/properties/fixtureScriptClassName",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": null,
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "key": {
                "id": "key",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"key\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/properties/key",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": "domain-app-demo/persist-all/item-3",
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "object": {
                "id": "object",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"object\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/properties/object",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": {
                    "rel": "urn:org.restfulobjects:rels/value",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/52",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Baz"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "className": {
                "id": "className",
                "memberType": "property",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsproperty=\"className\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/properties/className",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                    }
                ],
                "value": "domainapp.modules.simple.dom.impl.SimpleObject",
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "rebuildMetamodel": {
                "id": "rebuildMetamodel",
                "memberType": "action",
                "links": [
                    {
                        "rel": "urn:org.restfulobjects:rels/detailsaction=\"rebuildMetamodel\"",
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/actions/rebuildMetamodel",
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
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/actions/openRestApi",
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
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/actions/downloadLayoutXml",
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
                        "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS0zPC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjUyPC9vYmplY3QuYm9va21hcms-PC9tZW1lbnRvPg==/actions/clearHints",
                        "method": "GET",
                        "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                    }
                ]
            }
        }
    }"""

    // RECONSTRUCT_OBJECT_OBJECT_ISSUE
    val FR_OBJECT_FRODO = """{
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "domain-app-demo/persist-all/item-4:  Object: Frodo"
        }, {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/isisApplib.FixtureResult",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/domain-type\""
        }, {
            "rel": "urn:org.apache.isis.restfulobjects:rels/object-layout",
            "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/object-layout",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "domain-app-demo/persist-all/item-4:  Object: Frodo"
        }, {
            "rel": "urn:org.restfulobjects:rels/update",
            "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "arguments": {}
        }],
        "extensions": {
            "oid": "*isisApplib.FixtureResult:PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-",
            "isService": false,
            "isPersistent": true
        },
        "title": "domain-app-demo/persist-all/item-4:  Object: Frodo",
        "domainType": "isisApplib.FixtureResult",
        "instanceId": "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-",
        "members": {
            "fixtureScriptClassName": {
                "id": "fixtureScriptClassName",
                "memberType": "property",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsproperty=\"fixtureScriptClassName\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/properties/fixtureScriptClassName",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                }],
                "value": null,
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "key": {
                "id": "key",
                "memberType": "property",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsproperty=\"key\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/properties/key",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                }],
                "value": "domain-app-demo/persist-all/item-4",
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "object": {
                "id": "object",
                "memberType": "property",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsproperty=\"object\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/properties/object",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                }],
                "value": {
                    "rel": "urn:org.restfulobjects:rels/value",
                    "href": "http://localhost:8080/restful/objects/simple.SimpleObject/3",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
                    "title": "Object: Frodo"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "className": {
                "id": "className",
                "memberType": "property",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsproperty=\"className\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/properties/className",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
                }],
                "value": "domainapp.modules.simple.dom.impl.SimpleObject",
                "extensions": {
                    "x-isis-format": "string"
                },
                "disabledReason": "Non-cloneable view models are read-only Immutable"
            },
            "rebuildMetamodel": {
                "id": "rebuildMetamodel",
                "memberType": "action",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsaction=\"rebuildMetamodel\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/actions/rebuildMetamodel",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                }]
            },
            "openRestApi": {
                "id": "openRestApi",
                "memberType": "action",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsaction=\"openRestApi\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/actions/openRestApi",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                }]
            },
            "downloadLayoutXml": {
                "id": "downloadLayoutXml",
                "memberType": "action",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsaction=\"downloadLayoutXml\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/actions/downloadLayoutXml",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                }]
            },
            "clearHints": {
                "id": "clearHints",
                "memberType": "action",
                "links": [{
                    "rel": "urn:org.restfulobjects:rels/detailsaction=\"clearHints\"",
                    "href": "http://localhost:8080/restful/objects/isisApplib.FixtureResult/PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4KPG1lbWVudG8-PGtleT5kb21haW4tYXBwLWRlbW8vcGVyc2lzdC1hbGwvaXRlbS00PC9rZXk-PG9iamVjdC5ib29rbWFyaz5zaW1wbGUuU2ltcGxlT2JqZWN0OjM8L29iamVjdC5ib29rbWFyaz48L21lbWVudG8-/actions/clearHints",
                    "method": "GET",
                    "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-action\""
                }]
            }
        }
    }"""

    /*
    "httpStatusCode": 500,
    "message": "RESTEASY003210: Could not find resource for full path:
    http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu/actions/downloadTranslations/invokearguments",
    */
    val DOWNLOAD_TRANSLATIONS = """
            "http://localhost:8080/restful/services/isisApplib.TranslationServicePoMenu/actions/downloadTranslations/invoke"
            +
            "arguments"
            """

    val OBJECT_PROPERTY = """{
        "id": "notes",
        "memberType": "property",
        "links": [{
            "rel": "self",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }, {
            "rel": "up",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object\"",
            "title": "Object: Boo"
        }, {
            "rel": "urn:org.restfulobjects:rels/modifyproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "PUT",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\"",
            "arguments": {
                "value": null
            }
        }, {
            "rel": "urn:org.restfulobjects:rels/clearproperty=\"notes\"",
            "href": "http://localhost:8080/restful/objects/simple.SimpleObject/119/properties/notes",
            "method": "DELETE",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/object-property\""
        }, {
            "rel": "describedby",
            "href": "http://localhost:8080/restful/domain-types/simple.SimpleObject/properties/notes",
            "method": "GET",
            "type": "application/jsonprofile=\"urn:org.restfulobjects:repr-types/property-description\""
        }],
        "value": null,
        "extensions": {
            "x-isis-format": "string"
        }
    }"""

}

