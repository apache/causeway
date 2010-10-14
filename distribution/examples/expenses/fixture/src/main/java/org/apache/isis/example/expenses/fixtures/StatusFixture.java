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


package org.apache.isis.example.expenses.fixtures;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.example.expenses.claims.ClaimStatus;
import org.apache.isis.example.expenses.claims.ExpenseItemStatus;


public class StatusFixture extends AbstractFixture {


    public static ClaimStatus NEW_CLAIM;
    public static ClaimStatus SUBMITTED;
    public static ClaimStatus RETURNED;
    public static ClaimStatus PAID;

    @Override
    public void install() {
        createExpenseItemStatus(ExpenseItemStatus.NEW_COMPLETE);
        createExpenseItemStatus(ExpenseItemStatus.NEW_INCOMPLETE);
        createExpenseItemStatus(ExpenseItemStatus.REJECTED);
        createExpenseItemStatus(ExpenseItemStatus.APPROVED);
        createExpenseItemStatus(ExpenseItemStatus.QUERIED);

        NEW_CLAIM = createClaimStatus(ClaimStatus.NEW);
        SUBMITTED = createClaimStatus(ClaimStatus.SUBMITTED);
        RETURNED = createClaimStatus(ClaimStatus.RETURNED);
        PAID = createClaimStatus(ClaimStatus.PAID);
    }

    @Hidden
    public ExpenseItemStatus createExpenseItemStatus(final String description) {
        final ExpenseItemStatus status = newTransientInstance(ExpenseItemStatus.class);
        status.setTitleString(description);
        getContainer().persist(status);
        return status;
    }

    @Hidden
    public ClaimStatus createClaimStatus(final String description) {
        final ClaimStatus status = newTransientInstance(ClaimStatus.class);
        status.setTitleString(description);
        persist(status);
        return status;
    }
}
