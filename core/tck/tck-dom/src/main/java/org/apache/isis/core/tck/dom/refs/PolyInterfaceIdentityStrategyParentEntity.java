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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.Discriminator("PIIP")
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@ObjectType("PIIP")
public class PolyInterfaceIdentityStrategyParentEntity extends BaseEntity {

    
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


    // {{ Children
    @Persistent
    @Join
    @Extension(vendorName="datanucleus", key="mapping-strategy", value="identity")
    private Set<PolyInterfaceIdentityStrategy> children = new HashSet<PolyInterfaceIdentityStrategy>();

    @MemberOrder(sequence = "1")
    public Set<PolyInterfaceIdentityStrategy> getChildren() {
        return children;
    }

    public void setChildren(final Set<PolyInterfaceIdentityStrategy> children) {
        this.children = children;
    }
    // }}

    
    // {{ newSubtype1 (action)
    public PolyInterfaceIdentityStrategySubtype1Entity newSubtype1(final String name, int foo) {
        final PolyInterfaceIdentityStrategySubtype1Entity childEntity = newTransientInstance(PolyInterfaceIdentityStrategySubtype1Entity.class);
        childEntity.setName(name);
        childEntity.setFoo(foo);
        childEntity.setParent(this);
        this.getChildren().add(childEntity);
        persistIfNotAlready(childEntity);
        return childEntity;
    }
    // }}

    // {{ newSubtype2 (action)
    public PolyInterfaceIdentityStrategySubtype2Entity newSubtype2(final String name, String bar) {
        final PolyInterfaceIdentityStrategySubtype2Entity childEntity = newTransientInstance(PolyInterfaceIdentityStrategySubtype2Entity.class);
        childEntity.setName(name);
        childEntity.setParent(this);
        childEntity.setBar(bar);
        this.getChildren().add(childEntity);
        persistIfNotAlready(childEntity);
        return childEntity;
    }
    // }}
    
    // {{ newSubtype3 (action)
    public PolyInterfaceIdentityStrategySubtype3Entity newSubtype3(final String name, BigDecimal boz) {
        final PolyInterfaceIdentityStrategySubtype3Entity childEntity = newTransientInstance(PolyInterfaceIdentityStrategySubtype3Entity.class);
        childEntity.setName(name);
        childEntity.setParent(this);
        childEntity.setBoz(boz);
        this.getChildren().add(childEntity);
        persistIfNotAlready(childEntity);
        return childEntity;
    }
    // }}
    
    // {{ removeChild (action)
    public PolyInterfaceIdentityStrategyParentEntity removeChild(final PolyInterfaceIdentityStrategy childEntity) {
        if (getChildren().contains(childEntity)) {
            getChildren().remove(childEntity);
            childEntity.setParent(null);
        }
        return this;
    }

    public List<PolyInterfaceIdentityStrategy> choices0RemoveChild() {
        return Arrays.asList(getChildren().toArray(new PolyInterfaceIdentityStrategy[0]));
    }
    // }}

}
