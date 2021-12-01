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
package org.apache.isis.testdomain.persistence.jpa;

import java.sql.SQLException;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.JpaTestFixtures;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testdomain.jpa.entities.JpaProduct;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@DirtiesContext
class JpaBootstrappingTest extends IsisIntegrationTestAbstract {

    @Inject private Optional<PlatformTransactionManager> platformTransactionManager;
    @Inject private SpecificationLoader specLoader;
    @Inject private JpaTestFixtures testFixtures;

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
        SortedSet<JpaProduct> products = new TreeSet<>();

        products.add(JpaBook.of("Sample Book", "A sample book for testing.", 99., "Sample Author", "Sample ISBN",
                "Sample Publisher"));

        val inventory = new JpaInventory("Sample Inventory", products);
        repositoryService.persistAndFlush(inventory);
    }

    @Test @Order(0)
    void platformTransactionManager_shouldBeAvailable() {
        assertTrue(platformTransactionManager.isPresent());
        platformTransactionManager.ifPresent(ptm->{
            assertEquals("JpaTransactionManager", ptm.getClass().getSimpleName());
        });
    }

    @Test @Order(0)
    void transactionalAnnotation_shouldBeSupported() {
        assertTrue(platformTransactionManager.isPresent());
        platformTransactionManager.ifPresent(ptm->{

            val txDef = new DefaultTransactionDefinition();
            txDef.setPropagationBehavior(DefaultTransactionDefinition.PROPAGATION_MANDATORY);

            val txStatus = ptm.getTransaction(txDef);

            assertNotNull(txStatus);
            assertFalse(txStatus.isCompleted());

        });
    }

    @Test @Order(0)
    void jpaEntities_shouldBeRecognisedAsSuch() {
        val productSpec = specLoader.loadSpecification(JpaProduct.class);
        assertTrue(productSpec.isEntity());
        assertNotNull(productSpec.getFacet(EntityFacet.class));

        val inventorySpec = specLoader.loadSpecification(JpaInventory.class);
        assertTrue(inventorySpec.isEntity());
        assertNotNull(inventorySpec.getFacet(EntityFacet.class));
    }

    @Test @Order(1) @Rollback(false)
    void sampleInventoryShouldBeSetUp() {

        // given - expected pre condition: no inventories

        cleanUp();
        assertEquals(0, repositoryService.allInstances(JpaInventory.class).size());

        // when

        setUp();

        // then - expected post condition: ONE inventory

        val inventories = repositoryService.allInstances(JpaInventory.class);
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
