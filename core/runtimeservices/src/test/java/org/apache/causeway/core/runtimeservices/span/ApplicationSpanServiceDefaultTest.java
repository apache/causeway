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
package org.apache.causeway.core.runtimeservices.span;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationSpanServiceDefaultTest {

    @Test
    void callUsesStableNameContextualIdentityAndLowCardinalityAttributes() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final ApplicationSpanServiceDefault service = serviceWith(
                currentInteraction("demo.Customer", "updateName"),
                registry);
        final AtomicInteger calls = new AtomicInteger();

        assertEquals("result", service.call("load", () -> {
            calls.incrementAndGet();
            return "result";
        }));
        assertEquals(1, calls.get());
        assertNull(registry.getCurrentObservation());

        final Observation.Context context = handler.stopped.get(0);
        assertEquals(ApplicationSpanServiceDefault.OBSERVATION_NAME, context.getName());
        assertEquals("demo.Customer#updateName load", context.getContextualName());
        assertEquals("demo.Customer#updateName",
                value(context, ApplicationSpanServiceDefault.MEMBER_ID_ATTRIBUTE));
        assertEquals("load",
                value(context, ApplicationSpanServiceDefault.SUFFIX_ATTRIBUTE));
        assertTrue(context.getContextualName().indexOf('(') < 0);
    }

    @Test
    void callFallsBackToAppNameWithoutCurrentExecution() {
        final RecordingHandler handler = new RecordingHandler();
        final ApplicationSpanServiceDefault service = serviceWith(
                noCurrentInteraction(),
                registryWith(handler));

        service.call("load", () -> "done");

        final Observation.Context context = handler.stopped.get(0);
        assertEquals("app load", context.getContextualName());
        assertEquals(null,
                value(context, ApplicationSpanServiceDefault.MEMBER_ID_ATTRIBUTE));
    }

    @Test
    void invalidSuffixIsRejectedBeforeApplicationWork() {
        final AtomicInteger calls = new AtomicInteger();
        final ApplicationSpanServiceDefault service = serviceWith(
                noCurrentInteraction(),
                ObservationRegistry.NOOP);

        assertThrows(NullPointerException.class,
                () -> service.call(null, () -> calls.incrementAndGet()));
        assertThrows(IllegalArgumentException.class,
                () -> service.call(" ", () -> calls.incrementAndGet()));
        assertThrows(IllegalArgumentException.class,
                () -> service.call("x".repeat(47), () -> calls.incrementAndGet()));
        assertEquals(0, calls.get());
    }

    @Test
    void originalCheckedExceptionEscapesUnchangedAndIsRecorded() {
        final RecordingHandler handler = new RecordingHandler();
        final ObservationRegistry registry = registryWith(handler);
        final ApplicationSpanServiceDefault service = serviceWith(
                noCurrentInteraction(),
                registry);
        final CheckedFailure failure = new CheckedFailure("expected");

        final CheckedFailure thrown = assertThrows(CheckedFailure.class,
                () -> service.call("fail", () -> {
                    throw failure;
                }));

        assertSame(failure, thrown);
        assertSame(failure, handler.stopped.get(0).getError());
        assertNull(registry.getCurrentObservation());
    }

    @Test
    void uncheckedExceptionsAndErrorsEscapeUnchanged() {
        final ApplicationSpanServiceDefault service = serviceWith(
                noCurrentInteraction(),
                ObservationRegistry.NOOP);
        final IllegalStateException runtimeFailure =
                new IllegalStateException("runtime");
        final AssertionError error = new AssertionError("error");

        assertSame(runtimeFailure, assertThrows(IllegalStateException.class,
                () -> service.call("runtime", () -> {
                    throw runtimeFailure;
                })));
        assertSame(error, assertThrows(AssertionError.class,
                () -> service.run("error", () -> {
                    throw error;
                })));
    }

    @Test
    void runAndNoopRegistryExecuteWorkExactlyOnce() {
        final AtomicInteger calls = new AtomicInteger();
        final ApplicationSpanServiceDefault service = serviceWith(
                noCurrentInteraction(),
                ObservationRegistry.NOOP);

        service.run("process", calls::incrementAndGet);

        assertEquals(1, calls.get());
    }

    @Test
    void nestedCallsUseTheCurrentApplicationSpanAsParent() {
        final RecordingHandler handler = new RecordingHandler();
        final ApplicationSpanServiceDefault service = serviceWith(
                noCurrentInteraction(),
                registryWith(handler));

        service.run("outer", () -> service.run("inner", () -> {}));

        assertEquals(List.of("app outer <- <root>", "app inner <- app outer"),
                handler.started);
    }

    private static InteractionService currentInteraction(
            final String logicalTypeName,
            final String memberLogicalName) {
        final InteractionService interactionService = mock(InteractionService.class);
        final Interaction interaction = mock(Interaction.class);
        final Execution<?, ?> execution = mock(Execution.class);
        final Identifier identifier = Identifier.actionIdentifier(
                LogicalType.eager(Object.class, logicalTypeName),
                memberLogicalName);
        when(interactionService.currentInteraction()).thenReturn(Optional.of(interaction));
        doReturn(execution).when(interaction).getCurrentExecution();
        when(execution.getLogicalMemberIdentifier()).thenReturn(identifier);
        return interactionService;
    }

    private static InteractionService noCurrentInteraction() {
        final InteractionService interactionService = mock(InteractionService.class);
        when(interactionService.currentInteraction()).thenReturn(Optional.empty());
        return interactionService;
    }

    private static ApplicationSpanServiceDefault serviceWith(
            final InteractionService interactionService,
            final ObservationRegistry registry) {
        return new ApplicationSpanServiceDefault(
                interactionService,
                new CausewayObservationIntegration(registry));
    }

    private static ObservationRegistry registryWith(final RecordingHandler handler) {
        final ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(handler);
        return registry;
    }

    private static String value(final Observation.Context context, final String key) {
        final KeyValue keyValue = context.getLowCardinalityKeyValue(key);
        return keyValue != null ? keyValue.getValue() : null;
    }

    private static final class RecordingHandler
    implements ObservationHandler<Observation.Context> {

        private final List<String> started = new ArrayList<>();
        private final List<Observation.Context> stopped = new ArrayList<>();

        @Override
        public void onStart(final Observation.Context context) {
            final String parentName = context.getParentObservation() != null
                    ? context.getParentObservation().getContextView().getContextualName()
                    : "<root>";
            started.add(context.getContextualName() + " <- " + parentName);
        }

        @Override
        public void onStop(final Observation.Context context) {
            stopped.add(context);
        }

        @Override
        public boolean supportsContext(final Observation.Context context) {
            return true;
        }
    }

    private static final class CheckedFailure extends Exception {
        private static final long serialVersionUID = 1L;

        private CheckedFailure(final String message) {
            super(message);
        }
    }
}
