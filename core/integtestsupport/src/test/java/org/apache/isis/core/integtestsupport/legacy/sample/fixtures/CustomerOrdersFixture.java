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

package org.apache.isis.core.integtestsupport.legacy.sample.fixtures;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.fixtures.AbstractFixture;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Customer;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Product;
import org.apache.isis.core.integtestsupport.legacy.sample.service.CustomerRepository;
import org.apache.isis.core.integtestsupport.legacy.sample.service.ProductRepository;

public class CustomerOrdersFixture extends AbstractFixture {

    // use ctrl+space to bring up the NO templates.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ Logger
    private final static Logger LOGGER = LoggerFactory.getLogger(CustomerOrdersFixture.class);

    public Logger getLOGGER() {
        return LOGGER;
    }

    // }}

    @Override
    public void install() {
        getLOGGER().debug("installing");
        final Customer richard = getCustomerRepository().findByName("Pawson");
        final Product foldingTable = getProductRepository().findByCode("820-72721");
        final Product foldingChair = getProductRepository().findByCode("820-72725");
        final Product waspCatcher = getProductRepository().findByCode("850-18003");
        final Product coolbox = getProductRepository().findByCode("845-01020");

        setDate(2007, 4, 11);
        setTime(10, 15);
        richard.placeOrder(foldingTable, 1);
        setDate(2007, 4, 12);
        setTime(9, 35);
        richard.placeOrder(foldingChair, 6);
        setDate(2007, 4, 13);
        setTime(14, 20);
        richard.placeOrder(waspCatcher, 1);
        setDate(2007, 4, 14);
        setTime(11, 10);
        richard.placeOrder(coolbox, 1);
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
    public void setCustomerRepository(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // }}

    // {{ Injected: ProductRepository
    private ProductRepository productRepository;

    /**
     * This field is not persisted, nor displayed to the user.
     */
    protected ProductRepository getProductRepository() {
        return this.productRepository;
    }

    /**
     * Injected by the application container.
     */
    public void setProductRepository(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    // }}

}
