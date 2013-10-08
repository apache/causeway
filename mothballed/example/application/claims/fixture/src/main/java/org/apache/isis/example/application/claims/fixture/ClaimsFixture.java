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

package org.apache.isis.example.application.claims.fixture;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;
import org.apache.isis.example.application.claims.dom.claim.Claim;
import org.apache.isis.example.application.claims.dom.claim.ClaimItem;
import org.apache.isis.example.application.claims.dom.employee.Employee;

public class ClaimsFixture extends AbstractFixture {

    @Override
    public void install() {
        final Employee fred = createEmployee("Fred Smith", null);
        final Employee tom = createEmployee("Tom Brown", fred);
        createEmployee("Sam Jones", fred);

        Claim claim = createClaim(tom, -16, "Meeting with client");
        addItem(claim, -16, 38.50, "Lunch with client");
        addItem(claim, -16, 16.50, "Euston - Mayfair (return)");

        claim = createClaim(tom, -18, "Meeting in city office");
        addItem(claim, -16, 18.00, "Car parking");
        addItem(claim, -16, 26.50, "Reading - London (return)");

        claim = createClaim(fred, -14, "Meeting at clients");
        addItem(claim, -14, 18.00, "Car parking");
        addItem(claim, -14, 26.50, "Reading - London (return)");

    }

    private Employee createEmployee(final String name, final Employee approver) {
        Employee claimant;
        claimant = newTransientInstance(Employee.class);
        claimant.setName(name);
        claimant.setDefaultApprover(approver);
        persist(claimant);
        return claimant;
    }

    private Claim createClaim(final Employee claimant, final int days, final String description) {
        final Claim claim = newTransientInstance(Claim.class);
        claim.setClaimant(claimant);
        claim.setDescription(description);
        Date date = new Date();
        date = date.add(0, 0, days);
        claim.setDate(date);
        persist(claim);
        return claim;
    }

    private void addItem(final Claim claim, final int days, final double amount, final String description) {
        final ClaimItem claimItem = newTransientInstance(ClaimItem.class);
        Date date = new Date();
        date = date.add(0, 0, days);
        claimItem.setDateIncurred(date);
        claimItem.setDescription(description);
        claimItem.setAmount(new Money(amount, "USD"));
        persist(claimItem);
        claim.addToItems(claimItem);
    }

}
