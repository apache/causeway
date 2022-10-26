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

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.testdomain.conf.Configuration_usingJpa;
import org.apache.causeway.testdomain.jpa.JpaTestDomainPersona;
import org.apache.causeway.testdomain.jpa.entities.JpaBook;
import org.apache.causeway.testdomain.util.interaction.InteractionBoundaryProbe;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;
import org.apache.causeway.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.causeway.testing.integtestsupport.applib.CausewayInteractionHandler;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
                InteractionBoundaryProbe.class
        })
@TestPropertySource(CausewayPresets.UseLog4j2Test)
/**
 * With this test we manage CausewayInteractions ourselves. (not sub-classing CausewayIntegrationTestAbstract)
 */
@DirtiesContext
@ExtendWith(CausewayInteractionHandler.class)
@DisabledIfSystemProperty(named = "isRunningWithSurefire", matches = "true")
class JpaTransactionScopeListenerTest {

    @Inject private FixtureScripts fixtureScripts;
    @Inject private TransactionService transactionService;
    @Inject private RepositoryService repository;
    @Inject private InteractionService interactionService;
    @Inject private KVStoreForTesting kvStore;

    /* Expectations:
     * 1. for each InteractionScope there should be a new InteractionBoundaryProbe instance
     * 2. for each Transaction the current InteractionBoundaryProbe should get notified
     *
     * first we have 1 InteractionScope with 1 expected Transaction during 'setUp'
     * then we have 1 InteractionScope with 1 expected Transaction within the test method
     *
     */

    @BeforeEach
    void setUp() {

        // new CausewayInteractionScope with a new transaction (#1)
        interactionService.runAnonymous(()->{

            // cleanup
            fixtureScripts.runPersona(JpaTestDomainPersona.InventoryPurgeAll);

        });

    }

    @AfterEach
    void cleanUp() {
    }

    @Test
    void sessionScopedProbe_shouldBeReused_andBeAwareofTransactionBoundaries() {

        // new CausewayInteractionScope with a new transaction (#2)
        interactionService.runAnonymous(()->{

            // expected pre condition
            // reuse transaction (#2)
            assertEquals(0, repository.allInstances(JpaBook.class).size());

            // reuse transaction (#2)
            transactionService.runWithinCurrentTransactionElseCreateNew(()->{

                fixtureScripts.runPersona(JpaTestDomainPersona.InventoryWith1Book);

            });

            // expected post condition
            // reuse transaction (#2)
            assertEquals(1, repository.allInstances(JpaBook.class).size());

        });

        final int expectedIaCount = 3;

        assertEquals(expectedIaCount, InteractionBoundaryProbe.totalInteractionsStarted(kvStore));
        assertEquals(expectedIaCount, InteractionBoundaryProbe.totalInteractionsEnded(kvStore));
        assertEquals(expectedIaCount, InteractionBoundaryProbe.totalTransactionsEnding(kvStore));
        assertEquals(expectedIaCount, InteractionBoundaryProbe.totalTransactionsCommitted(kvStore));

    }


}
