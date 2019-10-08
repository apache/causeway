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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facetapi.FeatureType;
import org.apache.isis.metamodel.facetapi.MultiTypedFacet;
import org.apache.isis.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.metamodel.interactions.InteractionUtils;
import org.apache.isis.metamodel.interactions.UsabilityContext;
import org.apache.isis.metamodel.interactions.VisibilityContext;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;

public class ObjectActionContributee extends ObjectActionDefault implements ContributeeMember {

    private final Object servicePojo;
    private final ObjectActionDefault serviceAction;
    private final int contributeeParam;
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
        this.contributeeParam = contributeeParam;

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

    public int getContributeeParam() {
        return contributeeParam;
    }

    @Override
    public boolean isContributedBy(final ObjectAction serviceAction) {
        return serviceAction == this.serviceAction;
    }

    @Override
    public int getContributeeParamPosition() {
        return contributeeParam;
    }

    @Override
    protected synchronized List<ObjectActionParameter> determineParameters() {
        if (parameters != null) {
            // because possible race condition (caller isn't synchronized)
            return parameters;
        }

        final List<ObjectActionParameter> serviceParameters = serviceAction.getParameters();
        final List<FacetedMethodParameter> paramPeers = getFacetedMethod().getParameters();

        final List<ObjectActionParameter> contributeeParameters = _Lists.newArrayList();
        int contributeeParamNum = 0;

        for (int serviceParamNum = 0; serviceParamNum < serviceParameters.size(); serviceParamNum++ ) {
            if(serviceParamNum == contributeeParam) {
                // skip so is omitted from the Contributed action
                continue;
            }

            final ObjectActionParameterAbstract serviceParameter =
                    (ObjectActionParameterAbstract) serviceParameters.get(serviceParamNum);

            final ObjectActionParameterContributee contributedParam =
                    serviceParameter.getPeer().getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
                    ? new OneToOneActionParameterContributee(
                            servicePojo, serviceParameter, contributeeParamNum, this)
                            : new OneToManyActionParameterContributee(
                                    servicePojo, serviceParameter, contributeeParamNum, this);

                            contributeeParameters.add(contributedParam);

                            contributeeParamNum++;
        }
        return contributeeParameters;
    }

    @Override
    public Consent isVisible(
            final ManagedObject contributee,
            final InteractionInitiatedBy interactionInitiatedBy,
            Where where) {
        final VisibilityContext<?> ic = serviceAction.createVisibleInteractionContext(getServiceAdapter(),
                interactionInitiatedBy, where);
        ic.putContributee(this.contributeeParam, contributee);
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ManagedObject contributee,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where) {
        final UsabilityContext<?> ic = serviceAction.createUsableInteractionContext(getServiceAdapter(),
                interactionInitiatedBy, where);
        ic.putContributee(this.contributeeParam, contributee);
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    @Override
    public ManagedObject[] getDefaults(final ManagedObject target) {
        final ManagedObject[] contributorDefaults = serviceAction.getDefaults(getServiceAdapter());
        return removeElementFromArray(contributorDefaults, contributeeParam, new ManagedObject[]{});
    }

    @Override
    public ManagedObject[][] getChoices(
            final ManagedObject target,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject[][] serviceChoices = serviceAction.getChoices(getServiceAdapter(),
                interactionInitiatedBy);
        return removeElementFromArray(serviceChoices, contributeeParam, new ManagedObject[][]{});
    }

    @Override
    public Consent isProposedArgumentSetValid(
            final ManagedObject contributee,
            final ManagedObject[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject[] serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction.isProposedArgumentSetValid(getServiceAdapter(), serviceArguments, interactionInitiatedBy);
    }

    @Override
    public Consent isEachIndividualArgumentValid(
            final ManagedObject contributee,
            final ManagedObject[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject[] serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction.isEachIndividualArgumentValid(getServiceAdapter(), serviceArguments, interactionInitiatedBy);
    }

    @Override
    public Consent isArgumentSetValid(
            final ManagedObject contributee,
            final ManagedObject[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject[] serviceArguments = argsPlusContributee(contributee, proposedArguments);
        return serviceAction.isArgumentSetValid(getServiceAdapter(), serviceArguments, interactionInitiatedBy);
    }

    @Override
    public ManagedObject execute(
            final ManagedObject targetAdapter,
            final ManagedObject mixedInAdapter,
            final ManagedObject[] argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {

        setupCommand(targetAdapter, argumentAdapters);

        final ManagedObject[] serviceArguments = argsPlusContributee(targetAdapter, argumentAdapters);
        return serviceAction.executeInternal(
                getServiceAdapter(), mixedInAdapter, serviceArguments, interactionInitiatedBy);
    }


    private ManagedObject[] argsPlusContributee(final ManagedObject contributee, final ManagedObject[] arguments) {
        return addElementToArray(arguments, contributeeParam, contributee, new ManagedObject[]{});
    }

    // //////////////////////////////////////
    // FacetHolder
    // //////////////////////////////////////

    @Override
    public int getFacetCount() {
        return facetHolder.getFacetCount();
    }

    @Override
    public <T extends Facet> T getFacet(Class<T> cls) {
        return facetHolder.getFacet(cls);
    }

    @Override
    public boolean containsFacet(Class<? extends Facet> facetType) {
        return facetHolder.containsFacet(facetType);
    }

    @Override
    public boolean containsDoOpFacet(java.lang.Class<? extends Facet> facetType) {
        return facetHolder.containsDoOpFacet(facetType);
    }

    @Override
    public Stream<Facet> streamFacets() {
        return facetHolder.streamFacets();
    }

    @Override
    public void addFacet(Facet facet) {
        facetHolder.addFacet(facet);
    }

    @Override
    public void addFacet(MultiTypedFacet facet) {
        facetHolder.addFacet(facet);
    }

    @Override
    public void removeFacet(Facet facet) {
        facetHolder.removeFacet(facet);
    }

    @Override
    public void removeFacet(Class<? extends Facet> facetType) {
        facetHolder.removeFacet(facetType);
    }


    // //////////////////////////////////////

    /* (non-Javadoc)
     * @see org.apache.isis.metamodel.specloader.specimpl.ObjectMemberAbstract#getIdentifier()
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    // //////////////////////////////////////

    static <T> T[] addElementToArray(T[] array, final int n, final T element, final T[] type) {
        List<T> list = _Lists.newArrayList(Arrays.asList(array));
        list.add(n, element);
        return list.toArray(type);
    }

    static <T> T[] removeElementFromArray(T[] array, int n, T[] t) {
        List<T> list = _Lists.newArrayList(Arrays.asList(array));
        list.remove(n);
        return list.toArray(t);
    }

    public ManagedObject getServiceAdapter() {
        return getObjectAdapterProvider().adapterFor(servicePojo);
    }

    @Override
    public ObjectSpecification getServiceContributedBy() {
        return getServiceAdapter().getSpecification();
    }
}
