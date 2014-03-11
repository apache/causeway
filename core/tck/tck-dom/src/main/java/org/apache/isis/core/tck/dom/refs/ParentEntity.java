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

package org.apache.isis.core.tck.dom.refs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NotPersisted;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@ObjectType("PRNT")
public class ParentEntity extends BaseEntity {

    // {{ Name (also title)
    private String name;
    
    @Title
    @MemberOrder(sequence = "1")
    @Optional
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ HomogeneousCollection
    private List<SimpleEntity> homogeneousCollection = new ArrayList<SimpleEntity>();

    public List<SimpleEntity> getHomogeneousCollection() {
        return homogeneousCollection;
    }

    public void setHomogeneousCollection(final List<SimpleEntity> homogenousCollection) {
        this.homogeneousCollection = homogenousCollection;
    }

    // }}

    // {{ HeterogeneousCollection
    private List<BaseEntity> heterogeneousCollection = new ArrayList<BaseEntity>();

    public List<BaseEntity> getHeterogeneousCollection() {
        return heterogeneousCollection;
    }

    public void setHeterogeneousCollection(final List<BaseEntity> hetrogenousCollection) {
        this.heterogeneousCollection = hetrogenousCollection;
    }

    // }}

    // {{ Children
    private Set<ChildEntity> children = new HashSet<ChildEntity>();

    @MemberOrder(sequence = "1")
    public Set<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(final Set<ChildEntity> children) {
        this.children = children;
    }
    // }}

    
    // {{ NotPersisted
    @NotPersisted
    public List<SimpleEntity> getNotPersisted() {
        throw new org.apache.isis.applib.NonRecoverableException("unexpected call");
    }
    // }}

    

    // {{ newChild (action)
    public ChildEntity newChild(final String name) {
        final ChildEntity childEntity = newTransientInstance(ChildEntity.class);
        childEntity.setName(name);
        childEntity.setParent(this);
        this.getChildren().add(childEntity);
        persistIfNotAlready(childEntity);
        return childEntity;
    }
    // }}

    
    // {{ removeChild (action)
    public ParentEntity removeChild(final ChildEntity childEntity) {
        if (getChildren().contains(childEntity)) {
            getChildren().remove(childEntity);
            childEntity.setParent(null);
        }
        return this;
    }

    public Set<ChildEntity> choices0RemoveChild() {
        return getChildren();
    }
    // }}

}
