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

import java.util.List;
import java.util.Random;

import org.apache.isis.applib.value.Date;
import org.apache.isis.applib.value.Money;
import org.apache.isis.example.expenses.claims.Claim;
import org.apache.isis.example.expenses.claims.ClaimRepository;
import org.apache.isis.example.expenses.claims.ExpenseItem;
import org.apache.isis.example.expenses.claims.ExpenseType;
import org.apache.isis.example.expenses.claims.ProjectCode;
import org.apache.isis.example.expenses.claims.items.Airfare;
import org.apache.isis.example.expenses.claims.items.CarRental;
import org.apache.isis.example.expenses.claims.items.Hotel;
import org.apache.isis.example.expenses.claims.items.Journey;
import org.apache.isis.example.expenses.claims.items.PrivateCarJourney;
import org.apache.isis.example.expenses.employee.Employee;
import org.apache.isis.example.expenses.employee.EmployeeRepository;


public class RandomClaimFixture extends AbstractClaimFixture {

    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: ClaimRepository
    private ClaimRepository claimRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    @Override
    protected ClaimRepository getClaimRepository() {
        return this.claimRepository;
    }

    /**
     * Injected by the application container.
     */
    @Override
    public void setClaimRepository(final ClaimRepository claimRepository) {
        this.claimRepository = claimRepository;
    }
    // }}

    // {{ Injected: EmployeeRepository
    private EmployeeRepository employeeRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    @Override
    protected EmployeeRepository getEmployeeRepository() {
        return this.employeeRepository;
    }

    /**
     * Injected by the application container.
     */
    @Override
    public void setEmployeeRepository(final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    // }}

    // }}

    private final Random random = new Random();

    private ExpenseType[] expenseTypes;

    private ProjectCode[] codes;
    private int numberOfCodes;

    private final int claimCount = 4;

    @Override
    public void install() {
        loadExpenseTypes();
        loadProjectCodes();

        final Employee claimant = employeeRepository.findEmployeeByName("Sven Bloggs").get(0);
        createClaims(claimant);

    }

    private void loadExpenseTypes() {
        final List<ExpenseType> types = allInstances(ExpenseType.class);
        expenseTypes = types.toArray(new ExpenseType[types.size()]);
    }

    private void createClaims(final Employee employee) {
        for (int i = claimCount - 1; i >= 0; i--) {
            addRandomExpenseItems(createClaim(employee));
        }
    }

    private Claim createClaim(final Employee employee) {
        final Date rDate = new Date(random.nextInt(7) + 2000, random.nextInt(12) + 1, random.nextInt(28) + 1);
        final Claim claim = getClaimFactory().createNewClaim(employee, rDate.toString());
        claim.setDateCreated(rDate);
        return claim;
    }

    private void addRandomExpenseItems(final Claim claim) {
        for (int count = random.nextInt(10); count >= 0; count--) {
            addRandomExpenseItem(claim);
        }
    }

    private void addRandomExpenseItem(final Claim claim) {
        final ExpenseItem item = claim.createNewExpenseItem(expenseTypes[random.nextInt(8)]);
        populate(item);
        persist(item);
    }

    private void populate(final ExpenseItem item) {
        populateGeneral(item);

        if (item instanceof Journey) {
            populateJourney((Journey) item);
        }
        if (item instanceof Airfare) {
            populateAirfare((Airfare) item);
        }
        if (item instanceof CarRental) {
            populateCarRental((CarRental) item);
        }
        if (item instanceof PrivateCarJourney) {
            populatePrivateCarJourney((PrivateCarJourney) item);
        }

        if (item instanceof Hotel) {
            populateHotel((Hotel) item);
        }

    }

    private Money getRandomAmount() {
        return new Money(random.nextDouble() * 1000, "GBP");
    }

    private void populateJourney(final Journey journey) {
        journey.setOrigin((new String[] { "London", "New York", "Tokyo" })[random.nextInt(3)]);
        journey.setDestination((new String[] { "Chicago", "Sydney", "Berlin" })[random.nextInt(3)]);
        journey.setAmount(getRandomAmount());
        journey.setReturnJourney(new Boolean(random.nextBoolean()));
    }

    private void populateAirfare(final Airfare airfare) {
        airfare.setAirlineAndFlight((new String[] { "BA", "Air France", "RyanAir" })[random.nextInt(3)]);
    }

    private void populateCarRental(final CarRental rental) {
        rental.setRentalCompany((new String[] { "Avis", "EasyCar", "Hertz" })[random.nextInt(3)]);
        rental.setNumberOfDays(random.nextInt(14) + 1);
    }

    private void populatePrivateCarJourney(final PrivateCarJourney car) {
        car.setMileageRate((random.nextFloat() + .01) * 100.0);
        car.setTotalMiles(random.nextInt(1000) + 1);
    }

    private void populateGeneral(final ExpenseItem item) {
        final Date rDate = new Date(random.nextInt(7) + 2000, random.nextInt(12) + 1, random.nextInt(28) + 1);
        item.modifyAmount(getRandomAmount());
        item.modifyDescription(item.getClass().getSimpleName() + " " + rDate.toString());
        item.modifyDateIncurred(rDate);
        item.modifyProjectCode(getRandomProjectCode());
    }

    private void populateHotel(final Hotel item) {
        item.setHotelURL((new String[] { "The Grand", "The Ritz", "Albert at Bay" })[random.nextInt(3)]);
        item.setAccommodation(getRandomAmount());
        item.setFood(getRandomAmount());
        item.setOther(getRandomAmount());
        item.setNumberOfNights(random.nextInt(14) + 1);
    }

    private void loadProjectCodes() {
        final List<ProjectCode> allCodes = allInstances(ProjectCode.class);
        codes = allCodes.toArray(new ProjectCode[allCodes.size()]);
        numberOfCodes = allCodes.size();
    }

    private ProjectCode getRandomProjectCode() {
        return codes[random.nextInt(numberOfCodes)];
    }
}
