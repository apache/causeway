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
package org.apache.causeway.core.config.observation;

import java.util.ArrayList;
import java.util.List;

import org.approvaltests.Approvals;
import org.junit.jupiter.api.Test;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class ObservationClosureTest {

    @Test
    void successfulLifecycleClosesScopeAndStopsExactlyOnce() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final Observation observation = Observation.createNotStarted("causeway.test.success", registry);
        final ObservationClosure closure = new ObservationClosure();

        closure.startAndOpenScope(observation);
        final String currentAfterStart = currentObservationName(registry);
        closure.close();
        closure.close();

        Approvals.verify(report(
                "Successful observation lifecycle",
                List.of(
                        "Starting opens the observation scope and makes the observation current.",
                        "The first close closes the scope before stopping the observation.",
                        "The second close produces no additional callbacks.",
                        "No current observation remains after cleanup."),
                List.of(
                        "current observation after start: " + currentAfterStart,
                        "current observation after two closes: " + currentObservationName(registry)),
                handler));
    }

    @Test
    void failingLifecycleRecordsThrowableAndCleansUp() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final Observation observation = Observation.createNotStarted("causeway.test.failure", registry);
        final ObservationClosure closure = new ObservationClosure();

        closure.startAndOpenScope(observation);
        closure.onError(new AssertionError("expected"));
        closure.close();

        Approvals.verify(report(
                "Failing observation lifecycle",
                List.of(
                        "The reported Throwable reaches the observation handler with its type and message.",
                        "The error callback occurs before scope closure and observation stop.",
                        "Cleanup closes the scope before stopping the observation.",
                        "No current observation remains after cleanup."),
                List.of("current observation after close: " + currentObservationName(registry)),
                handler));
    }

    @Test
    void partialAndRepeatedCleanupAreNoops() {
        final List<String> completedOperations = new ArrayList<>();
        final ObservationClosure closure = new ObservationClosure();

        closure.startAndOpenScope(null);
        completedOperations.add("start with null observation: completed");
        closure.onError(new IllegalStateException("ignored"));
        completedOperations.add("error before start: completed");
        closure.close();
        completedOperations.add("first close: completed");
        closure.close();
        completedOperations.add("second close: completed");

        final List<String> report = new ArrayList<>(List.of(
                "Scenario: Defensive cleanup without an active observation",
                "",
                "This approval checks:",
                "- A null observation is accepted as a no-op.",
                "- Reporting an error before start is accepted as a no-op.",
                "- Closing before start is accepted as a no-op.",
                "- Repeated close is accepted as a no-op.",
                "",
                "Observed operations:"));
        completedOperations.forEach(operation -> report.add("- " + operation));
        Approvals.verify(String.join("\n", report));
    }

    private static String report(
            final String scenario,
            final List<String> checks,
            final List<String> observedState,
            final RecordingHandler handler) {
        final List<String> lines = new ArrayList<>();
        lines.add("Scenario: " + scenario);
        lines.add("");
        lines.add("This approval checks:");
        checks.forEach(check -> lines.add("- " + check));
        lines.add("");
        lines.add("Observed state:");
        observedState.forEach(state -> lines.add("- " + state));
        lines.add("");
        lines.add("Observed callback order:");
        lines.addAll(handler.numberedEvents());
        return String.join("\n", lines);
    }

    private static String currentObservationName(final ObservationRegistry registry) {
        final Observation current = registry.getCurrentObservation();
        return current != null ? current.getContext().getName() : "<none>";
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> events = new ArrayList<>();

        @Override
        public void onStart(final Observation.Context context) {
            events.add("onStart(name=" + context.getName() + ")");
        }

        @Override
        public void onScopeOpened(final Observation.Context context) {
            events.add("onScopeOpened(name=" + context.getName() + ")");
        }

        @Override
        public void onScopeClosed(final Observation.Context context) {
            events.add("onScopeClosed(name=" + context.getName() + ")");
        }

        @Override
        public void onError(final Observation.Context context) {
            final Throwable failure = context.getError();
            events.add("onError(type=" + failure.getClass().getSimpleName()
                    + ", message=" + failure.getMessage() + ")");
        }

        @Override
        public void onStop(final Observation.Context context) {
            events.add("onStop(name=" + context.getName() + ")");
        }

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }

        private List<String> numberedEvents() {
            final List<String> numberedEvents = new ArrayList<>();
            for (int i = 0; i < events.size(); i++) {
                numberedEvents.add((i + 1) + ". " + events.get(i));
            }
            return numberedEvents;
        }
    }
}
