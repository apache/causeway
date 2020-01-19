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

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.val;

public class ObjectActionContributee extends ObjectActionDefault implements ContributeeMember {

    private final Object servicePojo;
    private final ObjectActionDefault serviceAction;
    private final int contributeeParamIndex;
    private final ObjectSpecification contributeeType;

    /**
     * Hold facets rather than delegate to the contributed action (different types might
     * use layout metadata to position the contributee in different ways)
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();

    private final Identifier identifier;

    public ObjectActionContributee(
            final Object servicePojo,
            final ObjectActionDefault serviceAction,
            final int contributeeParam,
            final ObjectSpecification contributeeType) {

        super(serviceAction.getFacetedMethod());

        this.servicePojo = servicePojo;
        this.serviceAction = serviceAction;
        this.contributeeType = contributeeType;
        this.contributeeParamIndex = contributeeParam;

        // copy over facets from contributed to own.
        FacetUtil.copyFacets(serviceAction.getFacetedMethod(), facetHolder);

        // calculate the identifier
        final Identifier contributorIdentifier = serviceAction.getFacetedMethod().getIdentifier();
        final String memberName = contributorIdentifier.getMemberName();
        List<String> memberParameterNames = contributorIdentifier.getMemberParameterNames();
        identifier = Identifier.actionIdentifier(getOnType().getCorrespondingClass().getName(), memberName, memberParameterNames);
    }

    @Override
    public ObjectSpecification getOnType() {
        return contributeeType;
    }

    @Override
    public int getParameterCount() {
        return serviceAction.getParameterCount() - 1;
    }

    @Override
    public boolean isContributedBy(final ObjectAction serviceAction) {
        return serviceAction == this.serviceAction;
    }

    @Override
    public int getContributeeParamIndex() {
        return contributeeParamIndex;
    }

    @Override
    protected synchronized Can<ObjectActionParameter> determineParameters() {

        val serviceParameters = serviceAction.getParameters();
        //final List<FacetedMethodParameter> paramPeers = getFacetedMethod().getParameters(); //side effects?

        final List<ObjectActionParameter> contributeeParameters = _Lists.newArrayList();
        int contributeeParamNum = 0;

        for (int serviceParamNum = 0; serviceParamNum < serviceParameters.size(); serviceParamNum++ ) {
            if(serviceParamNum == contributeeParamIndex) {
                // skip so is omitted from the Contributed action
                continue;
            }

            final ObjectActionParameterAbstract serviceParameter =
                    (ObjectActionParameterAbstract) serviceParameters.getElseFail(serviceParamNum);

            final ObjectActionParameterContributee contributedParam =
                    serviceParameter.getPeer().getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
                    ? new OneToOneActionParameterContributee(
                            servicePojo, serviceParameter, contributeeParamNum, this)
                            : new OneToManyActionParameterContributee(
                                    servicePojo, serviceParameter, contributeeParamNum, this);

            contributeeParameters.add(contributedParam);

            contributeeParamNum++;
        }
        return Can.ofCollection(contributeeParameters);
    }

    @Override
    public Consent isVisible(
            final ManagedObject contributee,
            final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        final VisibilityContext<?> ic = serviceAction.createVisibleInteractionContext(getServiceAdapter(),
                interactionInitiatedBy, where);
        ic.putContributee(this.contributeeParamIndex, contributee);
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ManagedObject contributee,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where) {
        final UsabilityContext<?> ic = serviceAction.createUsableInteractionContext(getServiceAdapter(),
                interactionInitiatedBy, where);
        ic.putContributee(this.contributeeParamIndex, contributee);
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    @Override
    public Can<ManagedObject> getDefaults(final ManagedObject target) {
        val contributorDefaults = serviceAction.getDefaults(getServiceAdapter());
        return contributorDefaults.remove(contributeeParamIndex);
    }

    @Override
    public Can<Can<ManagedObject>> getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy) {
        val serviceChoices = serviceAction.getChoices(getServiceAdapter(), interactionInitiatedBy);
        return serviceChoices.remove(contributeeParamIndex);
    }

    @Override
    public Consent isProposedArgumentSetValid(
            final ManagedObject contributee,
            final List<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction
                .isProposedArgumentSetValid(getServiceAdapter(), serviceArguments, interactionInitiatedBy);
    }

    @Override
    public Consent isEachIndividualArgumentValid(
            final ManagedObject contributee,
            final List<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction
                .isEachIndividualArgumentValid(getServiceAdapter(), serviceArguments, interactionInitiatedBy);
    }

    @Override
    public Consent isArgumentSetValid(
            final ManagedObject contributee,
            final List<ManagedObject> proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        
        val serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction
                .isArgumentSetValid(getServiceAdapter(), serviceArguments, interactionInitiatedBy);
    }

    @Override
    public ManagedObject execute(
            final ManagedObject targetAdapter,
            final ManagedObject mixedInAdapter,
            final List<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(targetAdapter, argumentAdapters);

        val serviceArguments = argsPlusContributee(targetAdapter, argumentAdapters);
        return serviceAction.executeInternal(
                getServiceAdapter(), mixedInAdapter, serviceArguments, interactionInitiatedBy);
    }


    private List<ManagedObject> argsPlusContributee(
            final ManagedObject contributee, 
            final List<ManagedObject> arguments) {
        
        arguments.add(contributeeParamIndex, contributee);
        return arguments;
    }

    // //////////////////////////////////////
    // FacetHolder
    // //////////////////////////////////////

    @Override
    public FacetHolder getFacetHolder() {
        return facetHolder;
    }

    // //////////////////////////////////////

    /* (non-Javadoc)
     * @see ObjectMemberAbstract#getIdentifier()
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    // //////////////////////////////////////

    public ManagedObject getServiceAdapter() {
        return getObjectManager().adapt(servicePojo);
    }

    @Override
    public ObjectSpecification getServiceContributedBy() {
        return getServiceAdapter().getSpecification();
    }
}
