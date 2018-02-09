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

package org.apache.isis.core.metamodel.facets.param.defaults.togglebox;

import java.util.List;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class ActionParameterDefaultsFacetViaToggleBoxesFactory extends FacetFactoryAbstract {

    private DeploymentCategoryProvider deploymentCategoryProvider;

    /**
     * Note that the {@link Facet}s registered are the generic ones from
     * noa-architecture (where they exist)
     */
    public ActionParameterDefaultsFacetViaToggleBoxesFactory() {
        super(ImmutableList.of(FeatureType.OBJECT_POST_PROCESSING));
    }

    @Override
    public void process(final ProcessClassContext processClassContext) {
        final ObjectSpecification objectSpecification = getSpecificationLoader()
                .loadSpecification(processClassContext.getCls());
        postProcess(objectSpecification);

    }

    /**
     * NOT API.
     * Called as special case in SpecificationLoader.
     */
    public void postProcess(final ObjectSpecification objectSpecification) {

        // all the actions of this type
        final List<ActionType> actionTypes = inferActionTypes();
        List<ObjectAction> objectActions = objectSpecification.getObjectActions(actionTypes, Contributed.INCLUDED, Filters.<ObjectAction>any());

        // and all the collections of this type
        final List<OneToManyAssociation> oneToManyAssociations =
                objectSpecification.getCollections(Contributed.INCLUDED);

        // for each collection, ...
        for (final OneToManyAssociation otma : oneToManyAssociations) {

            // ... see if any of its actions has a collection parameter of the same type
            //
            // eg Order#getItems() and Order#removeItems(List<OrderItem>)
            //
            final String collectionId = otma.getId();
            final ObjectSpecification specification = otma.getSpecification();
            final ImmutableList<ObjectAction> actions = FluentIterable.from(objectActions)
                    .filter(
                        ObjectAction.Predicates.associatedWithAndWithCollectionParameterFor(collectionId, specification))
                    .toList();

            //
            // ... for the matching actions, install the default facet populated using toggle boxes
            //
            final ObjectActionParameter.Predicates.CollectionParameter whetherCollectionParamOfType =
                    new ObjectActionParameter.Predicates.CollectionParameter(specification);
            for (final ObjectAction action : actions) {
                final List<ObjectActionParameter> parameters = action.getParameters();
                final ImmutableList<ObjectActionParameter> collectionParams = FluentIterable.from(parameters)
                        .filter(whetherCollectionParamOfType).toList();
                for (final ObjectActionParameter collectionParam : collectionParams) {
                    FacetUtil.addFacet(new ActionParameterDefaultsFacetViaToggleBoxes(collectionParam));
                }
            }
        }
    }

    private List<ActionType> inferActionTypes() {
        final List<ActionType> actionTypes = Lists.newArrayList();
        actionTypes.add(ActionType.USER);
        final DeploymentCategory deploymentCategory = deploymentCategoryProvider.getDeploymentCategory();
        if ( !deploymentCategory.isProduction()) {
            actionTypes.add(ActionType.PROTOTYPE);
        }
        return actionTypes;
    }


    @Override
    public void setServicesInjector(final ServicesInjector servicesInjector) {
        super.setServicesInjector(servicesInjector);
        deploymentCategoryProvider = servicesInjector.getDeploymentCategoryProvider();
    }

}
