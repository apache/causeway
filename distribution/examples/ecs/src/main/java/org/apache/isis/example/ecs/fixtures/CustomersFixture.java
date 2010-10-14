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


package org.apache.isis.example.ecs.fixtures;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.example.ecs.City;
import org.apache.isis.example.ecs.CreditCard;
import org.apache.isis.example.ecs.Customer;
import org.apache.isis.example.ecs.Location;
import org.apache.isis.example.ecs.Telephone;


public class CustomersFixture extends AbstractFixture {

    public void install() {
        Customer newCustomer = (Customer) newTransientInstance(Customer.class);
        newCustomer.setFirstName("Richard");
        newCustomer.setLastName("Pawson");

        City boston = uniqueMatch(City.class, "Boston");
        City newYork = uniqueMatch(City.class, "New York");
        City washington = uniqueMatch(City.class, "Washington");

        Location location = newTransientInstance(Location.class);
        location.setCity(boston);
        location.setKnownAs("Home");
        location.setStreetAddress("433 Pine St.");
        newCustomer.addToLocations(location);

        location = newTransientInstance(Location.class);
        location.setCity(boston);
        location.setKnownAs("Office");
        location.setStreetAddress("944 Main St, Cambridge");
        newCustomer.addToLocations(location);

        location = newTransientInstance(Location.class);
        location.setCity(newYork);
        location.setKnownAs("QIC Headquarters");
        location.setStreetAddress("285 Park Avenue");
        newCustomer.addToLocations(location);

        location = newTransientInstance(Location.class);
        location.setCity(newYork);
        location.setStreetAddress("234 E 42nd Street");
        newCustomer.addToLocations(location);

        location = newTransientInstance(Location.class);
        location.setCity(newYork);
        location.setKnownAs("JFK Airport, BA Terminal");
        newCustomer.addToLocations(location);

        Telephone telephone = newTransientInstance(Telephone.class);
        telephone.setKnownAs("Home");
        telephone.setNumber("617/211 2899");
        newCustomer.getPhoneNumbers().add(telephone);

        telephone = newTransientInstance(Telephone.class);
        telephone.setKnownAs("Office");
        telephone.setNumber("617/353 9828");
        newCustomer.getPhoneNumbers().add(telephone);

        telephone = newTransientInstance(Telephone.class);
        telephone.setKnownAs("Mobile");
        telephone.setNumber("8777662671");
        newCustomer.getPhoneNumbers().add(telephone);

        CreditCard cc = newTransientInstance(CreditCard.class);
        cc.setNumber("4525365234232233");
        cc.setExpires("12/06");
        cc.setNameOnCard("MR R Pawson");
        newCustomer.setPreferredPaymentMethod(cc);

        persist(newCustomer);

        
        newCustomer = newTransientInstance(Customer.class);
        newCustomer.setFirstName("Robert");
        newCustomer.setLastName("Matthews");

        location = newTransientInstance(Location.class);
        location.setCity(washington);
        location.setKnownAs("Home");
        location.setStreetAddress("1112 Condor St, Carlton Park");
        newCustomer.addToLocations(location);

        location = newTransientInstance(Location.class);
        location.setCity(washington);
        location.setKnownAs("Office");
        location.setStreetAddress("299 Union St");
        newCustomer.addToLocations(location);

        location = newTransientInstance(Location.class);
        location.setCity(newYork);
        location.setKnownAs("Headquarters");
        location.setStreetAddress("285 Park Avenue");
        newCustomer.addToLocations(location);
        
        telephone = newTransientInstance(Telephone.class);
        telephone.setKnownAs("Home");
        telephone.setNumber("206/545 8444");
        newCustomer.getPhoneNumbers().add(telephone);

        telephone = newTransientInstance(Telephone.class);
        telephone.setKnownAs("Office");
        telephone.setNumber("206/234 443");
        newCustomer.getPhoneNumbers().add(telephone);

        cc = newTransientInstance(CreditCard.class);
        cc.setNumber("773829889938221");
        cc.setExpires("10/04");
        cc.setNameOnCard("MR R MATTHEWS");
        
        persist(newCustomer);
    }
    
}
