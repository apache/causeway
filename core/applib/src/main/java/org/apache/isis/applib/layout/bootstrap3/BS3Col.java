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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.layout.members.v1.ActionLayoutData;
import org.apache.isis.applib.layout.members.v1.CollectionLayoutData;
import org.apache.isis.applib.layout.members.v1.DomainObjectLayoutData;
import org.apache.isis.applib.layout.members.v1.FieldSet;

/**
 * A column within a row which, depending on its {@link #getSpan()}, could be as narrow as 1/12th of the page's width, all the way up to spanning the entire page.
 *
 * <p>
 *     Pretty much other content can be contained within a column, though most commonly it will be {@link FieldSet fieldset}s
 *     (a group of properties) or {@link CollectionLayoutData collection}s.  However, columns can also be used to
 *     contain further {@link BS3Row row}s (creating a nested grid of rows/cols/rows/cols) and {@link BS3TabGroup tabgroup}s.
 * </p>
 *
 * <p>
 *     It is also possible for them to contain specifically identified {@link org.apache.isis.applib.layout.members.v1.ActionLayoutData action}s and even the domain object's
 *     {@link DomainObjectLayoutData title and icon}.  Most pages however tend to show these elements in a top-level
 *     header, and so if that's the case then use the page's {@link BS3Page#setHeader(boolean) header} attribute that
 *     is provided as a convenience.
 * </p>
 *
 * <p>
 *     It is rendered as a (eg) &lt;div class=&quot;col-md-4 ...&quot;&gt;
 * </p>
 */
@XmlType(
        name = "col"
        , propOrder = {
            "domainObjectLayout",
            "actions",
            "rows",
            "tabGroup",
            "fieldSets",
            "collections",
        }
)
public class BS3Col extends BS3RowContent {

    private static final long serialVersionUID = 1L;


    private int span;

    @XmlAttribute(required = true)
    public int getSpan() {
        return span;
    }

    public void setSpan(final int span) {
        this.span = span;
    }



    private DomainObjectLayoutData domainObjectLayoutData;

    /**
     * Whether to show the object's icon and title.
     *
     * <p>
     *     Generally speaking it is easier
     * </p>
     */
    @XmlElement(name = "domainObjectLayout", required = false)
    public DomainObjectLayoutData getDomainObjectLayoutData() {
        return domainObjectLayoutData;
    }

    public void setDomainObjectLayoutData(final DomainObjectLayoutData domainObjectLayoutData) {
        this.domainObjectLayoutData = domainObjectLayoutData;
    }



    private List<ActionLayoutData> actions = new ArrayList<ActionLayoutData>();

    @XmlElementWrapper(required = false)
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    public void setActions(final List<ActionLayoutData> actions) {
        this.actions = actions;
    }



    private List<BS3Row> rows = new ArrayList<BS3Row>();

    // no wrapper
    @XmlElement(name = "row", required = false)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
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




    private List<FieldSet> propGroups = new ArrayList<FieldSet>();

    // no wrapper
    @XmlElement(name = "propGroup", required = false)
    public List<FieldSet> getPropGroups() {
        return propGroups;
    }

    public void setPropGroups(final List<FieldSet> propGroups) {
        this.propGroups = propGroups;
    }



}
