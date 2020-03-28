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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.PropertyLayoutData;

/**
 * Represents a tab within a {@link BS3TabGroup tab group}.
 *
 * <p>
 *     They simply contain one or more {@link BS3Row row}s.
 * </p>
 */
@XmlType(
        name = "tab"
        , propOrder = {
                "name",
                "rows"
        }
        )
public class BS3Tab extends BS3ElementAbstract implements BS3RowOwner {

    private static final long serialVersionUID = 1L;

    private String name;
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }


    private List<BS3Row> rows = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "row", required = true)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
        this.rows = rows;
    }



    private BS3TabOwner owner;

    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public BS3TabOwner getOwner() {
        return owner;
    }

    public void setOwner(final BS3TabOwner owner) {
        this.owner = owner;
    }


    public static class Predicates {
        public static Predicate<BS3Tab> notEmpty() {
            final AtomicBoolean visitingTheNode = new AtomicBoolean(false);
            final AtomicBoolean foundContent = new AtomicBoolean(false);

            return new Predicate<BS3Tab>() {
                @Override
                public boolean test(final BS3Tab thisBs3Tab) {
                    final BS3Grid owningGrid = thisBs3Tab.getGrid();
                    owningGrid.visit(new BS3Grid.VisitorAdapter() {

                        /**
                         * if found the tab, then reset 'foundContent' to false, and then use 'visitingTheNode' as
                         * a marker to indicate that the visitor is now being passed to the nodes underneath the tab.
                         * In those children, if visited (with the 'visitingTheNode' flag enabled), then simply set the
                         * 'foundContent' flag.
                         */
                        @Override
                        public void preVisit(final BS3Tab bs3Tab) {
                            if(bs3Tab == thisBs3Tab) {
                                foundContent.set(false);
                                visitingTheNode.set(true);
                            }
                        }

                        @Override public void postVisit(final BS3Tab bs3Tab) {
                            if(bs3Tab == thisBs3Tab) {
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
                }
            };
        }
    }

    @Override
    @XmlTransient
    @Programmatic
    public BS3Grid getGrid() {
        return getOwner().getGrid();
    }

    @Override public String toString() {
        return "BS3Tab{" +
                "name='" + name + '\'' +
                '}';
    }
}
