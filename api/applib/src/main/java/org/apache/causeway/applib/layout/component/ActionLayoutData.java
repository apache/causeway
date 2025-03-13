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

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.links.Link;

/**
 * Describes the layout of a single action, broadly corresponding to
 * {@link org.apache.causeway.applib.annotation.ActionLayout}.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(
        name = "action"
        )
@XmlType(
        name = "action"
        , propOrder = {
                "named"
                , "describedAs"
                , "metadataError"
                , "link"
        }
        )
public class ActionLayoutData implements Serializable, Owned<ActionLayoutDataOwner>, HasCssClass, HasCssClassFa,
HasDescribedAs, HasHidden, HasNamed {

    private static final long serialVersionUID = 1L;

    public ActionLayoutData() {
    }
    public ActionLayoutData(final String id) {
        setId(id);
    }

    private String id;
    /**
     * Method name.
     *
     * <p>
     *     Overloaded methods are not supported.
     * </p>
     */
    @XmlAttribute(name="id", required = true)
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

    private String cssClassFa;

    @Override
    @XmlAttribute(required = false)
    public String getCssClassFa() {
        return cssClassFa;
    }

    @Override
    public void setCssClassFa(final String cssClassFa) {
        this.cssClassFa = cssClassFa;
    }

    private CssClassFaPosition cssClassFaPosition;

    @Override
    @XmlAttribute(required = false)
    public CssClassFaPosition getCssClassFaPosition() {
        return cssClassFaPosition;
    }

    @Override
    public void setCssClassFaPosition(final CssClassFaPosition cssClassFaPosition) {
        this.cssClassFaPosition = cssClassFaPosition;
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

    private org.apache.causeway.applib.annotation.ActionLayout.Position position;

    @XmlAttribute(required = false)
    public org.apache.causeway.applib.annotation.ActionLayout.Position getPosition() {
        return position;
    }

    public void setPosition(final org.apache.causeway.applib.annotation.ActionLayout.Position position) {
        this.position = position;
    }

    private PromptStyle promptStyle;

    @XmlAttribute(required = false)
    public PromptStyle getPromptStyle() {
        return promptStyle;
    }

    public void setPromptStyle(final PromptStyle promptStyle) {
        this.promptStyle = promptStyle;
    }

    private ActionLayoutDataOwner owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @Override
    @XmlTransient
    public ActionLayoutDataOwner getOwner() {
        return owner;
    }

    public void setOwner(final ActionLayoutDataOwner owner) {
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
        return "ActionLayoutData{" +
                "id='" + id + '\'' +
                '}';
    }

}
