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
package org.apache.causeway.core.runtimeservices.wrapper.dom.employees;

import org.apache.causeway.applib.annotation.PropertyLayout;
import org.apache.causeway.core.runtimeservices.wrapper.dom.claims.Approver;
import org.apache.causeway.core.runtimeservices.wrapper.dom.claims.Claimant;

public class Employee implements Claimant, Approver {

    public String title() {
        return getName();
    }


    // {{ Name
    private String name;

    @PropertyLayout(sequence = "1")
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void modifyName(final String name) {
        setName(name);
    }

    public void clearName() {
        setName(null);
    }

    public boolean whetherHideName;

    public boolean hideName() {
        return whetherHideName;
    }

    public String reasonDisableName;

    public String disableName() {
        return reasonDisableName;
    }

    public String reasonValidateName;

    public String validateName(final String name) {
        return reasonValidateName;
    }

    // }}

    // {{ Password
    private String password;

    @PropertyLayout(sequence = "2")
    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    // }}

    // {{ Approver
    private Approver approver;

    @Override
    @PropertyLayout(sequence = "2")
    public Approver getApprover() {
        return approver;
    }

    public void setApprover(final Approver approver) {
        this.approver = approver;
    }

    // }}

    // {{ injected: EmployeeRepository
    private EmployeeRepository employeeRepository;

    public EmployeeRepository getEmployeeRepository() {
        return employeeRepository;
    }

    public void setEmployeeRepository(final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    // }}

}
