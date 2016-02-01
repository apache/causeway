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
package org.apache.isis.applib.layout.common;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.BookmarkPolicy;

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
        }
)
public class DomainObjectLayoutData implements Serializable {

    private static final long serialVersionUID = 1L;

    public DomainObjectLayoutData() {
    }


    private BookmarkPolicy bookmarking;

    @XmlAttribute(required = false)
    public BookmarkPolicy getBookmarking() {
        return bookmarking;
    }

    public void setBookmarking(BookmarkPolicy bookmarking) {
        this.bookmarking = bookmarking;
    }



    private String cssClass;

    @XmlAttribute(required = false)
    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }


    private String cssClassFa;

    @XmlAttribute(required = false)
    public String getCssClassFa() {
        return cssClassFa;
    }

    public void setCssClassFa(String cssClassFa) {
        this.cssClassFa = cssClassFa;
    }



    private org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition cssClassFaPosition;

    @XmlAttribute(required = false)
    public org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition getCssClassFaPosition() {
        return cssClassFaPosition;
    }

    public void setCssClassFaPosition(org.apache.isis.applib.annotation.ActionLayout.CssClassFaPosition cssClassFaPosition) {
        this.cssClassFaPosition = cssClassFaPosition;
    }


    private String describedAs;

    @XmlElement(required = false)
    public String getDescribedAs() {
        return describedAs;
    }

    public void setDescribedAs(String describedAs) {
        this.describedAs = describedAs;
    }



    private String named;

    @XmlElement(required = false)
    public String getNamed() {
        return named;
    }

    public void setNamed(String named) {
        this.named = named;
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



    private MemberRegionOwner owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public MemberRegionOwner getOwner() {
        return owner;
    }

    public void setOwner(final MemberRegionOwner owner) {
        this.owner = owner;
    }




}
