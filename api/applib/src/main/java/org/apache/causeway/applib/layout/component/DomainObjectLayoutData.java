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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.BookmarkPolicy;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.layout.links.Link;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Describes the layout of the title and icon of a domain object, broadly corresponding to {@link org.apache.causeway.applib.annotation.DomainObjectLayout}.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(name = "domainObject")
@XmlType(name = "domainObject", propOrder = {
    "named", "describedAs", "cssClass", "cssClassFa", "cssClassFaPosition",
    "paged", "tableDecorator", "metadataError", "link"})
@XmlAccessorType(XmlAccessType.FIELD)
public final class DomainObjectLayoutData implements Serializable, Owned<DomainObjectLayoutDataOwner>,
HasBookmarking, HasCssClass, HasCssClassFa, HasDescribedAs, HasNamed {
    private static final long serialVersionUID = 1L;

    public DomainObjectLayoutData() {}

    @XmlAttribute(required = false)
    @Getter @Setter
    private BookmarkPolicy bookmarking;

    @XmlAttribute(required = false)
    @Getter @Setter
    private String cssClass;

    @XmlAttribute(required = false)
    @Getter @Setter
    private String cssClassFa;

    @XmlAttribute(required = false)
    @Getter @Setter
    private CssClassFaPosition cssClassFaPosition;

    @XmlElement(required = false)
    @Getter @Setter
    private String describedAs;

    @XmlElement(required = false)
    @Getter @Setter
    private String named;

    @XmlElement(required = false)
    @Getter @Setter
    private Integer paged;

    @XmlElement(required = false)
    @Getter @Setter
    private Class<? extends TableDecorator> tableDecorator;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private String metadataError;

    /**
     * Owner.
     * <p>Set programmatically by framework after reading in from XML.
     */
    @XmlTransient
    @Getter @Setter @Accessors(fluent=true)
    private DomainObjectLayoutDataOwner owner;

    /**
     * The link to access this resource from the REST API (Restful Objects viewer).
     * <p>Populated by the framework automatically.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private Link link;

}
