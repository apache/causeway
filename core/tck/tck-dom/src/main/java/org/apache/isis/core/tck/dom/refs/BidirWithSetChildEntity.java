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

import java.util.List;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.Discriminator("BDSC")
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@ObjectType("BDSC")
public class BidirWithSetChildEntity extends AbstractDomainObject {

    // {{ Parent (title #1)
    private BidirWithSetParentEntity parent;

    @Title(sequence="1", append="-")
    @MemberOrder(sequence = "1")
    @Optional
    public BidirWithSetParentEntity getParent() {
        return parent;
    }

    public void setParent(final BidirWithSetParentEntity parent) {
        this.parent = parent;
    }

    // }}

    // {{ Name  (title #2)
    private String name;

    @Title(sequence="2")
    @MemberOrder(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    // }}

    // {{ moveTo
    @MemberOrder(sequence = "1")
    public BidirWithSetChildEntity moveTo(final BidirWithSetParentEntity newParent) {
        if (newParent == getParent()) {
            // nothing to do
            return this;
        }
        if (getParent() != null) {
            getParent().removeChild(this);
        }
        newParent.getChildren().add(this);
        this.setParent(newParent);
        return this;
    }

    public BidirWithSetParentEntity default0MoveTo() {
        return getParent();
    }

    public List<BidirWithSetParentEntity> choices0MoveTo() {
        return getContainer().allInstances(BidirWithSetParentEntity.class);
    }
    // }}

}
