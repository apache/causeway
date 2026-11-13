/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.extensions.audittrail.applib.spiimpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.CausewayConfiguration.Extensions.AuditTrail.PersistPolicy;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.extensions.audittrail.applib.dom.AuditTrailEntryRepository;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class EntityPropertyChangeSubscriberForAuditTrailObservationTest {

    private static final String NAME = "causeway.audittrail.write";

    @Test void bothCallbacksDelegateOnceWithinOnePrivateObservation() {
        var handler = new Recorder();
        var registry = registry(handler);
        var repository = mock(AuditTrailEntryRepository.class);
        var first = mock(EntityPropertyChange.class);
        var second = mock(EntityPropertyChange.class);
        var changes = Can.of(first, second);
        when(repository.createFor(first)).thenAnswer(call -> {
            assertEquals(NAME, registry.getCurrentObservation().getContextView().getName());
            return null;
        });
        when(repository.createFor(changes)).thenAnswer(call -> {
            assertEquals(NAME, registry.getCurrentObservation().getContextView().getName());
            return Can.empty();
        });
        var subscriber = subscriber(repository, registry, true);
        subscriber.onChanging(first);
        subscriber.onChanging(changes);
        verify(repository).createFor(first);
        verify(repository).createFor(changes);
        verifyNoMoreInteractions(repository);
        assertEquals(2, handler.started.size());
        assertEquals(handler.started, handler.stopped);
        for (var context : handler.stopped) {
            assertEquals(NAME, context.getName());
            assertEquals("write audit trail", context.getContextualName());
            assertEquals(Map.of("causeway.bean", "EntityPropertyChangeSubscriberForAuditTrail",
                    "causeway.module", "ext.auditTrail"),
                    context.getLowCardinalityKeyValues().stream()
                            .collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue)));
            assertEquals(0, context.getHighCardinalityKeyValues().stream().count());
        }
        assertNull(registry.getCurrentObservation());
    }

    @Test void restoresActionOrBackgroundParentAndParentsSynchronousWork() {
        for (String parentName : List.of("causeway.action.invocation", "fixture.background")) {
            var handler = new Recorder();
            var registry = registry(handler);
            var repository = mock(AuditTrailEntryRepository.class);
            var change = mock(EntityPropertyChange.class);
            when(repository.createFor(change)).thenAnswer(call -> {
                Observation.createNotStarted("fixture.jdbc", registry).observe(() -> { });
                return null;
            });
            var parent = Observation.start(parentName, registry);
            try (var ignored = parent.openScope()) {
                subscriber(repository, registry, true).onChanging(change);
                assertSame(parent, registry.getCurrentObservation());
            } finally {
                parent.stop();
            }
            var audit = handler.started.get(1);
            var jdbc = handler.started.get(2);
            assertSame(parent, audit.getParentObservation());
            assertSame(audit, jdbc.getParentObservation().getContextView());
            assertNull(registry.getCurrentObservation());
        }
    }

    @Test void originalFailuresCloseBothCallbackScopesAndAllowSubsequentSuccess() {
        for (boolean bulk : List.of(false, true)) {
            var handler = new Recorder();
            var registry = registry(handler);
            var repository = mock(AuditTrailEntryRepository.class);
            var change = mock(EntityPropertyChange.class);
            var changes = Can.of(change);
            var failure = new IllegalStateException("audit failed");
            if (bulk) {
                when(repository.createFor(changes)).thenThrow(failure).thenReturn(Can.empty());
            } else {
                when(repository.createFor(change)).thenThrow(failure).thenReturn(null);
            }
            var subscriber = subscriber(repository, registry, true);
            Runnable callback = () -> {
                if (bulk) subscriber.onChanging(changes);
                else subscriber.onChanging(change);
            };
            var parent = Observation.start("fixture.parent", registry);
            try (var ignored = parent.openScope()) {
                assertSame(failure, assertThrows(IllegalStateException.class, callback::run));
                assertSame(parent, registry.getCurrentObservation());
                callback.run();
                assertSame(parent, registry.getCurrentObservation());
            } finally {
                parent.stop();
            }
            assertEquals(3, handler.stopped.size());
            assertSame(failure, handler.stopped.get(0).getError());
            assertNull(handler.stopped.get(1).getError());
            assertNull(registry.getCurrentObservation());
        }
    }

    @Test void disabledAuditDoesNotCallRepositoryOrCreateObservations() {
        var handler = new Recorder();
        var registry = registry(handler);
        var repository = mock(AuditTrailEntryRepository.class);
        var subscriber = subscriber(repository, registry, false);
        subscriber.onChanging(mock(EntityPropertyChange.class));
        subscriber.onChanging(Can.of(mock(EntityPropertyChange.class)));
        verifyNoInteractions(repository);
        assertTrue(handler.started.isEmpty());
    }

    @Test void noopStillDelegatesBothOverloadsAndPropagatesFailures() {
        var repository = mock(AuditTrailEntryRepository.class);
        var subscriber = subscriber(repository, ObservationRegistry.NOOP, true);
        var change = mock(EntityPropertyChange.class);
        var changes = Can.of(change);
        subscriber.onChanging(change);
        subscriber.onChanging(changes);
        verify(repository).createFor(change);
        verify(repository).createFor(changes);
        var failure = new IllegalStateException("unchanged");
        when(repository.createFor(change)).thenThrow(failure);
        when(repository.createFor(changes)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> subscriber.onChanging(change)));
        assertSame(failure, assertThrows(IllegalStateException.class, () -> subscriber.onChanging(changes)));
    }

    private static EntityPropertyChangeSubscriberForAuditTrail subscriber(
            final AuditTrailEntryRepository repository, final ObservationRegistry registry, final boolean enabled) {
        var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(config.extensions().auditTrail().persist()).thenReturn(enabled ? PersistPolicy.ENABLED : PersistPolicy.DISABLED);
        return new EntityPropertyChangeSubscriberForAuditTrail(mock(TransactionService.class), repository,
                config, new CausewayObservationIntegration(registry));
    }

    private static ObservationRegistry registry(final Recorder handler) {
        var registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static final class Recorder implements ObservationHandler<Observation.Context> {
        final List<Observation.Context> started = new ArrayList<>();
        final List<Observation.Context> stopped = new ArrayList<>();
        @Override public boolean supportsContext(final Observation.Context context) { return true; }
        @Override public void onStart(final Observation.Context context) { started.add(context); }
        @Override public void onStop(final Observation.Context context) { stopped.add(context); }
    }
}
