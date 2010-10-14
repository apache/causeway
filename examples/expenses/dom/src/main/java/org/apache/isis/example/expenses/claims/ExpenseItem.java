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

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;


public interface ExpenseItem {

    // {{ Claim
    void setClaim(final AbstractClaim claim);

    @Hidden
    AbstractClaim getClaim();

    // }}

    // {{ ExpenseType
    @Hidden
    ExpenseType getExpenseType();

    void setExpenseType(ExpenseType type);

    // }}

    // {{ Amount
    Money requestedOrApprovedAmount();

    @MemberOrder(sequence = "3")
    Money getAmount();

    void modifyAmount(final Money amount);

    /**
     * Sets the amount to 0.0 in the correct currency
     * 
     */
    void initialiseAmount();

    // }}

    // {{ Date Incurred
    @MemberOrder(sequence = "1")
    Date getDateIncurred();

    void modifyDateIncurred(final Date dateIncurred);

    // }}

    // {{ Description
    @MemberOrder(sequence = "2")
    String getDescription();

    void modifyDescription(final String title);

    // }}

    // {{ Status
    @MemberOrder(sequence = "5")
    ExpenseItemStatus getStatus();

    // }}

    // {{ Project Code
    @MemberOrder(sequence = "4")
    ProjectCode getProjectCode();

    void modifyProjectCode(final ProjectCode projectCodeImpl);

    // }}

    void setLocked(boolean locked);

    void copyFrom(final ExpenseItem otherItem);

    void approve();

    void reject(final String reason);

    void query(final String reason);

    // {{ Status tests
    @Hidden
    public boolean isNewIncomplete();

    @Hidden
    public void changeStatusToNewIncomplete();

    @Hidden
    public boolean isNewComplete();

    @Hidden
    public void changeStatusToNewComplete();

    @Hidden
    public boolean isApproved();

    @Hidden
    public void changeStatusToApproved();

    @Hidden
    public boolean isRejected();

    @Hidden
    public void changeStatusToRejected();

    @Hidden
    public boolean isQueried();

    @Hidden
    public void changeStatusToQueried();
    // }}

}
