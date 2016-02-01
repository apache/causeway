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
package org.apache.isis.applib.layout.bootstrap3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.common.ActionLayoutData;
import org.apache.isis.applib.layout.common.ActionLayoutDataOwner;
import org.apache.isis.applib.layout.common.CollectionLayoutData;
import org.apache.isis.applib.layout.common.DomainObjectLayoutData;
import org.apache.isis.applib.layout.common.FieldSet;
import org.apache.isis.applib.layout.common.Grid;
import org.apache.isis.applib.layout.common.PropertyLayoutData;
import org.apache.isis.applib.services.dto.Dto;

/**
 * This is the top-level for rendering the domain object's properties, collections and actions.  It simply consists
 * of a number of rows.
 *
 * <p>
 *     The element is rendered as a &lt;div class=&quot;...&quot;&gt;
 * </p>
 */
@XmlRootElement(
        name = "grid"
)
@XmlType(
        name = "grid"
        , propOrder = {
            "rows"
            , "metadataErrors"
        }
)
public class BS3Grid extends BS3ElementAbstract implements Grid, Dto, BS3RowOwner {

    private static final long serialVersionUID = 1L;

    private List<BS3Row> rows = new ArrayList<BS3Row>(){{
        add(new BS3Row());
    }};

    // no wrapper
    @XmlElement(name = "row", required = true)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
        this.rows = rows;
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

