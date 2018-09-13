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

package org.apache.isis.core.metamodel.postprocessors.param;

import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionCollectionParameterDefaultsAndChoicesPostProcessor implements ObjectSpecificationPostProcessor,
ServicesInjectorAware {

    private DeploymentCategoryProvider deploymentCategoryProvider;
    private SpecificationLoader specificationLoader;
    private AuthenticationSessionProvider authenticationSessionProvider;
    private ObjectAdapterProvider adapterProvider;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        // all the actions of this type
        final List<ActionType> actionTypes = inferActionTypes();

        // and all the collections of this type
        final Stream<OneToManyAssociation> collections = objectSpecification.streamCollections(Contributed.INCLUDED);

        // for each collection, ...
        collections.forEach(collection->{
            // ... see if any of its actions has a collection parameter of the same type
            //
            // eg Order#getItems() and Order#removeItems(List<OrderItem>)
            //
            final ObjectSpecification specification = collection.getSpecification();

            final ObjectActionParameter.Predicates.CollectionParameter whetherCollectionParamOfType =
                    new ObjectActionParameter.Predicates.CollectionParameter(specification);

            final ObjectActionParameter.Predicates.ScalarParameter whetherScalarParamOfType =
                    new ObjectActionParameter.Predicates.ScalarParameter(specification);

            objectSpecification.streamObjectActions(actionTypes, Contributed.INCLUDED)
                    .filter(ObjectAction.Predicates.associatedWith(collection))
                    .forEach(action->{

                final List<ObjectActionParameter> parameters = action.getParameters();

                final ImmutableList<ObjectActionParameter> compatibleCollectionParams = FluentIterable.from(parameters)
                        .filter(whetherCollectionParamOfType::test).toList();

                final ImmutableList<ObjectActionParameter> compatibleScalarParams = FluentIterable.from(parameters)
                        .filter(whetherScalarParamOfType::test).toList();

                // for collection parameters, install an defaults facet (if there isn't one already)
                // this will cause the UI to render the collection with toggleboxes
                // with a thread-local used to provide the selected objects
                for (final ObjectActionParameter collectionParam : compatibleCollectionParams) {
                    addDefaultsFacetIfNoneAlready(collectionParam);
                }

                // for compatible collection parameters, install a choices facet (if there isn't one already)
                // using the associated collection for its values
                for (final ObjectActionParameter collectionParam : compatibleCollectionParams) {
                    addChoicesFacetIfNoneAlready(collection, collectionParam);
                }

                // similarly for compatible scalar parameters, install a choices facet (if there isn't one already)
                // using the associated collection for its values.
                for (final ObjectActionParameter scalarParam : compatibleScalarParams) {
                    addChoicesFacetIfNoneAlready(collection, scalarParam);
                }
            });
        });

    }

    private void addDefaultsFacetIfNoneAlready(final ObjectActionParameter collectionParam) {
        final ActionParameterDefaultsFacet defaultsFacet =
                collectionParam.getFacet(ActionParameterDefaultsFacet.class);
        if (existsAndNotDerived(defaultsFacet)) {
            // don't overwrite existing facet
        } else {
            FacetUtil.addFacet(new ActionParameterDefaultsFacetFromAssociatedCollection(collectionParam));
        }
    }

    private void addChoicesFacetIfNoneAlready(
            final OneToManyAssociation otma,
            final ObjectActionParameter scalarOrCollectionParam) {

        final ActionParameterChoicesFacet choicesFacet = scalarOrCollectionParam
                .getFacet(ActionParameterChoicesFacet.class);
        final ActionParameterAutoCompleteFacet autoCompleteFacet = scalarOrCollectionParam
                .getFacet(ActionParameterAutoCompleteFacet.class);

        if (existsAndNotDerived(choicesFacet) || existsAndNotDerived(autoCompleteFacet)) {
            // don't overwrite existing choices or autoComplete facet
        } else {
            FacetUtil.addFacet(
                    new ActionParameterChoicesFacetFromParentedCollection(
                            scalarOrCollectionParam, otma,
                            getDeploymentCategory(), specificationLoader,
                            authenticationSessionProvider, adapterProvider ));
        }
    }

    private static boolean existsAndNotDerived(final Facet facet) {
        return facet != null && !facet.isNoop() && !facet.isNoop();
    }

    private List<ActionType> inferActionTypes() {
        final List<ActionType> actionTypes = Lists.newArrayList();
        actionTypes.add(ActionType.USER);
        final DeploymentCategory deploymentCategory = getDeploymentCategory();
        if ( !deploymentCategory.isProduction()) {
            actionTypes.add(ActionType.PROTOTYPE);
        }
        return actionTypes;
    }

    private DeploymentCategory getDeploymentCategory() {
        return deploymentCategoryProvider.getDeploymentCategory();
    }

    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        deploymentCategoryProvider = servicesInjector.getDeploymentCategoryProvider();
        specificationLoader = servicesInjector.getSpecificationLoader();
        authenticationSessionProvider = servicesInjector.getAuthenticationSessionProvider();
        adapterProvider = servicesInjector.getPersistenceSessionServiceInternal();
    }

}
