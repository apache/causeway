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
package org.apache.isis.testdomain.model.good;

import java.util.List;
import java.util.Set;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.MinLength;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.annotation.PropertyLayout;
import org.apache.isis.incubator.model.applib.annotation.Model;

import lombok.Getter;
import lombok.Setter;


/**
 * 
 * <h1>Support Matrix</h1>
 * <pre>
 * Prefix     Obj Prop Coll Act Param
 * autoComplete    x             x
 * choices         x             x
 * default         x             x
 * disable     x   x    x    x   
 * hide            x    x    x   
 * validate    x             x   x
 * </pre>
 *
 */
@DomainObject(nature = Nature.VIEW_MODEL)
public class ProperMemberSupport {

    // -- PROPER ACTION

    @Action
    public void myAction(String p0, String p1) {

    }

    @Model
    public String disableMyAction() {
        return null;
    }
    
    @Model
    public boolean hideMyAction() {
        return false;
    }

    @Model
    public String validateMyAction(String p0, String p1) {
        return null;
    }
    
    @Model
    public Set<String> autoComplete0MyAction(@MinLength(3) String search) {
        return null;
    }

    @Model
    public Set<String> autoComplete1MyAction(@MinLength(3) String search) {
        return null;
    }
    
    @Model
    public Set<String> choices0MyAction() {
        return null;
    }

    @Model
    public Set<String> choices1MyAction() {
        return null;
    }
    
// variant with dependent arg
//    @Model
//    public Set<String> choices1MyAction(String p0) {
//        return null;
//    }
    
    @Model
    public String default0MyAction() {
        return null;
    }

    @Model
    public String default1MyAction() {
        return null;
    }

    @Model
    public String validate0MyAction(String p0) {
        return null;
    }

    @Model
    public String validate1MyAction(String p1) {
        return null;
    }

    // -- PROPER PROPERTY

    @Property
    @PropertyLayout(named = "foo", describedAs = "bar")
    @Getter @Setter private String myProp;

    @Model
    public Set<String> autoCompleteMyProp(@MinLength(3) String search) {
        return null;
    }
    
    @Model
    public Set<String> choicesMyProp() {
        return null;
    }
    
    @Model
    public String defaultMyProp() {
        return "";
    }

    @Model
    public String disableMyProp() {
        return null;
    }
    
    @Model
    public boolean hideMyProp() {
        return false;
    }

    // -- PROPER COLLECTION

    @Collection
    @CollectionLayout(named = "foo", describedAs = "bar")
    @Getter @Setter private List<String> myColl;

    @Model
    public String disableMyColl() {
        return null;
    }
    
    @Model
    public boolean hideMyColl() {
        return false;
    }

}
