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

object OBJECT_LAYOUT_XML : Response() {

    override val url = "http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/object-layout"
    override val str = """
<bs:grid xmlns:lnk="https://causeway.apache.org/applib/layout/links"
         xmlns:cpt="https://causeway.apache.org/applib/layout/component"
         xmlns:bs="https://causeway.apache.org/applib/layout/grid/bootstrap3">
    <bs:row>
        <bs:col span="10" unreferencedActions="true">
            <cpt:domainObject>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/element</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object"</lnk:type>
                </cpt:link>
            </cpt:domainObject>
            <cpt:action id="actionReturning">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/actionReturning
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="actionReturningCollection">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/actionReturningCollection
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action cssClassFa="fa fa-fw fa-download" cssClassFaPosition="LEFT"
                        id="downloadColumnOrderTxtFilesAsZip">
                <cpt:named>Download .columnOrder.txt files (ZIP)</cpt:named>
                <cpt:describedAs>Downloads all the .columnOrder.txt files for this object and its collections, as a zip
                    file
                </cpt:describedAs>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>
                        http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/downloadColumnOrderTxtFilesAsZip
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action cssClassFa="fa fa-fw fa-mask" cssClassFaPosition="LEFT" id="impersonate">
                <cpt:named>Impersonate</cpt:named>
                <cpt:describedAs>Switch to another user account (for prototype/testing only)</cpt:describedAs>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/impersonate
                    </lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
        </bs:col>
        <bs:col span="2">
            <cpt:fieldSet name="" id="sources">
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="sources" labelPosition="NONE">
                    <cpt:named>Sources</cpt:named>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/sources
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
        </bs:col>
    </bs:row>
    <bs:row>
        <bs:col span="6">
            <cpt:fieldSet name="Read Only Properties" id="read-only-properties">
                <cpt:property dateRenderAdjustDays="0" id="readOnlyProperty" labelPosition="LEFT" typicalLength="25">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readOnlyProperty
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Editable Properties" id="editable-properties">
                <cpt:property dateRenderAdjustDays="0" id="readWriteProperty" labelPosition="LEFT" typicalLength="25">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readWriteProperty
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Optional Properties" id="optional-properties">
                <cpt:property dateRenderAdjustDays="0" id="readOnlyOptionalProperty" labelPosition="LEFT"
                              typicalLength="25">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readOnlyOptionalProperty
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" id="readWriteOptionalProperty" labelPosition="LEFT"
                              typicalLength="25">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readWriteOptionalProperty
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Contributed by Mixins" id="contributed">
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="mixinProperty" typicalLength="25">
                    <cpt:named>Mixin Property</cpt:named>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/mixinProperty
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="@PropertyLayout(labelPosition=...)" id="label-positions">
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionLeft"
                              labelPosition="LEFT" typicalLength="25">
                    <cpt:describedAs>@PropertyLayout(labelPosition=LEFT)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readOnlyPropertyDerivedLabelPositionLeft
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionTop"
                              labelPosition="TOP" typicalLength="25">
                    <cpt:describedAs>@PropertyLayout(labelPosition=TOP)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readOnlyPropertyDerivedLabelPositionTop
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES"
                              id="readOnlyPropertyDerivedLabelPositionRight" labelPosition="RIGHT" typicalLength="25">
                    <cpt:describedAs>@PropertyLayout(labelPosition=RIGHT)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readOnlyPropertyDerivedLabelPositionRight
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionNone"
                              labelPosition="NONE" typicalLength="25">
                    <cpt:describedAs>@PropertyLayout(labelPosition=NONE)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/readOnlyPropertyDerivedLabelPositionNone
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Other" id="other" unreferencedProperties="true"/>
        </bs:col>
        <bs:col span="6">
            <cpt:fieldSet name="Description" id="description">
                <cpt:action id="clearHints" position="PANEL">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/clearHints
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="rebuildMetamodel" position="PANEL">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/rebuildMetamodel
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadLayout" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/downloadLayout
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="inspectMetamodel" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/inspectMetamodel
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadMetamodelXml" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/downloadMetamodelXml
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadJdoMetamodel" position="PANEL_DROPDOWN">
                    <cpt:metadataError>No such action</cpt:metadataError>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/downloadJdoMetamodel
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentCommands" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/recentCommands
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentExecutions" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/recentExecutions
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentAuditTrailEntries" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/recentAuditTrailEntries
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="impersonateWithRoles" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/impersonateWithRoles
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="openRestApi" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/actions/openRestApi
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:property dateRenderAdjustDays="0" id="description">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>
                            http://localhost:8080/restful/objects/demo.JavaLangStringEntity/159/properties/description
                        </lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"
                        </lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
        </bs:col>
    </bs:row>
    <bs:row>
        <bs:col span="12" unreferencedCollections="true"/>
    </bs:row>
</bs:grid>
"""
}
