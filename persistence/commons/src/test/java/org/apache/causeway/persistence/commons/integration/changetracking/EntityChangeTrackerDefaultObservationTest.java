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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.causeway.core.transaction.changetracking.EntityPropertyChangePublisher;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class EntityChangeTrackerDefaultObservationTest {

    @Test
    void evaluationIsObservedOnceForMultipleCandidateRecords() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        final EntityChangeTrackerDefault tracker = tracker(registry, evaluator);
        final PropertyFixture first = property("first", "before", "after");
        final PropertyFixture second = property("second", "before", "after");
        records(tracker).put(first.record.getId(), first.record);
        records(tracker).put(second.record.getId(), second.record);

        final Set<PropertyChangeRecord> changed = evaluateChangedProperties(tracker);

        assertEquals(2, changed.size());
        assertEquals(1, handler.countStarts(
                EntityChangeTrackerDefault.ENTITY_CHANGE_EVALUATION_OBSERVATION_NAME));
        verify(first.association).get(
                first.entity, InteractionInitiatedBy.PASS_THROUGH);
        verify(second.association).get(
                second.entity, InteractionInitiatedBy.PASS_THROUGH);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void emptyCandidateCollectionCreatesNoObservation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final EntityChangeTrackerDefault tracker = tracker(
                registry, mock(PreAndPostValueEvaluatorService.class));

        final Set<PropertyChangeRecord> changed = evaluateChangedProperties(tracker);

        assertEquals(Set.of(), changed);
        assertEquals(0, handler.countStarts(
                EntityChangeTrackerDefault.ENTITY_CHANGE_EVALUATION_OBSERVATION_NAME));
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void candidateWithoutPublishableDifferenceIsStillObserved() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(false);
        final EntityChangeTrackerDefault tracker = tracker(registry, evaluator);
        final PropertyFixture property = property("unchanged", "same", "same");
        records(tracker).put(property.record.getId(), property.record);

        final Set<PropertyChangeRecord> changed = evaluateChangedProperties(tracker);

        assertTrueEmpty(changed);
        assertEquals(1, handler.countStarts(
                EntityChangeTrackerDefault.ENTITY_CHANGE_EVALUATION_OBSERVATION_NAME));
        verify(property.association).get(
                property.entity, InteractionInitiatedBy.PASS_THROUGH);
    }

    @Test
    void concurrentModificationRetryRemainsWithinOneObservation() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        final EntityChangeTrackerDefault tracker = tracker(registry, evaluator);
        final PropertyFixture first = property("first", "before", "after");
        final PropertyFixture second = property("second", "before", "after");
        final PropertyFixture added = property("added", "before", "after");
        final Map<PropertyChangeRecordId, PropertyChangeRecord> records = records(tracker);
        records.put(first.record.getId(), first.record);
        records.put(second.record.getId(), second.record);
        final AtomicBoolean addDuringFirstAttempt = new AtomicBoolean(true);
        when(first.association.get(first.entity, InteractionInitiatedBy.PASS_THROUGH))
                .thenAnswer(invocation -> {
                    if(addDuringFirstAttempt.getAndSet(false)) {
                        records.put(added.record.getId(), added.record);
                    }
                    return first.value;
                });

        final Set<PropertyChangeRecord> changed = evaluateChangedProperties(tracker);

        assertEquals(3, changed.size());
        assertEquals(1, handler.countStarts(
                EntityChangeTrackerDefault.ENTITY_CHANGE_EVALUATION_OBSERVATION_NAME));
        verify(first.association, times(2)).get(
                first.entity, InteractionInitiatedBy.PASS_THROUGH);
        verify(second.association).get(
                second.entity, InteractionInitiatedBy.PASS_THROUGH);
        verify(added.association).get(
                added.entity, InteractionInitiatedBy.PASS_THROUGH);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void evaluationAndSynchronousWorkInheritCurrentParentage() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final CausewayObservationIntegration integration =
                new CausewayObservationIntegration(registry);
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        final EntityChangeTrackerDefault tracker = tracker(integration, evaluator);
        final PropertyFixture property = property("derived", "before", "after");
        when(property.association.get(property.entity, InteractionInitiatedBy.PASS_THROUGH))
                .thenAnswer(invocation -> {
                    integration.createNotStarted(getClass(), "SELECT derived-value")
                            .contextualName("SELECT derived-value")
                            .observe(() -> { });
                    return property.value;
                });
        records(tracker).put(property.record.getId(), property.record);
        final Observation action = integration.createNotStarted(
                getClass(), "causeway.action.invocation")
                .contextualName("act demo.InvoiceSummary#invoiceAll")
                .start();

        try (Observation.Scope ignored = action.openScope()) {
            evaluateChangedProperties(tracker);
        } finally {
            action.stop();
        }

        assertEquals(List.of(
                "causeway.action.invocation<-null",
                "causeway.entitychange.evaluate<-causeway.action.invocation",
                "SELECT derived-value<-causeway.entitychange.evaluate"),
                handler.parents);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void handledAccessorFailureKeepsExistingUnknownValueBehavior() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        final EntityChangeTrackerDefault tracker = tracker(registry, evaluator);
        final PropertyFixture property = property("handled", "before", "after");
        when(property.association.get(property.entity, InteractionInitiatedBy.PASS_THROUGH))
                .thenThrow(new IllegalStateException("handled accessor failure"));
        records(tracker).put(property.record.getId(), property.record);

        final Set<PropertyChangeRecord> changed = evaluateChangedProperties(tracker);

        assertEquals(1, changed.size());
        assertEquals(List.of(), handler.errors);
        assertEquals(1, handler.countStarts(
                EntityChangeTrackerDefault.ENTITY_CHANGE_EVALUATION_OBSERVATION_NAME));
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void escapingFailureIsRecordedAndRethrownWithoutLeakingScope() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final EntityChangeTrackerDefault tracker = tracker(
                registry, mock(PreAndPostValueEvaluatorService.class));
        final PropertyFixture property = property("failing", "before", "after");
        final IllegalStateException expected = new IllegalStateException("deadlock");
        when(property.association.get(property.entity, InteractionInitiatedBy.PASS_THROUGH))
                .thenThrow(expected);
        doAnswer(invocation -> {
            throw invocation.getArgument(0, RuntimeException.class);
        }).when(deadlockRecognizer(tracker)).rethrowIfDeadlock(any(RuntimeException.class));
        records(tracker).put(property.record.getId(), property.record);

        final IllegalStateException actual = assertThrows(
                IllegalStateException.class,
                () -> evaluateChangedProperties(tracker));

        assertSame(expected, actual);
        assertEquals(List.of("causeway.entitychange.evaluate:deadlock"), handler.errors);
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void noopObservationEvaluatesCandidatesExactlyOnce() {
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        final EntityChangeTrackerDefault tracker = tracker(
                ObservationRegistry.NOOP, evaluator);
        final PropertyFixture property = property("noop", "before", "after");
        records(tracker).put(property.record.getId(), property.record);

        final Set<PropertyChangeRecord> changed = evaluateChangedProperties(tracker);

        assertEquals(1, changed.size());
        verify(property.association).get(
                property.entity, InteractionInitiatedBy.PASS_THROUGH);
        assertNull(ObservationRegistry.NOOP.getCurrentObservation());
    }

    @Test
    void observationUsesOnlyStaticFrameworkMetadata() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final PreAndPostValueEvaluatorService evaluator = mock(
                PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        final EntityChangeTrackerDefault tracker = tracker(registry, evaluator);
        final PropertyFixture property = property("privateProperty", "secret-before", "secret-after");
        records(tracker).put(property.record.getId(), property.record);

        evaluateChangedProperties(tracker);

        assertEquals(List.of("evaluate property changes"), handler.contextualNames);
        assertEquals(Map.of(
                "causeway.bean", "EntityChangeTrackerDefault",
                "causeway.module", "persistence.commons"),
                handler.evaluationTags);
        assertFalse(handler.evaluationTags.containsValue("privateProperty"));
        assertFalse(handler.evaluationTags.containsValue("secret-before"));
        assertFalse(handler.evaluationTags.containsValue("secret-after"));
    }

    private static EntityChangeTrackerDefault tracker(
            final ObservationRegistry registry,
            final PreAndPostValueEvaluatorService evaluator) {
        return tracker(new CausewayObservationIntegration(registry), evaluator);
    }

    private static EntityChangeTrackerDefault tracker(
            final CausewayObservationIntegration integration,
            final PreAndPostValueEvaluatorService evaluator) {
        final Provider<InteractionProvider> interactionProvider =
                () -> mock(InteractionProvider.class);
        final EntityChangeTrackerDefault tracker = new EntityChangeTrackerDefault(
                mock(EntityPropertyChangePublisher.class),
                mock(EntityChangesPublisher.class),
                interactionProvider,
                evaluator,
                integration);
        setField(tracker, "deadlockRecognizer", mock(DeadlockRecognizer.class));
        return tracker;
    }

    private static DeadlockRecognizer deadlockRecognizer(
            final EntityChangeTrackerDefault tracker) {
        return (DeadlockRecognizer) getField(tracker, "deadlockRecognizer");
    }

    private static PropertyFixture property(
            final String propertyId,
            final Object preValue,
            final Object postValue) {
        final ManagedObject entity = mock(ManagedObject.class);
        when(entity.getBookmark()).thenReturn(java.util.Optional.of(
                Bookmark.forLogicalTypeNameAndIdentifier("demo.Invoice", propertyId)));
        when(entity.getEntityState()).thenReturn(EntityState.ATTACHED);
        final OneToOneAssociation association = mock(OneToOneAssociation.class);
        when(association.getId()).thenReturn(propertyId);
        final ManagedObject value = mock(ManagedObject.class);
        when(value.getPojo()).thenReturn(postValue);
        final PropertyChangeRecordId id = PropertyChangeRecordId.of(entity, association);
        final PropertyChangeRecord record = PropertyChangeRecord.ofCurrent(id, preValue);
        final PropertyFixture fixture = new PropertyFixture(entity, association, value, record);
        when(association.get(entity, InteractionInitiatedBy.PASS_THROUGH))
                .thenReturn(value);
        return fixture;
    }

    @SuppressWarnings("unchecked")
    private static Map<PropertyChangeRecordId, PropertyChangeRecord> records(
            final EntityChangeTrackerDefault tracker) {
        return (Map<PropertyChangeRecordId, PropertyChangeRecord>)
                getField(tracker, "enlistedPropertyChangeRecordsById");
    }

    @SuppressWarnings("unchecked")
    private static Set<PropertyChangeRecord> evaluateChangedProperties(
            final EntityChangeTrackerDefault tracker) {
        try {
            final Method method = EntityChangeTrackerDefault.class
                    .getDeclaredMethod("evaluateChangedProperties");
            method.setAccessible(true);
            return (Set<PropertyChangeRecord>) method.invoke(tracker);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if(cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if(cause instanceof Error) {
                throw (Error) cause;
            }
            throw new IllegalStateException(cause);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static Object getField(
            final EntityChangeTrackerDefault tracker,
            final String fieldName) {
        try {
            final Field field = EntityChangeTrackerDefault.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(tracker);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void setField(
            final EntityChangeTrackerDefault tracker,
            final String fieldName,
            final Object value) {
        try {
            final Field field = EntityChangeTrackerDefault.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(tracker, value);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static void assertTrueEmpty(final Set<PropertyChangeRecord> changed) {
        assertEquals(0, changed.size());
    }

    private static final class PropertyFixture {
        private final ManagedObject entity;
        private final OneToOneAssociation association;
        private final ManagedObject value;
        private final PropertyChangeRecord record;

        private PropertyFixture(
                final ManagedObject entity,
                final OneToOneAssociation association,
                final ManagedObject value,
                final PropertyChangeRecord record) {
            this.entity = entity;
            this.association = association;
            this.value = value;
            this.record = record;
        }
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> starts = new ArrayList<>();
        private final List<String> parents = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private final List<String> contextualNames = new ArrayList<>();
        private final Map<String, String> evaluationTags = new LinkedHashMap<>();

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
            if(EntityChangeTrackerDefault.ENTITY_CHANGE_EVALUATION_OBSERVATION_NAME
                    .equals(context.getName())) {
                contextualNames.add(context.getContextualName());
                for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
                    evaluationTags.put(keyValue.getKey(), keyValue.getValue());
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
