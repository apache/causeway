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
package org.apache.causeway.core.metamodel.specloader;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.metamodel.spec.IntrospectionState;
import org.apache.causeway.core.metamodel.specloader.specimpl.ObjectSpecificationAbstract;

/**
 * Retains the active specification-loading chain so a {@link StackOverflowError} can report domain types,
 * rather than only the framework methods visible in its stack trace.
 */
final class _IntrospectionDiagnostics {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    private static final ThreadLocal<State> STATE = ThreadLocal.withInitial(State::new);

    static Scope enter(
            final Class<?> type,
            final IntrospectionState requestedState,
            final @Nullable IntrospectionState currentState) {
        final State state = STATE.get();
        if(state.entries.isEmpty()) {
            state.stackOverflowReported = false;
        }
        final Entry entry = new Entry(
                state.entries.size(),
                type.getName(),
                requestedState,
                currentState,
                caller());
        state.entries.addLast(entry);
        if(state.observer != null) {
            state.observer.accept(snapshot(state));
        }
        return new Scope(state, entry);
    }

    static void reportStackOverflow(final Consumer<String> reporter) {
        final State state = STATE.get();
        if(state.stackOverflowReported) {
            return;
        }
        state.stackOverflowReported = true;
        reporter.accept(format(snapshot(state)));
    }

    /** Test seam for asserting successful nested loading without manufacturing a real stack overflow. */
    static Observation observe(final Consumer<List<Entry>> observer) {
        final State state = STATE.get();
        if(state.observer != null) {
            throw new IllegalStateException("an introspection diagnostic observer is already installed");
        }
        state.observer = observer;
        return new Observation(state);
    }

    private static List<Entry> snapshot(final State state) {
        return Collections.unmodifiableList(new ArrayList<>(state.entries));
    }

    private static String format(final List<Entry> entries) {
        final StringBuilder report = new StringBuilder(256)
                .append("Metamodel specification introspection chain at StackOverflowError:");
        entries.forEach(entry -> report
                .append(System.lineSeparator())
                .append(entry));
        return report.toString();
    }

    private static String caller() {
        return STACK_WALKER.walk(frames -> frames
                .filter(frame -> !isDiagnosticPlumbing(frame))
                .findFirst()
                .map(frame -> frame.getClassName()
                        + "#" + frame.getMethodName()
                        + ":" + frame.getLineNumber())
                .orElse("unknown"));
    }

    private static boolean isDiagnosticPlumbing(final StackWalker.StackFrame frame) {
        final String className = frame.getClassName();
        return className.equals(_IntrospectionDiagnostics.class.getName())
                || className.equals(SpecificationLoaderDefault.class.getName())
                || className.equals(SpecificationLoader.class.getName())
                || className.equals(ObjectSpecificationAbstract.class.getName())
                    && frame.getMethodName().equals("applicableMixinFacet");
    }

    static final class Entry {
        private final int depth;
        private final String typeName;
        private final IntrospectionState requestedState;
        private final @Nullable IntrospectionState currentState;
        private final String caller;

        private Entry(
                final int depth,
                final String typeName,
                final IntrospectionState requestedState,
                final @Nullable IntrospectionState currentState,
                final String caller) {
            this.depth = depth;
            this.typeName = typeName;
            this.requestedState = requestedState;
            this.currentState = currentState;
            this.caller = caller;
        }

        int depth() {
            return depth;
        }

        String typeName() {
            return typeName;
        }

        IntrospectionState requestedState() {
            return requestedState;
        }

        @Nullable IntrospectionState currentState() {
            return currentState;
        }

        String caller() {
            return caller;
        }

        @Override
        public String toString() {
            return String.format(
                    "[%d] type=%s requested=%s current=%s caller=%s",
                    depth,
                    typeName,
                    requestedState,
                    currentState != null ? currentState : "unavailable",
                    caller);
        }
    }

    static final class Scope implements AutoCloseable {
        private final State state;
        private final Entry entry;
        private boolean closed;

        private Scope(final State state, final Entry entry) {
            this.state = state;
            this.entry = entry;
        }

        @Override
        public void close() {
            if(closed) {
                return;
            }
            closed = true;
            if(state.entries.peekLast() != entry) {
                throw new IllegalStateException("introspection diagnostic scopes closed out of order");
            }
            state.entries.removeLast();
            removeStateIfUnused(state);
        }
    }

    static final class Observation implements AutoCloseable {
        private final State state;
        private boolean closed;

        private Observation(final State state) {
            this.state = state;
        }

        @Override
        public void close() {
            if(closed) {
                return;
            }
            closed = true;
            state.observer = null;
            removeStateIfUnused(state);
        }
    }

    private static void removeStateIfUnused(final State state) {
        if(state.entries.isEmpty() && state.observer == null) {
            STATE.remove();
        }
    }

    private static final class State {
        private final Deque<Entry> entries = new ArrayDeque<>();
        private boolean stackOverflowReported;
        private Consumer<List<Entry>> observer;
    }

    private _IntrospectionDiagnostics() {}
}
