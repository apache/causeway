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


package org.apache.isis.example.expenses.claims.items;

import org.apache.isis.applib.annotation.Disabled;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.value.Money;
import org.apache.isis.example.expenses.claims.ExpenseItem;


public class PrivateCarJourney extends Journey {

    // {{ Total Miles field
    private int totalMiles;

    public void setTotalMiles(final int miles) {
        this.totalMiles = miles;
    }

    @MemberOrder(sequence = "2.4")
    public int getTotalMiles() {
        return totalMiles;
    }

    public void modifyTotalMiles(final int newMiles) {
        setTotalMiles(newMiles);
        checkIfComplete();
        recalculateAmount();
    }

    public String disableTotalMiles() {
        return disabledIfLocked();
    }

    // }}

    // {{ MileageRate field;
    private double mileageRate;

    public void setMileageRate(final double rate) {
        this.mileageRate = rate;
    }

    @MemberOrder(sequence = "2.5")
    public double getMileageRate() {
        return mileageRate;
    }

    public void modifyMileageRate(final double newRate) {
        setMileageRate(newRate);
        checkIfComplete();
        recalculateAmount();
    }

    public String disableMileageRate() {
        return disabledIfLocked();
    }

    // }}

    @Disabled
    @Override
    public Money getAmount() {
        return super.getAmount();
    }

    private void recalculateAmount() {
        modifyAmount(new Money(totalMiles * mileageRate, getClaim().currencyCode()));
    }

    // {{ Copying
    @Override
    protected void copyAnyEmptyFieldsSpecificToSubclassOfJourney(final ExpenseItem otherItem) {
        if (otherItem instanceof PrivateCarJourney) {
            final PrivateCarJourney carJourney = (PrivateCarJourney) otherItem;
            if (totalMiles == 0) {
                modifyTotalMiles(carJourney.getTotalMiles());
            }
            if (mileageRate == 0) {
                modifyMileageRate(carJourney.getMileageRate());
            }
        }
    }

    // }}

    @Override
    protected boolean mandatoryJourneySubClassFieldsComplete() {
        return totalMiles > 0 && mileageRate > 0;
    }
}
