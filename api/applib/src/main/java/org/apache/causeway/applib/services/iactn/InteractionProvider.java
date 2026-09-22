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

import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.internal.debug._Probe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * Provides the current thread's {@link Interaction}.
 *
 * <p>
 * An {@link Interaction}  contains a top-level {@link Execution}
 * representing the invocation of an action or the editing of a property.
 * If that top-level action or property uses the
 * {@link org.apache.causeway.applib.services.wrapper.WrapperFactory} domain
 * service to invoke child actions/properties, then those sub-executions are
 * captured as a call-graph. The {@link Execution} is thus a
 * graph structure.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface InteractionProvider {

    /**
     * Whether there is a currently active {@link Interaction} for the calling thread.
     */
    boolean isInInteraction();

    /**
     * Optionally, the currently active {@link Interaction} for the calling thread.
     */
    Optional<Interaction> currentInteraction();

    /**
     * Optionally, the currently active {@link org.apache.causeway.applib.services.iactnlayer.InteractionContext} for the calling thread.
     */
    Optional<InteractionContext> currentInteractionContext();

    /**
     * The current action invocation, if the current interaction execution is an action.
     *
     * <p>
     * This describes execution through Causeway's action invocation machinery.
     * Inspect the returned {@link ActionInvocation}'s rule-checking status to determine
     * whether visibility, usability and validity rules were checked or skipped.
     * </p>
     *
     * @return the current action invocation, if any
     * @since 2.x
     */
    default Optional<ActionInvocation> currentActionInvocation() {
        return currentInteraction()
                .map(Interaction::getCurrentExecution)
                .filter(ActionInvocation.class::isInstance)
                .map(ActionInvocation.class::cast);
    }

    /**
     * Whether the current action invocation is for the identical {@code target} and
     * has the supplied logical member {@code identifier}.
     *
     * <p>
     * Target comparison deliberately uses object identity rather than
     * {@link Object#equals(Object)}.
     * For a mixin action the target is the transient mixin instance on which
     * {@code act} is invoked, not the mixed-in domain object.
     * </p>
     *
     * @param target action method receiver
     * @param identifier full logical action identifier
     * @return whether the target and identifier match the current action invocation
     * @since 2.x
     */
    default boolean isCurrentActionInvocation(
            final @NonNull Object target,
            final @NonNull Identifier identifier) {
        return currentActionInvocation()
                .filter(actionInvocation -> actionInvocation.getTarget() == target)
                .map(ActionInvocation::getLogicalMemberIdentifier)
                .filter(identifier::equals)
                .isPresent();
    }

    /**
     * Whether the current action invocation is for the identical {@code target} and
     * has the supplied logical {@code memberName}.
     *
     * <p>
     * This convenience form does not distinguish overloaded action identifiers.
     * Use {@link #isCurrentActionInvocation(Object, Identifier)} when the full action
     * identity is required.
     * For a mixin action, callers typically supply {@code this} as the target and
     * {@code "act"} as the member name.
     * </p>
     *
     * @param target action method receiver
     * @param memberName logical member name
     * @return whether the target and member name match the current action invocation
     * @since 2.x
     */
    default boolean isCurrentActionInvocation(
            final @NonNull Object target,
            final @NonNull String memberName) {
        return currentActionInvocation()
                .filter(actionInvocation -> actionInvocation.getTarget() == target)
                .map(ActionInvocation::getLogicalMemberIdentifier)
                .map(Identifier::memberLogicalName)
                .filter(memberName::equals)
                .isPresent();
    }

    /**
     * Whether the current action invocation matches the supplied target,
     * identifier and rule-checking status.
     *
     * @param target action method receiver
     * @param identifier full logical action identifier
     * @param ruleChecking expected rule-checking status
     * @return whether all supplied characteristics match the current action invocation
     * @since 2.x
     */
    default boolean isCurrentActionInvocation(
            final @NonNull Object target,
            final @NonNull Identifier identifier,
            final @NonNull ActionInvocation.RuleChecking ruleChecking) {
        return isCurrentActionInvocation(target, identifier)
                && currentActionInvocation()
                        .map(ActionInvocation::getRuleChecking)
                        .filter(ruleChecking::equals)
                        .isPresent();
    }

    /**
     * Whether the current action invocation matches the supplied target,
     * logical member name and rule-checking status.
     *
     * @param target action method receiver
     * @param memberName logical member name
     * @param ruleChecking expected rule-checking status
     * @return whether all supplied characteristics match the current action invocation
     * @since 2.x
     */
    default boolean isCurrentActionInvocation(
            final @NonNull Object target,
            final @NonNull String memberName,
            final @NonNull ActionInvocation.RuleChecking ruleChecking) {
        return isCurrentActionInvocation(target, memberName)
                && currentActionInvocation()
                        .map(ActionInvocation::getRuleChecking)
                        .filter(ruleChecking::equals)
                        .isPresent();
    }

    /**
     * Unique id of the current request- or test-scoped {@link Interaction}.
     */
    Optional<UUID> getInteractionId();

    /**
     * interaction-layer-stack size
     * */
    int getInteractionLayerCount();

    // -- SHORTCUTS

    default Interaction currentInteractionElseFail() {
    	return currentInteraction()
    	        .orElseThrow(()->
    	            _Exceptions
    	                .illegalState(
    	                        "no InteractionLayer available on current thread %s",
    	                        _Probe.currentThreadId()));
    }

    default InteractionContext currentInteractionContextElseFail() {
        return currentInteractionContext()
                .orElseThrow(()->
                    _Exceptions.illegalState(
                            "no InteractionLayer available on current thread %s",
                            _Probe.currentThreadId()));
    }


}
