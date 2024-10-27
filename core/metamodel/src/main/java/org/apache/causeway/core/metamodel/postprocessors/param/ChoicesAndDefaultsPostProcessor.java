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

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.config.progmodel.ProgrammingModelConstants;
import org.apache.causeway.core.metamodel.commons.MetaModelVisitor;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.causeway.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacetFromElementType;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromAction;
import org.apache.causeway.core.metamodel.facets.param.choices.ActionParameterChoicesFacetFromElementType;
import org.apache.causeway.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.causeway.core.metamodel.facets.properties.autocomplete.PropertyAutoCompleteFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.causeway.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetFromChoicesFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.causeway.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetFromDefaultedFacet;
import org.apache.causeway.core.metamodel.postprocessors.MetaModelPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailure;
import org.apache.causeway.core.metamodel.util.Facets;

/**
 * Does post-processing of
 * {@link ActionParameterDefaultsFacet} and {@link ActionParameterChoicesFacet},
 * as well as
 * {@link PropertyDefaultFacet} and {@link PropertyChoicesFacet}.
 *
 */
public class ChoicesAndDefaultsPostProcessor
extends MetaModelPostProcessorAbstract {

    @Inject
    public ChoicesAndDefaultsPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext, SKIP_MIXINS);
    }

    @Override
    public void postProcessParameter(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction,
            final ObjectActionParameter param) {

        // no need to perform this check if the action is always hidden in the UI
        // (one use case being if the action exists solely to be called via the WrapperFactory service,
        //  eg to emit an outbox event for integration)
        if (Facets.hiddenWhere(objectAction)
                  .filter(where -> where == Where.EVERYWHERE)
                  .isPresent()) {
            return;
        }

        if(!hasChoicesOrAutoComplete(param)) {

            if(FacetUtil
                    .addFacetIfPresent(
                        ActionParameterChoicesFacetFromAction
                            .create(objectAction, objectSpecification, param))
                    .isPresent()) {

                /* ActionParameterChoicesFacetFromAction has precedence over
                 * ActionParameterChoicesFacetFromElementType, so stop processing here.
                 * (also skips validation below) */
                return;
            }

            if(FacetUtil
                    .addFacetIfPresent(
                        ActionParameterChoicesFacetFromElementType
                            .create(param))
                    .isPresent()) {

                /* ActionParameterChoicesFacetFromElementType has precedence over
                 * ActionParameterAutoCompleteFacetFromElementType, so stop processing here.
                 * (also skips validation below) */
                return;
            }

            if(FacetUtil
                    .addFacetIfPresent(
                            ActionParameterAutoCompleteFacetFromElementType
                                .create(param))
                    .isPresent()) {

                /* skips validation below */
                return;
            }

        }

        if(MetaModelVisitor.SKIP_ABSTRACT.test(objectSpecification)) {
            checkParamHasChoicesOrAutoCompleteWhenRequired(param);
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
        if(!hasChoicesOrAutoComplete(prop)) {

            var choicesFacetIfAny = prop.getElementType()
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
        var elementType = coll.getElementType();
        var whetherCollectionParamOfType =
                new ObjectActionParameter.Predicates.CollectionParameter(elementType);
        var whetherScalarParamOfType =
                new ObjectActionParameter.Predicates.ScalarParameter(elementType);

        // processing actions ...
        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
        .filter(ObjectAction.Predicates.choicesFromAndHavingCollectionParameterFor(coll))
        .forEach(action->{

            var parameters = action.getParameters();

            var compatibleCollectionParams = parameters.filter(whetherCollectionParamOfType);
            var compatibleScalarParams = parameters.filter(whetherScalarParamOfType);

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

    private static boolean hasChoicesOrAutoComplete(final ObjectActionParameter param) {
        return param.containsNonFallbackFacet(ActionParameterChoicesFacet.class)
                || param.containsNonFallbackFacet(ActionParameterAutoCompleteFacet.class);
    }

    private static boolean hasChoicesOrAutoComplete(final OneToOneAssociation prop) {
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
        if(!hasChoicesOrAutoComplete(param)) {
            FacetUtil.addFacet(
                    new ActionParameterChoicesFacetFromParentedCollection(param, coll));
        }
    }

    private void checkParamHasChoicesOrAutoCompleteWhenRequired(final ObjectActionParameter param) {
        var elementType = param.getElementType();
        if(elementType == null
                || elementType.getBeanSort().isManagedBeanAny()
                || elementType.getBeanSort().isMixin()
                || elementType.getBeanSort().isVetoed()) {
            // ignore, as these cases are covered later by meta-model validation
            return;
        }
        if(elementType.isEntityOrViewModel()
                || param.isPlural()) {
            if(!hasChoicesOrAutoComplete(param)) {

                ValidationFailure.raiseFormatted(param,
                        ProgrammingModelConstants.MessageTemplate.PARAMETER_HAS_NO_CHOICES_NOR_AUTOCOMPLETE.builder()
                            .addVariable("paramId", param.getFeatureIdentifier().toString())
                            .buildMessage());
            }
        }
    }

}
