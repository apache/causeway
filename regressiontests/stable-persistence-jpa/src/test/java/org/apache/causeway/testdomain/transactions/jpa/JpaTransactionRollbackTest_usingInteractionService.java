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
package org.apache.causeway.testdomain.transactions.jpa;

import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._Refs;
import org.apache.causeway.commons.internal.base._Refs.ObjectReference;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.transaction.events.TransactionAfterCompletionEvent;
import org.apache.causeway.core.transaction.events.TransactionBeforeCompletionEvent;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaTestDomainPersona;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.causeway.testing.integtestsupport.applib.CausewayInteractionHandler;

import lombok.NonNull;
import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                JpaTransactionRollbackTest_usingInteractionService.CommitListener.class
        },
        properties = {
//                "logging.level.org.springframework.test.context.transaction.*=DEBUG",
//                "logging.level.org.springframework.orm.jpa.*=DEBUG",
        })
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@ExtendWith({CausewayInteractionHandler.class})
@DirtiesContext
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class JpaTransactionRollbackTest_usingInteractionService
//extends CausewayIntegrationTestAbstract
{

    @Inject private FixtureScripts fixtureScripts;
    @Inject private TransactionService transactionService;
    @Inject private InteractionService interactionService;
    @Inject private RepositoryService repository;
    @Inject private CommitListener commitListener;

    private ObjectReference<TransactionAfterCompletionEvent> transactionAfterCompletionEvent;

    @BeforeEach
    void setUp() {

        // cleanup
        fixtureScripts.runPersona(JpaTestDomainPersona.InventoryPurgeAll);

        transactionAfterCompletionEvent =
                _Refs.<TransactionAfterCompletionEvent>objectRef(null);
    }

    @AfterEach
    void cleanUp() {
    }

    @Test
    void happyCaseTx_shouldCommit() {

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // expected pre condition
            assertEquals(0, repository.allInstances(JpaBook.class).size());
        });

        commitListener.bind(transactionAfterCompletionEvent::set);

        interactionService.runAnonymous(()->{

            fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);
        });

        assertEquals(
                TransactionAfterCompletionEvent.COMMITTED,
                transactionAfterCompletionEvent.getValueElseDefault(null));

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // expected post condition
            assertEquals(1, repository.allInstances(JpaBook.class).size());
        });

    }

    @Test
    void whenExceptionWithinTx_whileNotParticipating_shouldRollback() {

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // expected pre condition
            assertEquals(0, repository.allInstances(JpaBook.class).size());
        });

        //_Probe.errOut("before tx that should trigger a rollback");

        commitListener.bind(transactionAfterCompletionEvent::set);

        val result = interactionService.runAnonymousAndCatch(()->{

            fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);

            throw new RuntimeException("Test: force current tx to rollback");
        });

        //_Probe.errOut("after tx that should have triggered a rollback");

        assertTrue(result.isFailure());
        assertEquals(
                TransactionAfterCompletionEvent.ROLLED_BACK,
                transactionAfterCompletionEvent.getValueElseDefault(null));

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // expected post condition
            assertEquals(0, repository.allInstances(JpaBook.class).size());
        });

    }

    @Test
    void whenExceptionWithinTx_whileParticipating_shouldRollback() {

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // expected pre condition
            assertEquals(0, repository.allInstances(JpaBook.class).size());
        });

        //_Probe.errOut("before outer tx");

        commitListener.bind(transactionAfterCompletionEvent::set);

        val result = interactionService.runAnonymousAndCatch(()->{

            //_Probe.errOut("before tx that should trigger a rollback");

            val innerResult = transactionService.runWithinCurrentTransactionElseCreateNew(()->{

                fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);

                throw new RuntimeException("Test: force current tx to rollback");
            });

            assertTrue(innerResult.isFailure());

            //_Probe.errOut("after tx that should have triggered a rollback");

        });

        //_Probe.errOut("after outer tx");

        // interactionService detects whether a rollback was requested and does not throw in such a case
        assertTrue(result.isSuccess());

        val actualEvent = transactionAfterCompletionEvent.getValueElseDefault(null);
        assertTrue(
                actualEvent == TransactionAfterCompletionEvent.ROLLED_BACK);

        transactionService.runWithinCurrentTransactionElseCreateNew(()->{

            // expected post condition
            assertEquals(0, repository.allInstances(JpaBook.class).size());
        });

    }

    // -- HELPER

    @Service
    public static class CommitListener {

        /** transaction end boundary (pre) */
        @EventListener(TransactionBeforeCompletionEvent.class)
        public void onPreCompletion(final TransactionBeforeCompletionEvent event) {
            //_Probe.errOut("=== TRANSACTION before completion");
        }

        /** transaction end boundary (post) */
        @EventListener(TransactionAfterCompletionEvent.class)
        public void onPostCompletion(final TransactionAfterCompletionEvent event) {
            //_Probe.errOut("=== TRANSACTION after completion (%s)", event.name());
            Optional.ofNullable(listener)
            .ifPresent(li->{
                li.accept(event);
                unbind();
            });
        }

        private Consumer<TransactionAfterCompletionEvent> listener;

        void bind(final @NonNull Consumer<TransactionAfterCompletionEvent> listener) {
            this.listener = listener;
        }

        void unbind() {
            this.listener = null;
        }

    }


}
