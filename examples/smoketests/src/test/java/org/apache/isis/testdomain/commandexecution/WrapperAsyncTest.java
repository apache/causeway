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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryManager;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;

import lombok.Getter;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                WrapperAsyncTest.ActionDomainEventListener.class
        }
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Incubating("wrapper.wrap(inventoryManager) throws NPE")
@DirtiesContext // because of the temporary installed ActionDomainEventListener
class WrapperAsyncTest {

    @Inject private FixtureScripts fixtureScripts;
    @Inject private RepositoryService repository;
    @Inject private FactoryService facoryService;
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

    @Test @Tag("Incubating")
    void testWrapper_waitingOnDomainEvent() throws InterruptedException, ExecutionException {

        val inventoryManager = facoryService.viewModel(JdoInventoryManager.class);
        val product = repository.allInstances(JdoProduct.class).get(0);

        assertEquals(99d, product.getPrice(), 1E-6);

        actionDomainEventListener.prepareLatch();

        wrapper.wrap(inventoryManager)
        .updateProductPrice(product, 123);

        assertTrue(
                actionDomainEventListener.getCountDownLatch()
                .await(2, TimeUnit.SECONDS)); //XXX just a reasonable long time period

        assertEquals(123d, product.getPrice(), 1E-6);
    }

    @Test @Tag("Incubating")
    void testWrapper_async_waitingOnDomainEvent() throws InterruptedException, ExecutionException {

        val inventoryManager = facoryService.viewModel(JdoInventoryManager.class);
        val product = repository.allInstances(JdoProduct.class).get(0);

        assertEquals(99d, product.getPrice(), 1E-6);

        actionDomainEventListener.prepareLatch();

        Future<JdoProduct> invocationResult = wrapper.async(inventoryManager)
                .withExecutor(Executors.newCachedThreadPool()) // use of custom executor (optional)
                .call(JdoInventoryManager::updateProductPrice, product, 123d);

//XXX type-safety should prevent this snippet from being compiled!        
//        Future<String> invocationResult2 = wrapper.async(inventoryManager)
//                .invoke(Product::toString);
        
        assertNotNull(invocationResult);
        

        assertTrue(
                actionDomainEventListener.getCountDownLatch()
                .await(2, TimeUnit.SECONDS)); //XXX just a reasonable long time period
        
        assertEquals(123d, product.getPrice(), 1E-6);
        
        val product_asReturnedByTheAsyncTask = invocationResult.get();
        assertNotNull(product_asReturnedByTheAsyncTask);
        assertEquals(123d, product_asReturnedByTheAsyncTask.getPrice(), 1E-6);
    }
    

    @Service @Log4j2
    public static class ActionDomainEventListener {

        @Getter private CountDownLatch countDownLatch;

        @EventListener(JdoInventoryManager.UpdateProductPriceEvent.class)
        public void onDomainEvent(JdoInventoryManager.UpdateProductPriceEvent event) {
            if(event.getEventPhase()==Phase.EXECUTED) {
                log.info("UpdateProductPriceEvent received.");
                countDownLatch.countDown();
            }
        }

        public void prepareLatch() {
            countDownLatch = new CountDownLatch(1);
        }

    }


}
