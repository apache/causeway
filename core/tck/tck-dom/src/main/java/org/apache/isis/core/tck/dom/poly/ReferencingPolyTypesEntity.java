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

/**
 * 
 */
package org.apache.isis.core.tck.dom.poly;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;

/**
 * This class is the collection point of all the polymorphism and related tests.
 * There is nothing really special about it, it is just a placeholder for the
 * various tests related to polymorphism or recursion (JIRA ISIS-117 and
 * ISIS-118).
 * 
 * @author Kevin
 * 
 */
public class ReferencingPolyTypesEntity extends AbstractDomainObject {
    
    public String title() {
        return string;
    }

    // {{ String type
    private String string;

    public String getString() {
        return string;
    }

    public void setString(final String string) {
        this.string = string;
    }

    // }}

    // {{ PolyBaseClass collection
    private List<StringBaseEntity> polyBaseClasses = new ArrayList<StringBaseEntity>();

    public List<StringBaseEntity> getPolyBaseClasses() {
        return polyBaseClasses;
    }

    public void setPolyBaseClasses(final List<StringBaseEntity> polyBaseClasses) {
        this.polyBaseClasses = polyBaseClasses;
    }

    // }}

    // {{ PolyInterfaceType: Can we store / restore properties by interface?
    private Stringable polyInterfaceType;

    public Stringable getPolyInterfaceType() {
        return polyInterfaceType;
    }

    public void setPolyInterfaceType(final Stringable polyInterfaceType) {
        this.polyInterfaceType = polyInterfaceType;
    }

    // }}

    // {{ PolyInterface collection
    private List<Stringable> stringables = new ArrayList<Stringable>();

    public List<Stringable> getPolyInterfaces() {
        return stringables;
    }

    public void setPolyInterfaces(final List<Stringable> stringables) {
        this.stringables = stringables;
    }

    // }}

    // {{ PolySelfRefClass: Can we store / restore classes that contain
    // self-referencing collections?
    private SelfReferencingEntity selfReferencingEntity;

    public SelfReferencingEntity getPolySelfRefClass() {
        return selfReferencingEntity;
    }

    public void setPolySelfRefClass(final SelfReferencingEntity selfReferencingEntity) {
        this.selfReferencingEntity = selfReferencingEntity;
    }
    // }}

}
