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
package org.apache.causeway.core.metamodel.postprocessors.param;

import javax.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.actions.action.choicesfrom.ChoicesFromFacet;
import org.apache.causeway.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromChoicesFromFacet;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetFromChoicesFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetFromDefaultedFacet;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;

/**
 * Does post-processing of
 * {@link ActionParameterDefaultsFacet} and {@link ActionParameterChoicesFacet},
 * as well as
 * {@link PropertyDefaultFacet} and {@link PropertyChoicesFacet}.
 *
 */
public class ChoicesAndDefaultsPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public ChoicesAndDefaultsPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessParameter(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction,
            final ObjectActionParameter param) {
        if(!hasMemberLevelChoices(param)) {

            // if available on action, installs as a low precedence facets onto the parameters,
            // so can be overwritten by member support (imperative) choices
            val choicesFromFacetIfAny = objectAction
                    .lookupFacet(ChoicesFromFacet.class);

            if(FacetUtil
                .addFacetIfPresent(
                    ActionParameterChoicesFacetFromChoicesFromFacet
                    .create(choicesFromFacetIfAny, objectSpecification, param))
                .isPresent()) {

                // ActionParameterChoicesFacetFromChoicesFromFacet has precedence over
                // ActionParameterChoicesFacetFromChoicesFacet, so stop processing here
                return;
            }

            val choicesFacetIfAny = param.getElementType()
                    .lookupNonFallbackFacet(ChoicesFacet.class);

            FacetUtil.addFacetIfPresent(
                    ActionParameterChoicesFacetFromChoicesFacet
                    .create(choicesFacetIfAny, param.getFacetHolder()));
        }
    }

    @Override
    public void postProcessProperty(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation prop) {
        if(!hasMemberLevelDefaults(prop)) {
            prop.getElementType()
            .lookupNonFallbackFacet(DefaultedFacet.class)
            .ifPresent(specFacet -> FacetUtil.addFacet(new PropertyDefaultFacetFromDefaultedFacet(
                                        specFacet, facetedMethodFor(prop))));
        }
        if(!hasMemberLevelChoices(prop)) {

            val choicesFacetIfAny = prop.getElementType()
                    .lookupNonFallbackFacet(ChoicesFacet.class);

            FacetUtil.addFacetIfPresent(
                    PropertyChoicesFacetFromChoicesFacet
                    .create(choicesFacetIfAny, facetedMethodFor(prop)));
        }
    }

    @Override
    public void postProcessCollection(
            final ObjectSpecification objectSpecification,
            final OneToManyAssociation coll) {

        // ... see if any of its actions has a collection parameter of the same type
        //
        // eg Order#getItems() and Order#removeItems(List<OrderItem>)
        //

        // setting up filters ...
        val elementType = coll.getElementType();
        val whetherCollectionParamOfType =
                new ObjectActionParameter.Predicates.CollectionParameter(elementType);
        val whetherScalarParamOfType =
                new ObjectActionParameter.Predicates.ScalarParameter(elementType);

        // processing actions ...
        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
        .filter(ObjectAction.Predicates.choicesFromAndHavingCollectionParameterFor(coll))
        .forEach(action->{

            val parameters = action.getParameters();

            val compatibleCollectionParams = parameters.filter(whetherCollectionParamOfType);
            val compatibleScalarParams = parameters.filter(whetherScalarParamOfType);

            // for collection parameters, install a defaults facet (if there isn't one already)
            // this will cause the UI to render the collection with toggleboxes
            for (final ObjectActionParameter collectionParam : compatibleCollectionParams) {
                addCollectionParamDefaultsFacetIfNoneAlready(collectionParam);
            }

            // for compatible collection parameters, install a choices facet (if there isn't one already)
            // using the associated collection for its values
            for (final ObjectActionParameter collectionParam : compatibleCollectionParams) {
                addCollectionParamChoicesFacetIfNoneAlready(coll, collectionParam);
            }

            // similarly for compatible scalar parameters, install a choices facet (if there isn't one already)
            // using the associated collection for its values.
            for (final ObjectActionParameter scalarParam : compatibleScalarParams) {
                addCollectionParamChoicesFacetIfNoneAlready(coll, scalarParam);
            }
        });
    }

    // -- HELPER

    private static boolean hasMemberLevelDefaults(final ObjectActionParameter param) {
        return param.containsNonFallbackFacet(ActionParameterDefaultsFacet.class);
    }

    private static boolean hasMemberLevelDefaults(final OneToOneAssociation prop) {
        return prop.containsNonFallbackFacet(PropertyDefaultFacet.class);
    }

    private static boolean hasMemberLevelChoices(final ObjectActionParameter param) {
        return param.containsNonFallbackFacet(ActionParameterChoicesFacet.class)
                || param.containsNonFallbackFacet(ActionParameterAutoCompleteFacet.class);
    }

    private static boolean hasMemberLevelChoices(final OneToOneAssociation prop) {
        return prop.containsNonFallbackFacet(PropertyChoicesFacet.class)
                || prop.containsNonFallbackFacet(PropertyAutoCompleteFacet.class);
    }

    private static void addCollectionParamDefaultsFacetIfNoneAlready(
            final ObjectActionParameter collectionParam) {
        if(!hasMemberLevelDefaults(collectionParam)) {
            FacetUtil.addFacet(
                    ActionParameterDefaultsFacetFromAssociatedCollection
                    .create(collectionParam));
        }
    }

    private static void addCollectionParamChoicesFacetIfNoneAlready(
            final OneToManyAssociation coll,
            final ObjectActionParameter param) {
        if(!hasMemberLevelChoices(param)) {
            FacetUtil.addFacet(
                    new ActionParameterChoicesFacetFromParentedCollection(param, coll));
        }
    }

}
