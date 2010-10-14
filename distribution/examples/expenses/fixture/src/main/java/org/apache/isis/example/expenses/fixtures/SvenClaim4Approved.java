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
import org.apache.isis.example.expenses.employee.Employee;


public class SvenClaim4Approved extends AbstractClaimFixture {

    public static Employee SVEN;
    public static Employee DICK;;
    public static Claim SVEN_CLAIM_4;

    @Override
    public void install() {
        SVEN = EmployeeFixture.SVEN;
        DICK = EmployeeFixture.DICK;

        Date date1 = new Date(2007, 7, 15);

        SVEN_CLAIM_4 = createNewClaim(SVEN, DICK, "July 07 - 2 visits to Dublin", ProjectCodeFixture.CODE2, date1);
        addPrivateCarJourney(SVEN_CLAIM_4, date1, "Own car to airport", "Henley on Thames", "Heathrow", true, 50);
        addGeneralExpense(SVEN_CLAIM_4, date1, "Car Parking at Heathrow", 42.90);
        addAirfare(SVEN_CLAIM_4, date1, null, 165.00, "Aer Lingus", "LHR", "DUB", true);
        addTaxi(SVEN_CLAIM_4, date1, "Taxis to & from Hotel", 30.0, "Dublin Airport", "Alexander Hotel", true);
        addHotel(SVEN_CLAIM_4, date1, "Alexander Hotel", 0.0, "http://ocallaghanhotels.visrez.com/dublinmain/Alexander.aspx", 1,
                89.00, 15.45, 3.50);
        addMeal(SVEN_CLAIM_4, date1, "Dinner", 28.00);

        date1 = new Date(2007, 7, 23);

        addPrivateCarJourney(SVEN_CLAIM_4, date1, "Own car to airport", "Henley on Thames", "Heathrow", true, 50);
        addGeneralExpense(SVEN_CLAIM_4, date1, "Car Parking at Heathrow", 42.90);
        addAirfare(SVEN_CLAIM_4, date1, null, 129.00, "Aer Lingus", "LHR", "DUB", true);
        addTaxi(SVEN_CLAIM_4, date1, "Taxis to & from Hotel", 32.0, "Dublin Airport", "Alexander Hotel", true);
        addHotel(SVEN_CLAIM_4, date1, "Alexander Hotel", 0.0, "http://ocallaghanhotels.visrez.com/dublinmain/Alexander.aspx", 1,
                89.00, 15.45, 4.80);
        addMeal(SVEN_CLAIM_4, date1, "Dinner", 31.00);

        SVEN_CLAIM_4.submit(DICK, false);
        SVEN_CLAIM_4.approveItems(false);
    }

}
