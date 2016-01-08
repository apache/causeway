/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.layout.v1_0;


import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.isis.applib.annotation.Where;

/**
 * Broadly corresponds to the {@link org.apache.isis.applib.annotation.CollectionLayout} annotation.
 * 
 * <p>
 *     Note that {@link org.apache.isis.applib.annotation.CollectionLayout#render()} is omitted because
 *     {@link #defaultView} is its replacement.
 * </p>
 */
@XmlType(
        propOrder = {
                "named"
                ,"describedAs"
                ,"sortedBy"
        }
)
public class CollectionLayout implements Serializable {

    private static final long serialVersionUID = 1L;

    private String cssClass;

    @XmlAttribute(required = false)
    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }



    private String describedAs;

    @XmlElement(required = false)
    public String getDescribedAs() {
        return describedAs;
    }

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

    @XmlAttribute(required = false)
    public Where getHidden() {
        return hidden;
    }

    public void setHidden(Where hidden) {
        this.hidden = hidden;
    }


    private String named;

    @XmlElement(required = false)
    public String getNamed() {
        return named;
    }

    public void setNamed(String named) {
        this.named = named;
    }


    private Boolean namedEscaped;

    @XmlAttribute(required = false)
    public Boolean getNamedEscaped() {
        return namedEscaped;
    }

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



    private Collection owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public Collection getOwner() {
        return owner;
    }

    public void setOwner(final Collection owner) {
        this.owner = owner;
    }

}