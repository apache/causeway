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
package org.apache.causeway.commons.internal.observation;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class ObservationClosureTest {
    @Test void restoresParentAndClosesOnlyOnce() {
        var registry = ObservationRegistry.create();
        var stopped = new ArrayList<String>();
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            public boolean supportsContext(Observation.Context context) { return true; }
            public void onStop(Observation.Context context) { stopped.add(context.getName()); }
        });
        var parent = Observation.start("parent", registry);
        try (var parentScope = parent.openScope()) {
            var child = Observation.createNotStarted("child", registry);
            var closure = new ObservationClosure().startAndOpenScope(child);
            assertSame(child, registry.getCurrentObservation());
            closure.close();
            closure.close();
            assertSame(parent, registry.getCurrentObservation());
            assertEquals(List.of("child"), stopped);
            assertNull(closure.observation());
            assertNull(closure.scope());
        } finally { parent.stop(); }
        assertNull(registry.getCurrentObservation());
    }

    @Test void rejectsDuplicateStartWithoutLosingOriginal() {
        var probe = new Probe();
        var closure = new ObservationClosure().startAndOpenScope(probe.observation());
        assertThrows(IllegalStateException.class, () -> closure.startAndOpenScope(probe.observation()));
        closure.close();
        assertEquals(List.of("start", "openScope", "closeScope", "stop"), probe.calls);
    }

    @Test void scopeOpenFailurePreservesOriginalEvenIfReportingAndStopFail() {
        var probe = new Probe();
        probe.openFailure = new AssertionError("open");
        probe.reportFailure = new IllegalStateException("report");
        probe.stopFailure = new IllegalStateException("stop");
        var closure = new ObservationClosure();
        assertSame(probe.openFailure, assertThrows(AssertionError.class,
                () -> closure.startAndOpenScope(probe.observation())));
        closure.close();
        assertEquals(List.of("start", "openScope", "error", "stop"), probe.calls);
        assertArrayEquals(new Throwable[]{probe.reportFailure, probe.stopFailure}, probe.openFailure.getSuppressed());
    }

    @Test void scopeCloseFailureStillStopsAndRetainsBothFailures() {
        var probe = new Probe();
        probe.closeFailure = new IllegalStateException("close");
        probe.stopFailure = new IllegalStateException("stop");
        var closure = new ObservationClosure().startAndOpenScope(probe.observation());
        assertSame(probe.closeFailure, assertThrows(IllegalStateException.class, closure::close));
        closure.close();
        assertEquals(List.of("start", "openScope", "closeScope", "stop"), probe.calls);
        assertArrayEquals(new Throwable[]{probe.stopFailure}, probe.closeFailure.getSuppressed());
    }

    @Test void workErrorRemainsPrimaryDuringCleanup() {
        var probe = new Probe();
        probe.closeFailure = new IllegalStateException("close");
        var failure = new AssertionError("work");
        var closure = new ObservationClosure().startAndOpenScope(probe.observation());
        closure.onError(failure);
        closure.close();
        assertSame(failure, probe.recordedFailure);
        assertArrayEquals(new Throwable[]{probe.closeFailure}, failure.getSuppressed());
        assertEquals(List.of("start", "openScope", "error", "closeScope", "stop"), probe.calls);
    }

    @Test void stopFailureClearsStateAndTagAndDiscardRemainAvailable() {
        var probe = new Probe();
        probe.stopFailure = new IllegalStateException("stop");
        var closure = new ObservationClosure().startAndOpenScope(probe.observation());
        closure.tag("key", () -> "value");
        assertSame(probe.stopFailure, assertThrows(IllegalStateException.class, closure::discard));
        closure.close();
        assertNull(closure.observation());
        assertEquals(List.of("start", "openScope", "highCardinalityKeyValue", "lowCardinalityKeyValue", "closeScope", "stop"), probe.calls);
    }

    private static final class Probe {
        final List<String> calls = new ArrayList<>();
        Throwable openFailure, closeFailure, stopFailure, reportFailure, recordedFailure;
        Observation observation() {
            return (Observation) Proxy.newProxyInstance(Observation.class.getClassLoader(), new Class<?>[]{Observation.class},
                    (proxy, method, args) -> {
                        calls.add(method.getName());
                        switch (method.getName()) {
                            case "openScope":
                                if (openFailure != null) throw openFailure;
                                return Proxy.newProxyInstance(Observation.Scope.class.getClassLoader(), new Class<?>[]{Observation.Scope.class},
                                        (scope, scopeMethod, scopeArgs) -> {
                                            if (scopeMethod.getName().equals("close")) {
                                                calls.add("closeScope");
                                                if (closeFailure != null) throw closeFailure;
                                            }
                                            return null;
                                        });
                            case "stop": if (stopFailure != null) throw stopFailure; return null;
                            case "error": recordedFailure = (Throwable) args[0]; if (reportFailure != null) throw reportFailure; return proxy;
                            default: return proxy;
                        }
                    });
        }
    }
}
