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
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember.MemberType;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.val;

public final class ActionInteraction extends MemberInteraction<ManagedAction, ActionInteraction> {

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
            @NonNull final ManagedObject owner,
            @NonNull final String memberId) {

        val managedAction = ManagedAction.lookupAction(owner, memberId);

        final _Either<ManagedAction, InteractionVeto> chain = managedAction.isPresent()
                ? _Either.left(managedAction.get())
                : _Either.right(InteractionVeto.notFound(MemberType.ACTION, memberId));

        return new ActionInteraction(
                managedAction.map(ManagedAction::getAction),
                chain);
    }

    ActionInteraction(
            @NonNull final Optional<ObjectAction> metamodel, 
            @NonNull final _Either<ManagedAction, InteractionVeto> chain) {
        super(chain);
        this.metamodel = metamodel;
    }
    
    /**
     * optionally the action's metamodel, based on whether even exists (eg. was found by memberId) 
     */
    @Getter
    private final Optional<ObjectAction> metamodel;
    
    public ActionInteraction checkSemanticConstraint(@NonNull SemanticConstraint semanticConstraint) {

        chain = chain.leftRemap(action->{

            val actionSemantics = action.getAction().getSemantics();

            switch(semanticConstraint) {
            case NONE:
                return _Either.left(action);

            case IDEMPOTENT:
                return (! actionSemantics.isIdempotentInNature()) 
                        ? _Either.right(InteractionVeto.actionNotIdempotent(action)) 
                        : _Either.left(action);
            case SAFE:
                return (! actionSemantics.isSafeInNature()) 
                        ? _Either.right(InteractionVeto.actionNotSafe(action)) 
                        : _Either.left(action);
            default:
                throw _Exceptions.unmatchedCase(semanticConstraint); // unexpected code reach
            }

        });

        return this;
    }

    public ActionInteraction startParameterNegotiation(@NonNull Consumer<ParameterNegotiationModel> onModel) {
        getManagedAction()
            .map(ManagedAction::startParameterNegotiation)
            .ifPresent(onModel);
        return this;
    }
    
    public static interface ParameterInvalidCallback {
        void onParameterInvalid(ManagedParameter2 managedParameter, InteractionVeto veto);
    }
    
    public _Either<ManagedObject, InteractionVeto> invokeWith(ParameterNegotiationModel pendingArgs) {
        pendingArgs.activateValidationFeedback();
        if(chain.isRight()) {
            return _Either.right(chain.rightIfAny());
        }
        val validityConsent = pendingArgs.validateParameterSet();
        if(validityConsent!=null && validityConsent.isVetoed()) {
            return _Either.right(InteractionVeto.actionParamInvalid(validityConsent));
        }
        val action = chain.leftIfAny();
        val actionResultOrVeto = action.invoke(pendingArgs.getParamValues());
        return actionResultOrVeto;
    }
    
    /**
     * @deprecated TODO we might rather make validation the responsibility of the {@link ParameterNegotiationModel}
     * and then maybe provide something like {@code invokeWith(ParameterNegotiationModel model, ... callback)} here
     */
    public ActionInteraction useParameters(
            @NonNull final Function<ManagedAction, Can<ManagedObject>> actionParameterProvider, 
            final ParameterInvalidCallback parameterInvalidCallback) {

        chain = chain.leftRemap(action->{
            
            state.setParameterList(actionParameterProvider.apply(action));
            
            val managedParameters = ManagedParameterList.ofValues(action, state.getParameterList());
            
            boolean invalid = false;
            for(val managedParameter : managedParameters) {
                // validate each individual argument
                val veto = managedParameter.validate();
                if(veto.isPresent()) {
                    invalid = true;
                    if(parameterInvalidCallback!=null) {
                        parameterInvalidCallback.onParameterInvalid(managedParameter, veto.get());
                    }
                }
            }
            
            if(invalid) {
                //TODO veto
            }
            
            // validate entire param-list
            val validityVeto = action.getAction()
                    .isArgumentSetValidForAction(action.getInteractionHead(), state.getParameterList(), InteractionInitiatedBy.USER);
            return validityVeto.isVetoed()
                    ? _Either.right(InteractionVeto.actionParamInvalid(validityVeto)) 
                    : _Either.left(action); 
                    
        });
        return this;
    }

    public <X extends Throwable> 
    Result getResultElseThrow(Function<InteractionVeto, ? extends X> onFailure) throws X {
        
        chain = chain.leftRemap(action->{
            val actionResultOrVeto = action.invoke(state.getParameterList());
            
            if(actionResultOrVeto.isLeft()) {
                val actionResult = actionResultOrVeto.leftIfAny();
                state.setInteractionResult(Result.of(action, state.getParameterList(), actionResult));
                return _Either.left(action);
            } else {
                return _Either.right(actionResultOrVeto.rightIfAny());
            }
            
        });
        
        if (chain.isLeft()) {
            return state.getInteractionResult();
        } else {
            throw onFailure.apply(chain.rightIfAny());
        }
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
    ManagedAction getManagedActionElseThrow(Function<InteractionVeto, ? extends X> onFailure) throws X {
        return super.getManagedMemberElseThrow(onFailure);
    }
    
    // -- HELPER
    
    private final State state = new State();
    @Data
    private static class State {
        @NonNull private Can<ManagedObject> parameterList = Can.empty();
        private Result interactionResult;
    }
    

}
