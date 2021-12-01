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

import org.apache.isis.commons.functional.ThrowingRunnable;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.RegressionTestAbstract;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoInventoryDao;
import org.apache.isis.testdomain.jdo.JdoTestFixtures;
import org.apache.isis.testdomain.jdo.entities.JdoInventory;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                JdoInventoryDao.class,
        })
@TestPropertySources({
    @TestPropertySource(IsisPresets.UseLog4j2Test)
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoExceptionTranslationTest_usingTransactional
extends RegressionTestAbstract {

    @Inject private JdoTestFixtures testFixtures;
    @Inject private Provider<JdoInventoryDao> inventoryDao;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test @Order(1)
    @Transactional @Rollback(false)
    void booksUniqueByIsbn_setupPhase() {
        interactionService.runAnonymous(()->{

            testFixtures.setUp3Books();

        });
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
                //.mapFailure(ex->_JdoExceptionTranslator.translate(ex, txManager))
                .ifFailure(ex->assertTrue(ex instanceof DataIntegrityViolationException))
                .optionalElseFail();

            });

        });

    }

    @Test @Order(3)
    @Transactional @Rollback(false)
    void booksUniqueByIsbn_verifyPhase() {

        // expected post condition: ONE inventory with 3 books

        interactionService.runAnonymous(()->{

            val inventories = repositoryService.allInstances(JdoInventory.class);
            assertEquals(1, inventories.size());

            val inventory = inventories.get(0);
            assertNotNull(inventory);

            assertNotNull(inventory);
            assertNotNull(inventory.getProducts());
            assertEquals(3, inventory.getProducts().size());

            testFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);

        });

    }

    @Test @Order(4)
    @Transactional @Rollback(false)
    void booksUniqueByIsbn_cleanupPhase() {

        interactionService.runAnonymous(()->{

            testFixtures.cleanUpRepository();

        });

    }


}
