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
package org.apache.causeway.testdomain.persistence.jpa;

import java.sql.SQLException;
import java.util.Optional;

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

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaInventory;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        })
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// @DirtiesContext // doesn't seem to tidy up correctly ... I see InteractionService still injected into entities in the _next_ tests run (JpaExceptionTranslationTest_usingTransactional)
class JpaBootstrappingTest extends CausewayIntegrationTestAbstract {

    @Inject private Optional<PlatformTransactionManager> platformTransactionManager;
    @Inject private SpecificationLoader specLoader;
    @Inject private JpaTestFixtures testFixtures;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
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
        assertNotNull(productSpec.entityFacetElseFail());

        val inventorySpec = specLoader.loadSpecification(JpaInventory.class);
        assertTrue(inventorySpec.isEntity());
        assertNotNull(inventorySpec.entityFacetElseFail());
    }

    @Test @Order(1) @Rollback(false)
    void sampleInventoryShouldBeSetUp() {

        // given - expected pre condition: no inventories

        testFixtures.reinstall(()->
            assertEquals(0, repositoryService.allInstances(JpaInventory.class).size()));

        // then - expected post condition: ONE inventory

        val inventories = repositoryService.allInstances(JpaInventory.class);
        assertEquals(1, inventories.size());

        val inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(3, inventory.getProducts().size());

        val expectedBookTitles = JdoTestFixtures.expectedBookTitles();

        val multipleBooks = Can.ofCollection(inventory.getProducts())
                .filter(book->expectedBookTitles.contains(book.getName()));

        assertEquals(3, multipleBooks.size());

        val firstProduct = inventory.getProducts().iterator().next();

        testFixtures.assertHasPersistenceId(firstProduct);
    }

    @Test @Order(2) @Rollback(false)
    void aSecondRunShouldWorkAsWell() {
        sampleInventoryShouldBeSetUp();
    }


}
