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
package org.apache.isis.applib.layout.v1_0;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.dto.Dto;

@XmlRootElement(
        name = "objectLayout"
)
@XmlType(
        name = "objectLayout"
        , propOrder = {
                "actions"
                , "left"
                , "tabGroups"
                , "right"
        }
)
public class ObjectLayoutMetadata implements Dto, ActionOwner, Serializable, ColumnOwner, HasPath, TabGroupOwner {

    private static final long serialVersionUID = 1L;

    private List<ActionLayoutMetadata> actions;

    @XmlElementWrapper(name = "actions", required = false)
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutMetadata> getActions() {
        return actions;
    }

    public void setActions(List<ActionLayoutMetadata> actionLayoutMetadatas) {
        this.actions = actionLayoutMetadatas;
    }



    private Column left;

    @XmlElement(required = false)
    public Column getLeft() {
        return left;
    }

    public void setLeft(final Column left) {
        this.left = left;
    }



    private List<TabGroup> tabGroups;

    // no wrapper
    @XmlElement(name = "tabGroup", required = true)
    public List<TabGroup> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(List<TabGroup> tabGroups) {
        this.tabGroups = tabGroups;
    }



    private Column right;

    @XmlElement(required = false)
    public Column getRight() {
        return right;
    }

    public void setRight(final Column right) {
        this.right = right;
    }
    

    public interface Visitor {
        void visit(final ObjectLayoutMetadata objectLayoutMetadata);
        void visit(final TabGroup tabGroup);
        void visit(final Tab tab);
        void visit(final Column column);
        void visit(final PropertyGroup propertyGroup);
        void visit(final PropertyLayoutMetadata propertyLayoutMetadata);
        void visit(final CollectionLayoutMetadata collectionLayoutMetadata);
        void visit(final ActionLayoutMetadata actionLayoutMetadata);
    }

    public static class VisitorAdapter implements Visitor {
        @Override
        public void visit(final ObjectLayoutMetadata objectLayoutMetadata) { }
        @Override
        public void visit(final TabGroup tabGroup) { }
        @Override
        public void visit(final Tab tab) { }
        @Override
        public void visit(final Column column) { }
        @Override
        public void visit(final PropertyGroup propertyGroup) {}
        @Override
        public void visit(final PropertyLayoutMetadata propertyLayoutMetadata) {}
        @Override
        public void visit(final CollectionLayoutMetadata collectionLayoutMetadata) {}
        @Override
        public void visit(final ActionLayoutMetadata actionLayoutMetadata) { }
    }


    /**
     * Visits all elements of the graph.  The {@link Visitor} implementation
     * can assume that all "owner" references are populated.
     */
    public void visit(final Visitor visitor) {
        visitor.visit(this);
        traverseActions(this, visitor);
        traverseColumn(getLeft(), this, visitor);
        final List<TabGroup> tabGroups = getTabGroups();
        for (final TabGroup tabGroup : tabGroups) {
            tabGroup.setOwner(this);
            visitor.visit(tabGroup);
            final List<Tab> tabs = tabGroup.getTabs();
            for (final Tab tab : tabs) {
                tab.setOwner(tabGroup);
                visitor.visit(tab);
                traverseColumn(tab.getLeft(), tab, visitor);
                traverseColumn(tab.getMiddle(), tab, visitor);
                traverseColumn(tab.getRight(), tab, visitor);
            }
        }
        traverseColumn(getRight(), this, visitor);
    }

    private void traverseColumn(final Column column, final ColumnOwner columnOwner, final Visitor visitor) {
        if(column == null) {
            return;
        }
        column.setOwner(columnOwner);
        visitor.visit(column);
        traversePropertyGroups(column, visitor);
        traverseCollections(column, visitor);
    }

    private void traversePropertyGroups(final Column column, final Visitor visitor) {
        for (final PropertyGroup propertyGroup : column.getPropertyGroups()) {
            propertyGroup.setOwner(column);
            visitor.visit(propertyGroup);
            traverseActions(propertyGroup, visitor);
            final List<PropertyLayoutMetadata> properties = propertyGroup.getProperties();
            for (final PropertyLayoutMetadata propertyLayoutMetadata : properties) {
                propertyLayoutMetadata.setOwner(propertyGroup);
                visitor.visit(propertyLayoutMetadata);
                traverseActions(propertyLayoutMetadata, visitor);
            }
        }
    }

    private void traverseCollections(final Column column, final Visitor visitor) {
        for (final CollectionLayoutMetadata collectionLayoutMetadata : column.getCollections()) {
            collectionLayoutMetadata.setOwner(column);
            visitor.visit(collectionLayoutMetadata);
            traverseActions(collectionLayoutMetadata, visitor);
        }
    }

    private void traverseActions(final ActionOwner actionOwner, final Visitor visitor) {
        final List<ActionLayoutMetadata> actionLayoutMetadatas = actionOwner.getActions();
        if(actionLayoutMetadatas == null) {
            return;
        }
        for (final ActionLayoutMetadata actionLayoutMetadata : actionLayoutMetadatas) {
            actionLayoutMetadata.setOwner(actionOwner);
            visitor.visit(actionLayoutMetadata);
        }
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, PropertyLayoutMetadata> getAllPropertiesById() {
        final LinkedHashMap<String, PropertyLayoutMetadata> propertiesById = Maps.newLinkedHashMap();
        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            public void visit(final PropertyLayoutMetadata propertyLayoutMetadata) {
                propertiesById.put(propertyLayoutMetadata.getId(), propertyLayoutMetadata);
            }
        });
        return propertiesById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, CollectionLayoutMetadata> getAllCollectionsById() {
        final LinkedHashMap<String, CollectionLayoutMetadata> collectionsById = Maps.newLinkedHashMap();

        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final CollectionLayoutMetadata collectionLayoutMetadata) {
                collectionsById.put(collectionLayoutMetadata.getId(), collectionLayoutMetadata);
            }
        });
        return collectionsById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, ActionLayoutMetadata> getAllActionsById() {
        final LinkedHashMap<String, ActionLayoutMetadata> actionsById = Maps.newLinkedHashMap();

        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final ActionLayoutMetadata actionLayoutMetadata) {
                actionsById.put(actionLayoutMetadata.getId(), actionLayoutMetadata);
            }
        });
        return actionsById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, Tab> getAllTabsByName() {
        final LinkedHashMap<String, Tab> tabsByName = Maps.newLinkedHashMap();

        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final Tab tab) {
                tabsByName.put(tab.getName(), tab);
            }
        });
        return tabsByName;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, PropertyGroup> getAllPropertyGroupsByName() {
        final LinkedHashMap<String, PropertyGroup> propertyGroupsByName = Maps.newLinkedHashMap();

        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final PropertyGroup propertyGroup) {
                propertyGroupsByName.put(propertyGroup.getName(), propertyGroup);
            }
        });
        return propertyGroupsByName;
    }


    private String path;

    @Programmatic
    @XmlTransient
    public String getPath() {
        return path;
    }

    @Programmatic
    public void setPath(final String path) {
        this.path = path;
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
