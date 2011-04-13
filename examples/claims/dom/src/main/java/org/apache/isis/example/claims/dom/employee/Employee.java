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

package org.apache.isis.example.claims.dom.employee;

import java.util.Arrays;
import java.util.List;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.example.claims.dom.claim.Approver;
import org.apache.isis.example.claims.dom.claim.Claimant;

public class Employee extends AbstractDomainObject implements Claimant, Approver /* , Locatable */{

    // {{ Title
    @Override
    public String title() {
        return getName();
    }

    // }}

    // {{ Icon
    public String iconName() {
        return getName().replaceAll(" ", "");
    }

    // }}

    // {{ Name
    private String name;

    @MemberOrder(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(String lastName) {
        this.name = lastName;
    }

    // }}

    // {{ Approver
    private Approver approver;

    @Override
    @MemberOrder(sequence = "2")
    public Approver getApprover() {
        return approver;
    }

    public void setApprover(Approver approver) {
        this.approver = approver;
    }

    public String validateApprover(final Approver approver) {
        if (approver == null)
            return null;
        if (approver == this) {
            return "Cannot act as own approver";
        }
        return null;
    }

    // }}

    // // {{ Location
    // private Location location;
    //
    // @Disabled
    // @MemberOrder(sequence = "1")
    // public Location getLocation() {
    // return location;
    // }
    //
    // public void setLocation(final Location location) {
    // this.location = location;
    // }
    // // }}

    // {{ SomeHiddenProperty
    private String someHiddenProperty;

    @Hidden
    @MemberOrder(sequence = "1")
    public String getSomeHiddenProperty() {
        return someHiddenProperty;
    }

    public void setSomeHiddenProperty(final String someHiddenProperty) {
        this.someHiddenProperty = someHiddenProperty;
    }

    // }}

    // {{ SomePropertyWithDefault
    private String somePropertyWithDefault;

    @MemberOrder(sequence = "1")
    public String getSomePropertyWithDefault() {
        return somePropertyWithDefault;
    }

    public void setSomePropertyWithDefault(final String somePropertyWithDefault) {
        this.somePropertyWithDefault = somePropertyWithDefault;
    }

    public String defaultSomePropertyWithDefault() {
        return "Foo";
    }

    // }}

    // {{ SomeActionWithParameterDefaults
    @MemberOrder(sequence = "1")
    public Employee someActionWithParameterDefaults(final int param0, final int param1) {
        setLimit(param0 + param1);
        return this;
    }

    public int default0SomeActionWithParameterDefaults() {
        return 5;
    }

    // }}

    // {{ someActionWithParameterChoices
    @MemberOrder(sequence = "1")
    public Employee someActionWithParameterChoices(final Integer param0, final Integer param1) {
        setLimit(param0 - param1);
        return this;
    }

    public List<Integer> choices0SomeActionWithParameterChoices() {
        return Arrays.asList(1, 2, 3);
    }

    // }}

    // {{ Limit
    private int limit;

    @Optional
    @MemberOrder(sequence = "1")
    public int getLimit() {
        return limit;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }
    // }}

}
