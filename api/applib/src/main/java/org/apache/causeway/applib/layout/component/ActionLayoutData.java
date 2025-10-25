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

import org.apache.causeway.applib.annotation.PromptStyle;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.links.Link;

import lombok.Getter;
import lombok.Setter;

/**
 * Describes the layout of a single action, broadly corresponding to
 * {@link org.apache.causeway.applib.annotation.ActionLayout}.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(name = "action")
@XmlType(name = "action",
    propOrder = {"named", "describedAs", "metadataError", "link"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ActionLayoutData implements Serializable, Owned<ActionLayoutDataOwner>, HasCssClass, HasCssClassFa,
HasDescribedAs, HasHidden, HasNamed {
    private static final long serialVersionUID = 1L;

    public ActionLayoutData() {}
    public ActionLayoutData(final String id) {
        setId(id);
    }

    /**
     * Method name.
     * <p>Overloaded methods are not supported.
     */
    @XmlAttribute(name="id", required = true)
    @Getter @Setter
    private String id;

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

    @XmlAttribute(required = false)
    @Getter @Setter
    private Where hidden;

    @XmlElement(required = false)
    @Getter @Setter
    private String named;

    @XmlAttribute(required = false)
    @Getter @Setter
    private org.apache.causeway.applib.annotation.ActionLayout.Position position;

    @XmlAttribute(required = false)
    @Getter @Setter
    private PromptStyle promptStyle;

    /**
     * Owner.
     * <p>Set programmatically by framework after reading in from XML.
     */
    @XmlTransient
    @Getter @Setter
    private ActionLayoutDataOwner owner;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private String metadataError;

    /**
     * The link to access this resource from the REST API (Restful Objects viewer).
     *
     * <p>Populated by the framework automatically.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private Link link;

    @Override public String toString() {
        return "ActionLayoutData{" +
                "id='" + id + '\'' +
                '}';
    }

}
