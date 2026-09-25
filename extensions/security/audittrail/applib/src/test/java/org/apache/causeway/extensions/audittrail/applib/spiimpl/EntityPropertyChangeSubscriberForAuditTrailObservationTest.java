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
package org.apache.causeway.extensions.audittrail.applib.spiimpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import org.springframework.core.env.ConfigurableEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntry;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class EntityPropertyChangeSubscriberForAuditTrailObservationTest {

    @Test
    void individualAndBulkCallbacksAreObservedWithoutPerEntryNesting() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final AuditTrailEntryRepository repository = mock(AuditTrailEntryRepository.class);
        final EntityPropertyChange first = change(1);
        final EntityPropertyChange second = change(2);
        final Can<EntityPropertyChange> changes = Can.of(first, second);
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registry);
        final List<String> currentObservationNames = new ArrayList<>();
        when(repository.createFor(first)).thenAnswer(invocation -> {
            currentObservationNames.add(currentName(registry));
            return mock(AuditTrailEntry.class);
        });
        when(repository.createFor(changes)).thenAnswer(invocation -> {
            currentObservationNames.add(currentName(registry));
            return Can.empty();
        });
        final EntityPropertyChangeSubscriberForAuditTrail subscriber =
                subscriber(repository, enabledConfiguration(), integration);

        subscriber.onChanging(first);
        subscriber.onChanging(changes);

        assertEquals(List.of(
                EntityPropertyChangeSubscriberForAuditTrail.AUDIT_TRAIL_WRITE_OBSERVATION_NAME,
                EntityPropertyChangeSubscriberForAuditTrail.AUDIT_TRAIL_WRITE_OBSERVATION_NAME),
                currentObservationNames);
        assertEquals(2, handler.countStarts(
                EntityPropertyChangeSubscriberForAuditTrail.AUDIT_TRAIL_WRITE_OBSERVATION_NAME));
        verify(repository).createFor(first);
        verify(repository).createFor(changes);
        verify(repository, never()).createFor(second);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void auditAndSynchronousDatabaseWorkInheritCurrentParentage() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registry);
        final AuditTrailEntryRepository repository = mock(AuditTrailEntryRepository.class);
        final EntityPropertyChange change = change(1);
        when(repository.createFor(change)).thenAnswer(invocation -> {
            integration.createNotStarted(getClass(), "SELECT audit-entry")
                    .contextualName("SELECT audit-entry")
                    .observe(() -> { });
            return mock(AuditTrailEntry.class);
        });
        final EntityPropertyChangeSubscriberForAuditTrail subscriber =
                subscriber(repository, enabledConfiguration(), integration);
        final Observation action = integration.createNotStarted(
                getClass(), "causeway.action.invocation")
                .contextualName("act demo.InvoiceSummary#invoiceAll")
                .start();
        try (Observation.Scope ignored = action.openScope()) {
            subscriber.onChanging(change);
        } finally {
            action.stop();
        }

        assertEquals(List.of(
                "causeway.action.invocation<-null",
                "causeway.audittrail.write<-causeway.action.invocation",
                "SELECT audit-entry<-causeway.audittrail.write"),
                handler.parents);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void failureIsRecordedAndRethrownWithoutLeakingScope() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final AuditTrailEntryRepository repository = mock(AuditTrailEntryRepository.class);
        final EntityPropertyChange change = change(1);
        final IllegalStateException expected = new IllegalStateException("audit failed");
        when(repository.createFor(change)).thenThrow(expected);
        final EntityPropertyChangeSubscriberForAuditTrail subscriber = subscriber(
                repository,
                enabledConfiguration(),
                new CausewayObservationIntegration(registry));

        final IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> subscriber.onChanging(change));

        assertSame(expected, actual);
        assertEquals(List.of("causeway.audittrail.write:audit failed"), handler.errors);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void disabledAuditTrailCreatesNeitherEntryNorObservation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final AuditTrailEntryRepository repository = mock(AuditTrailEntryRepository.class);
        final EntityPropertyChange change = change(1);
        final EntityPropertyChangeSubscriberForAuditTrail subscriber = subscriber(
                repository,
                disabledConfiguration(),
                new CausewayObservationIntegration(registry));

        subscriber.onChanging(change);

        verify(repository, never()).createFor(any(EntityPropertyChange.class));
        assertEquals(0, handler.countStarts(
                EntityPropertyChangeSubscriberForAuditTrail.AUDIT_TRAIL_WRITE_OBSERVATION_NAME));
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void noopObservationKeepsAuditPersistenceEnabled() {
        final AuditTrailEntryRepository repository = mock(AuditTrailEntryRepository.class);
        final EntityPropertyChange change = change(1);
        final EntityPropertyChangeSubscriberForAuditTrail subscriber = subscriber(
                repository,
                enabledConfiguration(),
                new CausewayObservationIntegration(ObservationRegistry.NOOP));

        subscriber.onChanging(change);

        verify(repository).createFor(change);
        assertNull(ObservationRegistry.NOOP.getCurrentObservation());
    }

    @Test
    void observationUsesOnlyStaticFrameworkMetadata() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final AuditTrailEntryRepository repository = mock(AuditTrailEntryRepository.class);
        final EntityPropertyChange change = change(1);
        final EntityPropertyChangeSubscriberForAuditTrail subscriber = subscriber(
                repository,
                enabledConfiguration(),
                new CausewayObservationIntegration(registry));

        subscriber.onChanging(change);

        assertEquals(List.of("write audit trail"), handler.auditContextualNames);
        assertEquals(Map.of(
                "causeway.bean", "EntityPropertyChangeSubscriberForAuditTrail",
                "causeway.module", "ext.auditTrail"),
                handler.auditTags);
    }

    private static EntityPropertyChange change(final int sequence) {
        return EntityPropertyChange.of(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                sequence,
                Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", "123"),
                "demo.Invoice#status",
                "status",
                "NEW",
                "INVOICED",
                "demo-user",
                new Timestamp(0));
    }

    private static EntityPropertyChangeSubscriberForAuditTrail subscriber(
            final AuditTrailEntryRepository repository,
            final CausewayConfiguration configuration,
            final CausewayObservationIntegration integration) {
        return new EntityPropertyChangeSubscriberForAuditTrail(
                mock(TransactionService.class),
                repository,
                configuration,
                integration);
    }

    private static CausewayConfiguration enabledConfiguration() {
        return configuration(true);
    }

    private static CausewayConfiguration disabledConfiguration() {
        return configuration(false);
    }

    private static CausewayConfiguration configuration(final boolean enabled) {
        final CausewayConfiguration configuration = new CausewayConfiguration(
                mock(ConfigurableEnvironment.class), Optional.empty());
        configuration.getExtensions().getAuditTrail().setPersist(enabled
                ? CausewayConfiguration.Extensions.AuditTrail.PersistPolicy.ENABLED
                : CausewayConfiguration.Extensions.AuditTrail.PersistPolicy.DISABLED);
        return configuration;
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static String currentName(final ObservationRegistry registry) {
        return registry.getCurrentObservation() != null
                ? registry.getCurrentObservation().getContextView().getName()
                : null;
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> starts = new ArrayList<>();
        private final List<String> parents = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> auditContextualNames = new ArrayList<>();
        private final Map<String, String> auditTags = new LinkedHashMap<>();

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        @Override
        public void onStart(final Observation.Context context) {
            starts.add(context.getName());
            parents.add(context.getName() + "<-" + (context.getParentObservation() != null
                    ? context.getParentObservation().getContextView().getName()
                    : "null"));
            if(EntityPropertyChangeSubscriberForAuditTrail.AUDIT_TRAIL_WRITE_OBSERVATION_NAME
                    .equals(context.getName())) {
                auditContextualNames.add(context.getContextualName());
                for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
                    auditTags.put(keyValue.getKey(), keyValue.getValue());
                }
            }
        }

        @Override
        public void onError(final Observation.Context context) {
            errors.add(context.getName() + ":" + context.getError().getMessage());
        }

        private long countStarts(final String name) {
            return starts.stream().filter(name::equals).count();
        }
    }
}
