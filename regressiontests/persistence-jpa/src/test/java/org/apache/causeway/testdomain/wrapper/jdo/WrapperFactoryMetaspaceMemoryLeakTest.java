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

import javax.inject.Inject;

import org.apache.causeway.commons.memory.MemoryUsage;

import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaInventoryManager;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

import java.util.List;


@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JpaWrapperSyncTest"
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
class WrapperFactoryMetaspaceMemoryLeakTest extends CausewayIntegrationTestAbstract {

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
    void testWrapper_waitingOnDomainEvent() throws InterruptedException {

// with caching
        MemoryUsage.measureMetaspace("whole thing", ()->{
//            extracted(1, 1);      // 1,980 KB
//            extracted(1, 2000);   // 3,794 KB
//            extracted(20, 1);     // 2,468 KB
            extracted(20, 2000);    // 3,636 KB
//
// without caching
//            extracted(1, 1);      //   2,114 KB
//            extracted(1, 1000);   //   9,846 KB
//            extracted(20, 1);     //   2,635 KB
//            extracted(20, 2000);  // 217,582 KB
        });
    }

    private void extracted(int instances, int loops) {
        for(int i = 0; i < instances; i++) {
            val inventoryManager = factoryService.viewModel(JpaInventoryManager.class);

            for (var j = 0; j< loops; j++) {
                List<JpaProduct> allProducts = wrapper.wrap(inventoryManager).getAllProducts();
                allProducts.forEach(product -> {
                    String unused = product.getName();
                });
            }
        }
    }
}


