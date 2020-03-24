package org.apache.isis.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.isis.client.kroviz.snapshots.Response

object SO_LAYOUT_XML: Response() {
    override val url = "http://localhost:8080/restful/domain-types/simple.SimpleObject/layout"
    override val str = """
<bs3:grid xmlns:cpt="http://isis.apache.org/applib/layout/component"
          xmlns:lnk="http://isis.apache.org/applib/layout/links"
          xmlns:bs3="http://isis.apache.org/applib/layout/grid/bootstrap3">
    <script/>
    <bs3:row>
        <bs3:col span="12" unreferencedActions="true">
            <cpt:domainObject bookmarking="AS_ROOT">
                <cpt:named>Simple Object</cpt:named>
                <cpt:plural>Simple Objects</cpt:plural>
            </cpt:domainObject>
        </bs3:col>
    </bs3:row>
    <bs3:row>
        <bs3:col span="6">
            <bs3:tabGroup>
                <bs3:tab name="General">
                    <bs3:row>
                        <bs3:col span="12">
                            <cpt:fieldSet name="Name" id="name">
                                <cpt:action bookmarking="NEVER" cssClass="btn-danger" cssClassFa="fa fa-fw fa-trash"
                                            cssClassFaPosition="LEFT" hidden="NOWHERE" id="delete" position="PANEL">
                                    <cpt:named>Delete</cpt:named>
                                    <cpt:describedAs>Deletes this object from the persistent datastore</cpt:describedAs>
                                </cpt:action>
                                <cpt:property hidden="NOWHERE" id="name" namedEscaped="true" typicalLength="25">
                                    <cpt:named>Name</cpt:named>
                                    <cpt:action bookmarking="NEVER" cssClassFa="fa fa-fw fa-edit"
                                                cssClassFaPosition="LEFT" hidden="NOWHERE" id="updateName"
                                                position="BELOW">
                                        <cpt:named>Update Name</cpt:named>
                                        <cpt:describedAs>Updates the object's name</cpt:describedAs>
                                    </cpt:action>
                                </cpt:property>
                                <cpt:property hidden="NOWHERE" id="notes" multiLine="10" namedEscaped="true"
                                              typicalLength="250">
                                    <cpt:named>Notes</cpt:named>
                                </cpt:property>
                            </cpt:fieldSet>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
                <bs3:tab name="Metadata">
                    <bs3:row>
                        <bs3:col span="12">
                            <cpt:fieldSet name="Metadata" id="metadata">
                                <cpt:property id="datanucleusIdLong" labelPosition="LEFT">
                                    <cpt:named>Id</cpt:named>
                                </cpt:property>
                                <cpt:property id="datanucleusVersionLong" labelPosition="LEFT">
                                    <cpt:named>Version</cpt:named>
                                </cpt:property>
                                <cpt:property id="datanucleusVersionTimestamp" labelPosition="LEFT">
                                    <cpt:named>Version</cpt:named>
                                </cpt:property>
                            </cpt:fieldSet>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
                <bs3:tab name="Other">
                    <bs3:row>
                        <bs3:col span="12">
                            <cpt:fieldSet name="Other" id="other" unreferencedProperties="true"/>
                        </bs3:col>
                    </bs3:row>
                </bs3:tab>
            </bs3:tabGroup>
            <bs3:tabGroup/>
        </bs3:col>
        <bs3:col span="6">
            <bs3:tabGroup unreferencedCollections="true"/>
        </bs3:col>
    </bs3:row>
</bs3:grid>
"""
}
