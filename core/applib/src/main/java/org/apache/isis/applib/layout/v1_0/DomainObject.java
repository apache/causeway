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
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.services.dto.Dto;

@XmlRootElement
@XmlType(
        propOrder = {
                "actions"
                , "tabGroups"
        }
)
public class DomainObject implements Dto, ActionHolder, Serializable {

    private static final long serialVersionUID = 1L;

    private List<Action> actions;

    @XmlElementWrapper(name = "actions", required = false)
    @XmlElement(name = "action", required = false)
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }


    // must have at least one tab group
    private List<TabGroup> tabGroups = new ArrayList<TabGroup>() {{
        add(new TabGroup());
    }};

    // no wrapper

    /**
     * Must have at least one tab group; no wrapper.
     */
    @XmlElement(name = "tabGroup", required = true)
    public List<TabGroup> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(List<TabGroup> tabGroups) {
        this.tabGroups = tabGroups;
    }


    public interface Visitor {
        void visit(final DomainObject domainObject);
        void visit(final TabGroup tabGroup);
        void visit(final Tab tab);
        void visit(final Column column);
        void visit(final PropertyGroup propertyGroup);
        void visit(final Property property);
        void visit(final Collection collection);
        void visit(final Action action);
    }

    public static class VisitorAdapter implements Visitor {
        @Override
        public void visit(final DomainObject domainObject) { }
        @Override
        public void visit(final TabGroup tabGroup) { }
        @Override
        public void visit(final Tab tab) { }
        @Override
        public void visit(final Column column) { }
        @Override
        public void visit(final PropertyGroup propertyGroup) {}
        @Override
        public void visit(final Property property) {}
        @Override
        public void visit(final Collection collection) {}
        @Override
        public void visit(final Action action) { }
    }

    /**
     * Initializes all "owner" references across the graph, eg {@link TabGroup#setOwner(DomainObject)} and {@link Tab#setOwner(TabGroup)} .
     */
    public void init() {
        visit(new VisitorAdapter());
    }

    public void visit(final Visitor visitor) {
        visitor.visit(this);
        traverseActions(this, visitor);
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
    }

    private void traverseColumn(final Column column, final Tab tab, final Visitor visitor) {
        if(column == null) {
            return;
        }
        column.setOwner(tab);
        visitor.visit(column);
        traversePropertyGroups(column, visitor);
        traverseCollections(column, visitor);
    }

    private void traversePropertyGroups(final Column column, final Visitor visitor) {
        for (final PropertyGroup propertyGroup : column.getPropertyGroups()) {
            propertyGroup.setOwner(column);
            visitor.visit(propertyGroup);
            traverseActions(propertyGroup, visitor);
            final List<Property> properties = propertyGroup.getProperties();
            for (final Property property : properties) {
                property.setOwner(propertyGroup);
                visitor.visit(property);
                traverseActions(property, visitor);
            }
        }
    }

    private void traverseCollections(final Column column, final Visitor visitor) {
        for (final Collection collection : column.getCollections()) {
            collection.setOwner(column);
            visitor.visit(collection);
            traverseActions(collection, visitor);
        }
    }

    private void traverseActions(final ActionHolder actionHolder, final Visitor visitor) {
        final List<Action> actions = actionHolder.getActions();
        if(actions == null) {
            return;
        }
        for (final Action action : actions) {
            action.setOwner(actionHolder);
            visitor.visit(action);
        }
    }

}
