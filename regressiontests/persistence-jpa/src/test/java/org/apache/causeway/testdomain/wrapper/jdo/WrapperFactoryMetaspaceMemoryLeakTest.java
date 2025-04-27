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

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.commons.memory.MemoryUsage;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.jpa.JpaInventoryManager;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;


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
        MemoryUsage.measureMetaspace("exercise", ()->{
// with caching
//            exercise(1, 0);         // 2,221 KB
//            exercise(1, 2000);      // 3,839 KB. // some leakage from collections
//            exercise(20, 0);        // 2,112 KB
//            exercise(20, 2000);     // 3,875 KB
//            exercise(2000, 0);      // 3,263 KB. // ? increased some, is it significant; a lot less than without caching
//            exercise(2000, 200);    // 4,294 KB.
//            exercise(20000, 0);     // 3,243 KB  // no noticeable leakage compared to 2000; MUCH less than without caching

// without caching
//            exercise(1, 0);        //   2,244 KB
//            exercise(1, 2000);     //.  3,669 KB // some leakage from collections
//            exercise(20, 0);       //   2,440 KB
//            exercise(20, 2000);    //.  4,286 KB
            exercise(2000, 0);     //  14,580 KB // significant leakage from 20
//            exercise(2000, 200);   //  20,423 KB
//            exercise(20000, 0);    //.115,729 KB
        });
    }

    private void exercise(int instances, int loops) {
        for (int i = 0; i < instances; i++) {
            val inventoryManager = factoryService.viewModel(JpaInventoryManager.class);
            JpaInventoryManager jpaInventoryManager = wrapper.wrap(inventoryManager);
            jpaInventoryManager.foo();

            for (var j = 0; j < loops; j++) {
                List<JpaProduct> allProducts = jpaInventoryManager.getAllProducts();
                allProducts.forEach(product -> {
                    String unused = product.getName();
                });
            }
        }
    }
}


