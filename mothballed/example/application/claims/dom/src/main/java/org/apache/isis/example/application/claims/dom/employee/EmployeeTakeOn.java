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
package org.apache.isis.example.application.claims.dom.employee;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.example.application.claims.dom.claim.Approver;

public class EmployeeTakeOn extends AbstractDomainObject {

    // {{ Lifecycle methods
    public void created() {
        state = "page1";
    }

    // }}

    // {{ Name
    private String name;

    @MemberOrder(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean hideName() {
        return !state.equals("page1") && isNotLast();
    }

    public String disableName() {
        return disableToConfirmIfLast();
    }

    // }}

    // {{ Approver
    private Approver approver;

    @MemberOrder(sequence = "2")
    @Optional
    public Approver getApprover() {
        return approver;
    }

    public void setApprover(final Approver approver) {
        this.approver = approver;
    }

    public boolean hideApprover() {
        return !state.equals("page2") && isNotLast();
    }

    public String disableApprover() {
        return disableToConfirmIfLast();
    }

    // }}

    // {{ next
    @MemberOrder(sequence = "1")
    public EmployeeTakeOn next() {
        if (state.equals("page1")) {
            state = "page2";
        } else if (state.equals("page2")) {
            state = "page3";
        }
        return this;
    }

    public boolean hideNext() {
        return isLast();
    }

    // }}

    // {{ finish
    @MemberOrder(sequence = "2")
    public Employee finish() {
        final Employee employee = newTransientInstance(Employee.class);
        employee.setName(getName());
        employee.setDefaultApprover(approver);
        persist(employee);
        return employee;
    }

    public boolean hideFinish() {
        return isNotLast();
    }

    // }}

    // {{ State
    private String state;

    @MemberOrder(sequence = "1")
    @Hidden
    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    // }}

    // {{ NotLast
    @MemberOrder(sequence = "1")
    @Hidden
    public boolean isNotLast() {
        return !isLast();
    }

    // }}

    // {{ Last
    @MemberOrder(sequence = "1")
    @Hidden
    public boolean isLast() {
        return state.equals("page3");
    }

    private String disableToConfirmIfLast() {
        return isLast() ? "confirm" : null;
    }
    // }}

}
