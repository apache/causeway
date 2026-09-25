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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import io.opentelemetry.api.trace.Span;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Coordinates bounded semantic names for Java-agent-owned foreground entry spans.
 *
 * <p>This is an internal integration shared by trusted Causeway instrumentation.
 * It deliberately does not expose trace naming as an application API.</p>
 *
 * @since 2.2
 */
public final class CausewaySemanticTraceNamer {

    public static final String TRACE_NAME_ATTRIBUTE = "causeway.trace.name";
    public static final String ACTION_ID_ATTRIBUTE = "causeway.action.id";
    public static final String OBJECT_TYPE_ATTRIBUTE = "causeway.object.type";

    private static final ThreadLocal<Deque<State>> ACTIVE_SCOPES =
            new ThreadLocal<>();

    @Getter
    @RequiredArgsConstructor
    public enum CandidateKind {
        VIEW(1, "view", OBJECT_TYPE_ATTRIBUTE),
        PROMPT(2, "prompt", ACTION_ID_ATTRIBUTE),
        ACTION(3, "act", ACTION_ID_ATTRIBUTE);

        private final int priority;
        private final String operation;
        private final String canonicalAttribute;
    }

    private CausewaySemanticTraceNamer() {
    }

    /**
     * Opens request-local nomination state for the current recording span.
     */
    public static Scope openCurrentSpan() {
        return open(Span.current());
    }

    /**
     * Opens nomination state for the supplied span.
     * Intended for framework integration and deterministic tests.
     */
    public static Scope open(final Span span) {
        Objects.requireNonNull(span, "span");
        if(!span.isRecording()) {
            return Scope.noop();
        }
        final State state = new State(span);
        Deque<State> scopes = ACTIVE_SCOPES.get();
        if(scopes == null) {
            scopes = new ArrayDeque<>();
            ACTIVE_SCOPES.set(scopes);
        }
        scopes.push(state);
        return new Scope(state);
    }

    public static void nominateAction(final String logicalMemberIdentifier) {
        nominate(
                CandidateKind.ACTION,
                logicalMemberIdentifier,
                canonicalActionIdentifier(logicalMemberIdentifier));
    }

    public static void nominatePrompt(
            final String logicalMemberIdentifier,
            final String canonicalActionIdentifier) {
        nominate(
                CandidateKind.PROMPT,
                logicalMemberIdentifier,
                canonicalActionIdentifier);
    }

    public static void nominateView(final String logicalTypeName) {
        nominate(CandidateKind.VIEW, logicalTypeName, logicalTypeName);
    }

    private static void nominate(
            final CandidateKind kind,
            final String displayIdentifier,
            final String canonicalIdentifier) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(displayIdentifier, "displayIdentifier");
        Objects.requireNonNull(canonicalIdentifier, "canonicalIdentifier");
        final Deque<State> scopes = ACTIVE_SCOPES.get();
        if(scopes == null || scopes.isEmpty()) {
            return;
        }
        scopes.peek().nominate(Candidate.of(
                kind, displayIdentifier, canonicalIdentifier));
    }

    private static String canonicalActionIdentifier(
            final String logicalMemberIdentifier) {
        Objects.requireNonNull(logicalMemberIdentifier, "logicalMemberIdentifier");
        return logicalMemberIdentifier.endsWith(")")
                ? logicalMemberIdentifier
                : logicalMemberIdentifier + "()";
    }

    private static String contextualName(
            final CandidateKind kind,
            final String canonicalIdentifier) {
        return kind == CandidateKind.VIEW
                ? CausewayObservationNaming.forType(kind.operation, canonicalIdentifier)
                : CausewayObservationNaming.forLogicalMember(kind.operation, canonicalIdentifier);
    }

    @RequiredArgsConstructor
    private static final class Candidate {
        private final CandidateKind kind;
        private final String canonicalIdentifier;
        private final String contextualName;

        private static Candidate of(
                final CandidateKind kind,
                final String displayIdentifier,
                final String canonicalIdentifier) {
            return new Candidate(
                    kind,
                    canonicalIdentifier,
                    contextualName(kind, displayIdentifier));
        }
    }

    @RequiredArgsConstructor
    private static final class State {
        private final Span span;
        private Candidate selected;

        private void nominate(final Candidate candidate) {
            if(selected == null
                    || candidate.kind.priority > selected.kind.priority) {
                selected = candidate;
            }
        }

        private void apply() {
            if(selected == null) {
                return;
            }
            span.updateName(selected.contextualName);
            span.setAttribute(TRACE_NAME_ATTRIBUTE, selected.contextualName);
            span.setAttribute(
                    selected.kind.canonicalAttribute,
                    selected.canonicalIdentifier);
        }
    }

    /**
     * Owns one request-local semantic naming scope.
     */
    public static class Scope implements AutoCloseable {
        private final State state;
        private boolean closed;

        private Scope(final State state) {
            this.state = state;
        }

        private static Scope noop() {
            return new Scope(null);
        }

        @Override
        public void close() {
            if(closed) {
                return;
            }
            if(state == null) {
                closed = true;
                return;
            }

            final Deque<State> scopes = ACTIVE_SCOPES.get();
            if(scopes == null || scopes.isEmpty() || scopes.peek() != state) {
                throw new IllegalStateException(
                        "Semantic trace naming scopes must close in reverse order");
            }
            scopes.pop();
            if(scopes.isEmpty()) {
                ACTIVE_SCOPES.remove();
            }
            closed = true;
            state.apply();
        }
    }
}
