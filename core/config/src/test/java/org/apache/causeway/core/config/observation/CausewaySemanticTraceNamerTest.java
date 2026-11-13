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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

class CausewaySemanticTraceNamerTest {
    @Test void priorityAndFirstTieRetainCanonicalSignature() {
        var span = mock(Span.class);
        try (var scope = CausewaySemanticTraceNamer.open(span)) {
            CausewaySemanticTraceNamer.nominateView("demo.Owner");
            CausewaySemanticTraceNamer.nominatePrompt("demo.Owner#prompt", "demo.Owner#prompt(java.lang.String)");
            CausewaySemanticTraceNamer.nominateAction("demo.Owner#updateName(java.lang.String)");
            CausewaySemanticTraceNamer.nominateAction("demo.Owner#later()");
        }
        verify(span).name("act demo.Owner#updateName");
        verify(span).tag("causeway.trace.name", "act demo.Owner#updateName");
        verify(span).tag("causeway.action.id", "demo.Owner#updateName(java.lang.String)");
        verify(span, never()).end();
    }
    @Test void boundedCasePreservingDisplaysKeepFullIdentity() {
        String identity = "very.long.namespace.with.many.components.UpperCaseOwner#UpdateName(java.lang.String)";
        var span = mock(Span.class);
        try (var scope = CausewaySemanticTraceNamer.open(span)) { CausewaySemanticTraceNamer.nominateAction(identity); }
        verify(span).name("act UpperCaseOwner#UpdateName");
        verify(span).tag("causeway.action.id", identity);
        var view = mock(Span.class);
        try (var scope = CausewaySemanticTraceNamer.open(view)) { CausewaySemanticTraceNamer.nominateView("demo.UpperCaseOwner"); }
        verify(view).name("view demo.UpperCaseOwner");
        verify(view).tag("causeway.object.type", "demo.UpperCaseOwner");
    }
    @Test void nestedScopesRestoreEnclosingStateAndCloseOnce() {
        var outer = mock(Span.class); var inner = mock(Span.class);
        var scope = CausewaySemanticTraceNamer.open(outer);
        CausewaySemanticTraceNamer.nominateView("demo.Outer");
        try (var nested = CausewaySemanticTraceNamer.open(inner)) { CausewaySemanticTraceNamer.nominateAction("demo.Inner#run()"); }
        CausewaySemanticTraceNamer.nominatePrompt("demo.Outer#prompt", "demo.Outer#prompt()");
        scope.close(); scope.close();
        verify(outer, times(1)).name("prompt demo.Outer#prompt");
        verify(inner).name("act demo.Inner#run");
    }
    @Test void noCandidateMissingAndNoopTracingDoNotMutateEntries() {
        var span = mock(Span.class);
        try (var scope = CausewaySemanticTraceNamer.open(span)) { }
        verify(span).isNoop(); verifyNoMoreInteractions(span);
        try (var scope = CausewaySemanticTraceNamer.open(null)) { CausewaySemanticTraceNamer.nominateView("demo.Ignored"); }
        try (var scope = new CausewaySemanticTraceNamer(Tracer.NOOP).openCurrentSpan()) { CausewaySemanticTraceNamer.nominateAction("demo.Ignored#run()"); }
    }
    @Test void inertNestedScopeDoesNotNominateForEnclosingRequest() {
        var entry = mock(Span.class);
        try (var outer = CausewaySemanticTraceNamer.open(entry)) {
            CausewaySemanticTraceNamer.nominateView("demo.Outer");
            try (var inactive = CausewaySemanticTraceNamer.open(null)) {
                CausewaySemanticTraceNamer.nominateAction("demo.Ignored#run()");
            }
        }
        verify(entry).name("view demo.Outer");
        verify(entry, never()).tag(eq("causeway.action.id"), anyString());
    }

    @Test void failedCloseRemovesStateBeforeBridgeMutationAndPreservesWorkFailure() {
        var broken = mock(Span.class);
        var closeFailure = new IllegalStateException("bridge");
        when(broken.name(anyString())).thenThrow(closeFailure);
        var workFailure = new IllegalArgumentException("work");
        assertSame(workFailure, assertThrows(IllegalArgumentException.class, () -> {
            try (var scope = CausewaySemanticTraceNamer.open(broken)) {
                CausewaySemanticTraceNamer.nominateAction("demo.Failed#run()");
                throw workFailure;
            }
        }));
        assertArrayEquals(new Throwable[]{closeFailure}, workFailure.getSuppressed());
        var later = mock(Span.class);
        try (var scope = CausewaySemanticTraceNamer.open(later)) { CausewaySemanticTraceNamer.nominateView("demo.Later"); }
        verify(later).name("view demo.Later");
    }
    @Test void reverseOrderIsEnforcedWithoutLosingTheStack() {
        var outer = CausewaySemanticTraceNamer.open(mock(Span.class));
        var inner = CausewaySemanticTraceNamer.open(mock(Span.class));
        assertThrows(IllegalStateException.class, outer::close);
        inner.close(); outer.close();
    }
    @Test void capturesEntryBeforeCurrentSpanChanges() {
        var tracer = mock(Tracer.class); var entry = mock(Span.class); var child = mock(Span.class);
        when(tracer.currentSpan()).thenReturn(entry);
        try (var scope = new CausewaySemanticTraceNamer(tracer).openCurrentSpan()) {
            when(tracer.currentSpan()).thenReturn(child);
            CausewaySemanticTraceNamer.nominateView("demo.Owner");
        }
        verify(entry).name("view demo.Owner"); verifyNoInteractions(child);
        verify(tracer, times(1)).currentSpan();
    }
    @Test void bootStopFilterRetainsDisplayAfterConventionsRefreshContext() {
        var tracer = mock(Tracer.class); var entry = mock(Span.class);
        when(tracer.currentSpan()).thenReturn(entry);
        var registry = io.micrometer.observation.ObservationRegistry.create();
        registry.observationConfig().observationHandler(new io.micrometer.observation.ObservationHandler<io.micrometer.observation.Observation.Context>() {
            public boolean supportsContext(io.micrometer.observation.Observation.Context context) { return true; }
        });
        var observation = io.micrometer.observation.Observation.start("http.server.requests", registry);
        try (var scope = observation.openScope()) {
            try (var naming = new CausewaySemanticTraceNamer(tracer, registry).openCurrentSpan()) { CausewaySemanticTraceNamer.nominateView("demo.Owner"); }
        }
        observation.getContext().setContextualName("GET /route");
        CausewaySemanticTraceNamer.retainSelectedName().map(observation.getContext());
        assertEquals("view demo.Owner", observation.getContext().getContextualName());
        assertEquals("http.server.requests", observation.getContext().getName());
        observation.stop();
    }

    @Test void inactiveDoesNotResolveAmbiguousApplicationTracers() {
        try (var context = new AnnotationConfigApplicationContext()) {
            var first = mock(Tracer.class); var second = mock(Tracer.class);
            context.registerBean("first", Tracer.class, () -> first);
            context.registerBean("second", Tracer.class, () -> second);
            context.register(CausewayObservationAutoConfiguration.class); context.refresh();
            try (var scope = context.getBean(CausewaySemanticTraceNamer.class).openCurrentSpan()) { CausewaySemanticTraceNamer.nominateView("demo.Ignored"); }
            verifyNoInteractions(first, second);
        }
    }
}
