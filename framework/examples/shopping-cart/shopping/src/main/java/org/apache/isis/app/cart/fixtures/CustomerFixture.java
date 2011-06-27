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


package org.apache.isis.app.cart.fixtures;

import org.apache.isis.app.cart.Address;
import org.apache.isis.app.cart.Customer;
import org.apache.isis.app.cart.PaymentMethod;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.applib.value.Password;

public class CustomerFixture extends AbstractFixture {
    
    protected Customer newCustomer(String name, String userName, String password) {
        Customer cust = newTransientInstance(Customer.class);
        cust.setName(name);
        cust.setUserName(userName);
        cust.setPassword(new Password(password));
        persist(cust);
        return cust;
    }

    protected Address newAddress(String name) {
        Address address = newTransientInstance(Address.class);
        address.setAddress(name);
        persist(address);
        return address;
    }

    protected PaymentMethod newPaymentMethod(String name, String number) {
        PaymentMethod card = newTransientInstance(PaymentMethod.class);
        card.setNameOnCard(name);
        card.setNumber(number);
        persist(card);
        return card;
    }

    public void install() {
        Customer customer = newCustomer("Ann Bloggs", "ann", "pass");
        Address defaultAddress = newAddress("82 Smithfield Avenue\nSkipton");
        customer.setDefaultAddress(defaultAddress);
        PaymentMethod defaultPaymentMethod = newPaymentMethod("ANN BLOGGS", "23982390128129322");
        customer.setDefaultPaymentMethod(defaultPaymentMethod);

        newCustomer("Aruna Bloggs", "ru", "pass");
    }
}


