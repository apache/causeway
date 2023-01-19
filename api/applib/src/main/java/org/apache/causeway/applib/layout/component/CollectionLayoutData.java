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
package org.apache.causeway.applib.layout.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.links.Link;

/**
 * Describes the layout of a single collection, broadly corresponds to the
 * {@link org.apache.causeway.applib.annotation.CollectionLayout} annotation.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(
        name = "collection"
        )
@XmlType(
        name = "collection"
        , propOrder = {
                "named"
                ,"describedAs"
                ,"cssClass"
                ,"defaultView"
                ,"hidden"
                ,"paged"
                ,"sortedBy"
                ,"tableDecorator"
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
     * Collection identifier, being the getter method without 'get' prefix, first letter lower cased.
     */
    @XmlAttribute(required = true)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }



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



    private String describedAs;

    @Override
    @XmlElement(required = false)
    public String getDescribedAs() {
        return describedAs;
    }

    @Override
    public void setDescribedAs(final String describedAs) {
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

    public void setDefaultView(final String defaultView) {
        this.defaultView = defaultView;
    }


    private Where hidden;

    @Override
    @XmlAttribute(required = false)
    public Where getHidden() {
        return hidden;
    }

    @Override
    public void setHidden(final Where hidden) {
        this.hidden = hidden;
    }


    private String named;

    @Override
    @XmlElement(required = false)
    public String getNamed() {
        return named;
    }

    @Override
    public void setNamed(final String named) {
        this.named = named;
    }



    private Integer paged;

    @XmlAttribute(required = false)
    public Integer getPaged() {
        return paged;
    }

    public void setPaged(final Integer paged) {
        this.paged = paged;
    }



    private String sortedBy;

    @XmlElement(required = false)
    public String getSortedBy() {
        return sortedBy;
    }

    public void setSortedBy(final String sortedBy) {
        this.sortedBy = sortedBy;
    }



    private Class<? extends TableDecorator> tableDecorator;

    @XmlElement(required = false)
    public Class<? extends TableDecorator> getTableDecorator() {
        return tableDecorator;
    }

    public void setTableDecorator(final Class<? extends TableDecorator> tableDecorator) {
        this.tableDecorator = tableDecorator;
    }



    private List<ActionLayoutData> actions = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    @Override
    public void setActions(final List<ActionLayoutData> actionLayoutDatas) {
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
