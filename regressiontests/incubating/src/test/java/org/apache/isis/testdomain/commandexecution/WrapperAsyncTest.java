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
package org.apache.isis.testdomain.commandexecution;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryManager;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.*;

import lombok.Getter;
import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                WrapperAsyncTest.ActionDomainEventListener.class
        }
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@DirtiesContext // because of the temporary installed ActionDomainEventListener
class WrapperAsyncTest extends IsisIntegrationTestAbstract {

    @Inject private FixtureScripts fixtureScripts;
    @Inject private RepositoryService repository;
    @Inject private FactoryService factoryService;
    @Inject private WrapperFactory wrapper;
    @Inject private ActionDomainEventListener actionDomainEventListener;
    
    @Configuration
    public class Config {
        // so that we get a new ApplicationContext.
    }

    @BeforeEach
    void setUp() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @AfterEach
    void tearDown() {
        actionDomainEventListener.getEvents().clear();
    }

    @Test @Tag("Incubating")
    void testWrapper_waitingOnDomainEvent() throws InterruptedException, ExecutionException {

        val inventoryManager = factoryService.viewModel(JdoInventoryManager.class);
        List<JdoProduct> jdoProducts = repository.allInstances(JdoProduct.class);
        val product = jdoProducts.get(0);

        assertEquals(99d, product.getPrice(), 1E-6);

        wrapper.wrap(inventoryManager).updateProductPrice(product, 123);

        assertEquals(123d, product.getPrice(), 1E-6);

        Assertions.assertThat(actionDomainEventListener.getEvents()).hasSize(5);
    }

    @Test @Tag("Incubating")
    void testWrapper_async_waitingOnDomainEvent() throws InterruptedException, ExecutionException, TimeoutException {

        val inventoryManager = factoryService.viewModel(JdoInventoryManager.class);
        val product = repository.allInstances(JdoProduct.class).get(0);

        assertEquals(99d, product.getPrice(), 1E-6);

        // when
        val control = returning(JdoProduct.class);
        wrapper.asyncWrap(inventoryManager, control)
                .updateProductPrice(product, 123d);

        // then
        Future<JdoProduct> future = control.getFuture();
        assertNotNull(future);

        JdoProduct product_from_async = future.get(2, TimeUnit.SECONDS);
        assertEquals(123d, product_from_async.getPrice(), 1E-6);
        assertNotSame(product, product_from_async.getPrice()); // what is returned is a copy...

        Assertions.assertThat(actionDomainEventListener.getEvents()).hasSize(5);

        // given still that ...
        assertEquals(99d, product.getPrice(), 1E-6);

        // when
        val productRefreshed = repositoryService.refresh(product);

        // then
        assertEquals(123d, productRefreshed.getPrice(), 1E-6);
    }
    

    @Service
    public static class ActionDomainEventListener {

        @Getter
        private final List<JdoInventoryManager.UpdateProductPriceEvent> events = new ArrayList<>();

        @EventListener(JdoInventoryManager.UpdateProductPriceEvent.class)
        public void onDomainEvent(JdoInventoryManager.UpdateProductPriceEvent event) {
            events.add(event);
        }

    }


}
