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


package org.apache.isis.example.expenses.services.inmemory;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.example.expenses.claims.Claim;
import org.apache.isis.example.expenses.claims.ClaimRepositoryAbstract;
import org.apache.isis.example.expenses.claims.ClaimStatus;
import org.apache.isis.example.expenses.claims.ExpenseItem;
import org.apache.isis.example.expenses.claims.ExpenseType;
import org.apache.isis.example.expenses.claims.items.AbstractExpenseItem;
import org.apache.isis.example.expenses.employee.Employee;

import java.util.ArrayList;
import java.util.List;


public class ClaimRepositoryInMemory extends ClaimRepositoryAbstract {

    @SuppressWarnings("unused")
    private Integer maxClaimsToRetrieve;

    @Hidden
    public List<Claim> findClaims(final Employee employee, final ClaimStatus status, final String description) {
        return allMatches(Claim.class, new Filter<Claim>() {
            public boolean accept(final Claim claim) {
                return (employee == null || claim.getClaimant() == employee) && (status == null || claim.getStatus() == status)
                        && (description == null || claim.getDescription().contains(description));
            }
        });
    }

    @Hidden
    public List<Claim> findRecentClaims(final Employee employee) {
        maxClaimsToRetrieve = Integer.valueOf(MAX_ITEMS);
        final List<Claim> foundClaims = findClaims(employee, null, null);
        maxClaimsToRetrieve = null;
        return foundClaims;
    }

    @Hidden
    public List<Claim> findClaimsAwaitingApprovalBy(final Employee approver) {
        return allMatches(Claim.class, new Filter<Claim>() {
            public boolean accept(final Claim claim) {
                return claim.getStatus().isSubmitted() && claim.getApprover() == approver;
            }
        });
    }

    @Override
    @Hidden
    public List<ExpenseItem> findExpenseItemsOfType(final Employee employee, final ExpenseType type) {
        final List<ExpenseItem> items = new ArrayList<ExpenseItem>();
        for (final AbstractExpenseItem item : allInstances(AbstractExpenseItem.class)) {
            if (item.getExpenseType() == type && item.getClaim().getClaimant() == employee) {
                items.add(item);
            }
        }
        return items;
    }

}
