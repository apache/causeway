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

import org.apache.isis.core.commons.internal.base._Either;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.interaction.ManagedMember.MemberType;

import lombok.NonNull;
import lombok.val;

public final class ActionInteraction extends MemberInteraction<ManagedAction, ActionInteraction> {

    public static enum SemanticConstraint {
        NONE,
        IDEMPOTENT,
        SAFE
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


//    public PropertyHandle modifyProperty(
//            @NonNull final Function<ManagedProperty, ManagedObject> newProperyValueProvider) {
//
//        chain = chain.leftRemap(property->{
//            val validityVeto = property.modifyProperty(newProperyValueProvider.apply(property));
//            return validityVeto.isPresent()
//                ? _Either.right(validityVeto.get()) 
//                : _Either.left(property); 
//        });
//        return this;
//    }

    
}
