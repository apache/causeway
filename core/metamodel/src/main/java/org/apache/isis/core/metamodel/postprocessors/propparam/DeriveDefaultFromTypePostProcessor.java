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

package org.apache.isis.core.metamodel.postprocessors.propparam;

import java.util.stream.Stream;

import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFacets;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromDefaultedFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionParameterAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

import lombok.Setter;
import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveDefaultFromTypePostProcessor
implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        // all the actions of this type
        val actionTypes = inferActionTypes();
        final Stream<ObjectAction> objectActions = objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED);

        // for each action, ...
        objectActions.flatMap(ObjectAction::streamParameters)
            .forEach(DeriveDefaultFromTypePostProcessor::deriveParameterDefaultFacetFromType);

        objectSpecification.streamProperties(MixedIn.INCLUDED)
                .forEach(DeriveDefaultFromTypePostProcessor::derivePropertyDefaultsFromType);

    }


    /**
     * Replaces {@link ActionParameterDefaultFacetDerivedFromTypeFactory}
     */
    private static void deriveParameterDefaultFacetFromType(final ObjectActionParameter parameter) {

        if (parameter.containsNonFallbackFacet(ActionDefaultsFacet.class)) {
            return;
        }

        // this loop within the outer loop (for every param) is really weird,
        // but arises from porting the old facet factory
        final ObjectAction objectAction = parameter.getAction();
        val parameterSpecs = objectAction.getParameterTypes();
        final DefaultedFacet[] parameterTypeDefaultedFacets = new DefaultedFacet[parameterSpecs.size()];
        boolean hasAtLeastOneDefault = false;
        for (int i = 0; i < parameterSpecs.size(); i++) {
            final ObjectSpecification parameterSpec = parameterSpecs.getElseFail(i);
            parameterTypeDefaultedFacets[i] = parameterSpec.getFacet(DefaultedFacet.class);
            hasAtLeastOneDefault = hasAtLeastOneDefault | (parameterTypeDefaultedFacets[i] != null);
        }
        if (hasAtLeastOneDefault) {
            FacetUtil.addFacet(new ActionParameterDefaultFacetDerivedFromTypeFacets(parameterTypeDefaultedFacets, peerFor(parameter)));
        }
    }


    /**
     * Replaces {@link PropertyDefaultFacetDerivedFromTypeFactory}
     */
    private static void derivePropertyDefaultsFromType(final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(PropertyDefaultFacet.class)) {
            return;
        }
        property.getSpecification()
        .lookupNonFallbackFacet(DefaultedFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacet(new PropertyDefaultFacetDerivedFromDefaultedFacet(
                                    specFacet, facetedMethodFor(property))));
    }


    private ImmutableEnumSet<ActionType> inferActionTypes() {
        return metaModelContext.getSystemEnvironment().isPrototyping()
                ? ActionType.USER_AND_PROTOTYPE
                : ActionType.USER_ONLY;
    }

    static DisabledFacetAbstract.Semantics inferSemanticsFrom(final ViewModelFacet facet) {
        return facet.isImplicitlyImmutable() ?
                DisabledFacetAbstract.Semantics.DISABLED :
                DisabledFacetAbstract.Semantics.ENABLED;
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
