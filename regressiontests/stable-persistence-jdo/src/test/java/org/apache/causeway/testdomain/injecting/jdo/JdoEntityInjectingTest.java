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
package org.apache.causeway.testdomain.injecting.jdo;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.primitives._Longs.Bound;
import org.apache.causeway.commons.internal.primitives._Longs.Range;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJdo;
import org.apache.causeway.testdomain.fixtures.EntityTestFixtures.Lock;
import org.apache.causeway.testdomain.jdo.JdoTestFixtures;
import org.apache.causeway.testdomain.jdo.entities.JdoBook;
import org.apache.causeway.testdomain.jdo.entities.JdoProduct;
import org.apache.causeway.testdomain.util.dto.BookDto;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;
import org.apache.causeway.testing.integtestsupport.applib.CausewayIntegrationTestAbstract;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        },
        properties = {
                "spring.datasource.url=jdbc:h2:mem:JdoEntityInjectingTest"
//                "logging.level.org.apache.causeway.persistence.jdo.integration.changetracking.JdoLifecycleListener=DEBUG",
//                "logging.level.org.apache.causeway.testdomain.injecting.jdo.JdoEntityInjectingTest=DEBUG"
        }
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class JdoEntityInjectingTest extends CausewayIntegrationTestAbstract {

    @Inject private JdoTestFixtures jdoTestFixtures;
    @Inject private RepositoryService repository;
    @Inject private KVStoreForTesting kvStore;
    private static Lock lock;

    @Test @Order(0) @Commit
    void aquireLockAndInit() {
        // given
        lock = jdoTestFixtures.aquireLock();
        jdoTestFixtures.resetTo3Books(()->kvStore.clear(JdoBook.class));
        assertInjectCountRange(3, 12); //TODO there is some injection redundancy
    }


    @Test @Order(1)
    void sampleBook_shouldHave_injectionPointsResolved() {
        log.debug("TEST 1 ENTERING");

        //assertInjectCountRange(1, 2);

        val book = getSampleBook();
        assertTrue(book.hasInjectionPointsResolved());

        //assertInjectCountRange(1, 3);

        log.debug("TEST 1 EXITING");
    }

    @Test @Order(2)
    void sampleBook_shouldHave_injectionPointsResolved_whenFetchedAgain() {

        log.debug("TEST 2 ENTERING");

        //assertInjectCountRange(1, 2);

        val book = getSampleBook();
        assertTrue(book.hasInjectionPointsResolved());

        //assertInjectCountRange(1, 3);

        log.debug("TEST 2 EXITING");

    }

    @Test @Order(3)
    void sampleBook_shouldHave_injectionPointsResolved_whenFetchedAgain2() {

        log.debug("TEST 3 ENTERING");

        //assertInjectCountRange(1, 3);

        val book = getSampleBook();
        assertTrue(book.hasInjectionPointsResolved());

        //assertInjectCountRange(1, 4);

        log.debug("TEST 3 EXITING");
    }

    @Test @Order(4) @Commit
    void releaseLock() {
        lock.release();
    }


    // -- HELPER

    private long getInjectCount() {
        return kvStore.getCounter(JdoBook.class, "injection-count");
    }

    private JdoBook getSampleBook() {
        val books = repository.allInstances(JdoProduct.class);
        assertEquals(3, books.size(), "book count");
        val book = books.get(0);
        assertEquals(BookDto.sample().getName(), book.getName(), "book name");
        return (JdoBook)book;
    }

    private void assertInjectCountRange(final long lower, final long upper) {
        _Assert.assertRangeContains(
                Range.of(Bound.inclusive(lower), Bound.inclusive(upper)),
                getInjectCount(), "injection count");
    }

}
