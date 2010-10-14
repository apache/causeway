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

import org.apache.isis.applib.value.Date;
import org.apache.isis.example.expenses.claims.Claim;
import org.apache.isis.example.expenses.claims.items.GeneralExpense;
import org.apache.isis.example.expenses.employee.Employee;


public class SvenClaim3Returned extends AbstractClaimFixture {

    public static Employee SVEN;
    public static Employee DICK;;
    public static Claim SVEN_CLAIM_3;

    @Override
    public void install() {
        SVEN = EmployeeFixture.SVEN;
        DICK = EmployeeFixture.DICK;

        SVEN_CLAIM_3 = createNewClaim(SVEN, DICK, "23rd Feb - Sales trip, London", ProjectCodeFixture.CODE1, new Date(2007, 4, 3));
        final Date feb23rd = new Date(2007, 2, 23);
        addTaxi(SVEN_CLAIM_3, feb23rd, null, 18.00, "Euston", "City", false);
        addTaxi(SVEN_CLAIM_3, feb23rd, null, 10.00, "City", "West End", false);
        final GeneralExpense meal = addMeal(SVEN_CLAIM_3, feb23rd, "Lunch with client", 50.00);
        SVEN_CLAIM_3.submit(DICK, false);
        meal.reject("Too expensive");
        SVEN_CLAIM_3.approveItems(true);
        SVEN_CLAIM_3.returnToClaimant("Please discuss Meal item with me", false);

    }

}
