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
package org.apache.causeway.testdomain.persistence.jpa.wrapper;

import jakarta.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.jpa.JpaInventoryManager;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JpaWrapperSyncTest"
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class JpaWrapperSyncTest extends CausewayIntegrationTestAbstract {

    @Inject private RepositoryService repository;
    @Inject private WrapperFactory wrapper;
    @Inject private JpaTestFixtures testFixtures;

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
    void testWrapper_waitingOnDomainEvent() {

        val inventoryManager = factoryService.viewModel(JpaInventoryManager.class);
        val sumOfPrices = repository.allInstances(JpaProduct.class)
                .stream()
                .mapToDouble(JpaProduct::getPrice)
                .sum();

        assertEquals(167d, sumOfPrices, 1E-6);

        val products = wrapper.wrap(inventoryManager).getAllProducts();

        assertEquals(3, products.size());
        assertEquals(JpaBook.class, products.get(0).getClass());
    }

}
