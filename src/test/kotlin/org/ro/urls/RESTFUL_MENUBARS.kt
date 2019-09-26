package org.ro.urls

object RESTFUL_MENUBARS : Response(){
    override val url = "http://localhost:8080/restful/menuBars"
    override val str = """
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
}
