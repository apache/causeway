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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;

/**
 * Represents a tab within a {@link BSTabGroup tab group}.
 *
 * <p>
 *     They simply contain one or more {@link BSRow row}s.
 * </p>
 *
 * @since 1.x {@index}
 */
@XmlType(
        name = "tab"
        , propOrder = {
                "name",
                "rows"
        }
        )
public class BSTab extends BSElementAbstract implements BSRowOwner {

    private static final long serialVersionUID = 1L;

    private String name;
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    private List<BSRow> rows = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "row", required = true)
    public List<BSRow> getRows() {
        return rows;
    }

    public void setRows(final List<BSRow> rows) {
        this.rows = rows;
    }

    private BSTabOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BSTabOwner getOwner() {
        return owner;
    }

    public void setOwner(final BSTabOwner owner) {
        this.owner = owner;
    }

    public static class Predicates {
        public static Predicate<BSTab> notEmpty() {
            final AtomicBoolean visitingTheNode = new AtomicBoolean(false);
            final AtomicBoolean foundContent = new AtomicBoolean(false);

            return thisBsTab -> {
                final BSGrid owningGrid = thisBsTab.getGrid();
                owningGrid.visit(new BSGrid.VisitorAdapter() {

                    /**
                     * if found the tab, then reset 'foundContent' to false, and then use 'visitingTheNode' as
                     * a marker to indicate that the visitor is now being passed to the nodes underneath the tab.
                     * In those children, if visited (with the 'visitingTheNode' flag enabled), then simply set the
                     * 'foundContent' flag.
                     */
                    @Override
                    public void preVisit(final BSTab bsTab) {
                        if(bsTab == thisBsTab) {
                            foundContent.set(false);
                            visitingTheNode.set(true);
                        }
                    }

                    @Override public void postVisit(final BSTab bsTab) {
                        if(bsTab == thisBsTab) {
                            visitingTheNode.set(false);
                        }
                    }

                    @Override
                    public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                        if(visitingTheNode.get()) {
                            foundContent.set(true);
                        }
                    }

                    @Override
                    public void visit(final ActionLayoutData actionLayoutData) {
                        if(visitingTheNode.get()) {
                            foundContent.set(true);
                        }
                    }

                    @Override
                    public void visit(final PropertyLayoutData propertyLayoutData) {
                        if(visitingTheNode.get()) {
                            foundContent.set(true);
                        }
                    }

                    @Override
                    public void visit(final CollectionLayoutData collectionLayoutData) {
                        if(visitingTheNode.get()) {
                            foundContent.set(true);
                        }
                    }
                });
                return foundContent.get();
            };
        }
    }

    @Override
    @XmlTransient
    @Programmatic
    public BSGrid getGrid() {
        return getOwner().getGrid();
    }

    /**
     * removes this tab from its tab-group
     */
    @Programmatic
    public void remove() {
        if(owner!=null) {
            owner.getTabs().remove(this);
            owner = null;
        }
    }

    @Override public String toString() {
        return "BSTab{" +
                "name='" + name + '\'' +
                '}';
    }
}
