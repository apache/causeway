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
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaInventoryDao;
import org.apache.causeway.testdomain.jpa.JpaTestFixtures;
import org.apache.causeway.testdomain.jpa.RegressionTestWithJpaFixtures;
import org.apache.causeway.testdomain.jpa.entities.JpaInventory;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                JpaInventoryDao.class
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JpaExceptionTranslationTest_usingTransactional",
        })
@TestPropertySources({
    @TestPropertySource(CausewayPresets.UseLog4j2Test)
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaExceptionTranslationTest_usingTransactional
extends RegressionTestWithJpaFixtures {

    @Inject private JpaTestFixtures testFixtures;
    @Inject private Provider<JpaInventoryDao> inventoryDao;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }


    @Test @Order(2)
    void booksUniqueByIsbn_whenViolated_shouldThrowTranslatedException() {

        // when adding a book for which one with same ISBN already exists in the database,
        // we expect to see a Spring recognized DataAccessException been thrown

        final ThrowingRunnable uniqueConstraintViolator =
                ()->inventoryDao.get().addBook_havingIsbnA_usingRepositoryService();

        assertThrows(DataIntegrityViolationException.class, ()->{

            interactionService.runAnonymous(()->{

                ThrowingRunnable.resultOf(uniqueConstraintViolator)
                .ifSuccess(__->fail("expected to fail, but did not"))
               // .mapFailure(ex->_JpaExceptionTranslator.translate(ex, txManager))
                .ifFailure(ex->assertTrue(ex instanceof DataIntegrityViolationException))
                .ifFailureFail();

            });

        });

    }

    @Test @Order(3)
    @Transactional @Rollback(false)
    void booksUniqueByIsbn_verifyPhase() {

        // expected post condition: ONE inventory with 3 books

        interactionService.runAnonymous(()->{

            val inventories = repositoryService.allInstances(JpaInventory.class);
            assertEquals(1, inventories.size());

            val inventory = inventories.get(0);
            assertNotNull(inventory);

            assertNotNull(inventory);
            assertNotNull(inventory.getProducts());
            assertEquals(3, inventory.getProducts().size());

            testFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);

        });

    }




}
