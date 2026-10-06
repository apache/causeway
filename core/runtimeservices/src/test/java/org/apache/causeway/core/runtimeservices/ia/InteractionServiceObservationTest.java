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
package org.apache.causeway.core.runtimeservices.ia;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.metamodel.execution.ExecutionContext;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.core.security.authentication.InteractionContextFactory;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class InteractionServiceObservationTest {
    @Test void nestedLayersRestoreParentAndStopExactlyOnce() {
        var fixture = new Fixture();
        fixture.service.run(InteractionContextFactory.testing(), () -> {
            var parent = fixture.registry.getCurrentObservation();
            fixture.service.run(InteractionContextFactory.testing("nested"), () -> {
                assertNotSame(parent, fixture.registry.getCurrentObservation());
                assertEquals(2, fixture.service.getInteractionLayerCount());
            });
            assertSame(parent, fixture.registry.getCurrentObservation());
        });
        assertEquals(2, fixture.stopped.size());
        assertNull(fixture.registry.getCurrentObservation());
        assertFalse(fixture.service.isInInteraction());
    }
    @Test void workErrorSurvivesTransactionCleanupFailure() {
        var fixture = new Fixture();
        var workFailure = new AssertionError("work");
        var cleanupFailure = new IllegalStateException("cleanup");
        doThrow(cleanupFailure).when(fixture.transactions).onClose(any());
        assertSame(workFailure, assertThrows(AssertionError.class, () ->
                fixture.service.run(InteractionContextFactory.testing(), () -> { throw workFailure; })));
        assertSame(workFailure, fixture.stopped.get(0).getError());
        assertNotNull(fixture.stopped.get(0).getHighCardinalityKeyValue("causeway.interaction.id"));
        assertArrayEquals(new Throwable[]{cleanupFailure}, workFailure.getSuppressed());
        assertNull(fixture.registry.getCurrentObservation());
        assertFalse(fixture.service.isInInteraction());
        reset(fixture.transactions);
        when(fixture.transactions.currentTransactionState()).thenReturn(TransactionState.MUST_ABORT);
        fixture.service.call(InteractionContextFactory.testing(), () -> "next");
        assertEquals(2, fixture.stopped.size());
        assertNull(fixture.stopped.get(1).getError());
    }
    @Test void identityOptionsAreIndependentAndPreserveOtherTags() {
        for (boolean userName : new boolean[]{false, true}) {
            for (boolean tenancy : new boolean[]{false, true}) {
                var fixture = new Fixture(new org.apache.causeway.core.config.observation.CausewayObservationPolicy(
                        userName, tenancy, false, java.time.Duration.ofMillis(2)));
                var user = org.apache.causeway.applib.services.user.UserMemento.ofName("sentinel-user")
                        .withMultiTenancyToken("sentinel-tenant");
                fixture.service.run(org.apache.causeway.applib.services.iactn.InteractionContext.ofUserWithSystemDefaults(user), () -> {});
                var context = fixture.stopped.get(0);
                var name = context.getHighCardinalityKeyValue("causeway.user.name");
                var token = context.getHighCardinalityKeyValue("causeway.user.multiTenancyToken");
                assertEquals(userName, name != null);
                assertEquals(tenancy, token != null);
                if (userName) assertEquals("sentinel-user", name.getValue());
                if (tenancy) assertEquals("sentinel-tenant", token.getValue());
                assertNotNull(context.getLowCardinalityKeyValue("causeway.user.impersonating"));
                assertNotNull(context.getHighCardinalityKeyValue("causeway.interaction.clock"));
                if (!userName) assertFalse(context.getAllKeyValues().toString().contains("sentinel-user"));
                if (!tenancy) assertFalse(context.getAllKeyValues().toString().contains("sentinel-tenant"));
            }
        }
    }
    @Test void emptyIdentityValuesAreOmittedEvenWhenEnabled() {
        var registry = new Fixture().registry;
        var obs = Observation.createNotStarted("test", registry);
        var user = mock(org.apache.causeway.applib.services.user.UserMemento.class);
        when(user.name()).thenReturn("");
        var ic = org.apache.causeway.applib.services.iactn.InteractionContext.ofUserWithSystemDefaults(user);
        _Observation.addTags(obs, ic, 0, new org.apache.causeway.core.config.observation.CausewayObservationPolicy(
                true, true, false, java.time.Duration.ofMillis(2)));
        assertNull(obs.getContext().getHighCardinalityKeyValue("causeway.user.name"));
        assertNull(obs.getContext().getHighCardinalityKeyValue("causeway.user.multiTenancyToken"));
    }
    @Test void correlationTracksReplayAndOnlyTagsRoot() {
        var fixture = new Fixture();
        var replayId = UUID.fromString("12345678-1234-1234-1234-123456789abc");
        fixture.service.run(InteractionContextFactory.testing(), () -> {
            var root = fixture.registry.getCurrentObservation();
            var interaction = fixture.service.currentInteraction().orElseThrow();
            assertEquals(interaction.getInteractionId().toString(),
                    root.getContext().getHighCardinalityKeyValue("causeway.interaction.id").getValue());
            var dto = new org.apache.causeway.schema.cmd.v2.CommandDto();
            dto.setInteractionId(replayId.toString());
            // The exact identifier replacement used by CommandExecutorServiceDefault.
            interaction.getCommand().updater().setCommandDtoAndIdentifier(dto);
            fixture.service.run(InteractionContextFactory.testing("nested"), () -> {
                assertNull(fixture.registry.getCurrentObservation().getContext()
                        .getHighCardinalityKeyValue("causeway.interaction.id"));
            });
            assertSame(root, fixture.registry.getCurrentObservation());
        });
        var root = fixture.stopped.get(1);
        assertEquals(replayId.toString(), root.getHighCardinalityKeyValue("causeway.interaction.id").getValue());
        assertNull(root.getLowCardinalityKeyValue("causeway.interaction.id"));
        fixture.service.run(InteractionContextFactory.testing(), () -> {});
        assertNotEquals(replayId.toString(), fixture.stopped.get(2)
                .getHighCardinalityKeyValue("causeway.interaction.id").getValue());
        assertNull(fixture.registry.getCurrentObservation());
    }

    @Test void replayFailureStillExportsEffectiveIdentifier() {
        var fixture = new Fixture();
        var publisher = mock(org.apache.causeway.core.metamodel.services.publishing.CommandPublisher.class);
        var failure = new IllegalStateException("publisher failure after replay identity replacement");
        doThrow(failure).when(publisher).ready(any());
        var executor = new org.apache.causeway.core.runtimeservices.command.CommandExecutorServiceDefault(
                null, null, null, null, fixture.service, null, null, () -> publisher, null, null);
        var dto = new org.apache.causeway.schema.cmd.v2.CommandDto();
        dto.setInteractionId("12345678-1234-1234-1234-123456789abc");
        assertSame(failure, assertThrows(IllegalStateException.class, () ->
                fixture.service.run(InteractionContextFactory.testing(), () -> executor.executeCommand(dto))));
        assertEquals(dto.getInteractionId(), fixture.stopped.get(0)
                .getHighCardinalityKeyValue("causeway.interaction.id").getValue());
        assertSame(failure, fixture.stopped.get(0).getError());
        assertNull(fixture.registry.getCurrentObservation());
        assertFalse(fixture.service.isInInteraction());
    }

    @Test void reusedLayerKeepsOneDeterministicRootIdentifier() {
        var fixture = new Fixture();
        var context = InteractionContextFactory.testing();
        fixture.service.run(context, () -> {
            var root = fixture.registry.getCurrentObservation();
            fixture.service.run(context, () -> {
                assertSame(root, fixture.registry.getCurrentObservation());
                assertEquals(1, fixture.service.getInteractionLayerCount());
            });
        });
        assertEquals(1, fixture.stopped.size());
        assertEquals(new UUID(0, 1).toString(), fixture.stopped.get(0)
                .getHighCardinalityKeyValue("causeway.interaction.id").getValue());
    }

    private static class Fixture {
        final ObservationRegistry registry = ObservationRegistry.create();
        final ArrayList<Observation.Context> stopped = new ArrayList<>();
        final TransactionServiceSpring transactions = mock(TransactionServiceSpring.class);
        final InteractionServiceDefault service;
        Fixture() { this(org.apache.causeway.core.config.observation.CausewayObservationPolicy.DEFAULT); }
        Fixture(org.apache.causeway.core.config.observation.CausewayObservationPolicy policy) {
            registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
                public boolean supportsContext(Observation.Context context) { return true; }
                public void onStop(Observation.Context context) { stopped.add(context); }
            });
            var beanFactory = mock(ConfigurableBeanFactory.class);
            var scope = mock(Scope.class, withSettings().extraInterfaces(InteractionScopeLifecycleHandler.class));
            when(beanFactory.getRegisteredScope(InteractionScopeBeanFactoryPostProcessor.SCOPE_NAME)).thenReturn(scope);
            when(transactions.currentTransactionState()).thenReturn(TransactionState.MUST_ABORT);
            var executionContext = mock(ExecutionContext.class, RETURNS_DEEP_STUBS);
            var sequence = new java.util.concurrent.atomic.AtomicLong();
            when(executionContext.idGenerator().interactionId()).thenAnswer(__ -> new UUID(0, sequence.incrementAndGet()));
            service = new InteractionServiceDefault(beanFactory, mock(ServiceInjector.class), transactions,
                    mock(ClockService.class), () -> null, executionContext, new CausewayObservationIntegration(registry, policy));
        }
    }
}
