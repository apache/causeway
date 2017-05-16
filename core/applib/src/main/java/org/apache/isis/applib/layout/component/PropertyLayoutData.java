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

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.LabelPosition;
import org.apache.isis.applib.annotation.PromptStyle;
import org.apache.isis.applib.annotation.Where;

/**
 * Describes the layout of a single property, broadly corresponds to the {@link org.apache.isis.applib.annotation.PropertyLayout} annotation.
 */
@XmlRootElement(
        name = "property"
)
@XmlType(
        name = "property"
        , propOrder = {
                "named"
                , "describedAs"
                , "actions"
                , "metadataError"
        }
)
public class PropertyLayoutData
        implements ActionLayoutDataOwner,
                   Serializable,
                   Owned<FieldSet>,
                   HasCssClass, HasDescribedAs, HasHidden, HasNamed  {

    private static final long serialVersionUID = 1L;

    public PropertyLayoutData() {
    }

    public PropertyLayoutData(final String id) {
        this.id = id;
    }

    private String id;

    /**
     * Property identifier, being the getter method without "get" or "is" prefix, first letter lower cased.
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


    private LabelPosition labelPosition;

    @XmlAttribute(required = false)
    public LabelPosition getLabelPosition() {
        return labelPosition;
    }

    public void setLabelPosition(LabelPosition labelPosition) {
        this.labelPosition = labelPosition;
    }


    private Integer multiLine;

    @XmlAttribute(required = false)
    public Integer getMultiLine() {
        return multiLine;
    }

    public void setMultiLine(Integer multiLine) {
        this.multiLine = multiLine;
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


    private PromptStyle promptStyle;

    @XmlAttribute(required = false)
    public PromptStyle getPromptStyle() {
        return promptStyle;
    }

    public void setPromptStyle(PromptStyle promptStyle) {
        this.promptStyle = promptStyle;
    }



    private Boolean renderedAsDayBefore;

    @XmlAttribute(required = false)
    public Boolean getRenderedAsDayBefore() {
        return renderedAsDayBefore;
    }

    public void setRenderedAsDayBefore(Boolean renderedAsDayBefore) {
        this.renderedAsDayBefore = renderedAsDayBefore;
    }


    private Integer typicalLength;

    @XmlAttribute(required = false)
    public Integer getTypicalLength() {
        return typicalLength;
    }

    public void setTypicalLength(Integer typicalLength) {
        this.typicalLength = typicalLength;
    }


    private Boolean unchanging;

    @XmlAttribute(required = false)
    public Boolean getUnchanging() {
        return unchanging;
    }

    public void setUnchanging(Boolean unchanging) {
        this.unchanging = unchanging;
    }



    private List<ActionLayoutData> actions = Lists.newArrayList();

    // no wrapper
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    public void setActions(List<ActionLayoutData> actionLayoutDatas) {
        this.actions = actionLayoutDatas;
    }


    private FieldSet owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public FieldSet getOwner() {
        return owner;
    }

    public void setOwner(final FieldSet owner) {
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


    @Override
    public String toString() {
        return "PropertyLayoutData{" +
                "id='" + id + '\'' +
                '}';
    }

}
