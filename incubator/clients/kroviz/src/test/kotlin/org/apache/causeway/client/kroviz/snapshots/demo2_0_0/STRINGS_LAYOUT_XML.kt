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
package org.apache.causeway.client.kroviz.snapshots.demo2_0_0

import org.apache.causeway.client.kroviz.snapshots.Response

object STRINGS_LAYOUT_XML: Response() {
    override val url = "http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/object-layout"
    override val str = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<bs3:grid xmlns:cpt="http://causeway.apache.org/applib/layout/component"
          xmlns:lnk="http://causeway.apache.org/applib/layout/links"
          xmlns:bs3="http://causeway.apache.org/applib/layout/grid/bootstrap3">
    <bs3:row>
        <bs3:col span="10" unreferencedActions="true">
            <cpt:domainObject>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/element</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object"</lnk:type>
                </cpt:link>
            </cpt:domainObject>
            <cpt:action bookmarking="NEVER" cssClassFa="fa fa-fw fa-mask" cssClassFaPosition="LEFT" id="impersonate">
                <cpt:named>Impersonate</cpt:named>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/impersonate
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action bookmarking="NEVER" cssClassFa="fa fa-fw fa-mask" cssClassFaPosition="LEFT"
                        id="impersonateWithRoles">
                <cpt:named>Impersonate With Roles</cpt:named>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/impersonateWithRoles
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action bookmarking="NEVER" cssClassFa="fa fa-fw fa-stop" cssClassFaPosition="LEFT"
                        id="stopImpersonating">
                <cpt:named>Stop Impersonating</cpt:named>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/stopImpersonating
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
        </bs3:col>
        <bs3:col span="2">
            <cpt:fieldSet name="" id="sources">
                <cpt:property hidden="ALL_TABLES" id="sources" labelPosition="NONE">
                    <cpt:named>Sources</cpt:named>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/properties/sources
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
        </bs3:col>
    </bs3:row>
    <bs3:row>
        <bs3:col span="6">
            <bs3:row>
                <bs3:col span="12">
                    <cpt:collection id="entities">
                        <cpt:link>
                            <lnk:rel>urn:org.restfulobjects:rels/collection</lnk:rel>
                            <lnk:method>GET</lnk:method>
                            <lnk:href>
                                http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/collections/entities
                            </lnk:href>
                            <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-collection"
                            </lnk:type>
                        </cpt:link>
                    </cpt:collection>
                </bs3:col>
                <bs3:col span="12">
                    <cpt:action id="openViewModel">
                        <cpt:link>
                            <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                            <lnk:method>GET</lnk:method>
                            <lnk:href>
                                http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/openViewModel
                            </lnk:href>
                            <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"
                            </lnk:type>
                        </cpt:link>
                    </cpt:action>
                </bs3:col>
            </bs3:row>
            <cpt:fieldSet name="Other" id="other" unreferencedProperties="true"/>
        </bs3:col>
        <bs3:col span="6">
            <cpt:fieldSet name="Description" id="description">
                <cpt:action id="clearHints" position="PANEL">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/clearHints
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadLayoutXml" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/downloadLayoutXml
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="rebuildMetamodel" position="PANEL">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/rebuildMetamodel
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadMetamodelXml" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/downloadMetamodelXml
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="inspectMetamodel" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/inspectMetamodel
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentCommands" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/recentCommands
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="openRestApi" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/actions/openRestApi
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:property id="description">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStrings/PADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8vPgo=/properties/description
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
        </bs3:col>
    </bs3:row>
    <bs3:row>
        <bs3:col span="12" unreferencedCollections="true"/>
    </bs3:row>
</bs3:grid>
"""
}