    private List<String> metadataErrors = Lists.newArrayList();

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     * 
     * <p>
     *     For example, if there is not exactly one {@link BS3Col} with the
     *     {@link BS3Col#isUnreferencedActions()} attribute set, then this is an error.  Ditto for
     *     {@link BS3Col#isUnreferencedCollections() collections}
     *     and {@link BS3Col#isUnreferencedProperties() properties}.
     * </p>
     */
    @XmlElement(required = false)
    public List<String> getMetadataErrors() {
        return metadataErrors;
    }

    public void setMetadataErrors(final List<String> metadataErrors) {
        this.metadataErrors = metadataErrors;
    }

    interface Visitor {
        void visit(final BS3Grid bs3Page);
        void visit(final BS3Row bs3Row);
        void visit(final BS3Col bs3Col);
        void visit(final BS3ClearFix bs3ClearFix);
        void visit(final BS3TabGroup bs3TabGroup);
        void visit(final BS3Tab bs3Tab);
        void visit(final FieldSet fieldSet);
        void visit(final DomainObjectLayoutData domainObjectLayout);
        void visit(final PropertyLayoutData propertyLayoutData);
        void visit(final CollectionLayoutData collectionLayoutData);
        void visit(final ActionLayoutData actionLayoutData);
    }

    public static class VisitorAdapter implements Visitor {
        @Override
        public void visit(final BS3Grid bs3Page) { }
        @Override
        public void visit(final BS3Row bs3Row) { }
        @Override
        public void visit(final BS3Col bs3Col) { }
        @Override
        public void visit(final BS3ClearFix bs3ClearFix) { }
        @Override
        public void visit(final BS3TabGroup bs3TabGroup) { }
        @Override
        public void visit(final BS3Tab bs3Tab) { }
        @Override
        public void visit(final DomainObjectLayoutData domainObjectLayout) { }
        @Override
        public void visit(final FieldSet fieldSet) {}
        @Override
        public void visit(final PropertyLayoutData propertyLayoutData) {}
        @Override
        public void visit(final CollectionLayoutData collectionLayoutData) {}
        @Override
        public void visit(final ActionLayoutData actionLayoutData) { }
    }

    public void visit(final BS3Grid.Visitor visitor) {
        visitor.visit(this);
        traverseRows(this, visitor);
    }

    protected void traverseRows(final BS3RowOwner rowOwner, final Visitor visitor) {
        for (BS3Row bs3Row : rowOwner.getRows()) {
            bs3Row.setOwner(this);
            visitor.visit(bs3Row);
            final List<BS3RowContent> cols = bs3Row.getCols();
            for (BS3RowContent rowContent : cols) {
                rowContent.setOwner(bs3Row);
                if(rowContent instanceof BS3Col) {
                    final BS3Col bs3Col = (BS3Col) rowContent;
                    visitor.visit(bs3Col);
                    traverseDomainObject(bs3Col, visitor);
                    traverseTabGroups(bs3Col, visitor);
                    traverseActions(bs3Col, visitor);
                    traverseFieldSets(bs3Col, visitor);
                    traverseCollections(bs3Col, visitor);
                    traverseRows(bs3Col, visitor);
                } else if (rowContent instanceof BS3ClearFix) {
                    final BS3ClearFix bs3ClearFix = (BS3ClearFix) rowContent;
                    visitor.visit(bs3ClearFix);
                } else {
                    throw new IllegalStateException(
                            "Unrecognized implementation of BS3RowContent, " + rowContent);
                }
            }
        }
    }

    private void traverseDomainObject(final BS3Col bs3Col, final Visitor visitor) {
        final DomainObjectLayoutData domainObject = bs3Col.getDomainObject();
        if(domainObject == null) {
            return;
        }
        domainObject.setOwner(bs3Col);
        visitor.visit(domainObject);
    }

    private void traverseTabGroups(
            final BS3TabGroupOwner bs3TabGroupOwner,
            final Visitor visitor) {
        final List<BS3TabGroup> tabGroups = bs3TabGroupOwner.getTabGroups();
        for (BS3TabGroup bs3TabGroup : tabGroups) {
            bs3TabGroup.setOwner(bs3TabGroupOwner);
            visitor.visit(bs3TabGroup);
            traverseTabs(bs3TabGroup, visitor);
        }
    }

    private void traverseTabs(
            final BS3TabOwner bs3TabOwner,
            final Visitor visitor) {
        final List<BS3Tab> tabs = bs3TabOwner.getTabs();
        for (BS3Tab tab : tabs) {
            tab.setOwner(bs3TabOwner);
            visitor.visit(tab);
            traverseRows(tab, visitor);
        }
    }

    private void traverseActions(
            final ActionLayoutDataOwner actionLayoutDataOwner,
            final Visitor visitor) {
        final List<ActionLayoutData> actionLayoutDatas = actionLayoutDataOwner.getActions();
        if(actionLayoutDatas == null) {
            return;
        }
        for (final ActionLayoutData actionLayoutData : actionLayoutDatas) {
            actionLayoutData.setOwner(actionLayoutDataOwner);
            visitor.visit(actionLayoutData);
        }
    }

    private void traverseFieldSets(final BS3Col bs3Col, final Visitor visitor) {
        final List<FieldSet> fieldSets = bs3Col.getFieldSets();
        for (FieldSet fieldSet : fieldSets) {
            fieldSet.setOwner(bs3Col);
            visitor.visit(fieldSet);
            traverseActions(fieldSet, visitor);
            final List<PropertyLayoutData> properties = fieldSet.getProperties();
            for (final PropertyLayoutData property : properties) {
                property.setOwner(fieldSet);
                visitor.visit(property);
                traverseActions(property, visitor);
            }
        }
    }

    private void traverseCollections(final BS3Col bs3Col, final Visitor visitor) {
        final List<CollectionLayoutData> collections = bs3Col.getCollections();
        for (CollectionLayoutData collection : collections) {
            collection.setOwner(bs3Col);
            visitor.visit(collection);
            traverseActions(collection, visitor);
        }
    }



    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, PropertyLayoutData> getAllPropertiesById() {
        final LinkedHashMap<String, PropertyLayoutData> propertiesById = Maps.newLinkedHashMap();
        visit(new BS3Grid.VisitorAdapter() {
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

        visit(new BS3Grid.VisitorAdapter() {
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

        visit(new BS3Grid.VisitorAdapter() {
            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                actionsById.put(actionLayoutData.getId(), actionLayoutData);
            }
        });
        return actionsById;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, FieldSet> getAllFieldSetsByName() {
        final LinkedHashMap<String, FieldSet> fieldSetsByName = Maps.newLinkedHashMap();

        visit(new BS3Grid.VisitorAdapter() {
            @Override
            public void visit(final FieldSet fieldSet) {
                fieldSetsByName.put(fieldSet.getName(), fieldSet);
            }
        });
        return fieldSetsByName;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, BS3Tab> getAllTabsByName() {
        final LinkedHashMap<String, BS3Tab> tabsByName = Maps.newLinkedHashMap();

        visit(new BS3Grid.VisitorAdapter() {
            @Override
            public void visit(final BS3Tab bS3Tab) {
                tabsByName.put(bS3Tab.getName(), bS3Tab);
            }
        });
        return tabsByName;
    }


    // TODO: need to figure out where the checking that can't have multiple divs with the same CSS id should go...
    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, HasCssId> getAllCssId() {
        final LinkedHashMap<String, HasCssId> divsByCssId = Maps.newLinkedHashMap();

        visit(new BS3Grid.VisitorAdapter() {
            @Override
            public void visit(final BS3Row bs3Row) {
                final String id = bs3Row.getId();
                divsByCssId.put(id, bs3Row);
            }
        });
        return divsByCssId;
    }

}
