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
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@XmlType(
        propOrder = {
                "span"
                , "content"
        }
)
public class Column implements Serializable {

    private static final long serialVersionUID = 1L;

    public Column() {
    }

    public Column(final int span) {
        setSpan(span);
    }

    private int span = 4;

    @XmlAttribute(required = true)
    public int getSpan() {
        return span;
    }

    public void setSpan(final int span) {
        this.span = span;
    }



    private List<ColumnContent> content = Lists.newArrayList();

    @XmlElements({
        @XmlElement(name = "propertyGroup", required = false, type = PropertyGroup.class),
        @XmlElement(name = "collection", required = false, type = Collection.class)

    })
    public List<ColumnContent> getContent() {
        return content;
    }

    public void setContent(List<ColumnContent> content) {
        this.content = content;
    }

    @XmlTransient
    public Iterable<PropertyGroup> getPropertyGroups() {
        return Iterables.transform(
                        Iterables.filter(getContent(), Util.is(PropertyGroup.class)),
                        Util.cast(PropertyGroup.class));
    }
    @XmlTransient
    public Iterable<Collection> getCollections() {
        return Iterables.transform(
                        Iterables.filter(getContent(), Util.is(Collection.class)),
                        Util.cast(Collection.class));
    }




    private Tab owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public Tab getOwner() {
        return owner;
    }

    public void setOwner(final Tab owner) {
        this.owner = owner;
    }


}
