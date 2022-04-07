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
package org.apache.isis.applib.layout.grid.bootstrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.grid.GridAbstract;
import org.apache.isis.applib.mixins.dto.Dto;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.  It simply consists
 * of a number of rows.
 *
 * <p>
 *     The element is rendered as a &lt;div class=&quot;...&quot;&gt;
 * </p>
 *
 * @since 1.x {@index}
 */
@XmlRootElement(
        name = "grid"
        )
@XmlType(
        name = "grid"
        , propOrder = {
                "rows",
                "metadataErrors"
        }
        )
public class BSGrid extends GridAbstract implements BSElement, Dto, BSRowOwner {

    private static final long serialVersionUID = 1L;


    private String cssClass;

    @Override
    @XmlAttribute(required = false)
    public String getCssClass() {
        return cssClass;
    }

    @Override
    public void setCssClass(final String cssClass) {
        this.cssClass = cssClass;
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



    private List<String> metadataErrors = new ArrayList<>();

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(name = "metadataError", required = false)
    public List<String> getMetadataErrors() {
        return metadataErrors;
    }

    public void setMetadataErrors(final List<String> metadataErrors) {
        this.metadataErrors = metadataErrors;
    }



    @SuppressWarnings("unused")
    private BSRowOwner owner;



    public interface Visitor extends Grid.Visitor {
        void preVisit(final BSGrid bs3Grid);
        void visit(final BSGrid bs3Grid);
        void postVisit(final BSGrid bs3Grid);
        void preVisit(final BSRow bs3Row);
        void visit(final BSRow bs3Row);
        void postVisit(final BSRow bs3Row);
        void preVisit(final BSCol bs3Col);
        void visit(final BSCol bs3Col);
        void postVisit(final BSCol bs3Col);
        void visit(final BSClearFix bs3ClearFix);
        void preVisit(final BSTabGroup bs3TabGroup);
        void visit(final BSTabGroup bs3TabGroup);
        void postVisit(final BSTabGroup bs3TabGroup);
        void preVisit(final BSTab bs3Tab);
        void visit(final BSTab bs3Tab);
        void postVisit(final BSTab bs3Tab);
    }

    public static class VisitorAdapter extends Grid.VisitorAdapter implements Visitor {
        @Override public void preVisit(final BSGrid bs3Grid) { }
        @Override public void visit(final BSGrid bs3Grid) { }
        @Override public void postVisit(final BSGrid bs3Grid) { }

        @Override public void preVisit(final BSRow bs3Row) { }
        @Override public void visit(final BSRow bs3Row) { }
        @Override public void postVisit(final BSRow bs3Row) { }

        @Override public void preVisit(final BSCol bs3Col) { }
        @Override public void visit(final BSCol bs3Col) { }
        @Override public void postVisit(final BSCol bs3Col) { }

        @Override public void visit(final BSClearFix bs3ClearFix) { }

        @Override public void preVisit(final BSTabGroup bs3TabGroup) { }
        @Override public void visit(final BSTabGroup bs3TabGroup) { }
        @Override public void postVisit(final BSTabGroup bs3TabGroup) { }

        @Override public void preVisit(final BSTab bs3Tab) { }
        @Override public void visit(final BSTab bs3Tab) { }
        @Override public void postVisit(final BSTab bs3Tab) { }
    }

    @Override
    public void visit(final Grid.Visitor visitor) {
        final BSGrid.Visitor bs3Visitor = asBs3Visitor(visitor);
        bs3Visitor.preVisit(this);
        bs3Visitor.visit(this);
        traverseRows(this, visitor);
        bs3Visitor.postVisit(this);
    }

    protected void traverseRows(final BSRowOwner rowOwner, final Grid.Visitor visitor) {
        final BSGrid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BSRow> rows = rowOwner.getRows();
        for (BSRow bs3Row : new ArrayList<>(rows)) {
            bs3Row.setOwner(this);
            bs3Visitor.preVisit(bs3Row);
            bs3Visitor.visit(bs3Row);
            traverseCols(visitor, bs3Row);
            bs3Visitor.postVisit(bs3Row);
        }
    }

    private void traverseCols(final Grid.Visitor visitor, final BSRow bs3Row) {
        final BSGrid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BSRowContent> cols = bs3Row.getCols();
        for (BSRowContent rowContent : new ArrayList<>(cols)) {
            rowContent.setOwner(bs3Row);
            if(rowContent instanceof BSCol) {
                final BSCol bs3Col = (BSCol) rowContent;
                bs3Visitor.preVisit(bs3Col);
                bs3Visitor.visit(bs3Col);
                traverseDomainObject(bs3Col, visitor);
                traverseTabGroups(bs3Col, visitor);
                traverseActions(bs3Col, visitor);
                traverseFieldSets(bs3Col, visitor);
                traverseCollections(bs3Col, visitor);
                traverseRows(bs3Col, visitor);
                bs3Visitor.postVisit(bs3Col);
            } else if (rowContent instanceof BSClearFix) {
                final BSClearFix bs3ClearFix = (BSClearFix) rowContent;
                bs3Visitor.visit(bs3ClearFix);
            } else {
                throw new IllegalStateException(
                        "Unrecognized implementation of BSRowContent, " + rowContent);
            }
        }
    }

    private void traverseDomainObject(final BSCol bs3Col, final Grid.Visitor visitor) {
        final DomainObjectLayoutData domainObject = bs3Col.getDomainObject();
        if(domainObject == null) {
            return;
        }
        domainObject.setOwner(bs3Col);
        visitor.visit(domainObject);
    }

    private void traverseTabGroups(
            final BSTabGroupOwner bs3TabGroupOwner,
            final Grid.Visitor visitor) {
        final BSGrid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BSTabGroup> tabGroups = bs3TabGroupOwner.getTabGroups();
        for (BSTabGroup bs3TabGroup : new ArrayList<>(tabGroups)) {
            bs3TabGroup.setOwner(bs3TabGroupOwner);
            bs3Visitor.preVisit(bs3TabGroup);
            bs3Visitor.visit(bs3TabGroup);
            traverseTabs(bs3TabGroup, visitor);
            bs3Visitor.postVisit(bs3TabGroup);
        }
    }

    private void traverseTabs(
            final BSTabOwner bs3TabOwner,
            final Grid.Visitor visitor) {
        final BSGrid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BSTab> tabs = bs3TabOwner.getTabs();
        for (BSTab tab : new ArrayList<>(tabs)) {
            tab.setOwner(bs3TabOwner);
            bs3Visitor.preVisit(tab);
            bs3Visitor.visit(tab);
            traverseRows(tab, visitor);
            bs3Visitor.postVisit(tab);
        }
    }

    private static Visitor asBs3Visitor(final Grid.Visitor visitor) {
        return visitor instanceof Visitor? (Visitor) visitor : new BSGrid.VisitorAdapter() {
            @Override public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                visitor.visit(domainObjectLayoutData);
            }

            @Override public void visit(final ActionLayoutData actionLayoutData) {
                visitor.visit(actionLayoutData);
            }

            @Override public void visit(final PropertyLayoutData propertyLayoutData) {
                visitor.visit(propertyLayoutData);
            }

            @Override public void visit(final CollectionLayoutData collectionLayoutData) {
                visitor.visit(collectionLayoutData);
            }

            @Override public void visit(final FieldSet fieldSet) {
                visitor.visit(fieldSet);
            }
        };
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, BSTab> getAllTabsByName() {
        final LinkedHashMap<String, BSTab> tabsByName = new LinkedHashMap<>();

        visit(new BSGrid.VisitorAdapter() {
            @Override
            public void visit(final BSTab bSTab) {
                tabsByName.put(bSTab.getName(), bSTab);
            }
        });
        return tabsByName;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, HasCssId> getAllCssId() {
        final LinkedHashMap<String, HasCssId> divsByCssId = new LinkedHashMap<>();

        visit(new BSGrid.VisitorAdapter() {
            @Override
            public void visit(final BSRow bs3Row) {
                final String id = bs3Row.getId();
                divsByCssId.put(id, bs3Row);
            }
        });
        return divsByCssId;
    }

    @Override
    @Programmatic
    @XmlTransient
    public BSGrid getGrid() {
        return this;
    }

}
