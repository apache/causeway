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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.MemberGroupLayout;
import org.apache.isis.applib.annotation.Programmatic;

@XmlType(
        propOrder = {
                "propertyGroups"
                , "collections"
        }
)
public class ColumnMetadata implements Serializable, Owner, Owned<ColumnOwner> {

    private static final long serialVersionUID = 1L;

    public ColumnMetadata() {
    }

    public ColumnMetadata(final int span) {
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



    private List<PropertyGroupMetadata> propertyGroups = Lists.newArrayList();

    // no wrapper
    @XmlElement(name = "propertyGroup", required = false)
    public List<PropertyGroupMetadata> getPropertyGroups() {
        return propertyGroups;
    }

    public void setPropertyGroups(final List<PropertyGroupMetadata> propertyGroups) {
        this.propertyGroups = propertyGroups;
    }


    private List<CollectionLayoutMetadata> collections = Lists.newArrayList();

    // no wrapper
    @XmlElement(name = "collection", required = false)
    public List<CollectionLayoutMetadata> getCollections() {
        return collections;
    }

    public void setCollections(final List<CollectionLayoutMetadata> collections) {
        this.collections = collections;
    }


    private ColumnOwner owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public ColumnOwner getOwner() {
        return owner;
    }

    public void setOwner(final ColumnOwner owner) {
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




    public enum Hint {
        LEFT,
        MIDDLE,
        RIGHT;

        public int from(MemberGroupLayout.ColumnSpans columnSpans) {
            if(this == LEFT) return columnSpans.getLeft();
            if(this == MIDDLE) return columnSpans.getMiddle();
            if(this == RIGHT) return columnSpans.getRight();
            throw new IllegalStateException();
        }

        public ColumnMetadata from(final TabMetadata tabMetadata) {
            if(tabMetadata == null) {
                return null;
            }
            if(this == LEFT) return tabMetadata.getLeft();
            if(this == MIDDLE) return tabMetadata.getMiddle();
            if(this == RIGHT) return tabMetadata.getRight();
            throw new IllegalStateException();
        }

    }
}
