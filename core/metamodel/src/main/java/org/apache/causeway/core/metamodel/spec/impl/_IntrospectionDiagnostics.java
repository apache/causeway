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

import java.util.ArrayDeque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.core.metamodel.spec.impl.IntrospectionStateHandler.IntrospectionRequest;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

/** Retained per-thread loading frames, not telemetry observations or a recursion workaround. */
final class _IntrospectionDiagnostics {
    private static final StackWalker WALKER = StackWalker.getInstance();
    private static final ThreadLocal<State> STATE = new ThreadLocal<>();

    static Scope enter(final Class<?> type, final IntrospectionRequest request) {
        var state = state();
        var frame = new Frame(state.frames.size(), type.getName(), request, caller());
        state.frames.addLast(frame);
        notifyCapture(state);
        return new Scope(state, frame);
    }

    static void reportStackOverflow(final StackOverflowError error, final Consumer<String> reporter) {
        try {
            var state = STATE.get();
            if (state == null) return;
            if (state.reported == null) state.reported = new IdentityHashMap<>();
            if (state.reported.put(error, Boolean.TRUE) != null) return;
            var report = new StringBuilder("Metamodel specification introspection chain at StackOverflowError:");
            for (var entry : snapshot(state)) {
                report.append(System.lineSeparator()).append('[').append(entry.depth()).append("] type=").append(entry.typeName())
                        .append(" requested=").append(entry.requestedRequest());
                if (!entry.typeName().equals(entry.substitutedTypeName())) report.append(" substituted=").append(entry.substitutedTypeName());
                if (entry.requestedRequest() != entry.effectiveRequest()) report.append(" effective=").append(entry.effectiveRequest());
                report.append(" current=").append(entry.currentState() != null ? entry.currentState() : "unavailable")
                        .append(" caller=").append(entry.caller());
            }
            reporter.accept(report.toString());
        } catch (Throwable ignored) {
            // Reporting is best-effort, especially with scarce stack or memory.
        }
    }

    /** Package-private test seam; capture does not change normal loading or log output. */
    static Capture capture(final Consumer<List<Entry>> observer) {
        var state = state();
        if (state.observer != null) throw new IllegalStateException("An introspection capture is already installed");
        state.observer = observer;
        return new Capture(state);
    }

    static boolean hasStateForTesting() { return STATE.get() != null; }

    private static State state() {
        var state = STATE.get();
        if (state == null) {
            state = new State();
            STATE.set(state);
        }
        return state;
    }

    private static List<Entry> snapshot(final State state) {
        return state.frames.stream().map(Frame::snapshot).toList();
    }

    private static void notifyCapture(final State state) {
        if (state.observer != null) {
            try { state.observer.accept(snapshot(state)); }
            catch (Throwable ignored) { /* A diagnostic observer must not affect loading. */ }
        }
    }

    private static String caller() {
        try {
            return WALKER.walk(frames -> frames.filter(frame -> !plumbing(frame.getClassName())).findFirst()
                    .map(frame -> frame.getClassName() + "#" + frame.getMethodName() + ":" + frame.getLineNumber())
                    .orElse("unavailable"));
        } catch (Throwable ignored) { return "unavailable"; }
    }

    private static boolean plumbing(final String name) {
        return name.equals(_IntrospectionDiagnostics.class.getName()) || name.startsWith(_IntrospectionDiagnostics.class.getName() + "$")
                || name.equals(SpecificationLoaderDefault.class.getName()) || name.equals(SpecificationLoaderInternal.class.getName())
                || name.equals(SpecificationLoader.class.getName()) || name.startsWith("java.") || name.startsWith("jdk.");
    }

    private static void release(final State state) {
        if (state.frames.isEmpty()) {
            state.reported = null;
            if (state.observer == null && STATE.get() == state) STATE.remove();
        }
    }

    record Entry(int depth, String typeName, String substitutedTypeName, IntrospectionRequest requestedRequest,
            IntrospectionRequest effectiveRequest, @Nullable String currentState, String caller) { }

    private static final class Frame {
        final int depth;
        final String typeName;
        final IntrospectionRequest requested;
        final String caller;
        String substitutedTypeName;
        IntrospectionRequest effective;
        @Nullable String currentState;
        Frame(final int depth, final String typeName, final IntrospectionRequest request, final String caller) {
            this.depth = depth;
            this.typeName = typeName;
            this.requested = request;
            this.effective = request;
            this.substitutedTypeName = typeName;
            this.caller = caller;
        }
        Entry snapshot() { return new Entry(depth, typeName, substitutedTypeName, requested, effective, currentState, caller); }
    }

    static final class Scope implements AutoCloseable {
        private final State state;
        private final Frame frame;
        private boolean closed;
        Scope(final State state, final Frame frame) { this.state = state; this.frame = frame; }
        void note(final Class<?> substitutedType, final IntrospectionRequest effective, final @Nullable String currentState) {
            frame.substitutedTypeName = substitutedType.getName();
            frame.effective = effective;
            frame.currentState = currentState;
            notifyCapture(state);
        }
        @Override public void close() {
            if (closed) return;
            closed = true;
            try {
                if (frame.depth == 0) state.frames.clear();
                else if (state.frames.peekLast() == frame) state.frames.removeLast();
                else state.frames.remove(frame);
                release(state);
            } catch (Throwable ignored) {
                // Never replace an original loader failure with diagnostic cleanup failure.
                try {
                    if (frame.depth == 0 && STATE.get() == state) STATE.remove();
                } catch (Throwable cleanupFailure) { /* Preserve the original loader failure. */ }
            }
        }
    }

    static final class Capture implements AutoCloseable {
        private final State state;
        private boolean closed;
        Capture(final State state) { this.state = state; }
        @Override public void close() {
            if (closed) return;
            closed = true;
            state.observer = null;
            release(state);
        }
    }

    private static final class State {
        final ArrayDeque<Frame> frames = new ArrayDeque<>();
        @Nullable IdentityHashMap<StackOverflowError, Boolean> reported;
        @Nullable Consumer<List<Entry>> observer;
    }
    private _IntrospectionDiagnostics() { }
}
