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


package org.apache.isis.extensions.wicket.testapp.claims.dom.claim;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.Ignore;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.annotation.NotPersistable;
import org.apache.isis.applib.value.Money;
import org.apache.isis.extensions.wicket.view.googlecharts.applib.PieChartable;

@NotPersistable
public class ClaimantExpenseSummary implements PieChartable,
        Comparable<ClaimantExpenseSummary> {

    // {{ Identification
    public String title() {
        return getClaimant() != null ? getClaimant().title() : "(untitled)";
    }

    // }}

    // {{ Claimant
    private Claimant claimant;

    @MemberOrder(sequence = "1")
    public Claimant getClaimant() {
        return claimant;
    }

    public void setClaimant(final Claimant claimant) {
        this.claimant = claimant;
    }

    // }}

    // {{ Amount
    private Money amount;

    @Disabled
    @MemberOrder(sequence = "1")
    public Money getAmount() {
        return amount;
    }

    public void setAmount(final Money amount) {
        this.amount = amount;
    }

    // }}

    // {{ programmatic
    @Ignore
    public void addAmount(Money amount) {
        if (this.amount == null) {
            this.amount = amount;
        } else {
            this.amount = this.amount.add(amount);
        }
    }

    // }}

    // {{ PieChartable impl
    @Ignore
    @Override
    public double getPieChartValue() {
        return getAmount().doubleValue();
    }

    @Ignore
    @Override
    public String getPieChartLabel() {
        return title();
    }

    // }}

    @Override
    public int compareTo(ClaimantExpenseSummary o) {
        if (getPieChartValue() < o.getPieChartValue())
            return -1;
        if (getPieChartValue() > o.getPieChartValue())
            return +1;
        return 0;
    }

}
