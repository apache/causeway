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

import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.example.expenses.claims.ExpenseItem;


public class CarRental extends AbstractExpenseItem {

    // {{ Rental Company
    private String rentalCompany;

    public void setRentalCompany(final String rentalCompany) {
        this.rentalCompany = rentalCompany;
    }

    @MemberOrder(sequence = "2.1")
    public String getRentalCompany() {
        return rentalCompany;
    }

    public void modifyRentalCompany(final String newRentalCompany) {
        setRentalCompany(newRentalCompany);
        checkIfComplete();
    }

    public void clearRentalCompany() {
        setRentalCompany(null);
        checkIfComplete();
    }

    public String disableRentalCompany() {
        return disabledIfLocked();
    }
    // }}

    // {{ Number of Days
    private int numberOfDays = 0;

    public void setNumberOfDays(final int noOfDays) {
        this.numberOfDays = noOfDays;
    }

    @MemberOrder(sequence = "2.2")
    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void modifyNumberOfDays(final int noOfDays) {
        setNumberOfDays(noOfDays);
        checkIfComplete();
    }

    public void clearNumberOfDays() {
        setNumberOfDays(0);
        checkIfComplete();
    }

    public String disableNumberOfDays() {
        return disabledIfLocked();
    }

    // }}

    // {{ Copying
    @Override
    protected void copyAnyEmptyFieldsSpecificToSubclassOfAbstractExpenseItem(final ExpenseItem otherItem) {
        if (otherItem instanceof CarRental) {
            final CarRental carRental = (CarRental) otherItem;

            if (rentalCompany == null || rentalCompany.length() == 0) {
                modifyRentalCompany(carRental.getRentalCompany());
            }
            if (numberOfDays == 0) {
                modifyNumberOfDays(carRental.getNumberOfDays());
            }
        }
    }

    // }}

    @Override
    protected boolean mandatorySubClassFieldsComplete() {
        return numberOfDays > 0 && rentalCompany != null & !rentalCompany.equals("");
    }

}
