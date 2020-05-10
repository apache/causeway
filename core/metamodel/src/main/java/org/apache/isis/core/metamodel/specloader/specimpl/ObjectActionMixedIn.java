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
import org.apache.isis.core.commons.collections.Can;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetedMethodParameter;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacetInferred;
import org.apache.isis.core.metamodel.interactions.InteractionContext.Head;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

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
    @Getter(onMethod = @__(@Override))
    private final FacetHolder facetHolder = new FacetHolderImpl();

    /**
     * Lazily initialized by {@link #getParameters()} (so don't use directly!)
     */
    private Can<ObjectActionParameter> parameters;

    private final Identifier identifier;

    public ObjectActionMixedIn(
            final Class<?> mixinType,
            final String mixinMethodName,
            final ObjectActionDefault mixinAction,
            final ObjectSpecification mixedInType) {

        super(mixinAction.getFacetedMethod());

        this.mixinType = mixinType;
        this.mixinAction = mixinAction;
        this.mixedInType = mixedInType;

        // copy over facets from mixin action to self
        FacetUtil.copyFacets(mixinAction.getFacetedMethod(), facetHolder);

        // adjust name if necessary
        final String name = getName();

        if(_Strings.isNullOrEmpty(name) || name.equalsIgnoreCase(mixinMethodName)) {
            String memberName = determineNameFrom(mixinAction);
            this.addFacet(new NamedFacetInferred(memberName, facetHolder));
        }

        // calculate the identifier
        final Identifier mixinIdentifier = mixinAction.getFacetedMethod().getIdentifier();
        List<String> memberParameterNames = mixinIdentifier.getMemberParameterNames();
        identifier = Identifier.actionIdentifier(getOnType().getCorrespondingClass().getName(), getId(), memberParameterNames);
    }

    @Override
    protected Head headFor(final ManagedObject mixedInAdapter) {
        val mixinAdapter = mixinAdapterFor(mixinType, mixedInAdapter);
        return Head.of(mixedInAdapter, mixinAdapter);
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
    public ManagedObject realTargetAdapter(final ManagedObject targetAdapter) {
        return mixinAdapterFor(targetAdapter);
    }

    @Override
    public PendingParameterModelHead newPendingParameterModelHead(@NonNull ManagedObject actionOwner) {
        return PendingParameterModelHead.of(this, actionOwner, mixinAdapterFor(actionOwner));
    }
//    
//    @Override
//    protected synchronized Can<ObjectActionParameter> determineParameters() {
//        if (parameters != null) {
//            // because possible race condition (caller isn't synchronized)
//            return parameters;
//        }
//        val mixinActionParameters = mixinAction.getParameters();
//        final List<FacetedMethodParameter> paramPeers = getFacetedMethod().getParameters();
//
//        final List<ObjectActionParameter> mixedInParameters = _Lists.newArrayList();
//
//        for(int paramIndex = 0; paramIndex < mixinActionParameters.size(); paramIndex++) {
//
//            val mixinParameter =
//                    (ObjectActionParameterAbstract) mixinActionParameters.getElseFail(paramIndex);
//
//            final TypedHolder paramPeer = paramPeers.get(paramIndex);
//            getSpecificationLoader().loadSpecification(paramPeer.getType());
//
//            final ObjectActionParameterMixedIn mixedInParameter =
//                    mixinParameter.getPeer().getFeatureType() == FeatureType.ACTION_PARAMETER_SCALAR
//                    ? new OneToOneActionParameterMixedIn(mixinParameter, this)
//                    : new OneToManyActionParameterMixedIn(mixinParameter, this);
//            mixedInParameters.add(mixedInParameter);
//        }
//        return Can.ofCollection(mixedInParameters);
//    }
//    


    @Override
    public Can<ManagedObject> getDefaults(final ManagedObject mixedInAdapter) {
        final ManagedObject mixinAdapter = mixinAdapterFor(mixedInAdapter);
        return mixinAction.getDefaults(mixinAdapter);
    }

    @Override
    public Can<Can<ManagedObject>> getChoices(
            final ManagedObject mixedInAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {
        final ManagedObject mixinAdapter = mixinAdapterFor(mixedInAdapter);
        return mixinAction.getChoices(mixinAdapter, interactionInitiatedBy);
    }

    protected ManagedObject mixinAdapterFor(final ManagedObject mixedInAdapter) {
        return mixinAdapterFor(mixinType, mixedInAdapter);
    }

    @Override
    public ManagedObject execute(
            final ManagedObject target,         // will be the mixedInAdapter
            final ManagedObject mixedInAdapter, // will be passed in as null
            final Can<ManagedObject> arguments,
            final InteractionInitiatedBy interactionInitiatedBy) {


        final ManagedObject targetAdapter = mixinAdapterFor(mixinType, target);
        final ManagedObject actualMixedInAdapter = target;

        setupCommand(actualMixedInAdapter, arguments);

        return mixinAction.executeInternal(
                targetAdapter, actualMixedInAdapter, arguments,
                interactionInitiatedBy);
    }

    /* (non-Javadoc)
     * @see ObjectMemberAbstract#getIdentifier()
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
