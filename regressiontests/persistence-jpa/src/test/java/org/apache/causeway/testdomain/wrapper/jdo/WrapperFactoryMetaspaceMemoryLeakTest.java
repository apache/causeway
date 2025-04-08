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

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaInventoryManager;
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

    @Inject private RepositoryService repository;
    @Inject private WrapperFactory wrapper;

    @Test
    void testWrapper_waitingOnDomainEvent() {

        val inventoryManager = factoryService.viewModel(JpaInventoryManager.class);
        val inventoryManager2 = factoryService.viewModel(JpaInventoryManager.class);

        // multiple calls with first target
        for (var i = 0; i<2 ; i++) {
            MemoryUsage.measureMetaspace("target #1." + i, ()->{
                wrapper.wrap(inventoryManager).getAllProducts();
            });
        }

        // multiple calls with first target
        for (var i = 0; i<2 ; i++) {
            MemoryUsage.measureMetaspace("target #2." + i, ()->{
                wrapper.wrap(inventoryManager2).getAllProducts();
            });
        }
    }
}


