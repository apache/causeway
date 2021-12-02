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
package org.apache.isis.testdomain.injecting.jdo;

import javax.inject.Inject;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.assertions._Assert;
import org.apache.isis.commons.internal.primitives._Longs.Bound;
import org.apache.isis.commons.internal.primitives._Longs.Range;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.jdo.entities.JdoProduct;
import org.apache.isis.testdomain.util.dto.BookDto;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
        },
        properties = {
//                "logging.level.org.apache.isis.persistence.jdo.integration.changetracking.JdoLifecycleListener=DEBUG",
//                "logging.level.org.apache.isis.testdomain.injecting.jdo.JdoEntityInjectingTest=DEBUG"
        }
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Log4j2
class JdoEntityInjectingTest extends IsisIntegrationTestAbstract {

    @Inject private FixtureScripts fixtureScripts;
    @Inject private RepositoryService repository;
    @Inject private KVStoreForTesting kvStore;

    @Test @Order(0) @Rollback(false)
    void init() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);
        kvStore.clear(JdoBook.class);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
        assertInjectCountRange(1, 3); //TODO there is some injection redundancy
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

    // -- HELPER

    private long getInjectCount() {
        return kvStore.getCounter(JdoBook.class, "injection-count");
    }

    private JdoBook getSampleBook() {
        val books = repository.allInstances(JdoProduct.class);
        assertEquals(1, books.size(), "book count");
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
