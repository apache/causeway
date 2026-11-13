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

import java.sql.DriverManager;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.execution.InteractionLayerTracker;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecord;
import org.apache.causeway.core.metamodel.services.objectlifecycle.PropertyChangeRecordId;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.transaction.changetracking.EntityChangesPublisher;
import org.apache.causeway.core.transaction.changetracking.EntityPropertyChangePublisher;

/** Public lifecycle of the actual tracker with real records and controlled accessors/publishers. */
public final class EntityChangeEvaluationTracingFixture {
    public static void evaluate(final CausewayObservationIntegration integration, final boolean agent,
            final boolean empty, final boolean fail, final Runnable publish) {
        var evaluator = mock(PreAndPostValueEvaluatorService.class);
        when(evaluator.differ(any())).thenReturn(true);
        var publisher = mock(EntityPropertyChangePublisher.class);
        var previous = integration.observationRegistry().getCurrentObservation();
        doAnswer(call -> {
            if (previous != integration.observationRegistry().getCurrentObservation())
                throw new AssertionError("evaluation scope leaked into publisher");
            publish.run();
            return null;
        }).when(publisher).publishChangedProperties();
        var tracker = new EntityChangeTrackerDefault(publisher, mock(EntityChangesPublisher.class),
                () -> mock(InteractionLayerTracker.class), evaluator, integration);
        var deadlocks = mock(DeadlockRecognizer.class);
        doAnswer(call -> { throw call.getArgument(0, RuntimeException.class); })
                .when(deadlocks).rethrowIfDeadlock(any(RuntimeException.class));
        set(tracker, "deadlockRecognizer", deadlocks);
        var config = mock(CausewayConfiguration.class, RETURNS_DEEP_STUBS);
        when(config.applib().service().metricsService().level()).thenReturn(
                CausewayConfiguration.Applib.Service.MetricsService.Level.COUNTERS_ONLY);
        set(tracker, "causewayConfiguration", config);
        var failure = new IllegalStateException("evaluation fixture failure");
        if (!empty) {
            for (String id : new String[] { "sentinel-first", "sentinel-second" }) {
                add(tracker, id, () -> {
                    if (agent) executeSql();
                    else integration.createNotStarted(EntityChangeEvaluationTracingFixture.class, "fixture.evaluation.jdbc")
                            .observe(EntityChangeEvaluationTracingFixture::executeSql);
                    if (fail) throw failure;
                });
            }
        }
        try {
            tracker.beforeCommit(false);
            tracker.entitiesDirtied(); // Already memoized: must not evaluate again.
            if (!empty && fail) throw new AssertionError("missing evaluation failure");
        } catch (IllegalStateException ex) {
            if (!empty && fail && ex != failure)
                throw new AssertionError("evaluation failure was replaced", ex);
            throw ex;
        } finally {
            if (previous != integration.observationRegistry().getCurrentObservation())
                throw new AssertionError("evaluation scope not restored");
        }
    }

    @SuppressWarnings("unchecked")
    private static void add(final EntityChangeTrackerDefault tracker, final String id, final Runnable accessor) {
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
        var value = ManagedObject.other(specification(String.class), "sentinel-after");
        when(property.get(entity, InteractionInitiatedBy.PASS_THROUGH)).thenAnswer(call -> {
            accessor.run();
            return value;
        });
        var record = PropertyChangeRecord.ofCurrent(PropertyChangeRecordId.of(entity, property), "sentinel-before");
        try {
            var field = EntityChangeTrackerDefault.class.getDeclaredField("enlistedPropertyChangeRecordsById");
            field.setAccessible(true);
            var records = (Map<PropertyChangeRecordId, PropertyChangeRecord>) field.get(tracker);
            records.put(record.getId(), record);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private static ObjectSpecification specification(final Class<?> type) {
        var spec = mock(ObjectSpecification.class, RETURNS_DEEP_STUBS);
        when(spec.getSpecificationLoader().specForType(type)).thenReturn(Optional.of(spec));
        return spec;
    }

    private static void set(final EntityChangeTrackerDefault tracker, final String name, final Object value) {
        try {
            var field = EntityChangeTrackerDefault.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(tracker, value);
        } catch (ReflectiveOperationException ex) { throw new IllegalStateException(ex); }
    }

    private static void executeSql() {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:evaluation;DB_CLOSE_DELAY=-1");
                var statement = connection.createStatement();
                var result = statement.executeQuery("SELECT 1")) {
            if (!result.next()) throw new AssertionError("missing evaluation JDBC result");
        } catch (java.sql.SQLException ex) { throw new IllegalStateException(ex); }
    }
}
