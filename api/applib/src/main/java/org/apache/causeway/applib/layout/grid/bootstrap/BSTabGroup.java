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
package org.apache.causeway.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Programmatic;

/**
 * Represents a tab group containing one or more {@link BSTab tab}s.
 *
 * @since 1.x {@index}
 */
@XmlType(
        name = "tabGroup"
        , propOrder = {
                "tabs",
                "metadataError"
        }
        )
public class BSTabGroup extends BSElementAbstract implements BSTabOwner {

    private static final long serialVersionUID = 1L;



    private Boolean unreferencedCollections;
    /**
     * Whether this tab group should be used to hold any unreferenced collections (contributed or &quot;native&quot;).
     *
     * <p>
     *     Any layout must have precisely one tab group or {@link BSCol col} that has this attribute set.
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



    private List<BSTab> tabs = new ArrayList<>();

    // no wrapper; required=false because may be auto-generated
    @Override
    @XmlElement(name = "tab", required = false)
    public List<BSTab> getTabs() {
        return tabs;
    }

    public void setTabs(final List<BSTab> tabs) {
        this.tabs = tabs;
    }


    private BSTabGroupOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BSTabGroupOwner getOwner() {
        return owner;
    }

    public void setOwner(final BSTabGroupOwner owner) {
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
    public BSGrid getGrid() {
        return getOwner().getGrid();
    }

}

