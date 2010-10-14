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


package org.apache.isis.examples.orders.services;

import java.util.List;


import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.Filter;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.examples.orders.domain.Customer;


@Named("Customers")
public class CustomerRepository extends AbstractFactoryAndRepository {

    // use ctrl+space to bring up the NO templates.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ showAll
    /**
     * Lists all products in the repository.
     */
    public List<Customer> showAll() {
        return allInstances(Customer.class);
    }

    // }}

    // {{ findAllByName, findByName
    /**
     * Returns a list of Customers with given last name.
     */
    public List<Customer> findAllByName(@Named("Last name") final String lastName) {
        return allMatches(Customer.class, new FilterLastName(lastName));
    }

    /**
     * Returns the first Customer with given last name.
     */
    public Customer findByName(@Named("Last name") final String lastName) {
        return firstMatch(Customer.class, new FilterLastName(lastName));
    }

    private final class FilterLastName implements Filter<Customer> {
        private final String name;

        private FilterLastName(String name) {
            this.name = name;
        }

        public boolean accept(Customer pojo) {
            return pojo.getLastName().toLowerCase().contains(name.toLowerCase());
        }
    }

    // }}

    // {{ newCustomer
    /**
     * Creates a new (still-transient) customer.
     */
    public Customer newCustomer() {
        Customer customer = (Customer) newTransientInstance(Customer.class);
        return customer;
    }

    /**
     * Creates a new (already persisted) customer.
     * 
     * <p>
     * For use by fixtures only.
     */
    @Hidden
    public Customer newCustomer(String firstName, String lastName, int customerNumber) {
        Customer customer = newCustomer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setCustomerNumber(customerNumber);
        persist(customer);
        return customer;
    }

    // }}

    // {{ identification
    /**
     * Use <tt>Customer.gif</tt> for icon.
     */
    public String iconName() {
        return "Customer";
    }
    // }}

}
