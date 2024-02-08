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
package org.apache.causeway.core.metamodel.consent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;

public class InteractionResult {

    /**
     * Initially {@link #ADVISING}; when call
     * {@link InteractionResult#getInteractionEvent()}, flips over into
     * {@link #ADVISED}.
     *
     * <p>
     * Subsequent attempts to
     * {@link InteractionResult#advise(String, InteractionAdvisor)} will then be
     * disallowed.
     */
    enum State {
        ADVISING, ADVISED
    }

    private final InteractionEvent interactionEvent;
    private final List<Consent.VetoReason> reasonBuf = new ArrayList<>();
    private final List<InteractionAdvisor> advisors = new ArrayList<InteractionAdvisor>();

    private State state = State.ADVISING;

    public InteractionResult(final InteractionEvent interactionEvent) {
        this.interactionEvent = interactionEvent;
    }

    /**
     * Returns the contained {@link InteractionEvent}, if necessary updated with
     * the {@link #advise(org.apache.causeway.core.metamodel.consent.Consent.VetoReason, InteractionAdvisor) advice} of the
     * interactions.
     *
     * <p>
     * That is, if still {@link State#ADVISING advising}, then copies over the
     * details from this result into the contained {@link InteractionEvent}, and
     * flips into {@link State#ADVISED advised (done)}.
     */
    public InteractionEvent getInteractionEvent() {
        if (state == State.ADVISING) {
            final String nullableReasonString = getReason().map(VetoReason::string).orElse(null);
            interactionEvent.advised(nullableReasonString, getAdvisorClass());
            state = State.ADVISED;
        }
        return interactionEvent;
    }

    private Class<?> getAdvisorClass() {
        final InteractionAdvisor advisor = getAdvisor();
        return advisor != null ? advisor.getClass() : null;
    }

    public void advise(final Consent.VetoReason reason, final InteractionAdvisor facet) {
        if (state == State.ADVISED) {
            throw new IllegalStateException("Cannot append since have called getInteractionEvent");
        }
        if (reason == null) {
            return;
        }
        advisors.add(facet);
        reasonBuf.add(reason);
    }

    public boolean isVetoing() {
        return !isNotVetoing();
    }

    public boolean isNotVetoing() {
        return reasonBuf.size() == 0;
    }

    /**
     * Returns the first of the {@link #getAdvisorFacets()} that has been
     * {@link #advise(org.apache.causeway.core.metamodel.consent.Consent.VetoReason, InteractionAdvisor) advised} , or <tt>null</tt> if
     * none yet.
     *
     * @see #getAdvisorFacets()
     */
    public InteractionAdvisor getAdvisor() {
        return advisors.size() >= 1 ? advisors.get(0) : null;
    }

    /**
     * Returns all {@link InteractionAdvisor advisor} (facet)s that have
     * {@link #advise(org.apache.causeway.core.metamodel.consent.Consent.VetoReason, InteractionAdvisor) append}ed reasons to the
     * buffer.
     *
     * @see #getAdvisor()
     */
    public List<InteractionAdvisor> getAdvisorFacets() {
        return Collections.unmodifiableList(advisors);
    }

    public Consent createConsent() {
        if (isNotVetoing()) {
            return new Allow(this);
        } else {
            return new Veto(this);
        }
    }

    /**
     * Gets the reason as currently known, but does not change the state.
     * <p>
     * If {@link #isNotVetoing()}, then returns <tt>Optional.empty()</tt>.
     */
    public Optional<Consent.VetoReason> getReason() {
        return reasonBuf.stream().reduce(Consent.VetoReason::reduce);
    }

    @Override
    public String toString() {
        return String.format("%s: %s: %s (%d facets advised)",
                interactionEvent, state, toStringInterpret(), advisors.size());
    }

    private String toStringInterpret() {
        return isNotVetoing()
                ? "allowed"
                : "vetoed";
    }

}
