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


package org.apache.isis.app.cart;

import org.apache.isis.app.cart.services.CustomerRepository;
import org.apache.isis.applib.AbstractDomainObject;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.value.Password;


public class Session extends AbstractDomainObject {
    private CustomerRepository customers;
    private Customer customer;
    private Cart cart;

    public Cart getCart() {
        resolve(cart);
        return cart;
    }
    
    public void setCart(Cart cart) {
        this.cart = cart;
        objectChanged();
    }

    public Customer getCustomer() {
        resolve(customer);
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
        objectChanged();
    }
    
    
    public Customer logon(@Named("User name") String userName, @Named("Password") Password password) {
        Customer customer = customers.customerForUserName(userName);
        if (validatePassword(customer, password)) {
            setCustomer(customer);
            return null;
        } else {
            warnUser("Failed to log in");
            return null;
        }
    }
    
    public boolean validatePassword(Customer customer, Password password) {
        return customer.getPassword().checkPassword(password.getPassword());
    }

    public void setRepository(CustomerRepository customers) {
        this.customers = customers;
    }

    
    public String toString() {
        return customer + " - " + cart;
    }
}

