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
package org.apache.isis.core.metamodel.postprocessors.collparam;

import javax.inject.Inject;

import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveCollectionParamDefaultsAndChoicesPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public DeriveCollectionParamDefaultsAndChoicesPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction act) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final ObjectAction objectAction, final ObjectActionParameter param) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToOneAssociation prop) {
    }

    @Override
    protected void doPostProcess(final ObjectSpecification objectSpecification, final OneToManyAssociation collection) {

        // ... see if any of its actions has a collection parameter of the same type
        //
        // eg Order#getItems() and Order#removeItems(List<OrderItem>)
        //
        final ObjectSpecification specification = collection.getElementType();

        final ObjectActionParameter.Predicates.CollectionParameter whetherCollectionParamOfType =
                new ObjectActionParameter.Predicates.CollectionParameter(specification);

        final ObjectActionParameter.Predicates.ScalarParameter whetherScalarParamOfType =
                new ObjectActionParameter.Predicates.ScalarParameter(specification);

        objectSpecification.streamRuntimeActions(MixedIn.INCLUDED)
        .filter(ObjectAction.Predicates.choicesFromAndHavingCollectionParameterFor(collection))
        .forEach(action->{

            val parameters = action.getParameters();

            val compatibleCollectionParams = parameters.filter(whetherCollectionParamOfType);
            val compatibleScalarParams = parameters.filter(whetherScalarParamOfType);

            // for collection parameters, install an defaults facet (if there isn't one already)
            // this will cause the UI to render the collection with toggleboxes
            // with a thread-local used to provide the selected objects
            for (final ObjectActionParameter collectionParam : compatibleCollectionParams) {
                addCollectionParamDefaultsFacetIfNoneAlready(collectionParam);
            }

            // for compatible collection parameters, install a choices facet (if there isn't one already)
            // using the associated collection for its values
            for (final ObjectActionParameter collectionParam : compatibleCollectionParams) {
                addCollectionParamChoicesFacetIfNoneAlready(collection, collectionParam);
            }

            // similarly for compatible scalar parameters, install a choices facet (if there isn't one already)
            // using the associated collection for its values.
            for (final ObjectActionParameter scalarParam : compatibleScalarParams) {
                addCollectionParamChoicesFacetIfNoneAlready(collection, scalarParam);
            }
        });
    }


    private static void addCollectionParamDefaultsFacetIfNoneAlready(
            final ObjectActionParameter collectionParam) {
        if(collectionParam.getParameterIndex()!=0) {
            return; // with current programming model this can only be the first parameter of an action dialog
        }
        if(collectionParam.containsNonFallbackFacet(ActionParameterDefaultsFacet.class)) {
            return;
        }
        FacetUtil.addFacet(ActionParameterDefaultsFacetFromAssociatedCollection
                .create(collectionParam));
    }

    private static void addCollectionParamChoicesFacetIfNoneAlready(
            final OneToManyAssociation otma,
            final ObjectActionParameter scalarOrCollectionParam) {
        if (scalarOrCollectionParam.containsNonFallbackFacet(ActionParameterChoicesFacet.class) ||
                scalarOrCollectionParam.containsNonFallbackFacet(ActionParameterAutoCompleteFacet.class)) {
            return;
        }

        FacetUtil.addFacet(new ActionParameterChoicesFacetFromParentedCollection(
                        scalarOrCollectionParam, otma));
    }


}
