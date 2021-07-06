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

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.persistence.jpa.applib.services.JpaSupportService;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.entities.JpaBook;
import org.apache.isis.testdomain.jpa.entities.JpaInventory;
import org.apache.isis.testdomain.jpa.entities.JpaProduct;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.testdomain.persistence.jpa._TestFixtures.assertInventoryHasBooks;
import static org.apache.isis.testdomain.persistence.jpa._TestFixtures.setUp3Books;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        }
        )
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaQueryTest extends IsisIntegrationTestAbstract {

    @Inject private JpaSupportService jpaSupport;
    @Inject private InteractionService interactionService;

//    @BeforeAll
//    static void beforeAll() throws SQLException {
//        // launch H2Console for troubleshooting ...
//        // Util_H2Console.main(null);
//        _Context.clear();
//    }

    @Inject
    ConfigurableBeanFactory configurableBeanFactory;

    @Test @Order(1)
    void sampleInventory_shouldBeSetUpWith3Books() {

        final boolean inInteraction = interactionService.isInInteraction();
        Assertions.assertThat(inInteraction).isTrue();
        setUp3Books(repositoryService);

        // when

        val inventories = repositoryService.allInstances(JpaInventory.class);

        // then - expected post condition: ONE inventory with 3 books

        assertEquals(1, inventories.size());

        val inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(3, inventory.getProducts().size());

        assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);
    }

    @Test @Order(2)
    void sampleInventory_shouldSupportQueryCount() {

        setUp3Books(repositoryService);

        assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)),
                1, 2, 3);

        assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)
                        .withLimit(2)),
                1, 2);
    }

    @Test @Order(3) @Disabled("start not supported, should throw unsupported exception maybe?")
    void sampleInventory_shouldSupportQueryStart() {

        setUp3Books(repositoryService);

        assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)
                        .withStart(1)),
                2, 3);

        assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JpaBook.class)
                        .withRange(1, 1)),
                2);
    }

    @Test @Order(4)
    void sampleInventory_shouldSupportNamedQueries() {

        setUp3Books(repositoryService);

        val query = Query.named(JpaBook.class, "JpaInventory.findAffordableProducts")
                .withParameter("priceUpperBound", 60.);

        val affordableBooks = repositoryService.allMatches(query);
        assertInventoryHasBooks(affordableBooks, 1, 2);
    }

    @Test @Order(5)
    void sampleInventory_shouldSupportJpaCriteria() {

        setUp3Books(repositoryService);

        val em = jpaSupport.getEntityManagerElseFail(JpaBook.class);

        val cb = em.getCriteriaBuilder();
        val cr = cb.createQuery(JpaBook.class);
        val root = cr.from(JpaBook.class);

        val affordableBooks = em
                .createQuery(cr.select(root).where(cb.between(root.get("price"), 0., 60. )))
                .getResultList();

        assertInventoryHasBooks(affordableBooks, 1, 2);
    }

    @Test @Order(99)
    void previousTest_shouldHaveRolledBack() {
        assertEquals(0, repositoryService.allInstances(JpaInventory.class).size());
        assertEquals(0, repositoryService.allInstances(JpaProduct.class).size());
    }


}
