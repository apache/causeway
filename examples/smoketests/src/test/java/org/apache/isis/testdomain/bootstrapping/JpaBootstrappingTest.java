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
package org.apache.isis.testdomain.bootstrapping;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
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

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.Incubating;
import org.apache.isis.testdomain.Smoketest;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.JpaBook;
import org.apache.isis.testdomain.jpa.JpaInventory;
import org.apache.isis.testdomain.jpa.JpaProduct;

import lombok.val;

@Smoketest
@SpringBootTest(
        classes = { 
                Configuration_usingJpa.class,
        }
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Incubating("JPA support is under construction")
class JpaBootstrappingTest {

    @Inject private RepositoryService repository;
    //@Inject private TransactionService transactionService;

    @BeforeAll
    static void beforeAll() throws SQLException {
        //XXX is it actually the case that tests might run in parallel?
        //assertFalse(IsisSession.isInSession()); // expected pre condition 
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @AfterAll
    static void afterAll() throws SQLException {
        //		assertFalse(IsisSession.isInSession()); // expected post condition
    }


    void cleanUp() {

        repository.allInstances(JpaInventory.class).forEach(repository::remove);
        repository.allInstances(JpaBook.class).forEach(repository::remove);
        repository.allInstances(JpaProduct.class).forEach(repository::remove);
        System.out.println("!!! CLEANUP DONE");
    }

    void setUp() {

        // setup sample Inventory
        Set<JpaProduct> products = new HashSet<>();

        products.add(JpaBook.of("Sample Book", "A sample book for testing.", 99., "Sample Author", "Sample ISBN",
                "Sample Publisher"));

        val inventory = JpaInventory.of("Sample Inventory", products);
        repository.persist(inventory);

        System.out.println("!!! SETUP DONE");
    }

    @Test @Order(1) @Rollback(false) 
    void sampleInventoryShouldBeSetUp() {

        // given - expected pre condition: no inventories

        cleanUp();
        assertEquals(0, repository.allInstances(JpaInventory.class).size());
        System.out.println("!!! VERIFY CLEANUP DONE");

        // when

        setUp();

        // then - expected post condition: ONE inventory

        val inventories = repository.allInstances(JpaInventory.class);
        assertEquals(1, inventories.size());

        val inventory = inventories.get(0);
        assertNotNull(inventory);
        assertNotNull(inventory.getProducts());
        assertEquals(1, inventory.getProducts().size());

        val product = inventory.getProducts().iterator().next();
        assertEquals("Sample Book", product.getName());

    }

    @Test @Order(2) @Rollback(false)
    void aSecondRunShouldWorkAsWell() {
        sampleInventoryShouldBeSetUp();
    }

}
