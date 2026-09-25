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
package org.apache.causeway.applib.services.iactn;

import java.util.List;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.schema.common.v2.InteractionType;
import org.apache.causeway.schema.ixn.v2.ActionInvocationDto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Represents an action method execution in an {@link Interaction} execution graph.
 *
 * @since 1.x {@index}
 */
public class ActionInvocation
extends Execution<ActionInvocationDto, ActionDomainEvent<?>> {

    /**
     * Whether Causeway's visibility, usability and validity rules were checked
     * before this action invocation.
     *
     * @since 2.x
     */
    @RequiredArgsConstructor
    public enum RuleChecking {
        /** Causeway checked visibility, usability and validity rules. */
        CHECKED(true),
        /** Causeway intentionally skipped rule checking. */
        SKIPPED(false),
        /** Rule-checking status was not supplied when the invocation was created. */
        UNKNOWN(false);

        private final boolean checked;

        /**
         * Reports whether rules were checked.
         *
         * @return whether Causeway checked rules before invoking the action
         */
        public boolean isChecked() {
            return checked;
        }
    }

    @Getter
    private final List<Object> args;

    /**
     * The domain-facing logical action identifier.
     *
     * <p>
     * This differs from {@link #getLogicalMemberIdentifier()} for a mixin action:
     * the inherited identifier describes the mixin implementation method, typically
     * {@code act}, while this identifier describes the contributed domain action.
     * </p>
     *
     * @since 2.x
     */
    @Getter
    private final Identifier domainFacingLogicalMemberIdentifier;

    @Getter
    private final RuleChecking ruleChecking;

    /**
     * Creates an action invocation whose rule-checking status is not known,
     * for example when reconstructing an invocation from serialized history.
     *
     * @param interaction owning interaction
     * @param memberId logical action identifier
     * @param target action method receiver
     * @param args action arguments
     */
    public ActionInvocation(
            final Interaction interaction,
            final Identifier memberId,
            final Object target,
            final List<Object> args) {
        this(interaction, memberId, memberId, target, args, RuleChecking.UNKNOWN);
    }

    /**
     * Creates an action invocation with explicit rule-checking status.
     *
     * @param interaction owning interaction
     * @param memberId logical action identifier
     * @param target action method receiver
     * @param args action arguments
     * @param ruleChecking whether Causeway checked rules before invoking the action
     */
    public ActionInvocation(
            final Interaction interaction,
            final Identifier memberId,
            final Object target,
            final List<Object> args,
            final @NonNull RuleChecking ruleChecking) {
        this(interaction, memberId, memberId, target, args, ruleChecking);
    }

    /**
     * Creates an action invocation with separate invoked-method and domain-facing identifiers.
     *
     * @param interaction owning interaction
     * @param memberId logical identifier of the method invoked on {@code target}
     * @param domainFacingLogicalMemberIdentifier domain-facing logical action identifier
     * @param target action method receiver
     * @param args action arguments
     * @param ruleChecking whether Causeway checked rules before invoking the action
     */
    public ActionInvocation(
            final Interaction interaction,
            final Identifier memberId,
            final @NonNull Identifier domainFacingLogicalMemberIdentifier,
            final Object target,
            final List<Object> args,
            final @NonNull RuleChecking ruleChecking) {
        super(interaction, InteractionType.ACTION_INVOCATION, memberId, target);
        this.args = args;
        this.domainFacingLogicalMemberIdentifier = domainFacingLogicalMemberIdentifier;
        this.ruleChecking = ruleChecking;
    }
    // ...
}
