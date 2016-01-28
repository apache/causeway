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
package org.apache.isis.applib.layout.bootstrap3;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(
        name = "div-column"
        , propOrder = {
            "rows",
            "tabGroup",
            "propGroups",
            "collections",
        }
)
public class DivColumn {


    private List<DivRow> rows = new ArrayList<DivRow>();

    // no wrapper
    @XmlElement(name = "row", required = false)
    public List<DivRow> getRows() {
        return rows;
    }

    public void setRows(final List<DivRow> rows) {
        this.rows = rows;
    }



    private BS3TabGroup tabGroup;

    @XmlElement(name="tabGroup", required = false)
    public BS3TabGroup getTabGroup() {
        return tabGroup;
    }

    public void setTabGroup(final BS3TabGroup tabGroup) {
        this.tabGroup = tabGroup;
    }




    private List<BS3PropGroup> propGroups = new ArrayList<BS3PropGroup>();

    // no wrapper
    @XmlElement(name = "propGroup", required = false)
    public List<BS3PropGroup> getPropGroups() {
        return propGroups;
    }

    public void setPropGroups(final List<BS3PropGroup> propGroups) {
        this.propGroups = propGroups;
    }




}
