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
package org.apache.causeway.testdomain.persistence.jdo;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures.Lock;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JdoBootstrappingTest"
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoBootstrappingTest extends CausewayIntegrationTestAbstract {

    @Inject private JdoTestFixtures testFixtures;
    private static Lock lock;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test @Order(0) @Commit
    void aquireLock() {
        lock = testFixtures.aquireLock(); // concurrent test synchronization
        lock.install();
    }

    @Test @Order(1) @Commit
    void sampleInventoryShouldBeSetUp() {

        // when - expected condition before install: no inventories
        testFixtures.resetTo3Books(()->
            assertEquals(0, repositoryService.allInstances(JdoInventory.class).size()));

        // then - expected post condition: ONE inventory

        var inventories = repositoryService.allInstances(JdoInventory.class);
        assertEquals(1, inventories.size());

        var inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(3, inventory.getProducts().size());

        var expectedBookTitles = JdoTestFixtures.expectedBookTitles();

        var multipleBooks = Can.ofCollection(inventory.getProducts())
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());

        var firstProduct = inventory.getProducts().iterator().next();

        testFixtures.assertHasPersistenceId(firstProduct);
    }

    @Test @Order(2) @Commit
    void aSecondRunShouldWorkAsWell() {
        sampleInventoryShouldBeSetUp();
    }

    @Test @Order(3) @Commit
    void releaseLock() {
        lock.release(); // concurrent test synchronization
    }

}
