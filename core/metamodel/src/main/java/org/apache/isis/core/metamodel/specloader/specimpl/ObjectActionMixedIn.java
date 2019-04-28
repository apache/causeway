/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.specloader.specimpl;

import java.util.List;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResultSet;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.UsabilityContext;
import org.apache.isis.core.metamodel.interactions.ValidityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

public class ObjectActionMixedIn extends ObjectActionDefault implements MixedInMember {

    /**
     * The type of the mixin (providing the action), eg annotated with {@link org.apache.isis.applib.annotation.Mixin}.
     */
    private final Class<?> mixinType;

    /**
     * The {@link ObjectActionDefault} for the action being mixed in (ie on the {@link #mixinType}.
     */
    final ObjectActionDefault mixinAction;

    /**
     * The domain object type being mixed in to (being supplemented).
     */
    private final ObjectSpecification mixedInType;

    /**
     * Hold facets rather than delegate to the mixin action
     */
    private final FacetHolder facetHolder = new FacetHolderImpl();

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private List<ObjectActionParameter> parameters;

    private final Identifier identifier;

    public ObjectActionMixedIn(
            final Class<?> mixinType,
            final String mixinMethodName,
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixedInType,
            final ServicesInjector objectMemberDependencies) {
        super(mixinAction.getFacetedMethod(), objectMemberDependencies);

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixedInType = mixedInType;

        // copy over facets from mixin action to self
        FacetUtil.copyFacets(mixinAction.getFacetedMethod(), facetHolder);

        // adjust name if necessary
        final String name = getName();

        if(_Strings.isNullOrEmpty(name) || name.equalsIgnoreCase(mixinMethodName)) {
            String memberName = determineNameFrom(mixinAction);
            FacetUtil.addFacet(new NamedFacetInferred(memberName, facetHolder));
        }

        // calculate the identifier
        final Identifier mixinIdentifier = mixinAction.getFacetedMethod().getIdentifier();
        List<String> memberParameterNames = mixinIdentifier.getMemberParameterNames();
        identifier = Identifier.actionIdentifier(getOnType().getCorrespondingClass().getName(), getId(), memberParameterNames);
    }

    @Override
    public String getId() {
        return determineIdFrom(this.mixinAction);
    }

    @Override
    public String getOriginalId() {
        return super.getId();
    }

    public boolean hasMixinAction(final ObjectAction mixinAction) {
        return this.mixinAction == mixinAction;
    }

    @Override
    public ObjectSpecification getOnType() {
        return mixedInType;
    }

    @Override
    public int getParameterCount() {
        return mixinAction.getParameterCount();
    }

    @Override
    public ObjectAdapter realTargetAdapter(final ObjectAdapter targetAdapter) {
        return mixinAdapterFor(targetAdapter);
    }

    @Override
    protected synchronized List<ObjectActionParameter> determineParameters() {
        if (parameters != null) {
            // because possible race condition (caller isn't synchronized)
            return parameters;
        }
        final List<ObjectActionParameter> mixinActionParameters = mixinAction.getParameters();
        final List<FacetedMethodParameter> paramPeers = getFacetedMethod().getParameters();

        final List<ObjectActionParameter> mixedInParameters = _Lists.newArrayList();

        for(int paramNum = 0; paramNum < mixinActionParameters.size(); paramNum++) {

            final ObjectActionParameterAbstract mixinParameter =
                    (ObjectActionParameterAbstract) mixinActionParameters.get(paramNum);

            final TypedHolder paramPeer = paramPeers.get(paramNum);
            final ObjectSpecification specification = ObjectMemberAbstract
                    .getSpecification(getSpecificationLoader(), paramPeer.getType());

            final ObjectActionParameterMixedIn mixedInParameter =
                    mixinParameter.getPeer().getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
                    ? new OneToOneActionParameterMixedIn(mixinParameter, this)
                            : new OneToManyActionParameterMixedIn(mixinParameter, this);
                    mixedInParameters.add(mixedInParameter);
        }
        return mixedInParameters;
    }

    @Override
    public Consent isVisible(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        final ObjectAdapter mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);
        final VisibilityContext<?> ic =
                mixinAction.createVisibleInteractionContext(mixinAdapter, interactionInitiatedBy, where);
        ic.setMixedIn(mixedInAdapter);
        return InteractionUtils.isVisibleResult(this, ic).createConsent();
    }

    @Override
    public Consent isUsable(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy, final Where where) {

        final ObjectAdapter mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);
        final UsabilityContext<?> ic =
                mixinAction.createUsableInteractionContext(mixinAdapter, interactionInitiatedBy, where);
        ic.setMixedIn(mixedInAdapter);
        return InteractionUtils.isUsableResult(this, ic).createConsent();
    }

    @Override
    public ObjectAdapter[] getDefaults(final ObjectAdapter mixedInAdapter) {
        final ObjectAdapter mixinAdapter = mixinAdapterFor(mixedInAdapter);
        return mixinAction.getDefaults(mixinAdapter);
    }

    @Override
    public ObjectAdapter[][] getChoices(
            final ObjectAdapter mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ObjectAdapter mixinAdapter = mixinAdapterFor(mixedInAdapter);
        return mixinAction.getChoices(mixinAdapter, interactionInitiatedBy);
    }

    protected ObjectAdapter mixinAdapterFor(final ObjectAdapter mixedInAdapter) {
        return mixinAdapterFor(mixinType, mixedInAdapter);
    }

    @Override
    protected void validateArgumentSet(
            final ObjectAdapter mixedInAdapter,
            final ObjectAdapter[] proposedArguments,
            final InteractionInitiatedBy interactionInitiatedBy,
            final InteractionResultSet resultSet) {

        final ObjectAdapter targetObject = mixinAdapterFor(mixinType, mixedInAdapter);

        final ValidityContext<?> ic =
                mixinAction.createActionInvocationInteractionContext(targetObject, proposedArguments, interactionInitiatedBy);
        ic.setMixedIn(mixedInAdapter);

        InteractionUtils.isValidResultSet(this, ic, resultSet);
    }



    @Override
    public ObjectAdapter execute(
            final ObjectAdapter target,         // will be the mixedInAdapter
            final ObjectAdapter mixedInAdapter, // will be passed in as null
            final ObjectAdapter[] arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {


        final ObjectAdapter targetAdapter = mixinAdapterFor(mixinType, target);
        final ObjectAdapter actualMixedInAdapter = target;

        setupCommand(actualMixedInAdapter, arguments);

        return mixinAction.executeInternal(
                targetAdapter, actualMixedInAdapter, arguments,
                interactionInitiatedBy);
    }

    // -- facetHolder
    @Override
    protected FacetHolder getFacetHolder() {
        return facetHolder;
    }


    /* (non-Javadoc)
     * @see org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract#getIdentifier()
     */
    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public ObjectSpecification getMixinType() {
        return getSpecificationLoader().loadSpecification(mixinType);

    }


}
