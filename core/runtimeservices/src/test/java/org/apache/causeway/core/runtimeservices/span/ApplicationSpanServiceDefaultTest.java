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
package org.apache.causeway.core.runtimeservices.span;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.ActionInvocation;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.applib.services.span.ApplicationSpanService;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

class ApplicationSpanServiceDefaultTest {
    @Test void resultsAndRunnableWorkArePreservedOnce() {
        var f = new Fixture();
        var calls = new AtomicInteger();
        var expected = new Object();
        assertSame(expected, f.service.call("load", () -> { calls.incrementAndGet(); return expected; }));
        f.service.run("process", calls::incrementAndGet);
        assertNull(f.service.call("nullable", () -> null));
        assertEquals(2, calls.get());
        assertEquals(3, f.handler.stopped.size());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void declaredAndMixinNamesUseDomainIdentityWithoutChangingInvokedIdentity() {
        for (boolean mixin : List.of(false, true)) {
            var f = new Fixture();
            var domain = id("approve");
            var physical = mixin ? Identifier.actionIdentifier(LogicalType.fqcn(Mixin.class), "act", String.class) : domain;
            var invocation = new ActionInvocation(f.interaction, physical, domain, new Object(), List.of("sentinel-argument"), ActionInvocation.RuleChecking.CHECKED);
            f.execution.set(invocation);
            f.service.run("process", () -> { });
            var context = f.handler.stopped.get(0);
            assertEquals(canonical(domain) + " process", context.getContextualName());
            assertEquals(Map.of("causeway.bean", "ApplicationSpanServiceDefault", "causeway.module", "runtimeservices",
                    "causeway.member.id", canonical(domain), "causeway.application.span.suffix", "process"),
                    context.getLowCardinalityKeyValues().stream().collect(Collectors.toMap(KeyValue::getKey, KeyValue::getValue)));
            assertEquals(0, context.getHighCardinalityKeyValues().stream().count());
            assertSame(physical, invocation.getLogicalMemberIdentifier());
            assertSame(domain, invocation.getDomainFacingLogicalMemberIdentifier());
            assertFalse(context.getContextualName().contains("sentinel"));
        }
    }

    @Test void nestedCurrentExecutionOverridesOuterIdentityAndRestoresObservation() {
        var f = new Fixture();
        var outer = new ActionInvocation(f.interaction, id("outer"), new Object(), List.of());
        var inner = new ActionInvocation(f.interaction, id("inner"), new Object(), List.of());
        f.execution.set(outer);
        var parent = Observation.start("fixture.action", f.registry);
        try (var ignored = parent.openScope()) {
            f.service.run("load", () -> {
                var outerObservation = f.registry.getCurrentObservation();
                f.execution.set(inner);
                try { f.service.run("process", () -> assertNotSame(outerObservation, f.registry.getCurrentObservation())); }
                finally { f.execution.set(outer); }
                assertSame(outerObservation, f.registry.getCurrentObservation());
            });
            assertSame(parent, f.registry.getCurrentObservation());
        } finally { parent.stop(); }
        var outerContext = f.handler.started.get(1);
        var innerContext = f.handler.started.get(2);
        assertEquals(canonical(id("outer")) + " load", outerContext.getContextualName());
        assertEquals(canonical(id("inner")) + " process", innerContext.getContextualName());
        assertSame(parent, outerContext.getParentObservation());
        assertSame(outerContext, innerContext.getParentObservation().getContextView());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void nonActionExecutionUsesItsExistingLogicalMemberIdentity() {
        var f = new Fixture();
        var execution = mock(Execution.class);
        when(execution.getLogicalMemberIdentifier()).thenReturn(id("property"));
        f.execution.set(execution);
        f.service.run("load", () -> { });
        assertEquals(canonical(id("property")) + " load", f.handler.stopped.get(0).getContextualName());
    }

    @Test void missingExecutionOrInteractionUsesFallbackWithoutMemberAttribute() {
        var f = new Fixture();
        f.service.run("load", () -> { });
        when(f.interactions.currentInteraction()).thenReturn(Optional.empty());
        f.service.run("process", () -> { });
        assertEquals(List.of("app load", "app process"), f.handler.stopped.stream().map(Observation.Context::getContextualName).toList());
        for (var context : f.handler.stopped) assertNull(context.getLowCardinalityKeyValue("causeway.member.id"));
    }

    @Test void longNamesRetainFullCanonicalMetadataAndSuffix() {
        var f = new Fixture();
        var action = id("operation".repeat(12));
        f.execution.set(new ActionInvocation(f.interaction, action, new Object(), List.of()));
        f.service.run("s".repeat(46), () -> { });
        var context = f.handler.stopped.get(0);
        assertEquals(50, context.getContextualName().length());
        assertTrue(context.getContextualName().endsWith(" " + "s".repeat(46)));
        assertEquals(canonical(action), context.getLowCardinalityKeyValue("causeway.member.id").getValue());
    }

    @Test void invalidArgumentsDoNotInvokeWorkOrStartObservationsEvenWhenInactive() {
        for (var registry : List.of(ObservationRegistry.create(), ObservationRegistry.NOOP)) {
            var f = new Fixture(registry);
            var calls = new AtomicInteger();
            assertThrows(NullPointerException.class, () -> f.service.call(null, () -> calls.incrementAndGet()));
            for (String invalid : List.of("", " \t\n", "x".repeat(47))) {
                assertThrows(IllegalArgumentException.class, () -> f.service.call(invalid, calls::incrementAndGet));
                assertThrows(IllegalArgumentException.class, () -> f.service.run(invalid, calls::incrementAndGet));
            }
            assertThrows(NullPointerException.class, () -> f.service.call("load", null));
            assertThrows(NullPointerException.class, () -> f.service.run("load", null));
            assertEquals(0, calls.get());
            assertTrue(f.handler.started.isEmpty());
        }
    }

    @Test void checkedRuntimeAndErrorFailuresEscapeUnchangedAndLaterCallsSucceed() {
        var f = new Fixture();
        for (Throwable failure : new Throwable[] { new IOException("checked"), new IllegalStateException("runtime"), new AssertionError("error") }) {
            assertSame(failure, assertThrows(Throwable.class, () -> f.service.call("load", () -> { throwFailure(failure); return null; })));
            assertSame(failure, f.handler.stopped.getLast().getError());
            assertNull(f.registry.getCurrentObservation());
            assertSame(failure, assertThrows(Throwable.class, () -> f.service.run("process", () -> throwFailure(failure))));
            assertSame(failure, f.handler.stopped.getLast().getError());
            assertNull(f.registry.getCurrentObservation());
        }
        assertEquals("ok", f.service.call("success", () -> "ok"));
        assertNull(f.handler.stopped.getLast().getError());
        assertEquals(f.handler.started.size(), f.handler.stopped.size());
    }

    @Test void telemetryFailuresAreSuppressedRatherThanReplacingWorkFailure() {
        var f = new Fixture();
        var primary = new IOException("work");
        f.handler.reportingFailure = new IllegalStateException("report");
        f.handler.stopFailure = new IllegalStateException("stop");
        assertSame(primary, assertThrows(Throwable.class, () -> f.service.call("load", () -> { throw primary; })));
        assertEquals(List.of(f.handler.reportingFailure, f.handler.stopFailure), List.of(primary.getSuppressed()));
        assertEquals(1, f.handler.started.size());
        assertEquals(1, f.handler.stopped.size());
        assertNull(f.registry.getCurrentObservation());
    }

    @Test void noopDefaultStillExecutesResultsAndFailures() {
        var f = new Fixture(ObservationRegistry.NOOP);
        assertEquals("ok", f.service.call("load", () -> "ok"));
        var failure = new IOException("checked");
        assertSame(failure, assertThrows(Throwable.class, () -> f.service.run("process", () -> { throw failure; })));
        assertTrue(f.handler.started.isEmpty());
    }

    @Test void springCanInjectServiceAndRuntimeModuleExplicitlyRegistersIt() {
        assertTrue(Arrays.asList(CausewayModuleCoreRuntimeServices.class.getAnnotation(Import.class).value()).contains(ApplicationSpanServiceDefault.class));
        try (var context = new AnnotationConfigApplicationContext()) {
            var interactions = mock(InteractionService.class);
            when(interactions.currentInteraction()).thenReturn(Optional.empty());
            context.registerBean(InteractionService.class, () -> interactions);
            context.registerBean(CausewayObservationIntegration.class, () -> new CausewayObservationIntegration(ObservationRegistry.NOOP));
            context.register(ApplicationSpanServiceDefault.class);
            context.refresh();
            assertEquals("injected", context.getBean(ApplicationSpanService.class).call("load", () -> "injected"));
        }
    }

    private static Identifier id(final String member) {
        return Identifier.actionIdentifier(LogicalType.eager(Invoice.class, "demo.Invoice"), member, String.class);
    }
    private static String canonical(final Identifier id) { return id.logicalTypeName() + "#" + id.memberLogicalName(); }
    private static class Invoice { }
    private static class Mixin { }
    @lombok.SneakyThrows private static void throwFailure(final Throwable failure) { throw failure; }

    private static final class Fixture {
        final Recorder handler = new Recorder();
        final ObservationRegistry registry;
        final InteractionService interactions = mock(InteractionService.class);
        final Interaction interaction = mock(Interaction.class);
        final AtomicReference<Execution<?, ?>> execution = new AtomicReference<>();
        final ApplicationSpanServiceDefault service;
        Fixture() { this(ObservationRegistry.create()); }
        Fixture(final ObservationRegistry registry) {
            this.registry = registry;
            if (registry != ObservationRegistry.NOOP) registry.observationConfig().observationHandler(handler);
            when(interactions.currentInteraction()).thenReturn(Optional.of(interaction));
            when(interaction.getCurrentExecution()).thenAnswer(call -> execution.get());
            service = new ApplicationSpanServiceDefault(interactions, new CausewayObservationIntegration(registry));
        }
    }
    private static final class Recorder implements ObservationHandler<Observation.Context> {
        final List<Observation.Context> started = new ArrayList<>();
        final List<Observation.Context> stopped = new ArrayList<>();
        RuntimeException reportingFailure;
        RuntimeException stopFailure;
        @Override public boolean supportsContext(final Observation.Context context) { return true; }
        @Override public void onStart(final Observation.Context context) { started.add(context); }
        @Override public void onStop(final Observation.Context context) { stopped.add(context); if (stopFailure != null) throw stopFailure; }
        @Override public void onError(final Observation.Context context) { if (reportingFailure != null) throw reportingFailure; }
    }
}
