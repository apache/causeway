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

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures.Lock;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.jpa.entities.JpaInventory;
import org.apache.causeway.testdomain.jpa.entities.JpaProduct;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JpaQueryTest",
        })
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class JpaQueryTest extends CausewayIntegrationTestAbstract {

    @Inject private JpaTestFixtures testFixtures;
    @Inject private InteractionService interactionService;
    @Inject private JpaSupportService jpaSupport;
    @Inject ConfigurableBeanFactory configurableBeanFactory;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    private static Lock lock;
    @AfterAll
    static void afterAll() throws SQLException {
        if(lock!=null) {
            lock.release();
        }
    }

    @Test @Order(0) @Commit
    void setUpWith3Books() {
        lock = testFixtures.aquireLockAndClear();
        lock.install();
    }

    @Test @Order(1)
    void sampleInventory_shouldBeSetUpWith3Books() {

        final boolean inInteraction = interactionService.isInInteraction();
        Assertions.assertThat(inInteraction).isTrue();

        // when

        val inventories = repositoryService.allInstances(JpaInventory.class);

        // then - expected post condition: ONE inventory with 3 books

        assertEquals(1, inventories.size());

        val inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(3, inventory.getProducts().size());

        testFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);
    }

    @Test @Order(2)
    void sampleInventory_shouldSupportQueryCount() {

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)),
                1, 2, 3);

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)
                        .withLimit(2)),
                1, 2);
    }

    @Test @Order(3) @Disabled("start not supported, should throw unsupported exception maybe?")
    void sampleInventory_shouldSupportQueryStart() {

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)
                        .withStart(1)),
                2, 3);

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)
                        .withRange(1, 1)),
                2);
    }

    @Test @Order(4)
    void sampleInventory_shouldSupportNamedQueries() {

        val query = Query.named(JpaBook.class, "JpaInventory.findAffordableProducts")
                .withParameter("priceUpperBound", 60.);

        val affordableBooks = repositoryService.allMatches(query);
        testFixtures.assertInventoryHasBooks(affordableBooks, 1, 2);
    }

    @Test @Order(5)
    void sampleInventory_shouldSupportJpaCriteria() {

        val em = jpaSupport.getEntityManagerElseFail(JpaBook.class);

        val cb = em.getCriteriaBuilder();
        val cr = cb.createQuery(JpaBook.class);
        val root = cr.from(JpaBook.class);

        val affordableBooks = em
                .createQuery(cr.select(root).where(cb.between(root.get("price"), 0., 60. )))
                .getResultList();

        testFixtures.assertInventoryHasBooks(affordableBooks, 1, 2);
    }

    @Test @Order(99) @Disabled("broken won't fix")
    void previousTest_shouldHaveRolledBack() {
        assertEquals(0, repositoryService.allInstances(JpaInventory.class).size());
        assertEquals(0, repositoryService.allInstances(JpaProduct.class).size());
    }


}
