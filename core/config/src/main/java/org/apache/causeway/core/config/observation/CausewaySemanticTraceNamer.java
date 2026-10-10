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

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationFilter;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;

import lombok.RequiredArgsConstructor;

/**
 * Coordinates bounded semantic names for existing foreground entry spans.
 *
 * <p>This is an internal integration shared by trusted Causeway instrumentation.
 * It deliberately does not expose trace naming as an application API.</p>
 *
 * @since 4.0.0
 */
public final class CausewaySemanticTraceNamer {

    public static final String TRACE_NAME_ATTRIBUTE = "causeway.trace.name";
    public static final String ACTION_ID_ATTRIBUTE = "causeway.action.id";
    public static final String OBJECT_TYPE_ATTRIBUTE = "causeway.object.type";

    private static final ThreadLocal<Deque<State>> ACTIVE_SCOPES =
            new ThreadLocal<>();

    @RequiredArgsConstructor
    private enum CandidateKind {
        VIEW(1, "view", OBJECT_TYPE_ATTRIBUTE),
        PROMPT(2, "prompt", ACTION_ID_ATTRIBUTE),
        ACTION(3, "act", ACTION_ID_ATTRIBUTE);

        private final int priority;
        private final String operation;
        private final String canonicalAttribute;
    }

    private final Tracer tracer;
    private final ObservationRegistry registry;
    private static final Object SELECTED_NAME = new Object();

    public CausewaySemanticTraceNamer(final Tracer tracer) {
        this(tracer, ObservationRegistry.NOOP);
    }

    public CausewaySemanticTraceNamer(final Tracer tracer, final ObservationRegistry registry) {
        this.tracer = Objects.requireNonNull(tracer, "tracer");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * Opens request-local nomination state for the current recording span.
     */
    public Scope openCurrentSpan() {
        var observation = registry.getCurrentObservation();
        return open(tracer.currentSpan(), observation == null ? null : observation.getContext());
    }

    /**
     * Opens nomination state for the supplied span.
     * Intended for framework integration and deterministic tests.
     */
    public static Scope open(final Span span) {
        return open(span, null);
    }

    private static Scope open(final Span span, final Observation.Context entryContext) {
        // An inert frame still shields any enclosing request from nominations
        // made while a different/inactive tracing context is selected.
        final State state = new State(span == null || span.isNoop() ? null : span, entryContext);
        Deque<State> scopes = ACTIVE_SCOPES.get();
        if(scopes == null) {
            scopes = new ArrayDeque<>();
            ACTIVE_SCOPES.set(scopes);
        }
        scopes.push(state);
        return new Scope(state);
    }

    public static void nominateAction(final String canonicalActionIdentifier) {
        nominate(
                CandidateKind.ACTION,
                withoutSignature(canonicalActionIdentifier),
                canonicalActionIdentifier);
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

    private static String withoutSignature(final String identifier) {
        Objects.requireNonNull(identifier, "identifier");
        final int parameters = identifier.indexOf('(');
        return parameters >= 0 ? identifier.substring(0, parameters) : identifier;
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
        private final Observation.Context entryContext;
        private Candidate selected;

        private void nominate(final Candidate candidate) {
            if(selected == null
                    || candidate.kind.priority > selected.kind.priority) {
                selected = candidate;
            }
        }

        private void apply() {
            if(span == null || selected == null) {
                return;
            }
            if (entryContext != null) entryContext.put(SELECTED_NAME, selected.contextualName);
            span.name(selected.contextualName);
            span.tag(TRACE_NAME_ATTRIBUTE, selected.contextualName);
            span.tag(
                    selected.kind.canonicalAttribute,
                    selected.canonicalIdentifier);
        }
    }

    /**
     * Boot's receiver handler applies its name at stop, after conventions have
     * refreshed the HTTP context. Retain the selected display at that point;
     * the operation name and all HTTP key values remain untouched.
     */
    public static ObservationFilter retainSelectedName() {
        return context -> {
            String selectedName = context.get(SELECTED_NAME);
            if (selectedName != null) context.setContextualName(selectedName);
            return context;
        };
    }

    /**
     * Owns one request-local semantic naming scope.
     */
    public static final class Scope implements AutoCloseable {
        private final State state;
        private boolean closed;

        private Scope(final State state) {
            this.state = state;
        }

        @Override
        public void close() {
            if(closed) {
                return;
            }
            final Deque<State> scopes = ACTIVE_SCOPES.get();
            if(scopes == null || scopes.isEmpty() || scopes.peek() != state) {
                throw new IllegalStateException(
                        "Semantic trace naming scopes must close in reverse order");
            }
            // Remove request state before touching the tracing bridge: even an
            // export/bridge failure must not contaminate the next request.
            scopes.pop();
            if(scopes.isEmpty()) {
                ACTIVE_SCOPES.remove();
            }
            closed = true;
            state.apply();
        }
    }
}
