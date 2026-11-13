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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;

class _IntrospectionDiagnosticsTest {
    @Test void retainsOrderedImmutableSnapshotsWithRequestStateSubstitutionAndCaller() {
        var captures = new ArrayList<List<_IntrospectionDiagnostics.Entry>>();
        try (var capture = _IntrospectionDiagnostics.capture(captures::add)) {
            try (var outer = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL)) {
                outer.note(B.class, IntrospectionRequest.TYPE_ONLY, "TYPE_BEING_INTROSPECTED");
                try (var inner = _IntrospectionDiagnostics.enter(C.class, IntrospectionRequest.REGISTER)) {
                    var entries = captures.getLast();
                    assertEquals(List.of(A.class.getName(), C.class.getName()), entries.stream().map(_IntrospectionDiagnostics.Entry::typeName).toList());
                    assertEquals(B.class.getName(), entries.getFirst().substitutedTypeName());
                    assertEquals(IntrospectionRequest.FULL, entries.getFirst().requestedRequest());
                    assertEquals(IntrospectionRequest.TYPE_ONLY, entries.getFirst().effectiveRequest());
                    assertEquals("TYPE_BEING_INTROSPECTED", entries.getFirst().currentState());
                    assertEquals(1, entries.getLast().depth());
                    assertTrue(entries.getLast().caller().contains(getClass().getName()));
                    assertThrows(UnsupportedOperationException.class, entries::clear);
                }
            }
        }
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void sameOverflowIsReportedOnceBeforeNestedFramesUnwind() {
        var reports = new ArrayList<String>();
        var original = new StackOverflowError("original");
        assertSame(original, assertThrows(StackOverflowError.class, () -> {
            try (var outer = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL)) {
                try {
                    try (var inner = _IntrospectionDiagnostics.enter(B.class, IntrospectionRequest.TYPE_ONLY)) {
                        inner.note(C.class, IntrospectionRequest.TYPE_ONLY, "MEMBERS_BEING_INTROSPECTED");
                        _IntrospectionDiagnostics.reportStackOverflow(original, reports::add);
                        throw original;
                    }
                } catch (StackOverflowError failure) {
                    _IntrospectionDiagnostics.reportStackOverflow(failure, reports::add);
                    throw failure;
                }
            }
        }));
        assertEquals(1, reports.size());
        var report = reports.getFirst();
        assertTrue(report.indexOf(A.class.getName()) < report.indexOf(B.class.getName()));
        assertTrue(report.contains("requested=FULL"));
        assertTrue(report.contains("substituted=" + C.class.getName()));
        assertTrue(report.contains("current=MEMBERS_BEING_INTROSPECTED"));
        assertTrue(report.contains("current=unavailable"));
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void distinctErrorsAndLaterIndependentChainsHaveIndependentReports() {
        var reports = new ArrayList<String>();
        var first = new StackOverflowError("first");
        var second = new StackOverflowError("second");
        try (var scope = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL)) {
            _IntrospectionDiagnostics.reportStackOverflow(first, reports::add);
            _IntrospectionDiagnostics.reportStackOverflow(second, reports::add);
            _IntrospectionDiagnostics.reportStackOverflow(first, reports::add);
        }
        assertEquals(2, reports.size());
        try (var scope = _IntrospectionDiagnostics.enter(C.class, IntrospectionRequest.REGISTER)) {
            _IntrospectionDiagnostics.reportStackOverflow(first, reports::add);
        }
        assertEquals(3, reports.size());
        assertFalse(reports.getLast().contains("type=" + A.class.getName()));
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void reportingAndCaptureFailuresCannotReplaceLoadingOutcome() {
        var original = new StackOverflowError("original");
        try (var capture = _IntrospectionDiagnostics.capture(entries -> { throw new AssertionError("observer"); })) {
            assertSame(original, assertThrows(StackOverflowError.class, () -> {
                try (var scope = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL)) {
                    scope.note(A.class, IntrospectionRequest.FULL, "NOT_INTROSPECTED");
                    _IntrospectionDiagnostics.reportStackOverflow(original, report -> { throw new AssertionError("logger"); });
                    throw original;
                }
            }));
        }
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void closingScopesAndCapturesIsIdempotentAndSuccessDoesNotReport() {
        var entries = new ArrayList<List<_IntrospectionDiagnostics.Entry>>();
        var capture = _IntrospectionDiagnostics.capture(entries::add);
        var scope = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL);
        scope.close();
        scope.close();
        capture.close();
        capture.close();
        assertEquals(1, entries.size());
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void concurrentThreadsRetainOnlyTheirOwnChain() throws Exception {
        try (var executor = Executors.newFixedThreadPool(2)) {
            var ready = new CountDownLatch(2);
            var release = new CountDownLatch(1);
            var first = executor.submit(() -> threadedReport(A.class, ready, release));
            var second = executor.submit(() -> threadedReport(B.class, ready, release));
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            release.countDown();
            var a = first.get(10, TimeUnit.SECONDS);
            var b = second.get(10, TimeUnit.SECONDS);
            assertTrue(a.contains("type=" + A.class.getName()));
            assertFalse(a.contains("type=" + B.class.getName()));
            assertTrue(b.contains("type=" + B.class.getName()));
            assertFalse(b.contains("type=" + A.class.getName()));
        }
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    @Test void warmedScopeTimingSampleIsNonGating() {
        for (int i = 0; i < 2000; i++) {
            try (var ignored = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL)) { }
        }
        int count = 20000;
        long started = System.nanoTime();
        for (int i = 0; i < count; i++) {
            try (var ignored = _IntrospectionDiagnostics.enter(A.class, IntrospectionRequest.FULL)) { }
        }
        long elapsed = System.nanoTime() - started;
        System.out.println("DIAGNOSTIC_SCOPE_SAMPLE count=" + count + " elapsed_ns=" + elapsed + " ns_per_scope=" + elapsed / count);
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
    }

    private static String threadedReport(final Class<?> type, final CountDownLatch ready, final CountDownLatch release) throws Exception {
        var reports = new ArrayList<String>();
        try (var scope = _IntrospectionDiagnostics.enter(type, IntrospectionRequest.FULL)) {
            ready.countDown();
            if (!release.await(10, TimeUnit.SECONDS)) throw new AssertionError("thread barrier timeout");
            _IntrospectionDiagnostics.reportStackOverflow(new StackOverflowError("test"), reports::add);
        }
        assertFalse(_IntrospectionDiagnostics.hasStateForTesting());
        return reports.getFirst();
    }
    private static final class A { }
    private static final class B { }
    private static final class C { }
}
