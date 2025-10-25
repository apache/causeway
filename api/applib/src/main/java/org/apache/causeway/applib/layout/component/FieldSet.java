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

import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;

import lombok.Getter;
import lombok.Setter;

/**
 * A {@link MemberRegion region} of the page containing a set of
 * related {@link PropertyLayoutData properties} and associated
 * {@link ActionLayoutData actions}.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(name = "fieldSet")
@XmlType(
        name = "fieldSet",
        propOrder = {"name", "actions", "properties", "metadataError"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FieldSet
implements MemberRegion<FieldSetOwner>, ActionLayoutDataOwner, Serializable {
    private static final long serialVersionUID = 1L;

    public FieldSet() {}
    public FieldSet(final String name) {
        setName(name);
    }

    /**
     * As per &lt;div id=&quot;...&quot;&gt;...&lt;/div&gt; : must be unique across entire page.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String id;


    /**
     * Whether this fieldset should be used to hold any unreferenced actions (contributed or &quot;native&quot;).
     *
     * <p>Any layout must have precisely one fieldset or {@link BSCol col} that has this attribute set.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean unreferencedActions;
    @XmlTransient public boolean isUnreferencedActions() {
        return unreferencedActions!=null && unreferencedActions.booleanValue();
    }

    /**
     * Whether this fieldset should be used to hold any unreferenced properties (contributed or &quot;native&quot;).
     *
     * <p>Any grid layout must have precisely one fieldset that has this attribute set.
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private Boolean unreferencedProperties;
    @XmlTransient public boolean isUnreferencedProperties() {
        return unreferencedProperties!=null && unreferencedProperties.booleanValue();
    }

    /**
     * Corresponds to the {@link PropertyLayout#fieldSetName()} (when applied to properties).
     */
    @XmlAttribute(required = false)
    @Getter @Setter
    private String name;

    @XmlElement(name = "action", required = false)
    @Getter
    private final List<ActionLayoutData> actions = new ArrayList<>();

    @XmlElement(name = "property", required = false)
    @Getter
    private final List<PropertyLayoutData> properties = new ArrayList<>();

    /**
     * Owner.
     * <p>Set programmatically by framework after reading in from XML.
     */
    @XmlTransient
    @Getter @Setter
    private FieldSetOwner owner;

    /**
     * For diagnostics; populated by the framework if and only if a metadata error.
     */
    @XmlElement(required = false)
    @Getter @Setter
    private String metadataError;

    @Override public String toString() {
        return "FieldSet{" +
                "id='" + id + '\'' +
                '}';
    }

}
