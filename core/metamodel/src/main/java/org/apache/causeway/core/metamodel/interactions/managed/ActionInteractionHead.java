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
package org.apache.causeway.core.metamodel.interactions.managed;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.extern.log4j.Log4j2;

@Log4j2
public record ActionInteractionHead(
    @NonNull InteractionHeadRecord interactionHeadRecord,
    @NonNull ObjectAction objectAction,
    @NonNull MultiselectChoices multiselectChoices)
implements InteractionHead, HasMetaModel<ObjectAction> {

    public static ActionInteractionHead of(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target) {
        return new ActionInteractionHead(new InteractionHeadRecord(owner, target), objectAction, Can::empty);
    }

    public static ActionInteractionHead of(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target,
            final @NonNull MultiselectChoices multiselectChoices) {
        return new ActionInteractionHead(new InteractionHeadRecord(owner, target), objectAction, multiselectChoices);
    }
    
    @Override public ObjectAction getMetaModel() { return objectAction(); }
    @Override public ManagedObject owner() { return interactionHeadRecord.owner(); }
    @Override public ManagedObject target() { return interactionHeadRecord.target(); }

    /**
     * See step 1 'Fill in defaults' in
     * <a href="https://cwiki.apache.org/confluence/display/CAUSEWAY/ActionParameterNegotiation">
     * ActionParameterNegotiation (wiki)
     * </a>
     */
    public ParameterNegotiationModel defaults(final ManagedAction managedAction) {

        // init with empty values
        var pendingParamModel = ParameterNegotiationModel.of(managedAction, emptyParameterValues());

        // fill in the parameter defaults with a single sweep through all default providing methods in order, 
        // updating the pendingParamModel at each iteration
        for(var param : getMetaModel().getParameters()) {
            pendingParamModel = pendingParamModel
                .withParamValue(param.getParameterIndex(), param.getDefault(pendingParamModel));
        }
        
        return pendingParamModel;
    }    
    
    // -- HELPER

    /**
     * Immutable tuple of ManagedObjects, each representing {@code null} and each holding
     * the corresponding parameter's {@code ObjectSpecification}.
     * <p>
     * The size of the tuple corresponds to the number of parameters.
     */
    private Can<ManagedObject> emptyParameterValues() {
        return getMetaModel().getParameters().stream()
            .map(objectActionParameter->
                ManagedObject.empty(objectActionParameter.getElementType()))
            .collect(Can.toCan());
    }

}
