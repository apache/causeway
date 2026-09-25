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
package org.apache.causeway.core.runtimeservices.session;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Provider;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.xactn.TransactionState;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.interaction.scope.InteractionScopeBeanFactoryPostProcessor;
import org.apache.causeway.core.interaction.scope.InteractionScopeLifecycleHandler;
import org.apache.causeway.core.metamodel.services.publishing.CommandPublisher;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;
import org.apache.causeway.core.runtime.events.MetamodelEventService;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class InteractionServiceDefaultObservationTest {

    private static final UUID INTERACTION_ID =
            UUID.fromString("12345678-1234-1234-1234-123456789abc");

    @Test
    void oneRootObservationCoversNestedLayersAndClosesExactlyOnce() {
        final Fixture fixture = fixture(TransactionState.MUST_ABORT);

        fixture.service.openInteraction(mock(InteractionContext.class));
        fixture.handler.record("layer count after root open: " + fixture.service.getInteractionLayerCount());
        fixture.service.openInteraction(mock(InteractionContext.class));
        fixture.handler.record("layer count after nested open: " + fixture.service.getInteractionLayerCount());
        fixture.service.closeInteractionLayers();
        fixture.handler.record("layer count after close: " + fixture.service.getInteractionLayerCount());
        fixture.handler.record("current observation after close: "
                + currentObservationName(fixture.registry));
        assertEquals(INTERACTION_ID.toString(), fixture.handler.interactionId);

        Approvals.verify(report(
                "One root observation covers nested interaction layers",
                List.of(
                        "Opening the root layer starts and scopes one root observation.",
                        "Opening a nested layer creates no additional interaction observation.",
                        "Closing all layers closes the scope before stopping the root observation.",
                        "No interaction layer or current observation remains after cleanup."),
                fixture.handler.events));
    }

    @Test
    void workFailureIsRecordedAndOriginalFailureRequestsRollback() {
        final Fixture fixture = fixture(TransactionState.MUST_ABORT);
        final IllegalStateException failure = new IllegalStateException("domain work failed");

        final IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                () -> fixture.service.call(mock(InteractionContext.class), () -> {
                    throw failure;
                }));
        assertSame(failure, thrown);
        verify(fixture.transactionService).requestRollback(any());
        fixture.handler.record("original failure rethrown: " + (thrown == failure));
        fixture.handler.record("current observation after close: "
                + currentObservationName(fixture.registry));
        assertEquals(INTERACTION_ID.toString(), fixture.handler.interactionId);

        Approvals.verify(report(
                "Root interaction work failure",
                List.of(
                        "The root observation records the original failure.",
                        "Existing transaction rollback behavior is retained.",
                        "The original failure is rethrown unchanged.",
                        "Observation scope and retained state are cleaned up."),
                fixture.handler.events));
    }

    @Test
    void closeFailureIsRecordedAndLifecycleStateIsRemoved() {
        final Fixture fixture = fixture(TransactionState.IN_PROGRESS);
        final IllegalStateException failure = new IllegalStateException("flush failed");
        fixture.service.openInteraction(mock(InteractionContext.class));
        org.mockito.Mockito.doThrow(failure).when(fixture.transactionService).flushTransaction();

        final IllegalStateException thrown = assertThrows(
                IllegalStateException.class,
                fixture.service::closeInteractionLayers);
        assertSame(failure, thrown);
        verify(fixture.transactionService).requestRollback(any());
        fixture.handler.record("original close failure rethrown: " + (thrown == failure));
        fixture.handler.record("layer count after failed close: " + fixture.service.getInteractionLayerCount());
        fixture.handler.record("current observation after failed close: "
                + currentObservationName(fixture.registry));
        assertEquals(INTERACTION_ID.toString(), fixture.handler.interactionId);

        Approvals.verify(report(
                "Root interaction close failure",
                List.of(
                        "A transaction flush failure is recorded on the root observation.",
                        "Existing rollback and rethrow behavior is retained.",
                        "The observation closes even though interaction close failed.",
                        "Thread-local interaction and observation state is removed."),
                fixture.handler.events));
    }

    private static Fixture fixture(final TransactionState transactionState) {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);

        final ConfigurableBeanFactory beanFactory = mock(ConfigurableBeanFactory.class);
        final Scope interactionScope = mock(
                Scope.class,
                withSettings().extraInterfaces(InteractionScopeLifecycleHandler.class));
        when(beanFactory.getRegisteredScope(InteractionScopeBeanFactoryPostProcessor.SCOPE_NAME))
                .thenReturn(interactionScope);

        final TransactionServiceSpring transactionService = mock(TransactionServiceSpring.class);
        when(transactionService.currentTransactionState()).thenReturn(transactionState);

        final InteractionIdGenerator interactionIdGenerator = mock(InteractionIdGenerator.class);
        when(interactionIdGenerator.interactionId()).thenReturn(INTERACTION_ID);

        @SuppressWarnings("unchecked")
        final Provider<CommandPublisher> commandPublisherProvider = mock(Provider.class);
        final InteractionServiceDefault service = new InteractionServiceDefault(
                mock(MetamodelEventService.class),
                mock(SpecificationLoader.class),
                mock(ServiceInjector.class),
                transactionService,
                mock(ClockService.class),
                commandPublisherProvider,
                beanFactory,
                interactionIdGenerator,
                new CausewayObservationIntegration(registry));
        return new Fixture(service, transactionService, registry, handler);
    }

    private static String currentObservationName(final ObservationRegistry registry) {
        final Observation current = registry.getCurrentObservation();
        return current != null ? current.getContext().getName() : "<none>";
    }

    private static String report(
            final String scenario,
            final List<String> checks,
            final List<String> events) {
        final List<String> lines = new ArrayList<>();
        lines.add("Scenario: " + scenario);
        lines.add("");
        lines.add("This approval checks:");
        checks.forEach(check -> lines.add("- " + check));
        lines.add("");
        lines.add("Observed lifecycle:");
        for (int i = 0; i < events.size(); i++) {
            lines.add((i + 1) + ". " + events.get(i));
        }
        return String.join("\n", lines);
    }

    private static final class Fixture {
        private final InteractionServiceDefault service;
        private final TransactionServiceSpring transactionService;
        private final ObservationRegistry registry;
        private final RecordingHandler handler;

        private Fixture(
                final InteractionServiceDefault service,
                final TransactionServiceSpring transactionService,
                final ObservationRegistry registry,
                final RecordingHandler handler) {
            this.service = service;
            this.transactionService = transactionService;
            this.registry = registry;
            this.handler = handler;
        }
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> events = new ArrayList<>();
        private String interactionId;

        @Override
        public void onStart(final Observation.Context context) {
            record("onStart(name=" + context.getName() + ")");
            interactionId = context.getHighCardinalityKeyValue("causeway.interaction.id").getValue();
        }

        @Override
        public void onScopeOpened(final Observation.Context context) {
            record("onScopeOpened(name=" + context.getName() + ")");
        }

        @Override
        public void onError(final Observation.Context context) {
            record("onError(type=" + context.getError().getClass().getSimpleName()
                    + ", message=" + context.getError().getMessage() + ")");
        }

        @Override
        public void onScopeClosed(final Observation.Context context) {
            record("onScopeClosed(name=" + context.getName() + ")");
        }

        @Override
        public void onStop(final Observation.Context context) {
            record("onStop(name=" + context.getName() + ")");
        }

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        private void record(final String event) {
            events.add(event);
        }
    }
}
