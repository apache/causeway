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

package org.apache.isis.core.metamodel.postprocessors.all;

import java.util.stream.Stream;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberDerivedFromType;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionParameterAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

import lombok.Setter;
import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveDescribedAsFromTypePostProcessor
implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        val actionTypes = inferActionTypes();
        final Stream<ObjectAction> objectActions = objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED);

        objectActions
                .flatMap(ObjectAction::streamParameters)
                .forEach(this::deriveParameterDescribedAsFromType);

        objectActions.forEach(this::deriveActionDescribedAsFromType);

        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(this::derivePropertyOrCollectionDescribedAsFromType);

        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(this::derivePropertyOrCollectionDescribedAsFromType);
    }

    static DisabledFacetAbstract.Semantics inferSemanticsFrom(final ViewModelFacet facet) {
        return facet.isImplicitlyImmutable() ?
                DisabledFacetAbstract.Semantics.DISABLED :
                    DisabledFacetAbstract.Semantics.ENABLED;
    }


    /**
     * Replaces some of the functionality in {@link DescribedAsFacetOnMemberFactory}.
     */
    private void deriveActionDescribedAsFromType(final ObjectAction objectAction) {
        if(objectAction.containsNonFallbackFacet(DescribedAsFacet.class)) {
            return;
        }
        objectAction.getReturnType()
        .lookupNonFallbackFacet(DescribedAsFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacet(new DescribedAsFacetOnMemberDerivedFromType(specFacet,
                                    facetedMethodFor(objectAction))));
    }

    /**
     * Replaces {@link DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory}
     */
    private void deriveParameterDescribedAsFromType(final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(DescribedAsFacet.class)) {
            return;
        }
        final ObjectSpecification paramSpec = parameter.getSpecification();
        final DescribedAsFacet specFacet = paramSpec.getFacet(DescribedAsFacet.class);

        //TODO: this ought to check if a do-op; if you come across this, you can probably change it (just taking smaller steps for now)
        //if(existsAndIsDoOp(specFacet)) {
        if(specFacet != null) {
            FacetUtil.addFacet(new DescribedAsFacetOnParameterDerivedFromType(specFacet, peerFor(parameter)));
        }
    }

    /**
     * Replaces some of the functionality in {@link DescribedAsFacetOnMemberFactory}.
     */
    private void derivePropertyOrCollectionDescribedAsFromType(final ObjectAssociation objectAssociation) {
        if(objectAssociation.containsNonFallbackFacet(DescribedAsFacet.class)) {
            return;
        }
        objectAssociation.getSpecification()
        .lookupNonFallbackFacet(DescribedAsFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacet(new DescribedAsFacetOnMemberDerivedFromType(
                                    specFacet, facetedMethodFor(objectAssociation))));
    }

    private ImmutableEnumSet<ActionType> inferActionTypes() {
        return metaModelContext.getSystemEnvironment().isPrototyping()
                ? ActionType.USER_AND_PROTOTYPE
                : ActionType.USER_ONLY;
    }

    private static FacetedMethod facetedMethodFor(final ObjectMember objectMember) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the action/association itself.
        final ObjectMemberAbstract objectActionImpl = (ObjectMemberAbstract) objectMember;
        return objectActionImpl.getFacetedMethod();
    }

    private static TypedHolder peerFor(final ObjectActionParameter param) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the param itself.
        final ObjectActionParameterAbstract objectActionImpl = (ObjectActionParameterAbstract) param;
        return objectActionImpl.getPeer();
    }

}
