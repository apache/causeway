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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a tab group containing one or more {@link BSTab tab}s.
 *
 * @since 1.x {@index}
 */
@XmlType(name = "tabGroup", propOrder = {"tabs", "metadataError"})
public final class BSTabGroup extends BSElementAbstract implements BSTabOwner {
    private static final long serialVersionUID = 1L;

    /**
     * Whether this tab group should be used to hold any unreferenced collections (contributed or &quot;native&quot;).
     *
     * <p>Any layout must have precisely one tab group or {@link BSCol col} that has this attribute set.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean unreferencedCollections;
    /** unwraps nullable Boolean */
    @XmlTransient public boolean isUnreferencedCollections() {
        return unreferencedCollections == null ? false : unreferencedCollections;
    }

    /**
     * If there is a single tab in the tabgroup, then whether to collapse and render without the outer tab.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean collapseIfOne;
    /** unwraps nullable Boolean */
    public boolean isCollapseIfOne(boolean _default) {
        return collapseIfOne == null ? _default : collapseIfOne;
    }

    // required=false because may be auto-generated
    @XmlElement(name = "tab", required = false)
    @Getter @Setter
    private List<BSTab> tabs = new ArrayList<>();

    /**
     * Owner.
     * <p>Set programmatically by framework after reading in from XML.
     */
    @XmlTransient
    @Getter @Setter
    private BSTabGroupOwner owner;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String metadataError;

}
