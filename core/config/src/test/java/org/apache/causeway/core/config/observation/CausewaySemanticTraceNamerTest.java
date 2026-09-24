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

import io.opentelemetry.api.trace.Span;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CausewaySemanticTraceNamerTest {

    @Test
    void higherPriorityCandidateWinsAndFirstEqualPriorityIsRetained() {
        final Span span = recordingSpan();

        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(span)) {
            CausewaySemanticTraceNamer.nominateView("demo.Customer");
            CausewaySemanticTraceNamer.nominatePrompt(
                    "demo.Customer#edit", "demo.Customer#edit()");
            CausewaySemanticTraceNamer.nominateAction("demo.Customer#save");
            CausewaySemanticTraceNamer.nominateAction("demo.Customer#delete");
        }

        verify(span).updateName("act demo.Customer#save");
        verify(span).setAttribute(
                CausewaySemanticTraceNamer.TRACE_NAME_ATTRIBUTE,
                "act demo.Customer#save");
        verify(span).setAttribute(
                CausewaySemanticTraceNamer.ACTION_ID_ATTRIBUTE,
                "demo.Customer#save()");
        verify(span, never()).setAttribute(
                CausewaySemanticTraceNamer.OBJECT_TYPE_ATTRIBUTE,
                "demo.Customer");
    }

    @Test
    void viewCandidateUsesTypeNamingAndCanonicalMetadata() {
        final Span span = recordingSpan();

        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(span)) {
            CausewaySemanticTraceNamer.nominateView("demo.Customer");
        }

        verify(span).updateName("view demo.Customer");
        verify(span).setAttribute(
                CausewaySemanticTraceNamer.OBJECT_TYPE_ATTRIBUTE,
                "demo.Customer");
    }

    @Test
    void contextualNamesPreserveCaseCompactNamespacesAndRemainBounded() {
        final Span span = recordingSpan();
        final String identifier = "very.long.logical.namespace.ApplicationUser#updateEmailAddress";

        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(span)) {
            CausewaySemanticTraceNamer.nominatePrompt(identifier, identifier + "()");
        }

        verify(span).updateName("prompt ApplicationUser#updateEmailAddress");
        verify(span).setAttribute(
                CausewaySemanticTraceNamer.ACTION_ID_ATTRIBUTE,
                identifier + "()");
    }

    @Test
    void nestedScopesApplyIndependentlyAndRestoreOuterScope() {
        final Span outer = recordingSpan();
        final Span inner = recordingSpan();

        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(outer)) {
            CausewaySemanticTraceNamer.nominateView("demo.Outer");
            try (CausewaySemanticTraceNamer.Scope nested =
                    CausewaySemanticTraceNamer.open(inner)) {
                CausewaySemanticTraceNamer.nominatePrompt(
                        "demo.Inner#edit", "demo.Inner#edit()");
            }
            CausewaySemanticTraceNamer.nominateAction("demo.Outer#save");
        }

        final InOrder order = inOrder(inner, outer);
        order.verify(inner).updateName("prompt demo.Inner#edit");
        order.verify(outer).updateName("act demo.Outer#save");
    }

    @Test
    void noCandidateLeavesRecordingSpanUnchanged() {
        final Span span = recordingSpan();

        CausewaySemanticTraceNamer.open(span).close();

        verify(span, never()).updateName(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void nonRecordingSpanAndNominationOutsideScopeAreNoops() {
        final Span span = mock(Span.class);
        when(span.isRecording()).thenReturn(false);

        try (CausewaySemanticTraceNamer.Scope ignored =
                CausewaySemanticTraceNamer.open(span)) {
            CausewaySemanticTraceNamer.nominateAction("demo.Customer#save");
        }
        CausewaySemanticTraceNamer.nominateView("demo.Customer");

        verify(span, never()).updateName(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void scopesMustCloseInReverseOrder() {
        final CausewaySemanticTraceNamer.Scope outer =
                CausewaySemanticTraceNamer.open(recordingSpan());
        final CausewaySemanticTraceNamer.Scope inner =
                CausewaySemanticTraceNamer.open(recordingSpan());

        assertThrows(IllegalStateException.class, outer::close);
        inner.close();
        outer.close();
        CausewaySemanticTraceNamer.open(recordingSpan()).close();
    }

    private static Span recordingSpan() {
        final Span span = mock(Span.class);
        when(span.isRecording()).thenReturn(true);
        return span;
    }
}
