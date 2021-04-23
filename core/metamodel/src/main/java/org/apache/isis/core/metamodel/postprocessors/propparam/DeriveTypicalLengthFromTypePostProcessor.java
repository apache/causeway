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
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromType;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
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
public class DeriveTypicalLengthFromTypePostProcessor
implements ObjectSpecificationPostProcessor, MetaModelContextAware {

    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        //XXX in principle it would be sufficient to just process declared members; can optimize if worth the effort

        // all the actions of this type
        val actionTypes = inferActionTypes();
        final Stream<ObjectAction> objectActions = objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED);

        // for each action, ...
        objectActions.flatMap(ObjectAction::streamParameters)
            .forEach(parameter -> {
                deriveParameterTypicalLengthFromType(parameter);
            });

        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(property -> {
            derivePropertyTypicalLengthFromType(property);
        });
    }


    /**
     * Replaces {@link TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory}
     */
    private static void deriveParameterTypicalLengthFromType(final ObjectActionParameter parameter) {
        if(parameter.containsNonFallbackFacet(TypicalLengthFacet.class)) {
            return;
        }
        parameter.getSpecification()
        .lookupNonFallbackFacet(TypicalLengthFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacet(new TypicalLengthFacetOnParameterDerivedFromType(specFacet,
                                    peerFor(parameter))));
    }

    /**
     * replaces {@link TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory}
     */
    private static void derivePropertyTypicalLengthFromType(final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(TypicalLengthFacet.class)) {
            return;
        }
        property.getSpecification()
        .lookupNonFallbackFacet(TypicalLengthFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacet(new TypicalLengthFacetOnPropertyDerivedFromType(
                                    specFacet, facetedMethodFor(property))));

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
