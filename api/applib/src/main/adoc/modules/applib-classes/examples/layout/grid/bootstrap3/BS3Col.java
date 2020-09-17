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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.ActionLayoutDataOwner;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutDataOwner;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.isis.applib.layout.component.FieldSet;
import org.apache.isis.applib.layout.component.FieldSetOwner;
import org.apache.isis.commons.internal.primitives._Ints;
import org.apache.isis.commons.internal.primitives._Ints.Bound;

/**
 * A column within a row which, depending on its {@link #getSpan()}, could be as narrow as 1/12th of the page's width, all the way up to spanning the entire page.
 *
 * <p>
 *     Pretty much other content can be contained within a column, though most commonly it will be {@link FieldSet fieldset}s
 *     (a group of properties) or {@link CollectionLayoutData collection}s.  However, columns can also be used to
 *     contain further {@link BS3Row row}s (creating a nested grid of rows/cols/rows/cols) and {@link BS3TabGroup tabgroup}s.
 * </p>
 *
 * <p>
 *     It is rendered as a (eg) &lt;div class=&quot;col-md-4 ...&quot;&gt;
 * </p>
 */
@XmlRootElement(
        name = "col"
        )
@XmlType(
        name = "col"
        , propOrder = {
                "sizeSpans",
                "domainObject",
                "actions",
                "rows",
                "tabGroups",
                "fieldSets",
                "collections",
                "metadataError"
        }
        )
public class BS3Col extends BS3RowContent
implements ActionLayoutDataOwner, BS3TabGroupOwner, BS3RowOwner, FieldSetOwner, HasCssId,
CollectionLayoutDataOwner, DomainObjectLayoutDataOwner {

    private static final long serialVersionUID = 1L;

    private static final _Ints.Range range1_12 = _Ints.Range.of(Bound.inclusive(1), Bound.inclusive(12));

    private String id;

    /**
     * As per &lt;div id=&quot;...&quot;&gt;...&lt;/div&gt; : must be unique across entire page.
     */
    @Override
    @XmlAttribute(required = false)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }


    private int span;

    @XmlAttribute(required = true)
    public int getSpan() {
        return range1_12.bounded(span);
    }

    public void setSpan(final int span) {
        this.span = range1_12.bounded(span);
    }


    private Boolean unreferencedActions;

    /**
     * Whether this column should be used to hold any unreferenced actions (contributed or &quot;native&quot;).
     *
     * <p>
     *     Any layout must have precisely one column or {@link FieldSet} that has this attribute set.
     * </p>
     */
    @XmlAttribute(required = false)
    public Boolean isUnreferencedActions() {
        return unreferencedActions;
    }

    public void setUnreferencedActions(final Boolean unreferencedActions) {
        this.unreferencedActions = unreferencedActions;
    }


    private Boolean unreferencedCollections;
    /**
     * Whether this column should be used to hold any unreferenced collections (contributed or &quot;native&quot;).
     *
     * <p>
     *     Any layout must have precisely one column or {@link BS3TabGroup tabgroup} that has this attribute set.
     * </p>
     */
    @XmlAttribute(required = false)
    public Boolean isUnreferencedCollections() {
        return unreferencedCollections;
    }

    public void setUnreferencedCollections(final Boolean unreferencedCollections) {
        this.unreferencedCollections = unreferencedCollections;
    }





    private DomainObjectLayoutData domainObject;

    /**
     * Whether to show the object's icon and title.
     */
    @Override
    @XmlElementRef(type=DomainObjectLayoutData.class, name="domainObject", required = false)
    public DomainObjectLayoutData getDomainObject() {
        return domainObject;
    }

    @Override
    public void setDomainObject(final DomainObjectLayoutData domainObjectLayoutData) {
        this.domainObject = domainObjectLayoutData;
    }


    private List<SizeSpan> sizeSpans = new ArrayList<>();

    // no wrapper
    @XmlElement(name = "sizeSpan", required = false)
    public List<SizeSpan> getSizeSpans() {
        return sizeSpans;
    }

    public void setSizeSpans(final List<SizeSpan> sizeSpans) {
        this.sizeSpans = sizeSpans;
    }



    private List<ActionLayoutData> actions = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElementRef(type = ActionLayoutData.class, name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    @Override
    public void setActions(final List<ActionLayoutData> actions) {
        this.actions = actions;
    }



    private List<BS3Row> rows = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "row", required = false)
    public List<BS3Row> getRows() {
        return rows;
    }

    public void setRows(final List<BS3Row> rows) {
        this.rows = rows;
    }



    private List<BS3TabGroup> tabGroups = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "tabGroup", required = false)
    public List<BS3TabGroup> getTabGroups() {
        return tabGroups;
    }

    public void setTabGroups(final List<BS3TabGroup> tabGroups) {
        this.tabGroups = tabGroups;
    }



    private List<FieldSet> fieldSets = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElementRef(type=FieldSet.class, name = "fieldSet", required = false)
    public List<FieldSet> getFieldSets() {
        return fieldSets;
    }

    public void setFieldSets(final List<FieldSet> fieldSets) {
        this.fieldSets = fieldSets;
    }



    private List<CollectionLayoutData> collections = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElementRef(type=CollectionLayoutData.class, name = "collection", required = false)
    public List<CollectionLayoutData> getCollections() {
        return collections;
    }

    public void setCollections(final List<CollectionLayoutData> collections) {
        this.collections = collections;
    }



    private String metadataError;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    public String getMetadataError() {
        return metadataError;
    }

    public void setMetadataError(final String metadataError) {
        this.metadataError = metadataError;
    }



    public String toCssClass() {
        final StringBuilder buf = new StringBuilder();
        final Size size = getSize() != null? getSize(): Size.MD;
        SizeSpan.with(size, getSpan()).appendCssClassFragment(buf);
        for (SizeSpan sizeSpan : getSizeSpans()) {
            sizeSpan.appendCssClassFragment(buf);
        }
        buf.append((getCssClass() != null? " " + getCssClass(): ""));
        return buf.toString();
    }

    @Override public String toString() {
        return (id != null? "#" + id + " ": "") + toCssClass();
    }
}
