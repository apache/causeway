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

package org.apache.isis.core.integtestsupport.legacy;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.services.eventbus.EventBusService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperObject;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Country;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Customer;
import org.apache.isis.core.integtestsupport.legacy.sample.domain.Product;
import org.apache.isis.core.integtestsupport.legacy.sample.fixtures.CountriesFixture;
import org.apache.isis.core.integtestsupport.legacy.sample.fixtures.CustomerOrdersFixture;
import org.apache.isis.core.integtestsupport.legacy.sample.fixtures.CustomersFixture;
import org.apache.isis.core.integtestsupport.legacy.sample.fixtures.ProductsFixture;
import org.apache.isis.core.integtestsupport.legacy.sample.service.CountryRepository;
import org.apache.isis.core.integtestsupport.legacy.sample.service.CustomerRepository;
import org.apache.isis.core.integtestsupport.legacy.sample.service.OrderRepository;
import org.apache.isis.core.integtestsupport.legacy.sample.service.ProductRepository;
import org.apache.isis.core.wrapper.WrapperFactoryDefault;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(IsisTestRunner.class)
@Fixtures({ @Fixture(CountriesFixture.class), @Fixture(ProductsFixture.class), @Fixture(CustomersFixture.class), @Fixture(CustomerOrdersFixture.class) })
@Services({ @Service(CountryRepository.class), @Service(ProductRepository.class), @Service(CustomerRepository.class), @Service(OrderRepository.class), @Service(WrapperFactoryDefault.class), @Service(EventBusService.Noop.class) })
public abstract class AbstractTest {

    protected Customer custJsDO;
    protected Customer custJsWO;

    protected Product product355DO;
    protected Product product355WO;

    protected Product product850DO;

    protected Country countryGbrDO;
    protected Country countryGbrWO;

    protected Country countryUsaDO;
    protected Country countryAusDO;

    private ProductRepository productRepository;
    private CustomerRepository customerRepository;
    private CountryRepository countryRepository;

    private DomainObjectContainer domainObjectContainer;
    private WrapperFactory wrapperFactory;

    @Before
    public void setUp() {

        product355DO = productRepository.findByCode("355-40311");
        product355WO = wrapperFactory.wrap(product355DO);
        product850DO = productRepository.findByCode("850-18003");

        countryGbrDO = countryRepository.findByCode("GBR");
        countryGbrWO = wrapperFactory.wrap(countryGbrDO);

        countryUsaDO = countryRepository.findByCode("USA");
        countryAusDO = countryRepository.findByCode("AUS");

        custJsDO = customerRepository.findByName("Pawson");
        custJsWO = wrapperFactory.wrap(custJsDO);
        
        assertThat(product355WO instanceof WrapperObject, is(true));
        assertThat(countryGbrWO instanceof WrapperObject, is(true));
        assertThat(custJsWO instanceof WrapperObject, is(true));
    }

    @After
    public void tearDown() {
    }

    // //////////////////////////////////////////////////////
    // Injected.
    // //////////////////////////////////////////////////////

    protected WrapperFactory getWrapperFactory() {
        return wrapperFactory;
    }

    public void setWrapperFactory(final WrapperFactory headlessViewer) {
        this.wrapperFactory = headlessViewer;
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
