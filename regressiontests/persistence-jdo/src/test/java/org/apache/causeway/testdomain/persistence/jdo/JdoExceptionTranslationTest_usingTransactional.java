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
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.TestPropertySources;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.RegressionTestAbstract;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures.Lock;
import org.apache.causeway.testdomain.jdo.JdoInventoryDao;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoInventory;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                JdoInventoryDao.class,
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JdoExceptionTranslationTest_usingTransactional"
        })
@TestPropertySources({
    @TestPropertySource(CausewayPresets.UseLog4j2Test)
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JdoExceptionTranslationTest_usingTransactional
extends RegressionTestAbstract {

    @Inject private JdoTestFixtures testFixtures;
    @Inject private Provider<JdoInventoryDao> inventoryDao;
    private static Lock lock;

    @BeforeAll
    static void beforeAll() throws SQLException {
        // launch H2Console for troubleshooting ...
        // Util_H2Console.main(null);
    }

    @Test @Order(0)
    void aquireLock() {
        lock = testFixtures.aquireLock(); // concurrent test synchronization
        lock.install();
    }

    @Test @Order(1)
    void booksUniqueByIsbn_whenViolated_shouldThrowTranslatedException() {

        // when adding a book for which one with same ISBN already exists in the database,
        // we expect to see a Spring recognized DataAccessException been thrown

        final ThrowingRunnable uniqueConstraintViolator =
                ()->inventoryDao.get().addBook_havingIsbnA_usingRepositoryService();

        assertThrows(DataIntegrityViolationException.class, ()->{

            interactionService.runAnonymous(()->{

                Try.run(uniqueConstraintViolator)
                .ifSuccess(__->fail("expected to fail, but did not"))
                .ifFailure(ex->{
                    if(!(ex instanceof DataIntegrityViolationException)) {
                        ex.printStackTrace();
                    }
                })
                .ifFailureFail();

            });

        });

    }

    @Test @Order(2)
    @Transactional @Commit
    void booksUniqueByIsbn_verifyPhase() {

        // expected post condition: ONE inventory with 3 books

        interactionService.runAnonymous(()->{

            var inventories = repositoryService.allInstances(JdoInventory.class);
            assertEquals(1, inventories.size());

            var inventory = inventories.get(0);
            assertNotNull(inventory);

            assertNotNull(inventory);
            assertNotNull(inventory.getProducts());
            assertEquals(3, inventory.getProducts().size());

            testFixtures.assertInventoryHasBooks(inventory.getProducts(), 1, 2, 3);

        });

    }

    @Test @Order(3)
    @Transactional @Commit
    void releaseLock() {
        lock.release(); // concurrent test synchronization
    }

}
