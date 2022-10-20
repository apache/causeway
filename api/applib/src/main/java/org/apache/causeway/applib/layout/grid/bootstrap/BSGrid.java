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
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.layout.grid.GridAbstract;
import org.apache.causeway.applib.mixins.dto.Dto;

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
        void preVisit(final BSGrid bsGrid);
        void visit(final BSGrid bsGrid);
        void postVisit(final BSGrid bsGrid);
        void preVisit(final BSRow bsRow);
        void visit(final BSRow bsRow);
        void postVisit(final BSRow bsRow);
        void preVisit(final BSCol bsCol);
        void visit(final BSCol bsCol);
        void postVisit(final BSCol bsCol);
        void visit(final BSClearFix bsClearFix);
        void preVisit(final BSTabGroup bsTabGroup);
        void visit(final BSTabGroup bsTabGroup);
        void postVisit(final BSTabGroup bsTabGroup);
        void preVisit(final BSTab bsTab);
        void visit(final BSTab bsTab);
        void postVisit(final BSTab bsTab);
    }

    public static class VisitorAdapter extends Grid.VisitorAdapter implements Visitor {
        @Override public void preVisit(final BSGrid bsGrid) { }
        @Override public void visit(final BSGrid bsGrid) { }
        @Override public void postVisit(final BSGrid bsGrid) { }

        @Override public void preVisit(final BSRow bsRow) { }
        @Override public void visit(final BSRow bsRow) { }
        @Override public void postVisit(final BSRow bsRow) { }

        @Override public void preVisit(final BSCol bsCol) { }
        @Override public void visit(final BSCol bsCol) { }
        @Override public void postVisit(final BSCol bsCol) { }

        @Override public void visit(final BSClearFix bsClearFix) { }

        @Override public void preVisit(final BSTabGroup bsTabGroup) { }
        @Override public void visit(final BSTabGroup bsTabGroup) { }
        @Override public void postVisit(final BSTabGroup bsTabGroup) { }

        @Override public void preVisit(final BSTab bsTab) { }
        @Override public void visit(final BSTab bsTab) { }
        @Override public void postVisit(final BSTab bsTab) { }
    }

    @Override
    public void visit(final Grid.Visitor visitor) {
        final BSGrid.Visitor bsVisitor = asBsVisitor(visitor);
        bsVisitor.preVisit(this);
        bsVisitor.visit(this);
        traverseRows(this, visitor);
        bsVisitor.postVisit(this);
    }

    protected void traverseRows(final BSRowOwner rowOwner, final Grid.Visitor visitor) {
        final BSGrid.Visitor bsVisitor = asBsVisitor(visitor);
        final List<BSRow> rows = rowOwner.getRows();
        for (BSRow bsRow : new ArrayList<>(rows)) {
            bsRow.setOwner(this);
            bsVisitor.preVisit(bsRow);
            bsVisitor.visit(bsRow);
            traverseCols(visitor, bsRow);
            bsVisitor.postVisit(bsRow);
        }
    }

    private void traverseCols(final Grid.Visitor visitor, final BSRow bsRow) {
        final BSGrid.Visitor bsVisitor = asBsVisitor(visitor);
        final List<BSRowContent> cols = bsRow.getCols();
        for (BSRowContent rowContent : new ArrayList<>(cols)) {
            rowContent.setOwner(bsRow);
            if(rowContent instanceof BSCol) {
                final BSCol bsCol = (BSCol) rowContent;
                bsVisitor.preVisit(bsCol);
                bsVisitor.visit(bsCol);
                traverseDomainObject(bsCol, visitor);
                traverseTabGroups(bsCol, visitor);
                traverseActions(bsCol, visitor);
                traverseFieldSets(bsCol, visitor);
                traverseCollections(bsCol, visitor);
                traverseRows(bsCol, visitor);
                bsVisitor.postVisit(bsCol);
            } else if (rowContent instanceof BSClearFix) {
                final BSClearFix bsClearFix = (BSClearFix) rowContent;
                bsVisitor.visit(bsClearFix);
            } else {
                throw new IllegalStateException(
                        "Unrecognized implementation of BSRowContent, " + rowContent);
            }
        }
    }

    private void traverseDomainObject(final BSCol bsCol, final Grid.Visitor visitor) {
        final DomainObjectLayoutData domainObject = bsCol.getDomainObject();
        if(domainObject == null) {
            return;
        }
        domainObject.setOwner(bsCol);
        visitor.visit(domainObject);
    }

    private void traverseTabGroups(
            final BSTabGroupOwner bsTabGroupOwner,
            final Grid.Visitor visitor) {
        final BSGrid.Visitor bsVisitor = asBsVisitor(visitor);
        final List<BSTabGroup> tabGroups = bsTabGroupOwner.getTabGroups();
        for (BSTabGroup bsTabGroup : new ArrayList<>(tabGroups)) {
            bsTabGroup.setOwner(bsTabGroupOwner);
            bsVisitor.preVisit(bsTabGroup);
            bsVisitor.visit(bsTabGroup);
            traverseTabs(bsTabGroup, visitor);
            bsVisitor.postVisit(bsTabGroup);
        }
    }

    private void traverseTabs(
            final BSTabOwner bsTabOwner,
            final Grid.Visitor visitor) {
        final BSGrid.Visitor bsVisitor = asBsVisitor(visitor);
        final List<BSTab> tabs = bsTabOwner.getTabs();
        for (BSTab tab : new ArrayList<>(tabs)) {
            tab.setOwner(bsTabOwner);
            bsVisitor.preVisit(tab);
            bsVisitor.visit(tab);
            traverseRows(tab, visitor);
            bsVisitor.postVisit(tab);
        }
    }

    private static Visitor asBsVisitor(final Grid.Visitor visitor) {
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
            public void visit(final BSRow bsRow) {
                final String id = bsRow.getId();
                divsByCssId.put(id, bsRow);
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
