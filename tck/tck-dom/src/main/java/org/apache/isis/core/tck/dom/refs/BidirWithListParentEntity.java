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
import java.util.List;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.Discriminator("BDLP")
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@ObjectType("BDLP")
public class BidirWithListParentEntity extends BaseEntity {

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
    @Persistent(mappedBy="parent")
    private List<BidirWithListChildEntity> children = new ArrayList<BidirWithListChildEntity>();

    @MemberOrder(sequence = "1")
    public List<BidirWithListChildEntity> getChildren() {
        return children;
    }

    public void setChildren(final List<BidirWithListChildEntity> children) {
        this.children = children;
    }
    // }}

    

    // {{ newChild (action)
    public BidirWithListChildEntity newChild(final String name) {
        final BidirWithListChildEntity childEntity = newTransientInstance(BidirWithListChildEntity.class);
        childEntity.setName(name);
        childEntity.setParent(this);
        this.getChildren().add(childEntity);
        persistIfNotAlready(childEntity);
        return childEntity;
    }
    // }}

    
    // {{ removeChild (action)
    public BidirWithListParentEntity removeChild(final BidirWithListChildEntity childEntity) {
        if (getChildren().contains(childEntity)) {
            getChildren().remove(childEntity);
            childEntity.setParent(null);
        }
        return this;
    }

    public List<BidirWithListChildEntity> choices0RemoveChild() {
        return getChildren();
    }
    // }}

}
