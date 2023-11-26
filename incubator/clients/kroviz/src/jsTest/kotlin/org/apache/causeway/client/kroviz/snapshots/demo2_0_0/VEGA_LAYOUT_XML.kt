package org.apache.causeway.client.kroviz.snapshots.demo2_0_0

import org.apache.causeway.client.kroviz.snapshots.Response

object VEGA_LAYOUT_XML : Response() {

    override val url = "http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/object-layout"
    override val str = """
        <bs:grid xmlns:lnk="https://causeway.apache.org/applib/layout/links" xmlns:cpt="https://causeway.apache.org/applib/layout/component" xmlns:bs="https://causeway.apache.org/applib/layout/grid/bootstrap3">
    <bs:row>
        <bs:col span="10" unreferencedActions="true">
            <cpt:domainObject>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/element</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object"</lnk:type>
                </cpt:link>
            </cpt:domainObject>
            <cpt:action id="actionReturning">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/actionReturning</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action id="actionReturningCollection">
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/actionReturningCollection</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action cssClassFa="fa fa-fw fa-download" cssClassFaPosition="LEFT" id="downloadColumnOrderTxtFilesAsZip">
                <cpt:named>Download .columnOrder.txt files (ZIP)</cpt:named>
                <cpt:describedAs>Downloads all the .columnOrder.txt files for this object and its collections, as a zip file</cpt:describedAs>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/downloadColumnOrderTxtFilesAsZip</lnk:href>
                    <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                </cpt:link>
            </cpt:action>
            <cpt:action cssClassFa="fa fa-fw fa-mask" cssClassFaPosition="LEFT" id="impersonate">
                <cpt:named>Impersonate</cpt:named>
                <cpt:describedAs>Switch to another user account (for prototype/testing only)</cpt:describedAs>
                <cpt:link>
                    <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                    <lnk:method>GET</lnk:method>
                    <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/impersonate</lnk:href>
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
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/sources</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
        </bs:col>
    </bs:row>
    <bs:row>
        <bs:col span="6">
            <cpt:fieldSet name="Read Only Properties" id="read-only-properties">
                <cpt:property dateRenderAdjustDays="0" id="readOnlyProperty" labelPosition="LEFT">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readOnlyProperty</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Editable Properties" id="editable-properties">
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readWriteProperty" labelPosition="LEFT" multiLine="5">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readWriteProperty</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Optional Properties" id="optional-properties">
                <cpt:property dateRenderAdjustDays="0" id="readOnlyOptionalProperty" labelPosition="LEFT">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readOnlyOptionalProperty</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readWriteOptionalProperty" labelPosition="LEFT" multiLine="5">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readWriteOptionalProperty</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="Contributed by Mixins" id="contributed">
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="mixinProperty">
                    <cpt:named>Mixin Property</cpt:named>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/mixinProperty</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
            </cpt:fieldSet>
            <cpt:fieldSet name="@PropertyLayout(labelPosition=...)" id="label-positions">
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionLeft" labelPosition="LEFT">
                    <cpt:describedAs>@PropertyLayout(labelPosition=LEFT)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readOnlyPropertyDerivedLabelPositionLeft</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionTop" labelPosition="TOP">
                    <cpt:describedAs>@PropertyLayout(labelPosition=TOP)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readOnlyPropertyDerivedLabelPositionTop</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionRight" labelPosition="RIGHT">
                    <cpt:describedAs>@PropertyLayout(labelPosition=RIGHT)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readOnlyPropertyDerivedLabelPositionRight</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
                    </cpt:link>
                </cpt:property>
                <cpt:property dateRenderAdjustDays="0" hidden="ALL_TABLES" id="readOnlyPropertyDerivedLabelPositionNone" labelPosition="NONE">
                    <cpt:describedAs>@PropertyLayout(labelPosition=NONE)</cpt:describedAs>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/readOnlyPropertyDerivedLabelPositionNone</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
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
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/clearHints</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="rebuildMetamodel" position="PANEL">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/rebuildMetamodel</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadLayout" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/downloadLayout</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="inspectMetamodel" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/inspectMetamodel</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadMetamodelXml" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/downloadMetamodelXml</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="downloadJdoMetamodel" position="PANEL_DROPDOWN">
                    <cpt:metadataError>No such action</cpt:metadataError>
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/downloadJdoMetamodel</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentCommands" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/recentCommands</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentExecutions" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/recentExecutions</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="recentAuditTrailEntries" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/recentAuditTrailEntries</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="impersonateWithRoles" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/impersonateWithRoles</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:action id="openRestApi" position="PANEL_DROPDOWN">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/action</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/actions/openRestApi</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-action"</lnk:type>
                    </cpt:link>
                </cpt:action>
                <cpt:property dateRenderAdjustDays="0" id="description">
                    <cpt:link>
                        <lnk:rel>urn:org.restfulobjects:rels/property</lnk:rel>
                        <lnk:method>GET</lnk:method>
                        <lnk:href>http://localhost:8080/restful/objects/demo.CausewayVegaEntity/88/properties/description</lnk:href>
                        <lnk:type>application/json;profile="urn:org.restfulobjects:repr-types/object-property"</lnk:type>
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