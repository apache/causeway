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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.BookmarkPolicy;
import org.apache.isis.applib.layout.links.Link;

/**
 * Describes the layout of the title and icon of a domain object, broadly corresponding to {@link org.apache.isis.applib.annotation.DomainObjectLayout}.
 */
@XmlRootElement(
        name = "domainObject"
        )
@XmlType(
        name = "domainObject"
        , propOrder = {
                "named"
                , "describedAs"
                , "plural"
                , "metadataError"
                , "link"
        }
        )
public class DomainObjectLayoutData implements Serializable, Owned<DomainObjectLayoutDataOwner>,
HasBookmarking, HasCssClass, HasCssClassFa, HasDescribedAs, HasNamed {

    private static final long serialVersionUID = 1L;

    public DomainObjectLayoutData() {
    }


    private BookmarkPolicy bookmarking;

    @Override
    @XmlAttribute(required = false)
    public BookmarkPolicy getBookmarking() {
        return bookmarking;
    }

    @Override
    public void setBookmarking(BookmarkPolicy bookmarking) {
        this.bookmarking = bookmarking;
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


    private String cssClassFa;

    @Override
    @XmlAttribute(required = false)
    public String getCssClassFa() {
        return cssClassFa;
    }

    @Override
    public void setCssClassFa(String cssClassFa) {
        this.cssClassFa = cssClassFa;
    }



    private org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition cssClassFaPosition;

    @Override
    @XmlAttribute(required = false)
    public org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition getCssClassFaPosition() {
        return cssClassFaPosition;
    }

    @Override
    public void setCssClassFaPosition(org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition cssClassFaPosition) {
        this.cssClassFaPosition = cssClassFaPosition;
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



    private String plural;

    @XmlElement(required = false)
    public String getPlural() {
        return plural;
    }

    public void setPlural(String plural) {
        this.plural = plural;
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



    private DomainObjectLayoutDataOwner owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @Override
    @XmlTransient
    public DomainObjectLayoutDataOwner getOwner() {
        return owner;
    }

    public void setOwner(final DomainObjectLayoutDataOwner owner) {
        this.owner = owner;
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





}
