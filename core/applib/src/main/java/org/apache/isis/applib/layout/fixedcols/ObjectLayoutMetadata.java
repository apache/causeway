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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.v1_0.ActionLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.ActionOwner;
import org.apache.isis.applib.layout.v1_0.CollectionLayoutMetadata;
import org.apache.isis.applib.layout.v1_0.ColumnOwner;
import org.apache.isis.applib.layout.v1_0.PropertyGroupMetadata;
import org.apache.isis.applib.layout.v1_0.PropertyLayoutMetadata;
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
public class ObjectLayoutMetadata implements Dto, ActionOwner, Serializable, ColumnOwner, TabGroupOwner {

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



    private ColumnMetadata left;

    @XmlElement(required = false)
    public ColumnMetadata getLeft() {
        return left;
    }

    public void setLeft(final ColumnMetadata left) {
        this.left = left;
    }



    private List<TabGroupMetadata> tabGroups;

    // no wrapper
    @XmlElement(name = "tabGroup", required = true)
    public List<TabGroupMetadata> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(List<TabGroupMetadata> tabGroups) {
        this.tabGroups = tabGroups;
    }



    private ColumnMetadata right;

    @XmlElement(required = false)
    public ColumnMetadata getRight() {
        return right;
    }

    public void setRight(final ColumnMetadata right) {
        this.right = right;
    }
    

    public interface Visitor {
        void visit(final ObjectLayoutMetadata objectLayoutMetadata);
        void visit(final TabGroupMetadata tabGroup);
        void visit(final TabMetadata tabMetadata);
        void visit(final ColumnMetadata columnMetadata);
        void visit(final PropertyGroupMetadata propertyGroupMetadata);
        void visit(final PropertyLayoutMetadata propertyLayoutMetadata);
        void visit(final CollectionLayoutMetadata collectionLayoutMetadata);
        void visit(final ActionLayoutMetadata actionLayoutMetadata);
    }

    public static class VisitorAdapter implements Visitor {
        @Override
        public void visit(final ObjectLayoutMetadata objectLayoutMetadata) { }
        @Override
        public void visit(final TabGroupMetadata tabGroup) { }
        @Override
        public void visit(final TabMetadata tabMetadata) { }
        @Override
        public void visit(final ColumnMetadata columnMetadata) { }
        @Override
        public void visit(final PropertyGroupMetadata propertyGroupMetadata) {}
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
        final List<TabGroupMetadata> tabGroups = getTabGroups();
        for (final TabGroupMetadata tabGroup : tabGroups) {
            tabGroup.setOwner(this);
            visitor.visit(tabGroup);
            final List<TabMetadata> tabs = tabGroup.getTabs();
            for (final TabMetadata tabMetadata : tabs) {
                tabMetadata.setOwner(tabGroup);
                visitor.visit(tabMetadata);
                traverseColumn(tabMetadata.getLeft(), tabMetadata, visitor);
                traverseColumn(tabMetadata.getMiddle(), tabMetadata, visitor);
                traverseColumn(tabMetadata.getRight(), tabMetadata, visitor);
            }
        }
        traverseColumn(getRight(), this, visitor);
    }

    private void traverseColumn(final ColumnMetadata columnMetadata, final ColumnOwner columnOwner, final Visitor visitor) {
        if(columnMetadata == null) {
            return;
        }
        columnMetadata.setOwner(columnOwner);
        visitor.visit(columnMetadata);
        traversePropertyGroups(columnMetadata, visitor);
        traverseCollections(columnMetadata, visitor);
    }

    private void traversePropertyGroups(final ColumnMetadata columnMetadata, final Visitor visitor) {
        for (final PropertyGroupMetadata propertyGroupMetadata : columnMetadata.getPropertyGroups()) {
            propertyGroupMetadata.setOwner(columnMetadata);
            visitor.visit(propertyGroupMetadata);
            traverseActions(propertyGroupMetadata, visitor);
            final List<PropertyLayoutMetadata> properties = propertyGroupMetadata.getProperties();
            for (final PropertyLayoutMetadata propertyLayoutMetadata : properties) {
                propertyLayoutMetadata.setOwner(propertyGroupMetadata);
                visitor.visit(propertyLayoutMetadata);
                traverseActions(propertyLayoutMetadata, visitor);
            }
        }
    }

    private void traverseCollections(final ColumnMetadata columnMetadata, final Visitor visitor) {
        for (final CollectionLayoutMetadata collectionLayoutMetadata : columnMetadata.getCollections()) {
            collectionLayoutMetadata.setOwner(columnMetadata);
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
    public LinkedHashMap<String, TabMetadata> getAllTabsByName() {
        final LinkedHashMap<String, TabMetadata> tabsByName = Maps.newLinkedHashMap();

        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final TabMetadata tabMetadata) {
                tabsByName.put(tabMetadata.getName(), tabMetadata);
            }
        });
        return tabsByName;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, PropertyGroupMetadata> getAllPropertyGroupsByName() {
        final LinkedHashMap<String, PropertyGroupMetadata> propertyGroupsByName = Maps.newLinkedHashMap();

        visit(new ObjectLayoutMetadata.VisitorAdapter() {
            @Override
            public void visit(final PropertyGroupMetadata propertyGroupMetadata) {
                propertyGroupsByName.put(propertyGroupMetadata.getName(), propertyGroupMetadata);
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
