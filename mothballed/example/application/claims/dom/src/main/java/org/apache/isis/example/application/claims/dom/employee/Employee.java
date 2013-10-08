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
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.example.application.claims.dom.claim.Approver;
import org.apache.isis.example.application.claims.dom.claim.Claimant;

public class Employee extends AbstractDomainObject implements Claimant, Approver {

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

    public void setName(final String lastName) {
        this.name = lastName;
    }

    // }}

    // {{ Approver
    private Approver defaultApprover;

    @Override
    @MemberOrder(sequence = "2")
    public Approver getDefaultApprover() {
        return defaultApprover;
    }

    public void setDefaultApprover(final Approver approver) {
        this.defaultApprover = approver;
    }

    public String validateDefaultApprover(final Approver approver) {
        if (approver == null) {
            return null;
        }
        if (approver == this) {
            return "Cannot act as own approver";
        }
        return null;
    }

    // }}

    // {{ Limit
    private Integer limit;

    @Optional
    @MemberOrder(sequence = "1")
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final Integer limit) {
        this.limit = limit;
    }
    // }}

}
