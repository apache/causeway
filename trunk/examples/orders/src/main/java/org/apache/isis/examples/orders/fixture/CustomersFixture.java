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


package org.apache.isis.examples.orders.fixture;

import org.apache.log4j.Logger;
import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.examples.orders.services.CustomerRepository;


public class CustomersFixture extends AbstractFixture {

    // use ctrl+space to bring up the NO templates.
    
    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    
    // {{ Logger
    private final static Logger LOGGER = Logger.getLogger(CustomersFixture.class);
    public Logger getLOGGER() {
        return LOGGER;
    }
    // }}

    public void install() {
        getCustomerRepository().newCustomer("Richard", "Pawson", 1);
        getCustomerRepository().newCustomer("Robert", "Matthews", 2);
        getCustomerRepository().newCustomer("Dan", "Haywood", 3);
        getCustomerRepository().newCustomer("Stef", "Cascarini", 4);
        getCustomerRepository().newCustomer("Dave", "Slaughter", 5);
    }
    
    // {{ Injected: CustomerRepository
    private CustomerRepository customerRepository;
    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected CustomerRepository getCustomerRepository() {
        return this.customerRepository;
    }
    /**
     * Injected by the application container.
     */
    public void setCustomerRepository(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    // }}
    

}