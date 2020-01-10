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
package org.apache.isis.applib.layout.component;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.links.Link;
import org.apache.isis.commons.internal.collections._Lists;

/**
 * Describes the layout of a single collection, broadly corresponds to the {@link org.apache.isis.applib.annotation.CollectionLayout} annotation.
 *
 * <p>
 *     Note that {@link org.apache.isis.applib.annotation.CollectionLayout#render()} is omitted because
 *     {@link #defaultView} is its replacement.
 * </p>
 */
@XmlRootElement(
        name = "collection"
        )
@XmlType(
        name = "collection"
        , propOrder = {
                "named"
                ,"describedAs"
                ,"sortedBy"
                , "actions"
                , "metadataError"
                , "link"
        }
        )
public class CollectionLayoutData
implements MemberRegion<CollectionLayoutDataOwner>,
ActionLayoutDataOwner,
Serializable,
HasCssClass, HasDescribedAs, HasHidden, HasNamed {

    private static final long serialVersionUID = 1L;

    public CollectionLayoutData() {
    }
    public CollectionLayoutData(final String id) {
        setId(id);
    }


    private String id;

    /**
     * Collection identifier, being the getter method without "get" prefix, first letter lower cased.
     */
    @XmlAttribute(required = true)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    private String cssClass;

    @Override
    @XmlAttribute(required = false)
    public String getCssClass() {
        return cssClass;
    }

    @Override
    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }



    private String describedAs;

    @Override
    @XmlElement(required = false)
    public String getDescribedAs() {
        return describedAs;
    }

    @Override
    public void setDescribedAs(String describedAs) {
        this.describedAs = describedAs;
    }



    private String defaultView;

    /**
     * Typically <code>table</code> or <code>hidden</code>, but could be any other named view that is configured and
     * appropriate, eg <code>gmap3</code> or <code>fullcalendar2</code>.
     */
    @XmlAttribute(required = false)
    public String getDefaultView() {
        return defaultView;
    }

    public void setDefaultView(String defaultView) {
        this.defaultView = defaultView;
    }


    private Where hidden;

    @Override
    @XmlAttribute(required = false)
    public Where getHidden() {
        return hidden;
    }

    @Override
    public void setHidden(Where hidden) {
        this.hidden = hidden;
    }


    private String named;

    @Override
    @XmlElement(required = false)
    public String getNamed() {
        return named;
    }

    @Override
    public void setNamed(String named) {
        this.named = named;
    }


    private Boolean namedEscaped;

    @Override
    @XmlAttribute(required = false)
    public Boolean getNamedEscaped() {
        return namedEscaped;
    }

    @Override
    public void setNamedEscaped(Boolean namedEscaped) {
        this.namedEscaped = namedEscaped;
    }


    private Integer paged;

    @XmlAttribute(required = false)
    public Integer getPaged() {
        return paged;
    }

    public void setPaged(Integer paged) {
        this.paged = paged;
    }



    private String sortedBy;

    @XmlElement(required = false)
    public String getSortedBy() {
        return sortedBy;
    }

    public void setSortedBy(String sortedBy) {
        this.sortedBy = sortedBy;
    }



    private List<ActionLayoutData> actions = _Lists.newArrayList();

    // no wrapper
    @Override
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<ActionLayoutData> actionLayoutDatas) {
        this.actions = actionLayoutDatas;
    }



    private CollectionLayoutDataOwner owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @Override
    @XmlTransient
    public CollectionLayoutDataOwner getOwner() {
        return owner;
    }

    public void setOwner(final CollectionLayoutDataOwner owner) {
        this.owner = owner;
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



    private Link link;

    /**
     * The link to access this resource from the REST API (Restful Objects viewer).
     *
     * <p>
     *     Populated by the framework automatically.
     * </p>
     */
    @XmlElement(required = false)
    public Link getLink() {
        return link;
    }

    public void setLink(final Link link) {
        this.link = link;
    }

    @Override public String toString() {
        return "CollectionLayoutData{" +
                "id='" + id + '\'' +
                '}';
    }
}
