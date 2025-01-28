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

import java.util.List;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;

import lombok.Getter;
import org.jspecify.annotations.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ActionInteractionHead
extends InteractionHead
implements HasMetaModel<ObjectAction> {

    @Getter(onMethod = @__(@Override))
    @NonNull private final ObjectAction metaModel;
    @Getter private final MultiselectChoices multiselectChoices;

    public static ActionInteractionHead of(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target) {
        return new ActionInteractionHead(objectAction, owner, target, Can::empty);
    }

    public static ActionInteractionHead of(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target,
            final @NonNull MultiselectChoices multiselectChoices) {
        return new ActionInteractionHead(objectAction, owner, target, multiselectChoices);
    }

    protected ActionInteractionHead(
            final @NonNull ObjectAction objectAction,
            final @NonNull ManagedObject owner,
            final @NonNull ManagedObject target,
            final @NonNull MultiselectChoices multiselectChoices) {
        super(owner, target);
        this.metaModel = objectAction;
        this.multiselectChoices = multiselectChoices;
    }

    /**
     * Immutable tuple of ManagedObjects, each representing {@code null} and each holding
     * the corresponding parameter's {@code ObjectSpecification}.
     * <p>
     * The size of the tuple corresponds to the number of parameters.
     */
    public Can<ManagedObject> getEmptyParameterValues() {
        return getMetaModel().getParameters().stream()
            .map(objectActionParameter->
                ManagedObject.empty(objectActionParameter.getElementType()))
            .collect(Can.toCan());
    }

    /**
     * Immutable tuple of ManagedObjects, wrapping the passed in argument pojos.
     * Nulls are allowed as arguments, but the list size must match the expected parameter count.
     * <p>
     * The size of the tuple corresponds to the number of parameters.
     * @param pojoArgList - argument pojos
     */
    public Can<ManagedObject> getPopulatedParameterValues(@Nullable final List<Object> pojoArgList) {
        var params = getMetaModel().getParameters();

        _Assert.assertEquals(params.size(), _NullSafe.size(pojoArgList));

        if(params.isEmpty()) return Can.empty();
        
        return params.zipMap(pojoArgList, (objectActionParameter, argPojo)->
            ManagedObject.adaptParameter(objectActionParameter, argPojo));
    }

    public ParameterNegotiationModel emptyModel(final ManagedAction managedAction) {
        return ParameterNegotiationModel.of(managedAction, getEmptyParameterValues());
    }

    /**
     * See step 1 'Fill in defaults' in
     * <a href="https://cwiki.apache.org/confluence/display/CAUSEWAY/ActionParameterNegotiation">
     * ActionParameterNegotiation (wiki)
     * </a>
     */
    public ParameterNegotiationModel defaults(final ManagedAction managedAction) {

        // init with empty values
        var pendingParamModel = ParameterNegotiationModel.of(managedAction, getEmptyParameterValues());

        // fill in the parameter defaults with a single sweep through all default providing methods in order, 
        // updating the pendingParamModel at each iteration
        for(var param : getMetaModel().getParameters()) {
            pendingParamModel = pendingParamModel
                .withParamValue(param.getParameterIndex(), param.getDefault(pendingParamModel));
        }
        
        return pendingParamModel;
    }

}
