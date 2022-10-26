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
package org.apache.causeway.client.kroviz.snapshots.simpleapp1_16_0

import org.apache.causeway.client.kroviz.snapshots.Response

object SO_LAYOUT_XML: Response() {
    override val url = "http://localhost:8080/restful/domain-types/simple.SimpleObject/layout"
    override val str = """
<bs3:grid xmlns:cpt="http://causeway.apache.org/applib/layout/component"
          xmlns:lnk="http://causeway.apache.org/applib/layout/links"
          xmlns:bs3="http://causeway.apache.org/applib/layout/grid/bootstrap3">
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
