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
package org.apache.causeway.viewer.wicket.ui.observation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket.Observation.Detail;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import static org.junit.jupiter.api.Assertions.*;

class WicketObservationCoordinatorTest {
    static final String TYPE = "demo.Owner", COLLECTION = TYPE + "#items";
    static WicketRenderObservationDescriptor page() { return WicketRenderObservationDescriptor.page(TYPE); }
    static WicketRenderObservationDescriptor property() { return WicketRenderObservationDescriptor.property(TYPE, TYPE + "#name"); }
    static WicketRenderObservationDescriptor collection() { return WicketRenderObservationDescriptor.collection(TYPE, COLLECTION); }
    static final class Recording implements ObservationHandler<Observation.Context> {
        final List<Observation.Context> started = new ArrayList<>(), stopped = new ArrayList<>();
        public boolean supportsContext(Observation.Context context) { return true; }
        public void onStart(Observation.Context context) { started.add(context); }
        public void onStop(Observation.Context context) { stopped.add(context); }
    }
    static CausewayObservationIntegration integration(Recording recording) {
        var registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(recording);
        return new CausewayObservationIntegration(registry);
    }
    static WicketObservationCoordinator.State state(Detail detail, int maximum) {
        return new WicketObservationCoordinator.State(new WicketObservationCoordinator.Settings(detail, maximum), System::nanoTime);
    }
    static long tag(Observation.Context context, String key) {
        return Long.parseLong(context.getHighCardinalityKeyValue(key).getValue());
    }
    @Test void detailHierarchy() {
        for(var detail : Detail.values()) {
            var recording = new Recording(); var integration = integration(recording); var state = state(detail, 0);
            var descriptors = List.of(page(), collection(), WicketRenderObservationDescriptor.row(TYPE, COLLECTION), property());
            for(var descriptor : descriptors) {
                var admitted = state.begin(integration, descriptor);
                assertEquals(detail.ordinal() >= descriptor.minimumDetail().ordinal(), admitted.isEmitted());
                admitted.finish();
            }
            assertNull(integration.observationRegistry().getCurrentObservation());
        }
    }
    @Test void closedSpansDoNotRefundAndReserveProtectsStructures() {
        var recording = new Recording(); var integration = integration(recording); var state = state(Detail.MEMBERS, 10);
        for(int i=0; i<9; i++) { var a = state.begin(integration, property()); assertTrue(a.isEmitted()); a.finish(); }
        var omitted = state.begin(integration, property()); assertFalse(omitted.isEmitted()); omitted.finish();
        var structural = state.begin(integration, collection()); assertTrue(structural.isEmitted()); structural.finish();
        var exhausted = state.begin(integration, page()); assertFalse(exhausted.isEmitted()); exhausted.finish();
        assertEquals(10, recording.started.size());
    }
    @Test void smallestBudgetAndUnlimited() {
        for(int maximum : new int[] {1, 0}) {
            var recording = new Recording(); var integration = integration(recording); var state = state(Detail.MEMBERS, maximum);
            var child = state.begin(integration, property()); assertEquals(maximum==0, child.isEmitted()); child.finish();
            var parent = state.begin(integration, page()); assertTrue(parent.isEmitted()); parent.finish();
        }
    }
    @Test void largestBudgetReserveArithmeticDoesNotOverflow() {
        var recording = new Recording(); var integration = integration(recording);
        var a = state(Detail.MEMBERS, Integer.MAX_VALUE).begin(integration, property());
        assertTrue(a.isEmitted()); a.finish();
        assertThrows(IllegalArgumentException.class, () -> state(Detail.PAGE, -1));
        assertThrows(NullPointerException.class, () -> state(null, 0));
    }
    @Test void omittedRowsStillContributeSummariesAndPreserveCurrentParent() {
        var recording = new Recording(); var integration = integration(recording); var clock = new AtomicLong(10);
        var state = new WicketObservationCoordinator.State(new WicketObservationCoordinator.Settings(Detail.REGIONS, 0), clock::get);
        var coll = state.begin(integration, collection());
        var parent = integration.observationRegistry().getCurrentObservation();
        var row = state.begin(integration, WicketRenderObservationDescriptor.row(TYPE, COLLECTION));
        assertFalse(row.isEmitted()); assertSame(parent, integration.observationRegistry().getCurrentObservation());
        var cell = state.begin(integration, property()); cell.finish(); clock.set(35); row.finish(); coll.finish();
        var summary = recording.stopped.get(0);
        assertEquals(1, tag(summary, WicketObservationCoordinator.ROW_COUNT_TAG));
        assertEquals(25, tag(summary, WicketObservationCoordinator.ROW_TOTAL_NANOS_TAG));
        assertEquals(25, tag(summary, WicketObservationCoordinator.ROW_MAX_NANOS_TAG));
        assertEquals(1, tag(summary, WicketObservationCoordinator.CELL_COUNT_TAG));
        assertEquals(2, tag(summary, WicketObservationCoordinator.SUPPRESSED_CHILD_TAG));
        assertEquals(2, tag(summary, WicketObservationCoordinator.SUPPRESSED_DETAIL_TAG));
        assertNull(summary.getHighCardinalityKeyValue(WicketObservationCoordinator.SUPPRESSED_BUDGET_TAG));
    }
    @Test void omittedCarriersDoNotCreateSyntheticSpans() {
        var recording = new Recording(); var integration = integration(recording); var state = state(Detail.NONE, 1);
        var coll = state.begin(integration, collection());
        var row = state.begin(integration, WicketRenderObservationDescriptor.row(TYPE, COLLECTION));
        row.finish(); coll.finish(); state.cleanup(null);
        assertTrue(recording.started.isEmpty()); assertNull(integration.observationRegistry().getCurrentObservation());
    }
    @Test void cleanupAttemptsAllClosuresAndPreservesOriginalFailure() {
        var recording = new Recording(); var integration = integration(recording); var state = state(Detail.MEMBERS, 0);
        final var stopAttempts = new ArrayList<String>();
        integration.observationRegistry().observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
            public void onStop(Observation.Context context) {
                stopAttempts.add(context.getName());
                if(context.getName().equals("causeway.wicket.property.render")) throw new IllegalStateException("stop failed");
            }
        });
        state.begin(integration, collection()); state.begin(integration, property());
        var original = new IllegalArgumentException("work failed");
        state.cleanup(original); state.cleanup(original);
        assertEquals(2, stopAttempts.size());
        assertNull(integration.observationRegistry().getCurrentObservation());
        assertEquals(1, original.getSuppressed().length);
    }
    @Test void multipleCollectionsShareBudgetAndEachKeepsItsOwnSummary() {
        var recording = new Recording(); var integration = integration(recording); var state = state(Detail.MEMBERS, 3);
        for(int i=0; i<2; i++) {
            var coll = state.begin(integration, collection());
            var row = state.begin(integration, WicketRenderObservationDescriptor.row(TYPE, COLLECTION));
            row.finish(); coll.finish();
        }
        assertEquals(3, recording.started.size());
        var collections = recording.stopped.stream().filter(c -> c.getName().equals("causeway.wicket.collection.render")).toList();
        assertEquals(2, collections.size());
        for(var c : collections) assertEquals(1, tag(c, WicketObservationCoordinator.ROW_COUNT_TAG));
        assertEquals(1, tag(collections.get(1), WicketObservationCoordinator.SUPPRESSED_BUDGET_TAG));
    }
    @Test void failedCallbacksCountOnceAndCleanupIsIdempotent() {
        var recording = new Recording(); var integration = integration(recording); var state = state(Detail.MEMBERS, 0);
        state.begin(integration, collection());
        var row = state.begin(integration, WicketRenderObservationDescriptor.row(TYPE, COLLECTION));
        var failure = new IllegalStateException("row failed"); row.onError(failure); state.cleanup(failure); row.finish(); state.cleanup(failure);
        assertEquals(2, recording.stopped.size());
        assertEquals(1, tag(recording.stopped.get(recording.stopped.size() - 1), WicketObservationCoordinator.ROW_COUNT_TAG));
        assertSame(failure, recording.stopped.get(0).getError());
        assertNull(integration.observationRegistry().getCurrentObservation());
    }
}
