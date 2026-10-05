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
package org.apache.causeway.viewer.wicket.viewer.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.IExceptionMapper;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.cycle.RequestCycleContext;
import org.apache.wicket.request.cycle.RequestCycleListenerCollection;
import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.metrics.MetricsService;
import org.apache.causeway.commons.internal.observation.ObservationClosure;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class TelemetryRequestCycleTest {

    @Test
    void restoresHttpScopeBeforeServletThreadIsReused() {
        verifyRequestScopes(false);
    }

    @Test
    void restoresHttpScopeAfterRequestFailure() {
        verifyRequestScopes(true);
    }

    private void verifyRequestScopes(boolean fail) {
        var registry = ObservationRegistry.create();
        var stopped = new ArrayList<String>();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
            public void onStop(Observation.Context context) { stopped.add(context.getName()); }
        });
        var interaction = new ObservationClosure();
        var listeners = new RequestCycleListenerCollection();
        // Same registration order as CausewayWicketApplication, using Wicket's
        // real reverse-order end notifications and a scoped inner interaction.
        listeners.add(new TelemetryStartHandler(new CausewayObservationIntegration(registry)));
        listeners.add(new IRequestCycleListener() {
            public void onBeginRequest(RequestCycle cycle) {
                interaction.startAndOpenScope(Observation.createNotStarted("interaction", registry));
            }
            public void onEndRequest(RequestCycle cycle) {
                interaction.close();
            }
        });
        listeners.add(new TelemetryStopHandler(mock(MetricsService.class)));

        // Reuse the thread, as when subsequent Wicket, HTMX or static-resource
        // requests are served by the same servlet worker.
        for (int request = 0; request < 2; request++) {
            var cycle = new RequestCycle2(new RequestCycleContext(mock(Request.class),
                    mock(Response.class), mock(IRequestMapper.class), mock(IExceptionMapper.class)));
            var http = Observation.start("http", registry);
            assertNull(http.getContext().getParentObservation());
            try (var scope = http.openScope()) {
                listeners.onBeginRequest(cycle);
                var wicket = cycle.observationClosure.observation();
                assertSame(wicket, interaction.observation().getContext().getParentObservation());
                if (fail) {
                    var failure = new IllegalStateException("request failed");
                    listeners.onException(cycle, failure);
                    assertSame(failure, wicket.getContext().getError());
                }
                listeners.onEndRequest(cycle);
                assertSame(http, registry.getCurrentObservation());
                assertNull(cycle.observationClosure.scope());
            } finally {
                http.stop();
            }
            assertNull(registry.getCurrentObservation());
        }
        assertEquals(List.of("interaction", "Apache Wicket Request Cycle", "http",
                "interaction", "Apache Wicket Request Cycle", "http"), stopped);
    }
}
