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
import org.apache.isis.core.integtestsupport.legacy.sample.service.ProductRepository;

public class ProductsFixture extends AbstractFixture {

    // use ctrl+space to bring up the NO templates.

    // also, use CoffeeBytes code folding with
    // user-defined regions of {{ and }}

    // {{ Logger
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductsFixture.class);

    public Logger getLOGGER() {
        return LOGGER;
    }

    // }}

    @Override
    public void install() {
        getLOGGER().debug("installing");
        getProductRepository().newProduct("355-40311", "Weekend camping pack", 5000);
        getProductRepository().newProduct("850-18003", "Stripy Wasp Catcher", 695);
        getProductRepository().newProduct("845-06203", "Combi Backpack Hamper", 5900);
        getProductRepository().newProduct("820-72721", "Folding Table", 4000);
        getProductRepository().newProduct("820-72725", "Folding Chair", 2500);
        getProductRepository().newProduct("845-01020", "Isotherm Cool Box", 2500);
    }

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
