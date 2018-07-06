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
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;

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
                "rows",
                "metadataErrors"
        }
        )
public class BS3Grid extends GridAbstract implements BS3Element, Dto, BS3RowOwner {

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





    private List<BS3Row> rows = _Lists.newArrayList();

    // no wrapper
    @Override
    @XmlElement(name = "row", required = true)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
        this.rows = rows;
    }



    private List<String> metadataErrors = _Lists.newArrayList();

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
    private BS3RowOwner owner;



    public interface Visitor extends Grid.Visitor {
        void preVisit(final BS3Grid bs3Grid);
        void visit(final BS3Grid bs3Grid);
        void postVisit(final BS3Grid bs3Grid);
        void preVisit(final BS3Row bs3Row);
        void visit(final BS3Row bs3Row);
        void postVisit(final BS3Row bs3Row);
        void preVisit(final BS3Col bs3Col);
        void visit(final BS3Col bs3Col);
        void postVisit(final BS3Col bs3Col);
        void visit(final BS3ClearFix bs3ClearFix);
        void preVisit(final BS3TabGroup bs3TabGroup);
        void visit(final BS3TabGroup bs3TabGroup);
        void postVisit(final BS3TabGroup bs3TabGroup);
        void preVisit(final BS3Tab bs3Tab);
        void visit(final BS3Tab bs3Tab);
        void postVisit(final BS3Tab bs3Tab);
    }

    public static class VisitorAdapter extends Grid.VisitorAdapter implements Visitor {
        @Override public void preVisit(final BS3Grid bs3Grid) { }
        @Override public void visit(final BS3Grid bs3Grid) { }
        @Override public void postVisit(final BS3Grid bs3Grid) { }

        @Override public void preVisit(final BS3Row bs3Row) { }
        @Override public void visit(final BS3Row bs3Row) { }
        @Override public void postVisit(final BS3Row bs3Row) { }

        @Override public void preVisit(final BS3Col bs3Col) { }
        @Override public void visit(final BS3Col bs3Col) { }
        @Override public void postVisit(final BS3Col bs3Col) { }

        @Override public void visit(final BS3ClearFix bs3ClearFix) { }

        @Override public void preVisit(final BS3TabGroup bs3TabGroup) { }
        @Override public void visit(final BS3TabGroup bs3TabGroup) { }
        @Override public void postVisit(final BS3TabGroup bs3TabGroup) { }

        @Override public void preVisit(final BS3Tab bs3Tab) { }
        @Override public void visit(final BS3Tab bs3Tab) { }
        @Override public void postVisit(final BS3Tab bs3Tab) { }
    }

    @Override
    public void visit(final Grid.Visitor visitor) {
        final BS3Grid.Visitor bs3Visitor = asBs3Visitor(visitor);
        bs3Visitor.preVisit(this);
        bs3Visitor.visit(this);
        traverseRows(this, visitor);
        bs3Visitor.postVisit(this);
    }

    protected void traverseRows(final BS3RowOwner rowOwner, final Grid.Visitor visitor) {
        final BS3Grid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BS3Row> rows = rowOwner.getRows();
        for (BS3Row bs3Row : _Lists.newArrayList(rows)) {
            bs3Row.setOwner(this);
            bs3Visitor.preVisit(bs3Row);
            bs3Visitor.visit(bs3Row);
            traverseCols(visitor, bs3Row);
            bs3Visitor.postVisit(bs3Row);
        }
    }

    private void traverseCols(final Grid.Visitor visitor, final BS3Row bs3Row) {
        final BS3Grid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BS3RowContent> cols = bs3Row.getCols();
        for (BS3RowContent rowContent : _Lists.newArrayList(cols)) {
            rowContent.setOwner(bs3Row);
            if(rowContent instanceof BS3Col) {
                final BS3Col bs3Col = (BS3Col) rowContent;
                bs3Visitor.preVisit(bs3Col);
                bs3Visitor.visit(bs3Col);
                traverseDomainObject(bs3Col, visitor);
                traverseTabGroups(bs3Col, visitor);
                traverseActions(bs3Col, visitor);
                traverseFieldSets(bs3Col, visitor);
                traverseCollections(bs3Col, visitor);
                traverseRows(bs3Col, visitor);
                bs3Visitor.postVisit(bs3Col);
            } else if (rowContent instanceof BS3ClearFix) {
                final BS3ClearFix bs3ClearFix = (BS3ClearFix) rowContent;
                bs3Visitor.visit(bs3ClearFix);
            } else {
                throw new IllegalStateException(
                        "Unrecognized implementation of BS3RowContent, " + rowContent);
            }
        }
    }

    private void traverseDomainObject(final BS3Col bs3Col, final Grid.Visitor visitor) {
        final DomainObjectLayoutData domainObject = bs3Col.getDomainObject();
        if(domainObject == null) {
            return;
        }
        domainObject.setOwner(bs3Col);
        visitor.visit(domainObject);
    }

    private void traverseTabGroups(
            final BS3TabGroupOwner bs3TabGroupOwner,
            final Grid.Visitor visitor) {
        final BS3Grid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BS3TabGroup> tabGroups = bs3TabGroupOwner.getTabGroups();
        for (BS3TabGroup bs3TabGroup : _Lists.newArrayList(tabGroups)) {
            bs3TabGroup.setOwner(bs3TabGroupOwner);
            bs3Visitor.preVisit(bs3TabGroup);
            bs3Visitor.visit(bs3TabGroup);
            traverseTabs(bs3TabGroup, visitor);
            bs3Visitor.postVisit(bs3TabGroup);
        }
    }

    private void traverseTabs(
            final BS3TabOwner bs3TabOwner,
            final Grid.Visitor visitor) {
        final BS3Grid.Visitor bs3Visitor = asBs3Visitor(visitor);
        final List<BS3Tab> tabs = bs3TabOwner.getTabs();
        for (BS3Tab tab : _Lists.newArrayList(tabs)) {
            tab.setOwner(bs3TabOwner);
            bs3Visitor.preVisit(tab);
            bs3Visitor.visit(tab);
            traverseRows(tab, visitor);
            bs3Visitor.postVisit(tab);
        }
    }

    private static Visitor asBs3Visitor(final Grid.Visitor visitor) {
        return visitor instanceof Visitor? (Visitor) visitor : new BS3Grid.VisitorAdapter() {
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
    public LinkedHashMap<String, BS3Tab> getAllTabsByName() {
        final LinkedHashMap<String, BS3Tab> tabsByName = _Maps.newLinkedHashMap();

        visit(new BS3Grid.VisitorAdapter() {
            @Override
            public void visit(final BS3Tab bS3Tab) {
                tabsByName.put(bS3Tab.getName(), bS3Tab);
            }
        });
        return tabsByName;
    }


    @Programmatic
    @XmlTransient
    public LinkedHashMap<String, HasCssId> getAllCssId() {
        final LinkedHashMap<String, HasCssId> divsByCssId = _Maps.newLinkedHashMap();

        visit(new BS3Grid.VisitorAdapter() {
            @Override
            public void visit(final BS3Row bs3Row) {
                final String id = bs3Row.getId();
                divsByCssId.put(id, bs3Row);
            }
        });
        return divsByCssId;
    }

    @Override
    @Programmatic
    @XmlTransient
    public BS3Grid getGrid() {
        return this;
    }

}
