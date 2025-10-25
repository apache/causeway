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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.links.Link;

import lombok.Getter;
import lombok.Setter;

/**
 * Describes the layout of a single collection, broadly corresponds to the
 * {@link org.apache.causeway.applib.annotation.CollectionLayout} annotation.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(name = "collection")
@XmlType(
    name = "collection", propOrder = {
        "named", "describedAs", "cssClass", "defaultView", "hidden", "paged",
        "sortedBy", "tableDecorator", "actions", "metadataError", "link"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionLayoutData
implements MemberRegion<CollectionLayoutDataOwner>,
ActionLayoutDataOwner,
Serializable,
HasCssClass, HasDescribedAs, HasHidden, HasNamed {
    private static final long serialVersionUID = 1L;

    public CollectionLayoutData() {}
    public CollectionLayoutData(final String id) {
        setId(id);
    }

    /**
     * Collection identifier, being the getter method without 'get' prefix, first letter lower cased.
     */
    @XmlAttribute(required = true)
    @Getter @Setter
    private String id;

    @XmlAttribute(required = false)
    @Getter @Setter
    private String cssClass;

    @XmlElement(required = false)
    @Getter @Setter
    private String describedAs;

    /**
     * Typically <code>table</code> or <code>hidden</code>, but could be any other named view that is configured and
     * appropriate, eg <code>gmap3</code> or <code>fullcalendar2</code>.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String defaultView;

    @XmlAttribute(required = false)
    @Getter @Setter
    private Where hidden;

    @XmlElement(required = false)
    @Getter @Setter
    private String named;

    @XmlAttribute(required = false)
    @Getter @Setter
    private Integer paged;

    @XmlElement(required = false)
    @Getter @Setter
    private String sortedBy;

    @XmlElement(required = false)
    @Getter @Setter
    private Class<? extends TableDecorator> tableDecorator;

    @XmlElement(name = "action", required = false)
    @Getter
    private final List<ActionLayoutData> actions = new ArrayList<>();

    /**
     * Owner.
     * <p>Set programmatically by framework after reading in from XML.
     */
    @XmlTransient
    @Getter @Setter
    private CollectionLayoutDataOwner owner;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private String metadataError;

    /**
     * The link to access this resource from the REST API (Restful Objects viewer).
     * <p>Populated by the framework automatically.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private Link link;

    @Override public String toString() {
        return "CollectionLayoutData{" +
                "id='" + id + '\'' +
                '}';
    }
}
