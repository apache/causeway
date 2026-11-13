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
package org.apache.causeway.persistence.commons.integration.changetracking;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.InteractionLayerTracker;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyValuePlaceholder;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.causeway.core.transaction.changetracking.EntityPropertyChangePublisher;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class EntityChangeTrackerDefaultObservationTest {
    private static final String NAME = "causeway.entitychange.evaluate";

    @Test void aggregateEvaluationRetainsOnlyChangedCandidatesAndStaticMetadata() {
        var f = new Fixture();
        var first = f.add("first", "after");
        var second = f.add("second", "before");
        when(f.evaluator.differ(any())).thenAnswer(call -> call.getArgument(0,
                org.apache.causeway.core.metamodel.services.objectlifecycle.PreAndPostValue.class).shouldPublish());
        assertEquals(Set.of(first.record), evaluate(f.tracker));
        verify(first.property).get(first.entity, InteractionInitiatedBy.PASS_THROUGH);
        verify(second.property).get(second.entity, InteractionInitiatedBy.PASS_THROUGH);
        assertEquals(1, f.handler.started.size());
        var context = f.handler.stopped.get(0);
        assertEquals(NAME, context.getName());
        assertEquals("evaluate property changes", context.getContextualName());
        assertEquals(Map.of("causeway.bean", "EntityChangeTrackerDefault", "causeway.module", "persistence.commons"),
                context.getLowCardinalityKeyValues().stream().collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue)));
        assertEquals(0, context.getHighCardinalityKeyValues().stream().count());
        assertNull(context.getParentObservation());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void unchangedCandidatesAreObservedButEmptyCandidatesAreNot() {
        var f = new Fixture();
        assertTrue(evaluate(f.tracker).isEmpty());
        assertTrue(f.handler.started.isEmpty());
        f.add("unchanged", "before");
        when(f.evaluator.differ(any())).thenReturn(false);
        assertTrue(evaluate(f.tracker).isEmpty());
        assertEquals(1, f.handler.stopped.size());
    }

    @Test void defensiveRetryStaysWithinOneObservation() {
        var f = new Fixture();
        var first = f.add("first", "after");
        var second = f.add("second", "after");
        var once = new AtomicBoolean(true);
        when(first.property.get(first.entity, InteractionInitiatedBy.PASS_THROUGH)).thenAnswer(call -> {
            if (once.getAndSet(false)) f.add("added", "after");
            assertEquals(NAME, f.registry.getCurrentObservation().getContextView().getName());
            return first.value;
        });
        assertEquals(3, evaluate(f.tracker).size());
        verify(first.property, times(2)).get(first.entity, InteractionInitiatedBy.PASS_THROUGH);
        verify(second.property).get(second.entity, InteractionInitiatedBy.PASS_THROUGH);
        assertEquals(1, f.handler.started.size());
        assertEquals(1, f.handler.stopped.size());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void handledAccessorFailureRemainsUnknownAndDoesNotFailObservation() {
        var f = new Fixture();
        var property = f.add("handled", "after");
        when(property.property.get(property.entity, InteractionInitiatedBy.PASS_THROUGH))
                .thenThrow(new IllegalStateException("handled"));
        assertEquals(Set.of(property.record), evaluate(f.tracker));
        assertSame(PropertyValuePlaceholder.UNKNOWN, property.record.getPreAndPostValue().getPost());
        assertNull(f.handler.stopped.get(0).getError());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void escapingFailureIsIdenticalAndScopeIsRestoredBeforeSubsequentSuccess() {
        var f = new Fixture();
        var property = f.add("failing", "after");
        var failure = new IllegalStateException("deadlock");
        when(property.property.get(property.entity, InteractionInitiatedBy.PASS_THROUGH)).thenThrow(failure).thenReturn(property.value);
        doAnswer(call -> { throw call.getArgument(0, RuntimeException.class); })
                .when(f.deadlocks).rethrowIfDeadlock(any(RuntimeException.class));
        var parent = Observation.start("fixture.parent", f.registry);
        try (var ignored = parent.openScope()) {
            assertSame(failure, assertThrows(IllegalStateException.class, () -> evaluate(f.tracker)));
            assertSame(parent, f.registry.getCurrentObservation());
            assertEquals(1, evaluate(f.tracker).size());
            assertSame(parent, f.registry.getCurrentObservation());
        } finally {
            parent.stop();
        }
        assertSame(failure, f.handler.stopped.get(0).getError());
        assertNull(f.handler.stopped.get(1).getError());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void actionAndBackgroundContextsParentEvaluationAndSynchronousWork() {
        for (String name : List.of("causeway.action.invocation", "fixture.background")) {
            var f = new Fixture();
            var property = f.add("derived", "after");
            when(property.property.get(property.entity, InteractionInitiatedBy.PASS_THROUGH)).thenAnswer(call -> {
                Observation.createNotStarted("fixture.jdbc", f.registry).observe(() -> { });
                return property.value;
            });
            var parent = Observation.start(name, f.registry);
            try (var ignored = parent.openScope()) {
                evaluate(f.tracker);
                assertSame(parent, f.registry.getCurrentObservation());
            } finally {
                parent.stop();
            }
            var evaluation = f.handler.started.get(1);
            assertSame(parent, evaluation.getParentObservation());
            assertSame(evaluation, f.handler.started.get(2).getParentObservation().getContextView());
            assertNull(f.registry.getCurrentObservation());
        }
    }

    @Test void noopRegistryStillEvaluatesAndPropagatesFailures() {
        var f = new Fixture(ObservationRegistry.NOOP);
        var property = f.add("noop", "after");
        assertEquals(Set.of(property.record), evaluate(f.tracker));
        verify(property.property).get(property.entity, InteractionInitiatedBy.PASS_THROUGH);
        var failure = new IllegalStateException("unchanged");
        when(f.evaluator.differ(any())).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> evaluate(f.tracker)));
        assertTrue(f.handler.started.isEmpty());
    }

    @Test void lazyPublicAccessMemoizesBeforePublishingOutsideEvaluation() {
        var f = new Fixture();
        var property = f.add("lazy", "after");
        var parent = Observation.start("fixture.parent", f.registry);
        doAnswer(call -> {
            assertSame(parent, f.registry.getCurrentObservation());
            assertTrue(f.handler.stopped.stream().anyMatch(c -> NAME.equals(c.getName())));
            assertTrue(records(f.tracker).isEmpty());
            return null;
        }).when(f.publisher).publishChangedProperties();
        try (var ignored = parent.openScope()) {
            f.tracker.entitiesDirtied();
            f.tracker.entitiesLoaded();
            f.tracker.beforeCommit(false);
        } finally {
            parent.stop();
        }
        verify(property.property).get(property.entity, InteractionInitiatedBy.PASS_THROUGH);
        verify(f.publisher).publishChangedProperties();
        verify(f.entityPublisher).publishChangingEntities(f.tracker);
        assertEquals(1, f.handler.started.stream().filter(c -> NAME.equals(c.getName())).count());
        assertNull(f.registry.getCurrentObservation());
    }

    private static final class Fixture {
        final Recorder handler = new Recorder();
        final ObservationRegistry registry;
        final PreAndPostValueEvaluatorService evaluator = mock(PreAndPostValueEvaluatorService.class);
        final DeadlockRecognizer deadlocks = mock(DeadlockRecognizer.class);
        final EntityPropertyChangePublisher publisher = mock(EntityPropertyChangePublisher.class);
        final EntityChangesPublisher entityPublisher = mock(EntityChangesPublisher.class);
        final EntityChangeTrackerDefault tracker;
        Fixture() { this(ObservationRegistry.create()); }
        Fixture(final ObservationRegistry registry) {
            this.registry = registry;
            if (registry != ObservationRegistry.NOOP) registry.observationConfig().observationHandler(handler);
            when(evaluator.differ(any())).thenReturn(true);
            tracker = new EntityChangeTrackerDefault(publisher, entityPublisher,
                    () -> mock(InteractionLayerTracker.class), evaluator, new CausewayObservationIntegration(registry));
            set(tracker, "deadlockRecognizer", deadlocks);
            var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
            when(config.applib().service().metricsService().level()).thenReturn(
                    CausewayConfiguration.Applib.Service.MetricsService.Level.COUNTERS_ONLY);
            set(tracker, "causewayConfiguration", config);
        }
        Property add(final String id, final String after) {
            var spec = specification(Object.class);
            when(spec.isEntity()).thenReturn(true);
            var facet = mock(EntityFacet.class);
            when(spec.entityFacet()).thenReturn(Optional.of(facet));
            when(spec.entityFacetElseFail()).thenReturn(facet);
            when(facet.isInjectionPointsResolved(any())).thenReturn(true);
            when(facet.validateBookmark(any())).thenAnswer(call -> call.getArgument(0));
            when(facet.getEntityState(any())).thenReturn(EntityState.ATTACHED);
            var entity = ManagedObject.entity(spec, new Object(),
                    Optional.of(Bookmark.forLogicalTypeNameAndIdentifier("sentinel.Invoice", id)));
            var property = mock(OneToOneAssociation.class);
            when(property.getId()).thenReturn(id);
            var value = ManagedObject.other(specification(String.class), after);
            when(property.get(entity, InteractionInitiatedBy.PASS_THROUGH)).thenReturn(value);
            var record = PropertyChangeRecord.ofCurrent(PropertyChangeRecordId.of(entity, property), "before");
            records(tracker).put(record.getId(), record);
            return new Property(entity, property, value, record);
        }
    }

    private record Property(ManagedObject entity, OneToOneAssociation property, ManagedObject value, PropertyChangeRecord record) { }

    private static ObjectSpecification specification(final Class<?> type) {
        var spec = mock(ObjectSpecification.class, RETURNS_DEEP_STUBS);
        when(spec.getSpecificationLoader().specForType(type)).thenReturn(Optional.of(spec));
        return spec;
    }

    @SuppressWarnings("unchecked")
    private static Map<PropertyChangeRecordId, PropertyChangeRecord> records(final EntityChangeTrackerDefault tracker) {
        try {
            var field = EntityChangeTrackerDefault.class.getDeclaredField("enlistedPropertyChangeRecordsById");
            field.setAccessible(true);
            return (Map<PropertyChangeRecordId, PropertyChangeRecord>) field.get(tracker);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    @SuppressWarnings("unchecked")
    private static Set<PropertyChangeRecord> evaluate(final EntityChangeTrackerDefault tracker) {
        try {
            var method = EntityChangeTrackerDefault.class.getDeclaredMethod("evaluateChangedProperties");
            method.setAccessible(true);
            return (Set<PropertyChangeRecord>) method.invoke(tracker);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException runtime) throw runtime;
            if (ex.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(ex.getCause());
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private static void set(final EntityChangeTrackerDefault tracker, final String name, final Object value) {
        try {
            var field = EntityChangeTrackerDefault.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(tracker, value);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private static final class Recorder implements ObservationHandler<Observation.Context> {
        final List<Observation.Context> started = new ArrayList<>();
        final List<Observation.Context> stopped = new ArrayList<>();
        @Override public boolean supportsContext(final Observation.Context context) { return true; }
        @Override public void onStart(final Observation.Context context) { started.add(context); }
        @Override public void onStop(final Observation.Context context) { stopped.add(context); }
    }
}
