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
package org.apache.causeway.testdomain.wrapper.jdo;

import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.jdo.JdoInventoryManager;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JdoWrapperSyncTest"
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class JdoWrapperSyncTest extends CausewayIntegrationTestAbstract {

    @Inject private RepositoryService repository;
    @Inject private FactoryService facoryService;
    @Inject private WrapperFactory wrapper;
    @Inject private JdoTestFixtures testFixtures;

    protected EntityTestFixtures.Lock lock;

    @BeforeEach
    void installFixture() {
        this.lock = testFixtures.aquireLock();
        lock.install();
    }

    @AfterEach
    void uninstallFixture() {
        this.lock.release();
    }

    @Test
    void testWrapper_waitingOnDomainEvent() throws InterruptedException, ExecutionException {

        val inventoryManager = facoryService.viewModel(JdoInventoryManager.class);
        val sumOfPrices = repository.allInstances(JdoProduct.class)
                .stream()
                .mapToDouble(JdoProduct::getPrice)
                .sum();

        assertEquals(167d, sumOfPrices, 1E-6);

        val products = wrapper.wrap(inventoryManager).getAllProducts();

        assertEquals(3, products.size());
        assertEquals(JdoBook.class, products.get(0).getClass());
    }

}
