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
package org.apache.isis.core.metamodel.spec.interaction;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.interaction.ManagedMember.MemberType;

import lombok.Data;
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
        private final List<ManagedObject> parameterList;
        private final ManagedObject actionReturnedObject;
    }

    public static final ActionInteraction start(
            @NonNull final ManagedObject owner,
            @NonNull final String memberId) {

        val managedAction = ManagedAction.lookupAction(owner, memberId);

        final _Either<ManagedAction, InteractionVeto> chain = managedAction.isPresent()
                ? _Either.left(managedAction.get())
                : _Either.right(InteractionVeto.notFound(MemberType.ACTION, memberId));

        return new ActionInteraction(chain);
    }

    ActionInteraction(@NonNull _Either<ManagedAction, InteractionVeto> chain) {
        super(chain);
    }

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

    public static interface ParameterInvalidCallback {
        void onParameterInvalid(ManagedParameter managedParameter, InteractionVeto veto);
    }
    
    
    public ActionInteraction useParameters(
            @NonNull final Function<ManagedAction, List<ManagedObject>> actionParameterProvider, 
            final ParameterInvalidCallback parameterInvalidCallback) {

        chain = chain.leftRemap(action->{
            
            state.setParameterList(actionParameterProvider.apply(action));
            
            val managedParameters = ManagedParameterList.of(action, state.getParameterList());
            
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
                    .isArgumentSetValid(action.getOwner(), state.getParameterList(), InteractionInitiatedBy.USER);
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
    
    // -- HELPER
    
    private final State state = new State();
    @Data
    private static class State {
        @NonNull private List<ManagedObject> parameterList = Collections.emptyList();
        private Result interactionResult;
    }

    

}
