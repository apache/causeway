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
import java.util.List;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.ActionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutDataOwner;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutDataOwner;
import org.apache.causeway.applib.layout.component.FieldSet;
import org.apache.causeway.applib.layout.component.FieldSetOwner;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.commons.internal.primitives._Ints.Bound;

import lombok.Getter;
import lombok.Setter;

/**
 * A column within a row which, depending on its {@link #getSpan()}, could be as narrow as 1/12th of the page's width, all the way up to spanning the entire page.
 *
 * <p>Pretty much other content can be contained within a column, though most commonly it will be {@link FieldSet fieldset}s
 * (a group of properties) or {@link CollectionLayoutData collection}s.  However, columns can also be used to
 * contain further {@link BSRow row}s (creating a nested grid of rows/cols/rows/cols) and {@link BSTabGroup tabgroup}s.
 *
 * <p>It is rendered as a (eg) &lt;div class=&quot;col-md-4 ...&quot;&gt;
 *
 * @since 1.x {@index}
 */
@XmlRootElement(name = "col")
@XmlType(
        name = "col",
        propOrder = {
                "sizeSpans",
                "domainObject",
                "actions",
                "rows",
                "tabGroups",
                "fieldSets",
                "collections",
                "metadataError"})
public final class BSCol extends BSRowContent
implements ActionLayoutDataOwner, BSTabGroupOwner, BSRowOwner, FieldSetOwner, HasElementId,
CollectionLayoutDataOwner, DomainObjectLayoutDataOwner {
    private static final long serialVersionUID = 1L;
    private static final _Ints.Range range1_12 = _Ints.Range.of(Bound.inclusive(1), Bound.inclusive(12));

    /**
     * As per &lt;div id=&quot;...&quot;&gt;...&lt;/div&gt; : must be unique across entire page.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String id;

    @XmlAttribute(required = true)
    private int span;
    public int getSpan() { return range1_12.bounded(span); }
    public void setSpan(final int span) { this.span = range1_12.bounded(span); }

    /**
     * Whether this column should be used to hold any unreferenced actions (contributed or &quot;native&quot;).
     *
     * <p>Any layout must have precisely one column or {@link FieldSet} that has this attribute set.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean unreferencedActions;
    /** unwraps nullable Boolean */
    @XmlTransient public boolean isUnreferencedActions() {
        return unreferencedActions == null ? false : unreferencedActions;
    }

    /**
     * Whether this column should be used to hold any unreferenced collections (contributed or &quot;native&quot;).
     *
     * <p>Any layout must have precisely one column or {@link BSTabGroup tabgroup} that has this attribute set.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean unreferencedCollections;
    /** unwraps nullable Boolean */
    @XmlTransient public boolean isUnreferencedCollections() {
        return unreferencedCollections == null ? false : unreferencedCollections;
    }

    /**
     * Whether to show the object's icon and title.
     */
    @XmlElementRef(type=DomainObjectLayoutData.class, name="domainObject", required = false)
    @Getter @Setter
    private DomainObjectLayoutData domainObject;

    @XmlElement(name = "sizeSpan", required = false)
    @Getter @Setter
    private List<SizeSpan> sizeSpans = new ArrayList<>();

    @XmlElementRef(type = ActionLayoutData.class, name = "action", required = false)
    @Getter @Setter
    private List<ActionLayoutData> actions = new ArrayList<>();

    @XmlElement(name = "row", required = false)
    @Getter @Setter
    private List<BSRow> rows = new ArrayList<>();

    @XmlElement(name = "tabGroup", required = false)
    @Getter @Setter
    private List<BSTabGroup> tabGroups = new ArrayList<>();

    @XmlElementRef(type=FieldSet.class, name = "fieldSet", required = false)
    @Getter @Setter
    private List<FieldSet> fieldSets = new ArrayList<>();

    @XmlElementRef(type=CollectionLayoutData.class, name = "collection", required = false)
    @Getter @Setter
    private List<CollectionLayoutData> collections = new ArrayList<>();

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String metadataError;

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
