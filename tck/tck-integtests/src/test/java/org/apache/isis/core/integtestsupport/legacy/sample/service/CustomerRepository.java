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

package org.apache.isis.core.integtestsupport.legacy.sample.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Named;
import org.apache.isis.applib.filter.Filter;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Country;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Customer;

@Named("Customers")
public class CustomerRepository extends AbstractFactoryAndRepository {

    // use ctrl+space to bring up the NO templates.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ Logger
    @SuppressWarnings("unused")
    private final static Logger LOGGER = LoggerFactory.getLogger(CustomerRepository.class);

    // }}

    /**
     * Lists all customers in the repository.
     */
    public List<Customer> showAll() {
        return allInstances(Customer.class);
    }

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
        final Customer firstMatch = firstMatch(Customer.class, new FilterLastName(lastName));
        return firstMatch;
    }

    private final class FilterLastName implements Filter<Customer> {
        private final String name;

        private FilterLastName(final String name) {
            this.name = name;
        }

        @Override
        public boolean accept(final Customer customer) {
            return customer.getLastName().toLowerCase().contains(name.toLowerCase());
        }
    }

    // }}

    /**
     * Creates a new (still-transient) customer.
     * 
     * @return
     */
    public Customer newCustomer() {
        final Customer customer = newTransientInstance(Customer.class);
        return customer;
    }

    /**
     * Creates a new (already persisted) customer.
     * 
     * <p>
     * For use by fixtures only.
     * 
     * @return
     */
    @Hidden
    public Customer newCustomer(final String firstName, final String lastName, final int customerNumber, final Country countryOfBirth) {

        final Customer customer = newCustomer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setCustomerNumber(customerNumber);
        customer.modifyCountryOfBirth(countryOfBirth);

        persist(customer);
        return customer;
    }

}
