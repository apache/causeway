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
package org.apache.isis.applib.layout.grid.bootstrap3;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * Represents a tab group containing one or more {@link BS3Tab tab}s.
 */
@XmlType(
        name = "tabGroup"
        , propOrder = {
                "tabs",
                "metadataError"
        }
        )
public class BS3TabGroup extends BS3ElementAbstract implements BS3TabOwner {

    private static final long serialVersionUID = 1L;



    private Boolean unreferencedCollections;
    /**
     * Whether this tab group should be used to hold any unreferenced collections (contributed or &quot;native&quot;).
     *
     * <p>
     *     Any layout must have precisely one tab group or {@link BS3Col col} that has this attribute set.
     * </p>
     */
    @XmlAttribute(required = false)
    public Boolean isUnreferencedCollections() {
        return unreferencedCollections;
    }

    public void setUnreferencedCollections(final Boolean unreferencedCollections) {
        this.unreferencedCollections = unreferencedCollections;
    }




    private Boolean collapseIfOne;
    /**
     * If there is a single tab in the tabgroup, then whether to collapse and render without the outer tab.
     */
    @XmlAttribute(required = false)
    public Boolean isCollapseIfOne() {
        return collapseIfOne;
    }

    public void setCollapseIfOne(final Boolean collapseIfOne) {
        this.collapseIfOne = collapseIfOne;
    }



    private List<BS3Tab> tabs = _Lists.newArrayList();

    // no wrapper; required=false because may be auto-generated
    @Override
    @XmlElement(name = "tab", required = false)
    public List<BS3Tab> getTabs() {
        return tabs;
    }

    public void setTabs(final List<BS3Tab> tabs) {
        this.tabs = tabs;
    }


    private BS3TabGroupOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BS3TabGroupOwner getOwner() {
        return owner;
    }

    public void setOwner(final BS3TabGroupOwner owner) {
        this.owner = owner;
    }



    private String metadataError;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    public String getMetadataError() {
        return metadataError;
    }

    public void setMetadataError(final String metadataError) {
        this.metadataError = metadataError;
    }

    @Override
    @XmlTransient
    @Programmatic
    public BS3Grid getGrid() {
        return getOwner().getGrid();
    }

}

