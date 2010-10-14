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


package org.apache.isis.example.expenses.claims;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Debug;
import org.apache.isis.applib.annotation.Exploration;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.example.expenses.employee.Employee;

import java.util.List;


public abstract class ClaimRepositoryAbstract extends AbstractFactoryAndRepository implements ClaimRepository {

    @Debug
    public List<ProjectCode> listAllCodes() {
        return allInstances(ProjectCode.class);
    }

    @Exploration
    public List<Claim> allClaims() {
        return allInstances(Claim.class);
    }

    @Hidden
    public boolean descriptionIsUniqueForClaimant(final Employee employee, final String initialDescription) {
        final Claim claimPattern = newTransientInstance(Claim.class);
        claimPattern.setClaimant(employee);
        claimPattern.setDescription(initialDescription);
        claimPattern.setDateCreated(null);
        claimPattern.setStatus(null);
        final List<Claim> allClaims = allMatches(Claim.class, claimPattern);
        // as this might just be a partial match look at each one and check for an exact match
        for (Claim claim : allClaims) {
            if (claim.getDescription().equalsIgnoreCase(initialDescription)) {
                return false;
            }
        }
        return true;
    }

    @Hidden
    public ClaimStatus findClaimStatus(final String title) {
        return uniqueMatch(ClaimStatus.class, title);
    }

    @Hidden
    public ExpenseItemStatus findExpenseItemStatus(final String title) {
        return uniqueMatch(ExpenseItemStatus.class, title);
    }

    @Hidden
    public List<ExpenseItem> findExpenseItemsLike(final ExpenseItem item) {
        // Simple implementation: could be extended to compare any fields that have already been set on the
        // item provided.
        return findExpenseItemsOfType(item.getClaim().getClaimant(), item.getExpenseType());
    }

    @Hidden
    public abstract List<ExpenseItem> findExpenseItemsOfType(final Employee employee, final ExpenseType type);
}
