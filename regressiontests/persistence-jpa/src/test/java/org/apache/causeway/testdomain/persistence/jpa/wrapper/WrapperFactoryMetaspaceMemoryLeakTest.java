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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.commons.internal.base._Blackhole;
import org.apache.causeway.commons.internal.debug._MemoryUsage;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures;
import org.apache.causeway.testdomain.jpa.JpaInventoryManager;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.RequiredArgsConstructor;

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

    //with caching
    //  exercise(1, 0);         // 2,221 KB
    //  exercise(1, 2000);      // 3,839 KB. // some leakage from collections
    //  exercise(20, 0);        // 2,112 KB
    //  exercise(20, 2000);     // 3,875 KB
    //  exercise(2000, 0);      // 3,263 KB  // ? increased some, is it significant; a lot less than without caching
    //  exercise(2000, 200);    // 4,294 KB
    //  exercise(20000, 0);     // 3,243 KB  // no noticeable leakage compared to 2000; MUCH less than without caching
    //without caching
    //  exercise(1, 0);        //   2,244 KB
    //  exercise(1, 2000);     //   3,669 KB // some leakage from collections
    //  exercise(20, 0);       //   2,440 KB
    //  exercise(20, 2000);    //   4,286 KB
    //  exercise(2000, 0);     //  14,580 KB // significant leakage from 20
    //  exercise(2000, 200);   //  20,423 KB
    //  exercise(20000, 0);    //.115,729 KB
    @RequiredArgsConstructor
    enum Scenario {
        TWO_K_TIMES_ONE(2000, 0, 4000),
        TWO_K_TIMES_TEN(2000, 10, 4000),;
        final int instances;
        final int loops;
        final int thresholdKibi;
    }

    @ParameterizedTest
    @EnumSource(Scenario.class)
    void testWrapper_waitingOnDomainEventScenario(final Scenario scenario) throws InterruptedException {
        var usage = _MemoryUsage.measureMetaspace(()->
            exercise(scenario.instances, scenario.loops));
        Assertions.assertTrue(usage.usedInKibiBytes() < scenario.thresholdKibi,
            ()->"%s exceeds expected %dKB threshold".formatted(usage, scenario.thresholdKibi));
        System.out.printf("scenario %s usage %s%n", scenario.name(), usage);
    }

    private void exercise(final int instances, final int loops) {
        for (int i = 0; i < instances; i++) {
            var jpaInventoryManager = wrapper.wrap(factoryService.viewModel(JpaInventoryManager.class));
            jpaInventoryManager.foo();

            for (var j = 0; j < loops; j++) {
                jpaInventoryManager
                    .getAllProducts()
                    .stream()
                    .map(JpaProduct::getName)
                    .forEach(_Blackhole::consume);
            }
        }
    }
}


