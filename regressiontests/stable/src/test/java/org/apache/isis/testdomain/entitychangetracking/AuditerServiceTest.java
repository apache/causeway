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
package org.apache.isis.testdomain.entitychangetracking;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.wrapper.DisabledException;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.testdomain.auditing.AuditerServiceForTesting;
import org.apache.isis.testdomain.auditing.Configuration_usingAuditing;
import org.apache.isis.testdomain.conf.Configuration_usingJdo;
import org.apache.isis.testdomain.jdo.JdoTestDomainPersona;
import org.apache.isis.testdomain.jdo.entities.JdoBook;
import org.apache.isis.testdomain.util.CollectionAssertions;
import org.apache.isis.testdomain.util.kv.KVStoreForTesting;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import static org.apache.isis.applib.services.wrapper.control.AsyncControl.returningVoid;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJdo.class,
                Configuration_usingAuditing.class
        }, 
        properties = {
                "logging.level.org.apache.isis.testdomain.util.rest.KVStoreForTesting=DEBUG"
        })
@TestPropertySource({
    IsisPresets.UseLog4j2Test
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuditerServiceTest extends IsisIntegrationTestAbstract {

    @Inject private RepositoryService repository;
    @Inject private FixtureScripts fixtureScripts;
    @Inject private WrapperFactory wrapper;
    @Inject private KVStoreForTesting kvStore;
    @Inject private PlatformTransactionManager txMan;

    @BeforeEach
    void setUp() {
        // cleanup
        fixtureScripts.runPersona(JdoTestDomainPersona.PurgeAll);

        // given
        fixtureScripts.runPersona(JdoTestDomainPersona.InventoryWith1Book);
    }

    @Test @Order(1)
    void auditerService_shouldBeAwareOfInventoryChanges() {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        AuditerServiceForTesting.clearAuditEntries(kvStore);

        _Probe.errOut("(1) BEFORE BOOK UPDATED");

        // when - running within its own transactional boundary
        val transactionTemplate = new TransactionTemplate(txMan);
        transactionTemplate.execute(status -> {

            book.setName("Book #2");
            repository.persist(book);

            // then - before the commit
            assertFalse(kvStore.get(AuditerServiceForTesting.class, "audit").isPresent());

            return null;
        });

        _Probe.errOut("(1) AFTER BOOK UPDATED");

        // then - after the commit
        assertHasAuditEntries(Can.of(
                "Jdo Book/name: 'Sample Book' -> 'Book #2'"));
    }

    @Test @Order(2)
    void auditerService_shouldBeAwareOfInventoryChanges_whenUsingSyncExecution() throws ExecutionException, InterruptedException, TimeoutException {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        AuditerServiceForTesting.clearAuditEntries(kvStore);

        _Probe.errOut("(2) BEFORE BOOK UPDATED");

        // when - running synchronous
        val control = SyncControl.control().withSkipRules();
        wrapper.wrap(book, control).setName("Book #2");

        _Probe.errOut("(2) AFTER BOOK UPDATED");

        // then - after the commit
        assertHasAuditEntries(Can.of(
                "Jdo Book/name: 'Sample Book' -> 'Book #2'"));
    }


    @Test @Order(3)
    void auditerService_shouldBeAwareOfInventoryChanges_whenUsingAsyncExecution() throws ExecutionException, InterruptedException, TimeoutException {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        AuditerServiceForTesting.clearAuditEntries(kvStore);
        val latch = kvStore.latch(AuditerServiceForTesting.class);

        _Probe.errOut("(3) BEFORE BOOK UPDATED");

        // when - running within its own background task
        AsyncControl<Void> control = returningVoid().withSkipRules();
        wrapper.asyncWrap(book, control).setName("Book #2");

        control.getFuture().get(10, TimeUnit.SECONDS); // blocks the current thread

        latch.await(2, TimeUnit.SECONDS);

        _Probe.errOut("(3) AFTER BOOK UPDATED");

        // then - after the commit
        assertHasAuditEntries(Can.of(
                "Jdo Book/name: 'Sample Book' -> 'Book #2'"));
    }

    @Test @Order(4)
    void auditerService_shouldNotBeAwareOfInventoryChanges_whenUsingAsyncExecutionThatFails() {

        // given
        val book = repository.allInstances(JdoBook.class).listIterator().next();
        AuditerServiceForTesting.clearAuditEntries(kvStore);

        // when - running within its own background task
        assertThrows(DisabledException.class, ()->{

            wrapper.asyncWrap(book, returningVoid()).setName("Book #2");

            returningVoid().getFuture().get(1000, TimeUnit.SECONDS);

        });

        // then - after the exception
        assertHasAuditEntries(Can.empty());
    }

    // -- HELPER

    private void assertHasAuditEntries(Can<String> expectedAuditEntries) {
        val actualAuditEntries = AuditerServiceForTesting.getAuditEntries(kvStore);
        CollectionAssertions.assertComponentWiseEquals(expectedAuditEntries, actualAuditEntries);
    }


}
