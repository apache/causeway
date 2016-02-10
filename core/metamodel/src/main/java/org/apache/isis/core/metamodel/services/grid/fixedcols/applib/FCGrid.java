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
package org.apache.isis.core.metamodel.services.grid.fixedcols.applib;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.ActionLayoutDataOwner;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.Grid;
import org.apache.isis.applib.layout.component.GridAbstract;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.services.dto.Dto;

/**
 * Top-level page, consisting of an optional {@link FCColumn column} on the far left and another (also optional) on the
 * far right, with the middle consisting of a number of {@link FCTabGroup tabgroup}s, stacked vertically.
 */
@XmlRootElement(
        name = "grid"
)
@XmlType(
        name = "grid"
        , propOrder = {
                "actions"
                , "left"
                , "tabGroups"
                , "right"
        }
)
public class FCGrid extends GridAbstract implements Dto, ActionLayoutDataOwner, Serializable, FCColumnOwner, FCTabGroupOwner {

    private static final long serialVersionUID = 1L;

    private List<ActionLayoutData> actions;

    // no wrapper
    @XmlElementRef(type = ActionLayoutData.class, name="action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    public void setActions(List<ActionLayoutData> actionLayoutDatas) {
        this.actions = actionLayoutDatas;
    }



    private FCColumn left;

    @XmlElement(required = false)
    public FCColumn getLeft() {
        return left;
    }

    public void setLeft(final FCColumn left) {
        this.left = left;
        left.setHint(FCColumn.Hint.LEFT);
    }



    private List<FCTabGroup> tabGroups;

    // no wrapper
    @XmlElement(name = "tabGroup", required = true)
    public List<FCTabGroup> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(List<FCTabGroup> tabGroups) {
        this.tabGroups = tabGroups;
    }



    private FCColumn right;

    @XmlElement(required = false)
    public FCColumn getRight() {
        return right;
    }

    public void setRight(final FCColumn right) {
        this.right = right;
        right.setHint(FCColumn.Hint.RIGHT);
    }



    interface Visitor extends Grid.Visitor {
        void visit(final FCGrid fcPage);
        void visit(final FCTabGroup fcTabGroup);
        void visit(final FCTab fcTab);
        void visit(final FCColumn fcColumn);
    }

    public static class VisitorAdapter extends Grid.VisitorAdapter implements Visitor {
        @Override
        public void visit(final FCGrid fcPage) { }
        @Override
        public void visit(final FCTabGroup fcTabGroup) { }
        @Override
        public void visit(final FCTab fcTab) { }
        @Override
        public void visit(final FCColumn fcColumn) { }
        @Override
        public void visit(final PropertyLayoutData propertyLayoutData) {}
        @Override
        public void visit(final CollectionLayoutData collectionLayoutData) {}
        @Override
        public void visit(final ActionLayoutData actionLayoutData) { }
    }


    /**
     * Visits all elements of the graph.  The {@link Visitor} implementation
     * can assume that all "owner" references are populated.
     */
    public void visit(final Grid.Visitor visitor) {
        FCGrid.Visitor fcVisitor = asFcVisitor(visitor);
        fcVisitor.visit(this);
        traverseActions(this, visitor);
        traverseColumn(getLeft(), this, visitor);
        final List<FCTabGroup> tabGroups = getTabGroups();
        for (final FCTabGroup fcTabGroup : tabGroups) {
            fcTabGroup.setOwner(this);
            fcVisitor.visit(fcTabGroup);
            final List<FCTab> tabs = fcTabGroup.getTabs();
            for (final FCTab fcTab : tabs) {
                fcTab.setOwner(fcTabGroup);
                fcVisitor.visit(fcTab);
                traverseColumn(fcTab.getLeft(), fcTab, visitor);
                traverseColumn(fcTab.getMiddle(), fcTab, visitor);
                traverseColumn(fcTab.getRight(), fcTab, visitor);
            }
        }
        traverseColumn(getRight(), this, visitor);
    }

    private void traverseColumn(
            final FCColumn fcColumn,
            final FCColumnOwner fcColumnOwner,
            final Grid.Visitor visitor) {
        if(fcColumn == null) {
            return;
        }
        FCGrid.Visitor fcVisitor = asFcVisitor(visitor);
        fcColumn.setOwner(fcColumnOwner);
        fcVisitor.visit(fcColumn);
        traverseFieldSets(fcColumn, visitor);
        traverseCollections(fcColumn, visitor);
    }

    private static Visitor asFcVisitor(final Grid.Visitor visitor) {
        return visitor instanceof Visitor? (Visitor) visitor : new VisitorAdapter() {
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


}
