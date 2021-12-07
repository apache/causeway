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
package org.apache.isis.testdomain.persistence.jdo;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestFixtures;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        }
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoBootstrappingTest extends IsisIntegrationTestAbstract {

    @Inject private JdoTestFixtures testFixtures;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    void cleanUp() {
        testFixtures.cleanUpRepository();
    }

    void setUp() {

        // setup sample Inventory
        Set<JdoProduct> products = new HashSet<>();

        products.add(JdoBook.of("Sample Book", "A sample book for testing.", 99., "Sample Author", "Sample ISBN",
                "Sample Publisher"));

        val inventory = JdoInventory.of("Sample Inventory", products);
        repositoryService.persist(inventory);
    }

    @Test @Order(1) @Rollback(false)
    void sampleInventoryShouldBeSetUp() {

        // given - expected pre condition: no inventories

        cleanUp();
        assertEquals(0, repositoryService.allInstances(JdoInventory.class).size());

        // when

        setUp();

        // then - expected post condition: ONE inventory

        val inventories = repositoryService.allInstances(JdoInventory.class);
        assertEquals(1, inventories.size());

        val inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(1, inventory.getProducts().size());

        val product = inventory.getProducts().iterator().next();
        assertEquals("Sample Book", product.getName());

        testFixtures.assertHasPersistenceId(product);
    }

    @Test @Order(2) @Rollback(false)
    void aSecondRunShouldWorkAsWell() {
        sampleInventoryShouldBeSetUp();
    }

}
