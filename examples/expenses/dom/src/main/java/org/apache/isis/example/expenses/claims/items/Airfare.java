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
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.example.expenses.claims.ExpenseItem;


public class Airfare extends Journey {

    // {{ Airline
    private String airlineAndFlight;

    public void setAirlineAndFlight(final String airline) {
        this.airlineAndFlight = airline;
    }

    @MemberOrder(sequence = "2.4")
    @Named("Airline & Flight No.")
    public String getAirlineAndFlight() {
        return airlineAndFlight;
    }

    public void modifyAirlineAndFlight(final String newAirline) {
        setAirlineAndFlight(newAirline);
        checkIfComplete();
    }

    public void clearAirlineAndFlight() {
        setAirlineAndFlight(null);
        checkIfComplete();
    }

    public String disableAirlineAndFlight() {
        return disabledIfLocked();
    }

    // }}

    // {{ Copying
    @Override
    protected void copyAnyEmptyFieldsSpecificToSubclassOfJourney(final ExpenseItem otherItem) {
        if (otherItem instanceof Airfare) {
            final Airfare airfare = (Airfare) otherItem;
            if (airlineAndFlight == null || airlineAndFlight.length() == 0) {
                modifyAirlineAndFlight(airfare.getAirlineAndFlight());
            }
        }
    }

    // }}

    @Override
    protected boolean mandatoryJourneySubClassFieldsComplete() {
        return airlineAndFlight != null & !airlineAndFlight.equals("");
    }

}
