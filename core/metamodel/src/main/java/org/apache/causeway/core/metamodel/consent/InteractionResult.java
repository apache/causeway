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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.Consent.VetoReason;

public record InteractionResult(
        InteractionEvent interactionEvent,
        Optional<Consent.VetoReason> vetoReason,
        /**
         * Any {@link InteractionAdvisor} advisors that have appended veto reasons.
         */
        Can<InteractionAdvisor> advisors) {

    public static Builder builder(final InteractionEvent interactionEvent) { return new Builder(interactionEvent); }
    public record Builder(
            InteractionEvent interactionEvent,
            List<Consent.VetoReason> reasonBuf,
            List<InteractionAdvisor> advisors) {
        public Builder(InteractionEvent interactionEvent) {
            this(interactionEvent, new ArrayList<>(), new ArrayList<>());
        }
        public void addAdvise(final VetoReason reason, final InteractionAdvisor facet) {
            reasonBuf.add(Objects.requireNonNull(reason));
            advisors.add(Objects.requireNonNull(facet));
        }
        public InteractionResult build() {
            Optional<Consent.VetoReason> reason = reasonBuf.stream().reduce(Consent.VetoReason::reduce);
            return new InteractionResult(interactionEvent, reason, Can.ofCollection(advisors));
        }
    }

    // canonical constructor
    public InteractionResult(
            InteractionEvent interactionEvent,
            Optional<Consent.VetoReason> vetoReason,
            Can<InteractionAdvisor> advisors) {
        this.interactionEvent = Objects.requireNonNull(interactionEvent);
        this.vetoReason = Objects.requireNonNull(vetoReason);
        this.advisors = Objects.requireNonNull(advisors);

        vetoReason.ifPresent(reason->{
            InteractionAdvisor advisor = advisors.stream().findFirst().orElseThrow();
            interactionEvent.advised(reason.string(), advisor.getClass());
        });
    }

    public boolean isAllowing() { return vetoReason.isEmpty(); }
    public boolean isVetoing() { return vetoReason.isPresent(); }

    public Consent createConsent() {
        return isAllowing()
            ? new Allow(this)
            : new Veto(this);
    }

    @Override
    public String toString() {
        return String.format("%s: %s (%d facets advised)",
                interactionEvent, isAllowing() ? "allowed" : "vetoed", advisors.size());
    }

}
