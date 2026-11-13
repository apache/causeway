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
package org.apache.causeway.regressiontests.tracing;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.causeway.core.config.observation.CausewayObservationIntegration;

import io.micrometer.tracing.Tracer;

/** Real exported async spans with a controlled external parent; not a remote service. */
final class SpanDurationAsyncFixture {
    private SpanDurationAsyncFixture() { }

    static void run(final Tracer tracer, final CausewayObservationIntegration integration) throws Exception {
        // given: a fixture-owned remote-parent context; its originating process is outside the capture
        var external = tracer.traceContextBuilder().traceId("88888888888888888888888888888888")
                .spanId("7777777777777777").sampled(true).build();
        var entry = tracer.spanBuilder().name("duration.evaluation.remote-entry").setParent(external).start();
        var executor = Executors.newSingleThreadExecutor();
        try (var entryScope = tracer.withSpan(entry)) {
            var observation = integration.createNotStarted(SpanDurationAsyncFixture.class, "causeway.evaluation.parent").start();
            io.micrometer.tracing.Span parent;
            try (var scope = observation.openScope()) {
                parent = tracer.currentSpan();
                if (parent == null) throw new AssertionError("Missing observation-owned span");
            } finally {
                observation.stop();
            }

            // when: a child starts on another thread after its parent's observation has ended
            executor.submit(() -> {
                var child = tracer.nextSpan(parent).name("duration.evaluation.late-error").start();
                try (var scope = tracer.withSpan(child)) {
                    child.error(new IllegalStateException("controlled evaluation failure"));
                } finally {
                    child.end();
                }
            }).get(10, TimeUnit.SECONDS);
        } finally {
            entry.end();
            executor.shutdownNow();
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) throw new AssertionError("Async fixture did not stop");
        }
    }
}
