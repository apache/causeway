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
package org.apache.isis.runtimes.dflt.objectstores.sql.testsystem.dataclasses.polymorphism;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;

/**
 * This class is the collection point of all the polymorphism and related tests. There is nothing really special about
 * it, it is just a placeholder for the various tests related to polymorphism or recursion (JIRA ISIS-117 and ISIS-118).
 * 
 * @author Kevin
 * 
 */
public class PolyTestClass extends AbstractDomainObject implements PolyTestInterface {
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

    // {{ PolyBaseClass
    // private List<PolyBaseClass> polyBaseClasses = new ArrayList<PolyBaseClass>();

    // @MemberOrder(sequence = "1")
    // public List<PolyBaseClass> getPolyBaseClasses() {
    // return polyBaseClasses;
    // }

    // public void setPolyBaseClasses(final List<PolyBaseClass> polyBaseClasses) {
    // this.polyBaseClasses = polyBaseClasses;
    // }

    // }}

    // {{ PolyTestClass collection
    private List<PolyTestClass> polyTestClasses = new ArrayList<PolyTestClass>();

    public List<PolyTestClass> getPolyTestClasses() {
        return polyTestClasses;
    }

    public void setPolyTestClasses(final List<PolyTestClass> polyTestClasses) {
        this.polyTestClasses = polyTestClasses;
    }

    // }}

    // {{ PolyTestClass property
    private PolyTestInterface polyTestInterface;

    public void setPolyTestInterface(PolyTestInterface polyTestInterface) {
        this.polyTestInterface = polyTestInterface;

    }

    public PolyTestInterface getPolyTestInterface() {
        return polyTestInterface;
    }

    // }}

    // {{ PolyInterfaceType
    private PolyInterface polyInterfaceType;

    public PolyInterface getPolyInterfaceType() {
        return polyInterfaceType;
    }

    public void setPolyInterfaceType(final PolyInterface polyInterfaceType) {
        this.polyInterfaceType = polyInterfaceType;
    }
    // }}

}
