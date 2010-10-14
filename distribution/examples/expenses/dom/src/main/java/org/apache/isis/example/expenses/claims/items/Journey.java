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
import org.apache.isis.applib.annotation.Optional;
import org.apache.isis.example.expenses.claims.ExpenseItem;


public abstract class Journey extends AbstractExpenseItem {

    // {{ Description
    @Override
    @Disabled
    public String getDescription() {
        return super.getDescription();
    }

    private void createDescription() {
        String description = getOrigin() + " - " + getDestination();
        if (getReturnJourney() != null && getReturnJourney().booleanValue()) {
            description += " (return)";
        }
        setDescription(description);
    }
    // }}

    // {{ Origin
    private String origin;

    public void setOrigin(final String origin) {
        this.origin = origin;
    }

    @MemberOrder(sequence = "2.1")
    public String getOrigin() {
        return origin;
    }

    public void modifyOrigin(final String newOrigin) {
        setOrigin(newOrigin);
        checkIfComplete();
        createDescription();
    }

    public String disableOrigin() {
        return disabledIfLocked();
    }
    // }}

    // {{ Destination
    private String destination;

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    @MemberOrder(sequence = "2.2")
    public String getDestination() {
        return destination;
    }

    public void modifyDestination(final String newDestination) {
        setDestination(newDestination);
        checkIfComplete();
        createDescription();
    }

    public String disableDestination() {
        return disabledIfLocked();
    }
    // }}

    // {{ Return journey
    private Boolean returnJourney;

    @MemberOrder(sequence = "2.3")
    @Optional
    public Boolean getReturnJourney() {
        return returnJourney;
    }

    public void setReturnJourney(final Boolean returnJourney) {
        this.returnJourney = returnJourney;
    }

    public void modifyReturnJourney(final Boolean newReturnJourney) {
        setReturnJourney(newReturnJourney);
        checkIfComplete();
        createDescription();
    }

    public String disableReturnJourney() {
        return disabledIfLocked();
    }

    // }}

    // {{ Copying
    @Override
    protected void copyAnyEmptyFieldsSpecificToSubclassOfAbstractExpenseItem(final ExpenseItem otherItem) {
        if (otherItem instanceof Journey) {
            final Journey journey = (Journey) otherItem;
            if (origin == null || origin.length() == 0) {
                modifyOrigin(journey.getOrigin());
            }
            if (destination == null || destination.length() == 0) {
                modifyDestination(journey.getDestination());
            }
            if (returnJourney == null) {
                modifyReturnJourney(journey.getReturnJourney());
            }
        }
        copyAnyEmptyFieldsSpecificToSubclassOfJourney(otherItem);
    }

    protected abstract void copyAnyEmptyFieldsSpecificToSubclassOfJourney(ExpenseItem otherItem);

    // }}

    // {{ Fields complete
    @Override
    protected boolean mandatorySubClassFieldsComplete() {
        return origin != null && !origin.equals("") && destination != null && !destination.equals("")
                && mandatoryJourneySubClassFieldsComplete();
    }

    protected abstract boolean mandatoryJourneySubClassFieldsComplete();

    // }}
}
