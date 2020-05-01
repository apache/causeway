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
package org.apache.isis.core.metamodel.specloader.specimpl;

import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

public abstract class ObjectActionParameterMixedInAbstract
extends ObjectActionParameterAbstract
implements ObjectActionParameterMixedIn {

    private final ObjectActionMixedIn mixedInAction;

    public ObjectActionParameterMixedInAbstract(
            final FeatureType featureType, 
            final ObjectActionParameterAbstract mixinParameter,
            final ObjectActionMixedIn mixedInAction) {
        
        super(featureType, mixinParameter.getNumber(), mixedInAction, mixinParameter.getPeer());
        this.mixedInAction = mixedInAction;
    }

    @Override
    public ActionArgValidityContext createProposedArgumentInteractionContext(
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> proposedArguments,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        val targetObject = mixedInAction.mixinAdapterFor(mixedInAdapter);

        val actionArgValidityContext = new ActionArgValidityContext(
                targetObject, mixedInAction.mixinAction, getIdentifier(), 
                proposedArguments, position, interactionInitiatedBy);
        actionArgValidityContext.setMixedIn(mixedInAdapter);
        return actionArgValidityContext;
    }

}
