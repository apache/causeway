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
package org.apache.causeway.core.metamodel.services.priming;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.priming.PrimingRegistrar;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class PrimingRegistryDefaultObservationTest {
    @Test void eachCallbackHasOneSequentialSiblingScopeAndNaturalChildren() {
        var f = new Fixture();
        var calls = new ArrayList<String>();
        f.register(registry -> {
            registry.action(Domain.class, "SendEmail", (target, args) -> { calls.add("a1"); f.sql(); });
            registry.action(Domain.class, "SendEmail", (target, args) -> { calls.add("a2"); f.sql(); });
            registry.view(Domain.class, target -> { calls.add("v1"); f.sql(); });
            registry.view(Domain.class, target -> { calls.add("v2"); f.sql(); });
        });
        var parent = Observation.start("fixture.current", f.registry);
        try (var ignored = parent.openScope()) {
            f.action();
            assertSame(parent, f.registry.getCurrentObservation());
            f.view();
            assertSame(parent, f.registry.getCurrentObservation());
        } finally { parent.stop(); }
        assertEquals(List.of("a1", "a2", "v1", "v2"), calls);
        var primers = f.handler.stopped.stream().filter(c -> c.getName().startsWith("causeway.priming.")).toList();
        assertEquals(4, primers.size());
        for (var primer : primers) assertSame(parent, primer.getParentObservation());
        var sql = f.handler.stopped.stream().filter(c -> c.getName().equals("fixture.jdbc")).toList();
        assertEquals(4, sql.size());
        for (int i = 0; i < sql.size(); i++) assertSame(primers.get(i), sql.get(i).getParentObservation().getContextView());
        assertEquals(9, f.handler.started.size()); // parent, four callbacks, four SQL children; no aggregate
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void metadataIsStaticDomainFacingAndRetainsResolvedSignature() {
        var f = new Fixture();
        when(f.action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(
                LogicalType.eager(Domain.class, "implementation.Mixin"), "SendEmail", String.class, int.class));
        when(f.action.isDeclaredOnMixin()).thenReturn(true);
        f.register(registry -> {
            registry.action(Domain.class, "SendEmail", (target, args) -> assertEquals("sentinel-argument", args.get(0)));
            registry.view(Domain.class, target -> { });
        });
        f.action();
        f.view();
        assertEquals("causeway.priming.action", f.handler.stopped.get(0).getName());
        assertEquals("causeway.priming.view", f.handler.stopped.get(1).getName());
        assertEquals("prime action demo.Customer#SendEmail", f.handler.stopped.get(0).getContextualName());
        assertEquals("prime view demo.Customer", f.handler.stopped.get(1).getContextualName());
        assertEquals(Map.of("causeway.bean", "PrimingRegistryDefault", "causeway.module", "metamodel",
                "causeway.object.type", "demo.Customer", "causeway.action.id", "demo.Customer#SendEmail(java.lang.String,int)"),
                tags(f.handler.stopped.get(0)));
        assertEquals(Map.of("causeway.bean", "PrimingRegistryDefault", "causeway.module", "metamodel",
                "causeway.object.type", "demo.Customer"), tags(f.handler.stopped.get(1)));
        for (var c : f.handler.stopped) {
            assertEquals(0, c.getHighCardinalityKeyValues().stream().count());
            assertFalse(tags(c).toString().contains("sentinel"));
            assertFalse(tags(c).toString().contains("implementation"));
        }
    }

    @Test void longNamesAreBoundedAndCanonicalMetadataIsFrozenAtRegistration() {
        var f = new Fixture();
        var type = "very.long.logical.namespace." + "CaseSensitiveOwner".repeat(5);
        when(f.spec.logicalTypeName()).thenReturn(type);
        f.register(registry -> {
            registry.action(Domain.class, "SendEmail", (target, args) -> { });
            registry.view(Domain.class, target -> { });
        });
        when(f.action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(LogicalType.fqcn(Domain.class), "changed"));
        f.action();
        f.view();
        for (var c : f.handler.stopped) {
            assertTrue(c.getContextualName().length() <= 50);
            assertTrue(c.getContextualName().contains("CaseSensitiveOwner"));
            assertEquals(type, tags(c).get("causeway.object.type"));
        }
        assertEquals(type + "#SendEmail(java.lang.String,int)", tags(f.handler.stopped.get(0)).get("causeway.action.id"));
    }

    @Test void runtimeAndErrorFailuresRemainIdenticalFailFastAndRestoreScope() {
        for (boolean action : List.of(false, true)) {
            for (Throwable failure : new Throwable[] { new IllegalStateException("callback"), new AssertionError("callback") }) {
                var f = new Fixture();
                var currentFailure = new AtomicReference<>(failure);
                var calls = new AtomicInteger();
                Runnable callback = () -> { calls.incrementAndGet(); rethrow(currentFailure.get()); };
                f.register(registry -> {
                    if (action) {
                        registry.action(Domain.class, "SendEmail", (target, args) -> callback.run());
                        registry.action(Domain.class, "SendEmail", (target, args) -> calls.incrementAndGet());
                    } else {
                        registry.view(Domain.class, target -> callback.run());
                        registry.view(Domain.class, target -> calls.incrementAndGet());
                    }
                });
                var parent = Observation.start("fixture.parent", f.registry);
                try (var ignored = parent.openScope()) {
                    assertSame(failure, assertThrows(Throwable.class, () -> { if (action) f.action(); else f.view(); }));
                    assertEquals(1, calls.get());
                    assertSame(parent, f.registry.getCurrentObservation());
                    assertSame(failure, f.handler.stopped.get(0).getError());
                    currentFailure.set(null);
                    if (action) f.action(); else f.view();
                    assertEquals(3, calls.get());
                    assertSame(parent, f.registry.getCurrentObservation());
                } finally { parent.stop(); }
                assertNull(f.registry.getCurrentObservation());
            }
        }
    }

    @Test void unmatchedLookupsNeverResolveObservationIntegrationOrCreateSpans() {
        var f = new Fixture();
        f.register(registry -> registry.action(Domain.class, "SendEmail", (target, args) -> fail("unmatched")));
        clearInvocations(f.services);
        f.priming.primeAction(f.spec, "unknown", new Domain(), List.of());
        f.view();
        verify(f.services, never()).lookupService(CausewayObservationIntegration.class);
        assertTrue(f.handler.started.isEmpty());
    }

    @Test void absentAndNoopIntegrationKeepInvocationAndFailureSemantics() {
        for (boolean absent : List.of(false, true)) {
            var f = new Fixture();
            when(f.services.lookupService(CausewayObservationIntegration.class)).thenReturn(absent ? Optional.empty()
                    : Optional.of(new CausewayObservationIntegration(ObservationRegistry.NOOP)));
            var calls = new AtomicInteger();
            var failure = new IllegalStateException("unchanged");
            f.register(registry -> {
                registry.action(Domain.class, "SendEmail", (target, args) -> calls.incrementAndGet());
                registry.view(Domain.class, target -> { calls.incrementAndGet(); throw failure; });
            });
            f.action();
            assertSame(failure, assertThrows(IllegalStateException.class, f::view));
            assertEquals(2, calls.get());
            assertTrue(f.handler.started.isEmpty());
            assertNull(f.registry.getCurrentObservation());
        }
    }

    @Test void telemetryFailuresDoNotReplacePrimaryCallbackFailure() {
        var f = new Fixture();
        var failure = new IllegalStateException("primary");
        f.handler.reportingFailure = new IllegalStateException("report");
        f.handler.stopFailure = new IllegalStateException("stop");
        f.register(registry -> registry.view(Domain.class, target -> { throw failure; }));
        assertSame(failure, assertThrows(IllegalStateException.class, f::view));
        assertEquals(List.of(f.handler.reportingFailure, f.handler.stopFailure), List.of(failure.getSuppressed()));
        assertEquals(1, f.handler.stopped.size());
        assertNull(f.registry.getCurrentObservation());
    }

    private static Map<String, String> tags(final Observation.Context c) {
        return c.getLowCardinalityKeyValues().stream().collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue));
    }
    private static void rethrow(final Throwable failure) {
        if (failure instanceof RuntimeException ex) throw ex;
        if (failure instanceof Error ex) throw ex;
    }
    private static final class Domain {
        @Override public String toString() { return "sentinel-target"; }
    }
    private static final class Fixture {
        final Recorder handler = new Recorder();
        final ObservationRegistry registry = ObservationRegistry.create();
        final SpecificationLoader loader = mock(SpecificationLoader.class);
        final ServiceRegistry services = mock(ServiceRegistry.class);
        final ObjectSpecification spec = mock(ObjectSpecification.class);
        final ObjectAction action = mock(ObjectAction.class);
        final PrimingRegistryDefault priming = new PrimingRegistryDefault(loader, services);
        Fixture() {
            registry.observationConfig().observationHandler(handler);
            when(services.lookupService(CausewayObservationIntegration.class)).thenReturn(Optional.of(new CausewayObservationIntegration(registry)));
            doReturn(Domain.class).when(spec).correspondingClass();
            when(spec.logicalTypeName()).thenReturn("demo.Customer");
            doReturn(Can.of(spec)).when(loader).snapshotSpecifications();
            when(action.getFeatureIdentifier()).thenReturn(Identifier.actionIdentifier(LogicalType.eager(Domain.class, "demo.Customer"), "SendEmail", String.class, int.class));
            when(spec.getAction("SendEmail", MixedIn.INCLUDED)).thenReturn(Optional.of(action));
        }
        void register(final PrimingRegistrar registrar) {
            when(services.select(PrimingRegistrar.class)).thenReturn(Can.of(registrar));
            priming.onMetamodelLoaded();
        }
        void action() { priming.primeAction(spec, "SendEmail", new Domain(), List.of("sentinel-argument")); }
        void view() { priming.primeView(spec, new Domain()); }
        void sql() { Observation.createNotStarted("fixture.jdbc", registry).observe(() -> { }); }
    }
    private static final class Recorder implements ObservationHandler<Observation.Context> {
        final List<Observation.Context> started = new ArrayList<>();
        final List<Observation.Context> stopped = new ArrayList<>();
        RuntimeException reportingFailure;
        RuntimeException stopFailure;
        @Override public boolean supportsContext(final Observation.Context c) { return true; }
        @Override public void onStart(final Observation.Context c) { started.add(c); }
        @Override public void onStop(final Observation.Context c) { stopped.add(c); if (stopFailure != null) throw stopFailure; }
        @Override public void onError(final Observation.Context c) { if (reportingFailure != null) throw reportingFailure; }
    }
}
