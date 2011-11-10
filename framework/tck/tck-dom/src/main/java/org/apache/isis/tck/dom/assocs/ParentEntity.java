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

package org.apache.isis.tck.dom.assocs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.applib.util.TitleBuffer;

public class ParentEntity extends AbstractDomainObject {

    // {{ Identification
    public String title() {
        final TitleBuffer buf = new TitleBuffer();
        buf.append(getName());
        return buf.toString();
    }
    // }}


    // {{ Name
    private String name;

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
    private List<ChildEntity> children = new ArrayList<ChildEntity>();

    @MemberOrder(sequence = "1")
    public List<ChildEntity> getChildren() {
        return children;
    }

    public void setChildren(final List<ChildEntity> children) {
        this.children = children;
    }
    // }}

    // {{ newChild (action)
    public ChildEntity newChild(String name) {
        final ChildEntity childEntity = newTransientInstance(ChildEntity.class);
        childEntity.setName(name);
        childEntity.setParent(this);
        this.getChildren().add(childEntity);
        persistIfNotAlready(childEntity);
        return childEntity;
    }
    // }}

    // {{ removeChild (action)
    public ParentEntity removeChild(ChildEntity childEntity) {
        if(children.contains(childEntity)) {
            children.remove(childEntity);
            childEntity.setParent(null);
        }
        return this;
    }
    public List<ChildEntity> choices0RemoveChild() {
        return getChildren();
    }
    // }}
    
}
