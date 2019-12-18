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
package org.apache.isis.metamodel.specloader.specimpl;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.interactions.ActionArgValidityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;

public abstract class ObjectActionParameterMixedInAbstract
extends ObjectActionParameterAbstract
implements ObjectActionParameterMixedIn {

    private final ObjectActionParameter mixinParameter;
    private final ObjectActionMixedIn mixedInAction;

    public ObjectActionParameterMixedInAbstract(
            final FeatureType featureType, 
            final ObjectActionParameterAbstract mixinParameter,
            final ObjectActionMixedIn mixedInAction) {
        
        super(featureType, mixinParameter.getNumber(), mixedInAction, mixinParameter.getPeer());
        this.mixinParameter = mixinParameter;
        this.mixedInAction = mixedInAction;
    }

    @Override
    public ManagedObject[] getAutoComplete(
            final ManagedObject mixedInAdapter,
            final Can<ManagedObject> pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return mixinParameter.getAutoComplete(
                mixinAdapterFor(mixedInAdapter),
                pendingArgs,
                searchArg,
                interactionInitiatedBy);
    }

    @Override
    protected ManagedObject targetForDefaultOrChoices(final ManagedObject mixedInAdapter) {
        return mixinAdapterFor(mixedInAdapter);
    }

    private ManagedObject mixinAdapterFor(final ManagedObject mixedInAdapter) {
        return mixedInAction.mixinAdapterFor(mixedInAdapter);
    }

    @Override
    public ActionArgValidityContext createProposedArgumentInteractionContext(
            final ManagedObject mixedInAdapter,
            final ManagedObject[] proposedArguments,
            final int position,
            final InteractionInitiatedBy interactionInitiatedBy) {

        final ManagedObject targetObject = mixinAdapterFor(mixedInAdapter);

        final ActionArgValidityContext actionArgValidityContext = new ActionArgValidityContext(
                targetObject, mixedInAction.mixinAction, getIdentifier(), proposedArguments, position, interactionInitiatedBy);
        actionArgValidityContext.setMixedIn(mixedInAdapter);
        return actionArgValidityContext;
    }

}
