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
package org.apache.isis.applib.layout.v1_0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Function;

import org.apache.isis.applib.annotation.MemberOrder;

@XmlType(
        propOrder = {
                "name"
                , "actions"
                , "properties"
        }
)
public class PropertyGroup implements ColumnContent, ActionHolder, Serializable {

    private static final long serialVersionUID = 1L;

    public PropertyGroup() {
    }

    public PropertyGroup(final String name) {
        setName(name);
    }

    private String name;

    /**
     * Corresponds to the {@link MemberOrder#name()} (when applied to properties).
     */
    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private List<ActionLayoutMetadata> actions;

    @XmlElementWrapper(required = false)
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutMetadata> getActions() {
        return actions;
    }

    public void setActions(List<ActionLayoutMetadata> actionLayoutMetadatas) {
        this.actions = actionLayoutMetadatas;
    }



    // must be at least one property in the property group
    private List<PropertyLayoutMetadata> properties = new ArrayList<PropertyLayoutMetadata>() {{
        add(new PropertyLayoutMetadata());
    }};

    @XmlElement(name = "property", required = true)
    public List<PropertyLayoutMetadata> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyLayoutMetadata> properties) {
        this.properties = properties;
    }


    private Column owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public Column getOwner() {
        return owner;
    }

    public void setOwner(final Column owner) {
        this.owner = owner;
    }


    public static class Util {
        private Util(){}
        public static Function<? super PropertyGroup, String> nameOf() {
            return new Function<PropertyGroup, String>() {
                @Nullable @Override
                public String apply(@Nullable final PropertyGroup propertyGroup) {
                    return propertyGroup.getName();
                }
            };
        }
    }

}
