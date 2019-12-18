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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.metamodel.commons.ListExtensions;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;

/**
 * 
 * @deprecated contributed actions from services are deprecated, use mixins instead
 *
 */
@Deprecated
public abstract class ObjectActionParameterContributeeAbstract
extends ObjectActionParameterAbstract
implements ObjectActionParameterContributee {

    private final Object servicePojo;
    private final ObjectActionParameter serviceActionParameter;
    private final ObjectActionContributee contributeeAction;

    public ObjectActionParameterContributeeAbstract(
            final FeatureType featureType,
            final Object servicePojo,
            final ObjectActionParameterAbstract serviceActionParameter,
            final int contributeeParamNumber,
            final ObjectActionContributee contributeeAction) {
        
        super(featureType, contributeeParamNumber, contributeeAction, serviceActionParameter.getPeer());
        this.servicePojo = servicePojo;
        this.serviceActionParameter = serviceActionParameter;
        this.contributeeAction = contributeeAction;
    }

    @Override
    public ManagedObject[] getAutoComplete(
            final ManagedObject adapter,
            final Can<ManagedObject> pendingArgs,
            final String searchArg,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        return serviceActionParameter.getAutoComplete(
                getServiceAdapter(),
                pendingArgs,
                searchArg,
                interactionInitiatedBy);
    }

    protected ManagedObject getServiceAdapter() {
        return getObjectManager().adapt(servicePojo);
    }

    @Override
    protected ManagedObject targetForDefaultOrChoices(final ManagedObject adapter) {
        return getServiceAdapter();
    }

    
    @Override
    protected Can<ManagedObject> argsForDefaultOrChoices(
            final ManagedObject contributee,
            final Can<ManagedObject> pendingArgs) {

        final List<ManagedObject> suppliedArgs = pendingArgs.stream()
                .collect(Collectors.toCollection(ArrayList::new));
        final int contributeeParam = contributeeAction.getContributeeParam();
        ListExtensions.insert(suppliedArgs, contributeeParam, contributee);
        return Can.ofCollection(suppliedArgs);
    }

}
