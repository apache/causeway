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

object DEMO_TAB_LAYOUT_XML: Response() {
    override val url = "http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/object-layout"
    override val str = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- Licensed to the Apache Software Foundation (ASF) under one or more contributor 
	license agreements. See the NOTICE file distributed with this work for additional 
	information regarding copyright ownership. The ASF licenses this file to 
	you under the Apache License, Version 2.0 (the "License"); you may not use 
	this file except in compliance with the License. You may obtain a copy of 
	the License at http://www.apache.org/licenses/LICENSE-2.0 Unless required 
	by applicable law or agreed to in writing, software distributed under the 
	License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS 
	OF ANY KIND, either express or implied. See the License for the specific 
	language governing permissions and limitations under the License. -->
<bs3:grid xmlns:cpt="http://isis.apache.org/applib/layout/component"
          xmlns:lnk="http://isis.apache.org/applib/layout/links"
          xmlns:bs3="http://isis.apache.org/applib/layout/grid/bootstrap3">
    <bs3:row>
        <bs3:col span="12" unreferencedActions="true">
            <cpt:domainObject>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/element</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object"</lnk:type>
                </cpt:link>
            </cpt:domainObject>
            <cpt:action id="clearHints">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/clearHints
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="downloadLayoutXml">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/downloadLayoutXml
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="openRestApi">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/openRestApi
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="rebuildMetamodel">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/rebuildMetamodel
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="doHideField">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/doHideField
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="doShowField">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/doShowField
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action bookmarking="NEVER" cssClassFa="fa fa-fw fa-download" cssClassFaPosition="LEFT"
                        id="downloadMetaModelXml">
                <cpt:named>Download Meta Model Xml</cpt:named>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/actions/downloadMetaModelXml
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
        </bs3:col>
    </bs3:row>
    <bs3:row>
        <bs3:col span="6">
            <bs3:tabGroup>
                <bs3:tab name="Tab 1">
                    <bs3:row>
                        <bs3:col span="12">
                            <cpt:fieldSet name="Hideable Field" id="fs1">
                                <cpt:property id="field1">
                                    <cpt:link>
                                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                                        <lnk:method>GET</lnk:method>
                                        <lnk:href>
                                            http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/properties/field1
                                        </lnk:href>
                                        <lnk:type>
                                            application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                                        </lnk:type>
                                    </cpt:link>
                                </cpt:property>
                            </cpt:fieldSet>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
                <bs3:tab name="Tab 2">
                    <bs3:row>
                        <bs3:col span="12">
                            <cpt:fieldSet id="fs2">
                                <cpt:property id="field2">
                                    <cpt:link>
                                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                                        <lnk:method>GET</lnk:method>
                                        <lnk:href>
                                            http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/properties/field2
                                        </lnk:href>
                                        <lnk:type>
                                            application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                                        </lnk:type>
                                    </cpt:link>
                                </cpt:property>
                            </cpt:fieldSet>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
            </bs3:tabGroup>
        </bs3:col>
        <bs3:col span="6" unreferencedCollections="true">
            <cpt:fieldSet name="Description" id="description" unreferencedProperties="true">
                <cpt:property id="description" labelPosition="NONE">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.Tab/ADw_eG1sIHZlcnNpb249IjEuMCIgZW5jb2Rpbmc9IlVURi04IiBzdGFuZGFsb25lPSJ5ZXMiPz4KPERlbW8-CiAgICA8ZmllbGQxPmZpZWxkIDE8L2ZpZWxkMT4KICAgIDxmaWVsZDI-ZmllbGQgMjwvZmllbGQyPgogICAgPGhpZGRlbj5mYWxzZTwvaGlkZGVuPgo8L0RlbW8-Cg==/properties/description
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
        </bs3:col>
    </bs3:row>
</bs3:grid>
"""
}
