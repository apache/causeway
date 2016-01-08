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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;

@XmlType(
        name="tab"
        , propOrder = {
                "name"
                , "left"
                , "middle"
                , "right"
        }
)
public class Tab implements ColumnHolder, Serializable {

    private static final long serialVersionUID = 1L;

    private String name;

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private Column left = new Column();

    @XmlElement(required = true)
    public Column getLeft() {
        return left;
    }

    public void setLeft(final Column left) {
        this.left = left;
    }


    private Column middle;

    @XmlElement(required = false)
    public Column getMiddle() {
        return middle;
    }

    public void setMiddle(final Column middle) {
        this.middle = middle;
    }


    private Column right;

    @XmlElement(required = false)
    public Column getRight() {
        return right;
    }

    public void setRight(final Column right) {
        this.right = right;
    }



    private TabGroup owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public TabGroup getOwner() {
        return owner;
    }

    public void setOwner(final TabGroup owner) {
        this.owner = owner;
    }

    /**
     * Aggregates the contents of all collections on this tab.
     */
    @Programmatic
    public List<ColumnContent> getContents() {
        final List<ColumnContent> contents = Lists.newArrayList();
        appendContent(contents, getLeft());
        appendContent(contents, getMiddle());
        appendContent(contents, getRight());
        return contents;
    }

    private static void appendContent(final List<ColumnContent> contents, final Column column) {
        if(column == null) {
            return;
        }
        final List<PropertyGroup> propertyGroups = column.getPropertyGroups();
        if(propertyGroups != null) {
            contents.addAll(propertyGroups);
        }
        final List<CollectionLayoutMetadata> collectionLayoutMetadatas = column.getCollections();
        if(collectionLayoutMetadatas != null) {
            contents.addAll(collectionLayoutMetadatas);
        }
    }

    public static class Predicates {
        public static Predicate<Tab> notEmpty() {
            return new Predicate<Tab>() {
                @Override
                public boolean apply(final Tab tab) {
                    return !tab.getContents().isEmpty();
                }
            };
        }
    }
}
