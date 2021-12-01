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

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.isis.applib.query.Query;
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
        })
@TestPropertySource(IsisPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class JdoQueryTest extends IsisIntegrationTestAbstract {

 //   @Inject private JdoSupportService jdoSupport;
    @Inject private JdoTestFixtures testFixtures;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test @Order(1)
    void sampleInventory_shouldBeSetUpWith3Books() {

        testFixtures.setUp3Books();

        // when

        val inventories = repositoryService.allInstances(JdoInventory.class);

        // then - expected post condition: ONE inventory with 3 books

        assertEquals(1, inventories.size());

        val inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(3, inventory.getProducts().size());

        testFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);
    }

    @Test @Order(2) @Disabled("broken won't fix")
    void sampleInventory_shouldSupportQueryCount() {

        testFixtures.setUp3Books();

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JdoBook.class)),
                1, 2, 3);

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JdoBook.class)
                        .withLimit(2)),
                1, 2);
    }

    @Test @Order(3) @Disabled("start not supported, should throw unsupported exception maybe?")
    void sampleInventory_shouldSupportQueryStart() {

        testFixtures.setUp3Books();

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JdoBook.class)
                        .withStart(1)),
                2, 3);

        testFixtures.assertInventoryHasBooks(repositoryService
                .allMatches(Query.allInstances(JdoBook.class)
                        .withRange(1, 1)),
                2);
    }

    @Test @Order(4) @Disabled("broken won't fix")
    void sampleInventory_shouldSupportNamedQueriesThroughApplib() {

        testFixtures.setUp3Books();

        val query = Query.named(JdoBook.class, "findAffordableBooks")
                .withParameter("priceUpperBound", 60.);

        val affordableBooks = repositoryService.allMatches(query);
        testFixtures.assertInventoryHasBooks(affordableBooks, 1, 2);
    }

//    @Test @Order(4)
//    void sampleInventory_shouldSupportNamedQueriesDirectly() {
//
//        setUp3Books();
//
//        val namedParams = _Maps.<String, Object>newHashMap();
//
//        val pm = jdoSupport.getPersistenceManagerFactory().getPersistenceManager();
//        val query = pm.newNamedQuery(JdoProduct.class, "findAffordableProducts")
//                .setNamedParameters(namedParams);
//        namedParams.put("priceUpperBound", 60.);
//
//        val affordableBooks = query.executeList();
//        assertInventoryHasBooks(affordableBooks, 1, 2);
//    }

//    @Test @Order(5)
//    void sampleInventory_shouldSupportJdoQuery() {
//
//        setUp3Books();
//
//        val pm = jdoSupport.getPersistenceManagerFactory().getPersistenceManager();
//        val query = pm.newQuery(JdoBook.class)
//                .filter("price <= 60.");
//
//        val affordableBooks = query.executeList();
//        assertInventoryHasBooks(affordableBooks, 1, 2);
//    }

    @Test @Order(99) @Disabled("broken won't fix")
    void previousTest_shouldHaveRolledBack() {
        assertEquals(0, repositoryService.allInstances(JdoInventory.class).size());
        assertEquals(0, repositoryService.allInstances(JdoProduct.class).size());
    }


}
