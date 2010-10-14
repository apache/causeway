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
import org.apache.isis.example.expenses.claims.ClaimRepository;
import org.apache.isis.example.expenses.currency.Currency;
import org.apache.isis.example.expenses.employee.Employee;
import org.apache.isis.example.expenses.employee.EmployeeRepository;


public class EmployeeFixture extends AbstractFixture {

    public static Employee SVEN;
    public static Employee DICK;
    public static Employee BOB;
    public static Employee JOE;

    @Override
    public void install() {

        createEmployees();
    }

    private void createEmployees() {
        SVEN = createEmployee("Sven Bloggs", "sven", "sven@example.com", CurrencyFixture.GBP);
        DICK = createEmployee("Dick Barton", "dick", "dick@example.com", CurrencyFixture.GBP);
        BOB = createEmployee("Robert Bruce", "bob", "bob@example.com", CurrencyFixture.USD);
        JOE = createEmployee("Joe Sixpack", "joe", "joe@example.com", CurrencyFixture.USD);
        createEmployee("Intrepid Explorer", "exploration", "exploration@example.com", CurrencyFixture.USD);

        SVEN.setNormalApprover(DICK);
        DICK.setNormalApprover(BOB);
    }

    @Hidden
    public Employee createEmployee(final String myName, final String userName, final String emailAddress, final Currency currency) {
        final Employee employee = newTransientInstance(Employee.class);
        assert (myName != null && myName != "");
        assert (userName != null && userName != "");
        assert (emailAddress != null && emailAddress != "");
        assert (currency != null);

        employee.setName(myName);
        employee.setUserName(userName);
        employee.setEmailAddress(emailAddress);
        employee.setCurrency(currency);
        persist(employee);
        return employee;
    }

    
    // {{ Injected Services
    /*
     * This region contains references to the services (Repositories, Factories or other Services) used by
     * this domain object. The references are injected by the application container.
     */

    // {{ Injected: ClaimantRepository
    private EmployeeRepository employeeRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected EmployeeRepository getClaimantRepository() {
        return this.employeeRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setClaimantRepository(final EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // }}

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

    // }}


}
