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

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;
import org.apache.isis.example.expenses.claims.AbstractClaim;
import org.apache.isis.example.expenses.claims.Claim;
import org.apache.isis.example.expenses.claims.ClaimFactory;
import org.apache.isis.example.expenses.claims.ClaimRepository;
import org.apache.isis.example.expenses.claims.ExpenseItem;
import org.apache.isis.example.expenses.claims.ExpenseType;
import org.apache.isis.example.expenses.claims.ProjectCode;
import org.apache.isis.example.expenses.claims.items.AbstractExpenseItem;
import org.apache.isis.example.expenses.claims.items.Airfare;
import org.apache.isis.example.expenses.claims.items.CarRental;
import org.apache.isis.example.expenses.claims.items.GeneralExpense;
import org.apache.isis.example.expenses.claims.items.Hotel;
import org.apache.isis.example.expenses.claims.items.Journey;
import org.apache.isis.example.expenses.claims.items.PrivateCarJourney;
import org.apache.isis.example.expenses.claims.items.Taxi;
import org.apache.isis.example.expenses.employee.Employee;
import org.apache.isis.example.expenses.employee.EmployeeRepository;


public abstract class AbstractClaimFixture extends AbstractFixture {

    // {{ Injected Services

    // {{ Injected: ClaimRepository
    private ClaimRepository claimRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected ClaimRepository getClaimRepository() {
        return this.claimRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setClaimRepository(final ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }

    // }}

    // {{ Injected: ClaimFactory
    private ClaimFactory claimFactory;

    /**
     * This property is not persisted, nor displayed to the user.
     */
    protected ClaimFactory getClaimFactory() {
        return this.claimFactory;
    }

    /**
     * Injected by the application container.
     */
    public void setClaimFactory(final ClaimFactory claimFactory) {
        this.claimFactory = claimFactory;
    }
    // }}

    // {{ Injected: EmployeeRepository
    private EmployeeRepository employeeRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected EmployeeRepository getEmployeeRepository() {
        return this.employeeRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setEmployeeRepository(final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // }}
    // }}

    protected Claim createNewClaim(
            final Employee employee,
            final Employee approver,
            final String description,
            final ProjectCode projectCode,
            final Date dateCreated) {
        final Claim claim = getClaimFactory().createNewClaim(employee, description);
        claim.modifyApprover(approver);
        claim.modifyProjectCode(projectCode);
        if (dateCreated != null) {
            claim.setDateCreated(dateCreated);
        }
        return claim;
    }

    private ExpenseItem createExpenseItem(
            final AbstractClaim claim,
            final ExpenseType type,
            final Date dateIncurred,
            final String description,
            final double amount) {
        final AbstractExpenseItem item = (AbstractExpenseItem) claim.createNewExpenseItem(type);
        item.modifyDateIncurred(dateIncurred);
        item.modifyDescription(description);
        item.modifyAmount(money(amount, claim));
        return item;
    }

    private void modifyStandardJourneyFields(
            final Journey journey,
            final String origin,
            final String destination,
            final boolean returnJourney) {
        journey.modifyOrigin(origin);
        journey.modifyDestination(destination);
        journey.modifyReturnJourney(new Boolean(returnJourney));
    }

    protected GeneralExpense addGeneralExpense(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final double amount) {
        final GeneralExpense item = (GeneralExpense) createExpenseItem(claim, ExpenseTypeFixture.GENERAL, dateIncurred,
                description, amount);
        persist(item);
        return item;
    }

    protected Airfare addAirfare(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final double amount,
            final String airline,
            final String origin,
            final String destination,
            final boolean returnJourney) {
        final Airfare item = (Airfare) createExpenseItem(claim, ExpenseTypeFixture.AIRFARE, dateIncurred, description, amount);
        item.modifyAirlineAndFlight(airline);
        modifyStandardJourneyFields(item, origin, destination, returnJourney);
        persist(item);
        return item;
    }

    protected Hotel addHotel(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final double amount,
            final String hotelURL,
            final int numberOfNights,
            final double accommodation,
            final double food,
            final double other) {
        final Hotel item = (Hotel) createExpenseItem(claim, ExpenseTypeFixture.HOTEL, dateIncurred, description, amount);
        item.modifyHotelURL(hotelURL);
        item.modifyNumberOfNights(numberOfNights);
        item.modifyAccommodation(money(accommodation, claim));
        item.modifyFood(money(food, claim));
        item.modifyOther(money(other, claim));
        persist(item);
        return item;
    }

    protected CarRental addCarRental(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final double amount,
            final String rentalCompany,
            final int noOfDays) {
        final CarRental item = (CarRental) createExpenseItem(claim, ExpenseTypeFixture.CAR_RENTAL, dateIncurred, description,
                amount);
        item.modifyRentalCompany(rentalCompany);
        item.modifyNumberOfDays(noOfDays);
        persist(item);
        return item;
    }

    protected GeneralExpense addMobilePhone(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String phoneNumber,
            final double amount) {
        final GeneralExpense item = (GeneralExpense) createExpenseItem(claim, ExpenseTypeFixture.MOBILE_PHONE, dateIncurred,
                "Phone No. " + phoneNumber, amount);
        persist(item);
        return item;
    }

    protected PrivateCarJourney addPrivateCarJourney(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final String origin,
            final String destination,
            final boolean returnJourney,
            final int totalMiles) {
        final PrivateCarJourney item = (PrivateCarJourney) createExpenseItem(claim, ExpenseTypeFixture.PRIVATE_CAR, dateIncurred,
                description, 0.0);
        modifyStandardJourneyFields(item, origin, destination, returnJourney);
        item.modifyTotalMiles(totalMiles);
        item.modifyMileageRate(0.40);
        persist(item);
        return item;
    }

    protected Taxi addTaxi(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final double amount,
            final String origin,
            final String destination,
            final boolean returnJourney) {
        final Taxi item = (Taxi) createExpenseItem(claim, ExpenseTypeFixture.TAXI, dateIncurred, description, amount);
        modifyStandardJourneyFields(item, origin, destination, returnJourney);
        persist(item);
        return item;
    }

    protected GeneralExpense addMeal(
            final AbstractClaim claim,
            final Date dateIncurred,
            final String description,
            final double amount) {
        final GeneralExpense item = (GeneralExpense) createExpenseItem(claim, ExpenseTypeFixture.MEAL, dateIncurred, description,
                amount);

        persist(item);
        return item;
    }

    private Money money(final double amount, final AbstractClaim claim) {
        return new Money(amount, claim.currencyCode());
    }
}
