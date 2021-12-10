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
package org.apache.isis.core.metamodel.interactions.managed;

import java.util.Optional;
import java.util.function.Function;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Either;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember.MemberType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember.AuthorizationException;

import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public final class ActionInteraction
extends MemberInteraction<ManagedAction, ActionInteraction> {

    public static enum SemanticConstraint {
        NONE,
        IDEMPOTENT,
        SAFE
    }

    @Value(staticConstructor = "of")
    public static class Result {
        private final ManagedAction managedAction;
        private final Can<ManagedObject> parameterList;
        private final ManagedObject actionReturnedObject;
    }

    public static final ActionInteraction start(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where) {
        return startWithMultiselect(owner, memberId, where, Can::empty);
    }

    public static final ActionInteraction startWithMultiselect(
            final @NonNull ManagedObject owner,
            final @NonNull String memberId,
            final @NonNull Where where,
            final @NonNull MultiselectChoices multiselectChoices) {

        val managedAction = ManagedAction.lookupActionWithMultiselect(owner, memberId, where, multiselectChoices);

        final _Either<ManagedAction, InteractionVeto> chain = managedAction.isPresent()
                ? _Either.left(managedAction.get())
                : _Either.right(InteractionVeto.notFound(MemberType.ACTION, memberId));

        return new ActionInteraction(
                managedAction.map(ManagedAction::getAction),
                chain);
    }

    public static ActionInteraction wrap(final @NonNull ManagedAction managedAction) {
        return new ActionInteraction(
                Optional.of(managedAction.getAction()),
                _Either.left(managedAction));
    }

    public static ActionInteraction empty(final String actionId) {
        return new ActionInteraction(
                Optional.empty(),
                _Either.right(InteractionVeto.notFound(MemberType.ACTION, actionId)));
    }

    ActionInteraction(
            final @NonNull Optional<ObjectAction> metamodel,
            final @NonNull _Either<ManagedAction, InteractionVeto> chain) {
        super(chain);
        this.metamodel = metamodel;
    }

    /**
     * optionally the action's metamodel, based on whether even exists (eg. was found by memberId)
     */
    @Getter
    private final Optional<ObjectAction> metamodel;

    public ActionInteraction checkSemanticConstraint(@NonNull final SemanticConstraint semanticConstraint) {

        chain = chain.mapIfLeft(action->{

            val actionSemantics = action.getAction().getSemantics();

            switch(semanticConstraint) {
            case NONE:
                return _Either.left(action);

            case IDEMPOTENT:
                return actionSemantics.isIdempotentInNature()
                        ? _Either.left(action)
                        : _Either.right(InteractionVeto.actionNotIdempotent(action)) ;
            case SAFE:
                return actionSemantics.isSafeInNature()
                        ? _Either.left(action)
                        : _Either.right(InteractionVeto.actionNotSafe(action));
            default:
                throw _Exceptions.unmatchedCase(semanticConstraint); // unexpected code reach
            }

        });

        return this;
    }

    public Optional<ParameterNegotiationModel> startParameterNegotiation() {
        return getManagedAction()
            .map(ManagedAction::startParameterNegotiation);
    }

    public static interface ParameterInvalidCallback {
        void onParameterInvalid(ManagedParameter managedParameter, InteractionVeto veto);
    }

    public _Either<ManagedObject, InteractionVeto> invokeWith(final ParameterNegotiationModel pendingArgs) {
        pendingArgs.activateValidationFeedback();
        val veto = validate(pendingArgs);
        if(veto.isPresent()) {
            return _Either.right(veto.get());
        }
        val action = chain.leftIfAny();
        val actionResultOrVeto = action.invoke(pendingArgs.getParamValues());
        return actionResultOrVeto;
    }

    public ManagedObject invokeWithRuleChecking(
            final ParameterNegotiationModel pendingArgs) throws AuthorizationException {
        val action = chain.leftIfAny();
        return action.invokeWithRuleChecking(pendingArgs.getParamValues());
    }

    public Optional<InteractionVeto> validate(
            final @NonNull ParameterNegotiationModel pendingArgs) {

        if(chain.isRight()) {
            return chain.right();
        }
        val validityConsent = pendingArgs.validateParameterSet(); // full validation
        if(validityConsent!=null && validityConsent.isVetoed()) {
            return Optional.of(InteractionVeto.actionParamInvalid(validityConsent));
        }
        return Optional.empty();
    }

    /**
     * @return optionally the ManagedAction based on whether there
     * was no interaction veto within the originating chain
     */
    public Optional<ManagedAction> getManagedAction() {
        return super.getManagedMember();
    }

    /**
     * @return this Interaction's ManagedAction
     * @throws X if there was any interaction veto within the originating chain
     */
    public <X extends Throwable>
    ManagedAction getManagedActionElseThrow(final Function<InteractionVeto, ? extends X> onFailure) throws X {
        return super.getManagedMemberElseThrow(onFailure);
    }

    public <X extends Throwable>
    ManagedAction getManagedActionElseFail() {
        return getManagedActionElseThrow(veto->
            _Exceptions.unrecoverable("action vetoed: " + veto.getReason()));
    }

}
