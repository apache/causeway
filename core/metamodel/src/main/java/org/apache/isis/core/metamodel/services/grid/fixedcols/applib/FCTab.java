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
package org.apache.isis.core.metamodel.services.grid.fixedcols.applib;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.MemberRegion;
import org.apache.isis.applib.layout.component.Owned;
import org.apache.isis.applib.layout.component.FieldSet;

@XmlType(
        name="tab"
        , propOrder = {
                "name"
                , "left"
                , "middle"
                , "right"
        }
)
public class FCTab implements FCColumnOwner, Serializable, Owned<FCTabGroup> {

    private static final long serialVersionUID = 1L;

    private String name;

    @XmlAttribute(required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    private FCColumn left = new FCColumn();

    @XmlElement(required = true)
    public FCColumn getLeft() {
        return left;
    }

    public void setLeft(final FCColumn left) {
        this.left = left;
        left.setHint(Hint.LEFT);
    }


    private FCColumn middle;

    @XmlElement(required = false)
    public FCColumn getMiddle() {
        return middle;
    }

    public void setMiddle(final FCColumn middle) {
        this.middle = middle;
        middle.setHint(Hint.MIDDLE);
    }


    private FCColumn right;

    @XmlElement(required = false)
    public FCColumn getRight() {
        return right;
    }

    public void setRight(final FCColumn right) {
        this.right = right;
        right.setHint(Hint.RIGHT);
    }



    private FCTabGroup owner;
    /**
     * Owner.
     *
     * <p>
     *     Set programmatically by framework after reading in from XML.
     * </p>
     */
    @XmlTransient
    public FCTabGroup getOwner() {
        return owner;
    }

    public void setOwner(final FCTabGroup owner) {
        this.owner = owner;
    }

    /**
     * Aggregates the contents of all collections on this tab.
     */
    @Programmatic
    public List<MemberRegion> getContents() {
        final List<MemberRegion> contents = Lists.newArrayList();
        appendContent(contents, getLeft());
        appendContent(contents, getMiddle());
        appendContent(contents, getRight());
        return contents;
    }



    private static void appendContent(final List<MemberRegion> contents, final FCColumn FCColumn) {
        if(FCColumn == null) {
            return;
        }
        final List<FieldSet> fieldSets = FCColumn.getFieldSets();
        if(fieldSets != null) {
            contents.addAll(fieldSets);
        }
        final List<CollectionLayoutData> collectionLayoutDatas = FCColumn.getCollections();
        if(collectionLayoutDatas != null) {
            contents.addAll(collectionLayoutDatas);
        }
    }

    public static class Predicates {
        public static Predicate<FCTab> notEmpty() {
            return new Predicate<FCTab>() {
                @Override
                public boolean apply(final FCTab FCTab) {
                    return !FCTab.getContents().isEmpty();
                }
            };
        }
    }
}
