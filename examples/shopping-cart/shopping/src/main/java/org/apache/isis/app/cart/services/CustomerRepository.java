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


package org.apache.isis.app.cart.services;

import java.util.List;

import org.apache.isis.app.cart.Cart;
import org.apache.isis.app.cart.Customer;
import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Exploration;


public class CustomerRepository extends AbstractFactoryAndRepository {
    //private CartRepository cartRepository;
    
    public String getId() {
        return "customers";
    }

    @Exploration
    public List<Customer> allCustomers() {
        return allInstances(Customer.class);
    }
    
    public Customer customerForUserName(String userName) {
        Customer pattern = newTransientInstance(Customer.class);
        pattern.setUserName(userName);
        return firstMatch(Customer.class, pattern);
    }
    
    public void associateCustomerWithCart(Customer customer, Cart cart) {
        cart.setCustomer(customer);
    }

/*
    public void logon(String sessionId, String userName, Password password) {
        Customer customer = customerForUserName(userName);
        if (validatePassword(customer, password)) {
            Cart cart = cartRepository.findCart(sessionId);
            associateCustomerWithCart(customer, cart);
        } else {
            warnUser("Failed to log in");
        }
    }
    
   public void setCartRepository(CartRepository cartRepository) {
    this.cartRepository = cartRepository;
}
 */ 
    
}


