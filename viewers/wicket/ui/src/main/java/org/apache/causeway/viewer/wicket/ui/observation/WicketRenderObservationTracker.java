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
package org.apache.causeway.viewer.wicket.ui.observation;

import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.request.cycle.RequestCycle;

import org.springframework.lang.Nullable;

import org.apache.causeway.core.config.observation.ObservationClosure;

/**
 * Request-local ownership of render observations whose normal Wicket completion callback can be skipped.
 *
 * @since 2.2
 */
public final class WicketRenderObservationTracker {

    private WicketRenderObservationTracker() {}

    private static final MetaDataKey<State> STATE_KEY = new MetaDataKey<State>() {
        private static final long serialVersionUID = 1L;
    };

    static void register(
            final RequestCycle requestCycle,
            final WicketRenderObservationBehavior owner,
            final ObservationClosure closure) {
        state(requestCycle).register(owner, closure);
    }

    static void complete(
            final RequestCycle requestCycle,
            final WicketRenderObservationBehavior owner) {
        final State state = requestCycle.getMetaData(STATE_KEY);
        if(state == null) {
            owner.clearActiveClosure();
            return;
        }
        state.complete(owner);
        if(state.isEmpty()) {
            requestCycle.setMetaData(STATE_KEY, null);
        }
    }

    public static void cleanup(
            final RequestCycle requestCycle,
            final @Nullable Throwable failure) {
        final State state = requestCycle.getMetaData(STATE_KEY);
        requestCycle.setMetaData(STATE_KEY, null);
        if(state != null) {
            state.cleanup(failure);
        }
    }

    static State state(final RequestCycle requestCycle) {
        State state = requestCycle.getMetaData(STATE_KEY);
        if(state == null) {
            state = new State();
            requestCycle.setMetaData(STATE_KEY, state);
        }
        return state;
    }

    static final class State {

        private final Deque<ActiveObservation> activeObservations = new ArrayDeque<>();

        void register(
                final WicketRenderObservationBehavior owner,
                final ObservationClosure closure) {
            activeObservations.addLast(new ActiveObservation(owner, closure));
        }

        void complete(final WicketRenderObservationBehavior owner) {
            ActiveObservation matching = null;
            for (ActiveObservation active : activeObservations) {
                if(active.owner == owner) {
                    matching = active;
                }
            }
            if(matching != null) {
                activeObservations.remove(matching);
                matching.close(null);
            } else {
                owner.clearActiveClosure();
            }
        }

        void cleanup(final @Nullable Throwable failure) {
            while(!activeObservations.isEmpty()) {
                activeObservations.removeLast().close(failure);
            }
        }

        boolean isEmpty() {
            return activeObservations.isEmpty();
        }
    }

    private static final class ActiveObservation {

        private final WicketRenderObservationBehavior owner;
        private final ObservationClosure closure;

        private ActiveObservation(
                final WicketRenderObservationBehavior owner,
                final ObservationClosure closure) {
            this.owner = owner;
            this.closure = closure;
        }

        private void close(final @Nullable Throwable failure) {
            try {
                closure.onError(failure);
            } finally {
                try {
                    closure.close();
                } finally {
                    owner.clearActiveClosure();
                }
            }
        }
    }
}
