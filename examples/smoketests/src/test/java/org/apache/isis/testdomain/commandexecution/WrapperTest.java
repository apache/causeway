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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.events.domain.AbstractDomainEvent.Phase;
import org.apache.isis.applib.services.background.BackgroundService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.WrapperFactory.ExecutionMode;
import org.apache.isis.extensions.fixtures.fixturescripts.FixtureScripts;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.InventoryManager;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.Product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.Getter;
import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJdo.class,
                WrapperTest.ActionDomainEventListener.class
        }, 
        properties = {
                "logging.config=log4j2-test.xml",
                //        		"logging.level.org.apache.isis.jdo.persistence.IsisPlatformTransactionManagerForJdo=DEBUG",
                //        		"logging.level.org.apache.isis.jdo.persistence.PersistenceSession5=DEBUG",
                //        		"logging.level.org.apache.isis.jdo.persistence.IsisTransactionJdo=DEBUG",
        })
class WrapperTest {

    @Inject FixtureScripts fixtureScripts;
    @Inject RepositoryService repository;
    @Inject BackgroundService backgroundService;
    @Inject FactoryService facoryService;
    @Inject WrapperFactory wrapperFactory;
    @Inject ActionDomainEventListener actionDomainEventListener;

    @BeforeEach
    void setUp() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @Test
    void testWrapper_waitingOnDomainEvent() throws InterruptedException, ExecutionException {

        val inventoryManager = facoryService.viewModel(InventoryManager.class);
        val product = repository.allInstances(Product.class).get(0);

        assertEquals(99d, product.getPrice(), 1E-6);

        actionDomainEventListener.prepareLatch();

        wrapperFactory.wrap(inventoryManager).updateProductPrice(product, 123);

        assertTrue(
                actionDomainEventListener.getCountDownLatch()
                .await(5, TimeUnit.SECONDS));

        assertEquals(123d, product.getPrice(), 1E-6);
    }

    @Test
    void testWrapper_async_waitingOnDomainEvent() throws InterruptedException, ExecutionException {

        val inventoryManager = facoryService.viewModel(InventoryManager.class);
        val product = repository.allInstances(Product.class).get(0);

        assertEquals(99d, product.getPrice(), 1E-6);

        actionDomainEventListener.prepareLatch();

        wrapperFactory.wrap(inventoryManager, ExecutionMode.ASYNC)
            .updateProductPrice(product, 123);

        assertTrue(
                actionDomainEventListener.getCountDownLatch()
                .await(5, TimeUnit.SECONDS));

        assertEquals(123d, product.getPrice(), 1E-6);
    }
    

    @Service
    public static class ActionDomainEventListener {

        @Getter private CountDownLatch countDownLatch;

        @EventListener(InventoryManager.UpdateProductPriceEvent.class)
        public void onDomainEvent(InventoryManager.UpdateProductPriceEvent event) {
            if(event.getEventPhase()==Phase.EXECUTED) {
                countDownLatch.countDown();
            }
        }

        public void prepareLatch() {
            countDownLatch = new CountDownLatch(1);
        }

    }


}
