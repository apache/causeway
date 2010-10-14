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


package org.apache.isis.extensions.headless.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.extensions.headless.applib.HeadlessViewer;
import org.apache.isis.extensions.headless.applib.listeners.InteractionListener;
import org.apache.isis.extensions.headless.junit.Fixture;
import org.apache.isis.extensions.headless.junit.Fixtures;
import org.apache.isis.extensions.headless.junit.IsisTestRunner;
import org.apache.isis.extensions.headless.junit.Service;
import org.apache.isis.extensions.headless.junit.Services;
import org.apache.isis.extensions.headless.junit.sample.domain.Country;
import org.apache.isis.extensions.headless.junit.sample.domain.Customer;
import org.apache.isis.extensions.headless.junit.sample.domain.Product;
import org.apache.isis.extensions.headless.junit.sample.fixtures.CountriesFixture;
import org.apache.isis.extensions.headless.junit.sample.fixtures.CustomerOrdersFixture;
import org.apache.isis.extensions.headless.junit.sample.fixtures.CustomersFixture;
import org.apache.isis.extensions.headless.junit.sample.fixtures.ProductsFixture;
import org.apache.isis.extensions.headless.junit.sample.service.CountryRepository;
import org.apache.isis.extensions.headless.junit.sample.service.CustomerRepository;
import org.apache.isis.extensions.headless.junit.sample.service.OrderRepository;
import org.apache.isis.extensions.headless.junit.sample.service.ProductRepository;


@RunWith(IsisTestRunner.class)
@Fixtures({
	@Fixture(CountriesFixture.class),
	@Fixture(ProductsFixture.class),
	@Fixture(CustomersFixture.class),
	@Fixture(CustomerOrdersFixture.class)
})
@Services({
	@Service(CountryRepository.class),
	@Service(ProductRepository.class),
	@Service(CustomerRepository.class),
	@Service(OrderRepository.class)
})
public abstract class AbstractTest {
	
    protected Customer custJsDO;
    protected Customer custJsVO;

    protected Product product355DO;
    protected Product product355VO;

    protected Product product850DO;

    protected Country countryGbrDO;
    protected Country countryGbrVO;

    protected Country countryUsaDO;
    protected Country countryAusDO;

    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private CountryRepository countryRepository;
    
    private DomainObjectContainer domainObjectContainer;
    private HeadlessViewer headlessViewer;
    
    private InteractionListener interactionListener;

    @Before
    public void setUp() {
    	
        product355DO = productRepository.findByCode("355-40311");
        product355VO = headlessViewer.view(product355DO);
        product850DO = productRepository.findByCode("850-18003");

        countryGbrDO = countryRepository.findByCode("GBR");
        countryGbrVO = headlessViewer.view(countryGbrDO);

        countryUsaDO = countryRepository.findByCode("USA");
        countryAusDO = countryRepository.findByCode("AUS");

        custJsDO = customerRepository.findByName("Pawson");
        custJsVO = headlessViewer.view(custJsDO);
    }

    @After
    public void tearDown() {
    }

    protected InteractionListener getInteractionListener() {
        return interactionListener;
    }

    
    ////////////////////////////////////////////////////////
    // Injected.
    ////////////////////////////////////////////////////////
    
    protected HeadlessViewer getHeadlessViewer() {
        return headlessViewer;
    }
    public void setHeadlessViewer(HeadlessViewer headlessViewer) {
    	this.headlessViewer = headlessViewer;
    }

    protected DomainObjectContainer getDomainObjectContainer() {
        return domainObjectContainer;
    }

    public void setDomainObjectContainer(final DomainObjectContainer domainObjectContainer) {
        this.domainObjectContainer = domainObjectContainer;
    }


    protected ProductRepository getProductRepository() {
        return productRepository;
    }

    public void setProductRepository(final ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    protected CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public void setCustomerRepository(final CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    protected CountryRepository getCountryRepository() {
        return countryRepository;
    }

    public void setCountryRepository(final CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }



    

}
