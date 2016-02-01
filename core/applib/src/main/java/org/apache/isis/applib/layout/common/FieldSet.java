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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Programmatic;

/**
 * A {@link MemberRegion region} of the page containing a set of
 * related {@link PropertyLayoutData properties} and associated
 * {@link ActionLayoutData actions}.
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
        }
)
public class FieldSet implements MemberRegion, ActionLayoutDataOwner, Serializable {

    private static final long serialVersionUID = 1L;

    public FieldSet() {
    }

    public FieldSet(final String name) {
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



    private List<ActionLayoutData> actions = Lists.newArrayList();

    // no wrapper
    @XmlElement(name = "action", required = false)
    public List<ActionLayoutData> getActions() {
        return actions;
    }

    public void setActions(List<ActionLayoutData> actionLayoutDatas) {
        this.actions = actionLayoutDatas;
    }



    private List<PropertyLayoutData> properties = new ArrayList<PropertyLayoutData>() {{
        add(new PropertyLayoutData());
    }};

    @XmlElement(name = "property", required = true)
    public List<PropertyLayoutData> getProperties() {
        return properties;
    }

    public void setProperties(List<PropertyLayoutData> properties) {
        this.properties = properties;
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




    private String path;

    @Programmatic
    @XmlTransient
    public String getPath() {
        return path;
    }

    @Programmatic
    public void setPath(final String path) {
        this.path = path;
    }




    public static class Util {
        private Util(){}
        public static Function<? super FieldSet, String> nameOf() {
            return new Function<FieldSet, String>() {
                @Nullable @Override
                public String apply(@Nullable final FieldSet fieldSet) {
                    return fieldSet.getName();
                }
            };
        }
    }

}
