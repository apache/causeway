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
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.ObjectType;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.annotation.Title;

@javax.jdo.annotations.PersistenceCapable(identityType=IdentityType.DATASTORE)
@javax.jdo.annotations.Discriminator("PCCH")
@javax.jdo.annotations.DatastoreIdentity(strategy=javax.jdo.annotations.IdGeneratorStrategy.IDENTITY)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@ObjectType("PCCH")
public abstract class PolyClassChildEntity extends AbstractDomainObject {

    // {{ Parent (title #1)
    private PolyClassParentEntity parent;

    @Title(sequence="1", append="-")
    @MemberOrder(sequence = "1")
    @Optional
    public PolyClassParentEntity getParent() {
        return parent;
    }

    public void setParent(final PolyClassParentEntity parent) {
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
    public PolyClassChildEntity moveTo(final PolyClassParentEntity newParent) {
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

    public PolyClassParentEntity default0MoveTo() {
        return getParent();
    }

    public List<PolyClassParentEntity> choices0MoveTo() {
        return getContainer().allInstances(PolyClassParentEntity.class);
    }
    // }}

}
