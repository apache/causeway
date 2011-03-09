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
import org.apache.isis.example.expenses.claims.ExpenseType;
import org.apache.isis.example.expenses.claims.items.Airfare;
import org.apache.isis.example.expenses.claims.items.CarRental;
import org.apache.isis.example.expenses.claims.items.GeneralExpense;
import org.apache.isis.example.expenses.claims.items.Hotel;
import org.apache.isis.example.expenses.claims.items.PrivateCarJourney;
import org.apache.isis.example.expenses.claims.items.Taxi;


public class ExpenseTypeFixture extends AbstractFixture {

    public static ExpenseType GENERAL;
    public static ExpenseType AIRFARE;
    public static ExpenseType CAR_RENTAL;
    public static ExpenseType HOTEL;
    public static ExpenseType MEAL;
    public static ExpenseType MOBILE_PHONE;
    public static ExpenseType PRIVATE_CAR;
    public static ExpenseType TAXI;

    @Override
    public void install() {
        GENERAL = createType(GeneralExpense.class, "General Expense");
        AIRFARE = createType(Airfare.class, "Airfare");
        CAR_RENTAL = createType(CarRental.class, "Car Rental");
        HOTEL = createType(Hotel.class, "Hotel");
        MEAL = createType(GeneralExpense.class, "Meal");
        MOBILE_PHONE = createType(GeneralExpense.class, "Mobile Phone");
        PRIVATE_CAR = createType(PrivateCarJourney.class, "Private Car Journey");
        TAXI = createType(Taxi.class, "Taxi");
    }

    @Hidden
    public ExpenseType createType(final Class<?> correspondingClass, final String titleString) {
        final ExpenseType type = newTransientInstance(ExpenseType.class);
        type.setCorrespondingClassName(correspondingClass.getName());
        type.setTitleString(titleString);
        persist(type);
        return type;
    }
}
