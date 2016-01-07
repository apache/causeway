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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.isis.applib.services.dto.Dto;

@XmlRootElement
@XmlType(
        propOrder = {
                "actions"
                , "tabGroups"
        }
)
public class DomainObject implements Dto, ActionHolder {

    private List<Action> actions = Lists.newArrayList();

    @XmlElementWrapper(name = "actions", required = true)
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
    @XmlElement(name = "tabGroup", required = true)
    public List<TabGroup> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(List<TabGroup> tabGroups) {
        this.tabGroups = tabGroups;
    }


    public interface Visitor {
        void visit(DomainObject domainObject);
        void visit(TabGroup tabGroup);
        void visit(Tab tab);
        void visit(Column column);
        void visit(PropertyGroup propertyGroup);
        void visit(Property property);
        void visit(@Nullable PropertyLayout propertyLayout, Property forProperty);
        void visit(Collection collection);
        void visit(@Nullable CollectionLayout collectionLayout, Collection forCollection);
        void visit(Action action, ActionHolder holder);
        void visit(@Nullable ActionLayout actionLayout, Action forAction);
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
        public void visit(PropertyGroup propertyGroup) {}
        @Override
        public void visit(Property property) {}
        @Override
        public void visit(@Nullable final PropertyLayout propertyLayout, final Property forProperty) { }
        @Override
        public void visit(Collection collection) {}
        @Override
        public void visit(@Nullable final CollectionLayout collectionLayout, final Collection forCollection) { }
        @Override
        public void visit(final Action action, final ActionHolder actionHolder) { }
        @Override
        public void visit(@Nullable final ActionLayout actionLayout, final Action forAction) { }
    }


    public void visit(final Visitor visitor) {
        visitor.visit(this);
        traverseActions(this, visitor);
        final List<TabGroup> tabGroups = getTabGroups();
        for (final TabGroup tabGroup : tabGroups) {
            visitor.visit(tabGroup);
            final List<Tab> tabs = tabGroup.getTabs();
            for (final Tab tab : tabs) {
                visitor.visit(tab);
                traverseColumn(tab.getLeft(), visitor);
                traverseColumn(tab.getMiddle(), visitor);
                traverseColumn(tab.getRight(), visitor);
            }
        }
    }

    private void traverseColumn(final Column column, final Visitor visitor) {
        if(column == null) {
            return;
        }
        visitor.visit(column);
        traversePropertyGroups(column, visitor);
        traverseCollections(column, visitor);
    }

    private void traversePropertyGroups(final Column column, final Visitor visitor) {
        List<ColumnContent> content = column.getContent();
        final Iterable<PropertyGroup> propertyGroups =
                Iterables.transform(
                        Iterables.filter(content, is(PropertyGroup.class)),
                        cast(PropertyGroup.class));
        for (final PropertyGroup propertyGroup : propertyGroups) {
            visitor.visit(propertyGroup);
            traverseActions(propertyGroup, visitor);
            final List<Property> properties = propertyGroup.getProperties();
            for (final Property property : properties) {
                visitor.visit(property);
                visitor.visit(property.getLayout(), property);
                traverseActions(property, visitor);
            }
        }
    }

    private void traverseCollections(final Column column, final Visitor visitor) {
        final Iterable<Collection> collections =
                Iterables.transform(
                        Iterables.filter(column.getContent(), is(Collection.class)),
                        cast(Collection.class));
        for (final Collection collection : collections) {
            visitor.visit(collection);
            visitor.visit(collection.getLayout(), collection);
            traverseActions(collection, visitor);
        }
    }

    private void traverseActions(final ActionHolder actionHolder, final Visitor visitor) {
        final List<Action> actions = actionHolder.getActions();
        for (final Action action : actions) {
            visitor.visit(action, actionHolder);
            visitor.visit(action.getLayout(), action);
        }
    }

    private <F, T extends F> CastFunction<F, T> cast(final Class<T> cls) {
        return new CastFunction<>();
    }

    private <F,T> Predicate<F> is(final Class<T> cls) {
        return new Predicate<F>() {
            @Override public boolean apply(@Nullable final F from) {
                return cls.isAssignableFrom(from.getClass());
            }
        };
    }

    private static class CastFunction<F, T extends F> implements Function<F, T> {
        @Override
        public final T apply(final F from) {
            return (T) from;
        }
    }

}
