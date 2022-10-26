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

import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.applib.layout.grid.bootstrap.BSCol;

/**
 * A {@link MemberRegion region} of the page containing a set of
 * related {@link PropertyLayoutData properties} and associated
 * {@link ActionLayoutData actions}.
 *
 * @since 1.x {@index}
 */
@XmlRootElement(
        name = "fieldSet"
        )
@XmlType(
        name = "fieldSet"
        , propOrder = {
                "name"
                , "actions"
                , "properties"
                , "metadataError"
        }
        )
public class FieldSet
implements MemberRegion<FieldSetOwner>,
ActionLayoutDataOwner,
Serializable {

    private static final long serialVersionUID = 1L;

    public FieldSet() {
    }

    public FieldSet(final String name) {
        setName(name);
    }

    
    
    private String id;

    /**
     * As per &lt;div id=&quot;...&quot;&gt;...&lt;/div&gt; : must be unique across entire page.
     */
    @XmlAttribute(required = false)
    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }



    private Boolean unreferencedActions;

    /**
     * Whether this fieldset should be used to hold any unreferenced actions (contributed or &quot;native&quot;).
     *
     * <p>
     *     Any layout must have precisely one fieldset or {@link BSCol col} that has this attribute set.
     * </p>
     */
    @XmlAttribute(required = false)
    public Boolean isUnreferencedActions() {
        return unreferencedActions;
    }

    public void setUnreferencedActions(final Boolean unreferencedActions) {
        this.unreferencedActions = unreferencedActions;
    }


    private Boolean unreferencedProperties;
    /**
     * Whether this fieldset should be used to hold any unreferenced properties (contributed or &quot;native&quot;).
     *
     * <p>
     *     Any grid layout must have precisely one fieldset that has this attribute set.
     * </p>
     */
    @XmlAttribute(required = false)
    public Boolean isUnreferencedProperties() {
        return unreferencedProperties;
    }

    public void setUnreferencedProperties(final Boolean unreferencedProperties) {
        this.unreferencedProperties = unreferencedProperties;
    }




    private String name;

    /**
     * Corresponds to the {@link PropertyLayout#fieldSetName()} (when applied to properties).
     */
    @XmlAttribute(required = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private List<ActionLayoutData> actions = new ArrayList<>();

    // no wrapper
    @Override
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<ActionLayoutData> actionLayoutDatas) {
        this.actions = actionLayoutDatas;
    }



    private List<PropertyLayoutData> properties = new ArrayList<>();

    // no wrapper; required=false because may be auto-generated
    @XmlElement(name = "property", required = false)
    public List<PropertyLayoutData> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyLayoutData> properties) {
        this.properties = properties;
    }


    private FieldSetOwner owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @Override
    @XmlTransient
    public FieldSetOwner getOwner() {
        return owner;
    }

    public void setOwner(final FieldSetOwner owner) {
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

    @Override public String toString() {
        return "FieldSet{" +
                "id='" + id + '\'' +
                '}';
    }

}
