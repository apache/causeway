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


package org.apache.isis.support.prototype.dom.employee;

import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.support.prototype.dom.claim.Approver;
import org.apache.isis.support.prototype.dom.claim.Claimant;

public class Employee extends AbstractDomainObject implements Claimant,
        Approver /* , Locatable */{

    // {{ Title
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

    @MemberOrder(sequence = "2")
    public Approver getApprover() {
        return approver;
    }

    public void setApprover(Approver approver) {
        this.approver = approver;
    }

    // }}

//    // {{ Location
//    private Location location;
//
//    @Disabled
//    @MemberOrder(sequence = "1")
//    public Location getLocation() {
//        return location;
//    }
//
//    public void setLocation(final Location location) {
//        this.location = location;
//    }
//    // }}
    

    
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

