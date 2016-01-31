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
package org.apache.isis.applib.layout.fixedcols;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.ActionOwner;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.Page;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.services.dto.Dto;

/**
 * Top-level page, consisting of an optional {@link FCColumn column} on the far left and another (also optional) on the
 * far right, with the middle consisting of a number of {@link FCTabGroup tabgroup}s, stacked vertically.
 */
@XmlRootElement(
        name = "page"
)
@XmlType(
        name = "page"
        , propOrder = {
                "actions"
                , "left"
                , "tabGroups"
                , "right"
        }
)
public class FCPage implements Page, Dto, ActionOwner, Serializable, FCColumnOwner, FCTabGroupOwner {

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
    }


    interface Visitor {
        void visit(final FCPage FCPage);
        void visit(final FCTabGroup tabGroup);
        void visit(final FCTab FCTab);
        void visit(final FCColumn FCColumn);
        void visit(final FieldSet fieldSet);
        void visit(final PropertyLayoutData propertyLayoutData);
        void visit(final CollectionLayoutData collectionLayoutData);
        void visit(final ActionLayoutData actionLayoutData);
    }

    public static class VisitorAdapter implements Visitor {
        @Override
        public void visit(final FCPage FCPage) { }
        @Override
        public void visit(final FCTabGroup tabGroup) { }
        @Override
        public void visit(final FCTab FCTab) { }
        @Override
        public void visit(final FCColumn FCColumn) { }
        @Override
        public void visit(final FieldSet fieldSet) {}
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
    public void visit(final Visitor visitor) {
        visitor.visit(this);
        traverseActions(this, visitor);
        traverseColumn(getLeft(), this, visitor);
        final List<FCTabGroup> tabGroups = getTabGroups();
        for (final FCTabGroup tabGroup : tabGroups) {
            tabGroup.setOwner(this);
            visitor.visit(tabGroup);
            final List<FCTab> tabs = tabGroup.getTabs();
            for (final FCTab FCTab : tabs) {
                FCTab.setOwner(tabGroup);
                visitor.visit(FCTab);
                traverseColumn(FCTab.getLeft(), FCTab, visitor);
                traverseColumn(FCTab.getMiddle(), FCTab, visitor);
                traverseColumn(FCTab.getRight(), FCTab, visitor);
            }
        }
        traverseColumn(getRight(), this, visitor);
    }

    private void traverseColumn(final FCColumn FCColumn, final FCColumnOwner FCColumnOwner, final Visitor visitor) {
        if(FCColumn == null) {
            return;
        }
        FCColumn.setOwner(FCColumnOwner);
        visitor.visit(FCColumn);
        traversePropertyGroups(FCColumn, visitor);
        traverseCollections(FCColumn, visitor);
    }

    private void traversePropertyGroups(final FCColumn FCColumn, final Visitor visitor) {
        for (final FieldSet fieldSet : FCColumn.getFieldSets()) {
            fieldSet.setOwner(FCColumn);
            visitor.visit(fieldSet);
            traverseActions(fieldSet, visitor);
            final List<PropertyLayoutData> properties = fieldSet.getProperties();
            for (final PropertyLayoutData propertyLayoutData : properties) {
                propertyLayoutData.setOwner(fieldSet);
                visitor.visit(propertyLayoutData);
                traverseActions(propertyLayoutData, visitor);
            }
        }
    }

    private void traverseCollections(final FCColumn FCColumn, final Visitor visitor) {
        for (final CollectionLayoutData collectionLayoutData : FCColumn.getCollections()) {
            collectionLayoutData.setOwner(FCColumn);
            visitor.visit(collectionLayoutData);
            traverseActions(collectionLayoutData, visitor);
        }
    }

    private void traverseActions(final ActionOwner actionOwner, final Visitor visitor) {
        final List<ActionLayoutData> actionLayoutDatas = actionOwner.getActions();
        if(actionLayoutDatas == null) {
            return;
        }
        for (final ActionLayoutData actionLayoutData : actionLayoutDatas) {
            actionLayoutData.setOwner(actionOwner);
            visitor.visit(actionLayoutData);
        }
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, PropertyLayoutData> getAllPropertiesById() {
        final LinkedHashMap<String, PropertyLayoutData> propertiesById = Maps.newLinkedHashMap();
        visit(new FCPage.VisitorAdapter() {
            public void visit(final PropertyLayoutData propertyLayoutData) {
                propertiesById.put(propertyLayoutData.getId(), propertyLayoutData);
            }
        });
        return propertiesById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, CollectionLayoutData> getAllCollectionsById() {
        final LinkedHashMap<String, CollectionLayoutData> collectionsById = Maps.newLinkedHashMap();

        visit(new FCPage.VisitorAdapter() {
            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                collectionsById.put(collectionLayoutData.getId(), collectionLayoutData);
            }
        });
        return collectionsById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, ActionLayoutData> getAllActionsById() {
        final LinkedHashMap<String, ActionLayoutData> actionsById = Maps.newLinkedHashMap();

        visit(new FCPage.VisitorAdapter() {
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                actionsById.put(actionLayoutData.getId(), actionLayoutData);
            }
        });
        return actionsById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, FCTab> getAllTabsByName() {
        final LinkedHashMap<String, FCTab> tabsByName = Maps.newLinkedHashMap();

        visit(new FCPage.VisitorAdapter() {
            @Override
            public void visit(final FCTab FCTab) {
                tabsByName.put(FCTab.getName(), FCTab);
            }
        });
        return tabsByName;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, FieldSet> getAllPropertyGroupsByName() {
        final LinkedHashMap<String, FieldSet> propertyGroupsByName = Maps.newLinkedHashMap();

        visit(new FCPage.VisitorAdapter() {
            @Override
            public void visit(final FieldSet fieldSet) {
                propertyGroupsByName.put(fieldSet.getName(), fieldSet);
            }
        });
        return propertyGroupsByName;
    }




    private boolean normalized;

    @Programmatic
    @XmlTransient
    public boolean isNormalized() {
        return normalized;
    }

    @Programmatic
    public void setNormalized(final boolean normalized) {
        this.normalized = normalized;
    }

}
