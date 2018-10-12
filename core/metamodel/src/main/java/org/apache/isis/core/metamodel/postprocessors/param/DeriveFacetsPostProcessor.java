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

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.isis.applib.filter.Filters;
import org.apache.isis.core.commons.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.deployment.DeploymentCategoryProvider;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.collections.disabled.fromimmutable.DisabledFacetOnCollectionDerivedFromImmutable;
import org.apache.isis.core.metamodel.facets.collections.disabled.fromimmutable.DisabledFacetOnCollectionDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberDerivedFromType;
import org.apache.isis.core.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.immutableannot.CopyImmutableFacetOntoMembersFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnCollectionDerivedFromRecreatableObject;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnCollectionDerivedFromViewModelFacetFactory;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObject;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFacets;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromDefaultedFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutable;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromType;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorAware;
import org.apache.isis.core.metamodel.services.persistsession.PersistenceSessionServiceInternal;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionParameterAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveFacetsPostProcessor implements ObjectSpecificationPostProcessor,
        ServicesInjectorAware {

    private DeploymentCategoryProvider deploymentCategoryProvider;
    private SpecificationLoader specificationLoader;
    private AuthenticationSessionProvider authenticationSessionProvider;
    private PersistenceSessionServiceInternal adapterManager;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        // all the actions of this type
        final List<ActionType> actionTypes = inferActionTypes();
        List<ObjectAction> objectActions = objectSpecification.getObjectActions(actionTypes, Contributed.INCLUDED, Filters.<ObjectAction>any());

        // and all the collections of this type
        final List<OneToManyAssociation> collections = objectSpecification.getCollections(Contributed.INCLUDED);

        // and all the properties of this type
        final List<OneToOneAssociation> properties = objectSpecification.getProperties(Contributed.INCLUDED);

        // for each action, ...
        for (final ObjectAction objectAction : objectActions) {

            // ... for each action parameter
            final List<ObjectActionParameter> parameters = objectAction.getParameters();

            for (final ObjectActionParameter parameter : parameters) {

                deriveParameterDefaultFacetFromType(parameter);
                deriveParameterChoicesFromExistingChoices(parameter);
                deriveParameterDescribedAsFromType(parameter);
                deriveParameterTypicalLengthFromType(parameter);
            }

            deriveActionDescribedAsFromType(objectAction);

            // previously was also copying ImmutableFacet from spec onto Action (as for properties and collections ...
            // corresponds to CopyImmutableFacetOntoMembersFactory.  However, ImmutableFacet only ever disables for
            // properties and collections, so no point in copying over.

        }

        // for each property, ...
        for (final OneToOneAssociation property : properties) {
            derivePropertyChoicesFromExistingChoices(property);
            derivePropertyDefaultsFromType(property);
            derivePropertyTypicalLengthFromType(property);
            derivePropertyOrCollectionDescribedAsFromType(property);
            derivePropertyDisabledFromViewModel(property);
            derivePropertyOrCollectionImmutableFromSpec(property);
            derivePropertyDisabledFromImmutable(property);
        }


        // for each collection, ...
        for (final OneToManyAssociation collection : collections) {

            derivePropertyOrCollectionDescribedAsFromType(collection);
            deriveCollectionDisabledFromViewModel(collection);
            derivePropertyOrCollectionImmutableFromSpec(collection);
            deriveCollectionDisabledFromImmutable(collection);

            // ... see if any of its actions has a collection parameter of the same type
            //
            // eg Order#getItems() and Order#removeItems(List<OrderItem>)
            //
            final ObjectSpecification specification = collection.getSpecification();

            final ObjectActionParameter.Predicates.CollectionParameter whetherCollectionParamOfType =
                    new ObjectActionParameter.Predicates.CollectionParameter(specification);

            final ObjectActionParameter.Predicates.ScalarParameter whetherScalarParamOfType =
                    new ObjectActionParameter.Predicates.ScalarParameter(specification);

            final ImmutableList<ObjectAction> actionsAssociatedWithCollection = FluentIterable.from(objectActions)
                    .filter(ObjectAction.Predicates.associatedWith(collection))
                    .toList();

            for (final ObjectAction action : actionsAssociatedWithCollection) {

                final List<ObjectActionParameter> parameters = action.getParameters();

                final ImmutableList<ObjectActionParameter> compatibleCollectionParams = FluentIterable.from(parameters)
                        .filter(whetherCollectionParamOfType).toList();

                final ImmutableList<ObjectActionParameter> compatibleScalarParams = FluentIterable.from(parameters)
                        .filter(whetherScalarParamOfType).toList();

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
            }
        }
    }

    static DisabledFacetAbstract.Semantics inferSemanticsFrom(final ViewModelFacet facet) {
        return facet.isImplicitlyImmutable() ?
                DisabledFacetAbstract.Semantics.DISABLED :
                DisabledFacetAbstract.Semantics.ENABLED;
    }

    private FacetedMethod facetedMethodFor(final ObjectMember objectMember) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the action/association itself.
        final ObjectMemberAbstract objectActionImpl = (ObjectMemberAbstract) objectMember;
        return objectActionImpl.getFacetedMethod();
    }
    private TypedHolder peerFor(final ObjectActionParameter param) {
        // TODO: hacky, need to copy facet onto underlying peer, not to the param itself.
        final ObjectActionParameterAbstract objectActionImpl = (ObjectActionParameterAbstract) param;
        return objectActionImpl.getPeer();
    }

    /**
     * Replaces some of the functionality in {@link DescribedAsFacetOnMemberFactory}.
     */
    private void deriveActionDescribedAsFromType(final ObjectAction objectAction) {
        if(objectAction.containsDoOpFacet(DescribedAsFacet.class)) {
            return;
        }
        final ObjectSpecification returnSpec = objectAction.getReturnType();
        final DescribedAsFacet specFacet = returnSpec.getFacet(DescribedAsFacet.class);
        //TODO: this ought to check if a do-op; if you come across this, you can probably change it (just taking smaller steps for now)
        //if(existsAndIsDoOp(specFacet)) {
        if(specFacet != null) {
            FacetUtil.addFacet(new DescribedAsFacetOnMemberDerivedFromType(specFacet, facetedMethodFor(objectAction)));
        }
    }

    /**
     * Replaces {@link org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory}
     */
    private void deriveParameterDefaultFacetFromType(final ObjectActionParameter parameter) {

        if (parameter.containsDoOpFacet(ActionDefaultsFacet.class)) {
            return;
        }

        // this loop within the outer loop (for every param) is really weird,
        // but arises from porting the old facet factory
        final ObjectAction objectAction = parameter.getAction();
        final List<ObjectSpecification> parameterSpecs = objectAction.getParameterTypes();
        final DefaultedFacet[] parameterTypeDefaultedFacets = new DefaultedFacet[parameterSpecs.size()];
        boolean hasAtLeastOneDefault = false;
        for (int i = 0; i < parameterSpecs.size(); i++) {
            final ObjectSpecification parameterSpec = parameterSpecs.get(i);
            parameterTypeDefaultedFacets[i] = parameterSpec.getFacet(DefaultedFacet.class);
            hasAtLeastOneDefault = hasAtLeastOneDefault | (parameterTypeDefaultedFacets[i] != null);
        }
        if (hasAtLeastOneDefault) {
            FacetUtil.addFacet(
                new ActionParameterDefaultFacetDerivedFromTypeFacets(parameterTypeDefaultedFacets, peerFor(parameter)));
        }
    }

    /**
     * Replaces {@link ActionParameterChoicesFacetDerivedFromChoicesFacetFactory}.
     */
    private void deriveParameterChoicesFromExistingChoices(final ObjectActionParameter parameter) {
        if(parameter.containsDoOpFacet(ActionParameterChoicesFacet.class)) {
            return;
        }
        final ObjectSpecification paramSpec = parameter.getSpecification();
        final ChoicesFacet specFacet = paramSpec.getFacet(ChoicesFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(
                new ActionParameterChoicesFacetDerivedFromChoicesFacet(
                    peerFor(parameter),
                    getDeploymentCategory(), specificationLoader, authenticationSessionProvider, adapterManager));
        }
    }

    /**
     * Replaces {@link DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory}
     */
    private void deriveParameterDescribedAsFromType(final ObjectActionParameter parameter) {
        if(parameter.containsDoOpFacet(DescribedAsFacet.class)) {
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
     * Replaces {@link TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory}
     */
    private void deriveParameterTypicalLengthFromType(final ObjectActionParameter parameter) {
        if(parameter.containsDoOpFacet(TypicalLengthFacet.class)) {
            return;
        }
        final ObjectSpecification paramSpec = parameter.getSpecification();
        final TypicalLengthFacet specFacet = paramSpec.getFacet(TypicalLengthFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(new TypicalLengthFacetOnParameterDerivedFromType(specFacet, peerFor(parameter)));
        }
    }

    /**
     * Replaces {@link PropertyChoicesFacetDerivedFromChoicesFacetFactory}
     */
    private void derivePropertyChoicesFromExistingChoices(final OneToOneAssociation property) {
        if(property.containsDoOpFacet(PropertyChoicesFacet.class)) {
            return;
        }
        final ObjectSpecification propertySpec = property.getSpecification();
        final ChoicesFacet specFacet = propertySpec.getFacet(ChoicesFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(
                    new PropertyChoicesFacetDerivedFromChoicesFacet(facetedMethodFor(property), specificationLoader));
        }
    }

    /**
     * Replaces {@link PropertyDefaultFacetDerivedFromTypeFactory}
     */
    private void derivePropertyDefaultsFromType(final OneToOneAssociation property) {
        if(property.containsDoOpFacet(PropertyDefaultFacet.class)) {
            return;
        }
        final ObjectSpecification propertySpec = property.getSpecification();
        final DefaultedFacet specFacet = propertySpec.getFacet(DefaultedFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(
                new PropertyDefaultFacetDerivedFromDefaultedFacet(
                        specFacet, facetedMethodFor(property), adapterManager));
        }
    }

    /**
     * replaces {@link TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory}
     */
    private void derivePropertyTypicalLengthFromType(final OneToOneAssociation property) {
        if(property.containsDoOpFacet(TypicalLengthFacet.class)) {
            return;
        }
        final ObjectSpecification propertySpec = property.getSpecification();
        final TypicalLengthFacet specFacet = propertySpec.getFacet(TypicalLengthFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(
                    new TypicalLengthFacetOnPropertyDerivedFromType(specFacet, facetedMethodFor(property)));
        }
    }

    /**
     * Replaces some of the functionality in {@link DescribedAsFacetOnMemberFactory}.
     */
    private void derivePropertyOrCollectionDescribedAsFromType(final ObjectAssociation objectAssociation) {
        if(objectAssociation.containsDoOpFacet(DescribedAsFacet.class)) {
            return;
        }
        final ObjectSpecification returnSpec = objectAssociation.getSpecification();
        final DescribedAsFacet specFacet = returnSpec.getFacet(DescribedAsFacet.class);
        //TODO: this ought to check if a do-op; if you come across this, you can probably change it (just taking smaller steps for now)
        //if(existsAndIsDoOp(specFacet)) {
        if(specFacet != null) {
            FacetUtil.addFacet(
                    new DescribedAsFacetOnMemberDerivedFromType(specFacet, facetedMethodFor(objectAssociation)));
        }
    }

    /**
     * Replaces {@link org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory}
     * @param property
     */
    private void derivePropertyDisabledFromViewModel(final OneToOneAssociation property) {
        if(property.containsDoOpFacet(DisabledFacet.class)){
            return;
        }

        final ObjectSpecification propertySpec = property.getOnType();
        final ViewModelFacet specFacet = propertySpec.getFacet(ViewModelFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            final DisabledFacetAbstract.Semantics semantics = inferSemanticsFrom(specFacet);
            FacetUtil.addFacet(new DisabledFacetOnPropertyDerivedFromRecreatableObject(facetedMethodFor(property), semantics));
        }
    }


    /**
     * Replaces {@link DisabledFacetOnPropertyDerivedFromImmutableFactory}
     */
    private void derivePropertyDisabledFromImmutable(final OneToOneAssociation property) {
        if(property.containsDoOpFacet(DisabledFacet.class)) {
            return;
        }
        final ObjectSpecification onType = property.getOnType();
        final ImmutableFacet specFacet = onType.getFacet(ImmutableFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(
                    new DisabledFacetOnPropertyDerivedFromImmutable(specFacet, facetedMethodFor(property)));
        }
    }

    /**
     * Replaces {@link CopyImmutableFacetOntoMembersFactory}
     *
     * TODO: this looks to be redundant, see {@link #derivePropertyDisabledFromImmutable(OneToOneAssociation)} and {@link #deriveCollectionDisabledFromImmutable(OneToManyAssociation)}.  What differs is the implementation of the disabling facet (DisabledFacetOn... vs ImmutableFacetFor...).
     */
    private void derivePropertyOrCollectionImmutableFromSpec(final ObjectAssociation objectAssociation) {
        if(objectAssociation.containsDoOpFacet(DisabledFacet.class)) {
            return;
        }
        final ObjectSpecification owningSpec = objectAssociation.getOnType();
        final ImmutableFacet specFacet = owningSpec.getFacet(ImmutableFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            specFacet.copyOnto(facetedMethodFor(objectAssociation));
        }
    }


    /**
     * Replaces {@link DisabledFacetOnCollectionDerivedFromViewModelFacetFactory}
     * @param collection
     */
    private void deriveCollectionDisabledFromViewModel(final OneToManyAssociation collection) {
        if(collection.containsDoOpFacet(DisabledFacet.class)){
            return;
        }

        final ObjectSpecification collectionSpec = collection.getOnType();
        final ViewModelFacet specFacet = collectionSpec.getFacet(ViewModelFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            final DisabledFacetAbstract.Semantics semantics = inferSemanticsFrom(specFacet);
            FacetUtil.addFacet(new DisabledFacetOnCollectionDerivedFromRecreatableObject(facetedMethodFor(collection), semantics));
        }

    }

    /**
     * Replaces {@link DisabledFacetOnCollectionDerivedFromImmutableFactory}
     */
    private void deriveCollectionDisabledFromImmutable(final OneToManyAssociation collection) {
        if(collection.containsDoOpFacet(DisabledFacet.class)) {
            return;
        }
        final ObjectSpecification onType = collection.getOnType();
        final ImmutableFacet specFacet = onType.getFacet(ImmutableFacet.class);
        if(existsAndIsDoOp(specFacet)) {
            FacetUtil.addFacet(
                    new DisabledFacetOnCollectionDerivedFromImmutable(specFacet, facetedMethodFor(collection)));
        }
    }



    private void addCollectionParamDefaultsFacetIfNoneAlready(final ObjectActionParameter collectionParam) {
        if(collectionParam.containsDoOpFacet(ActionParameterDefaultsFacet.class)) {
            return;
        }
        FacetUtil.addFacet(new ActionParameterDefaultsFacetFromAssociatedCollection(collectionParam));
    }

    private void addCollectionParamChoicesFacetIfNoneAlready(
            final OneToManyAssociation otma,
            final ObjectActionParameter scalarOrCollectionParam) {
        if (scalarOrCollectionParam.containsDoOpFacet(ActionParameterChoicesFacet.class) ||
            scalarOrCollectionParam.containsDoOpFacet(ActionParameterAutoCompleteFacet.class)) {
            return;
        }

        FacetUtil.addFacet(
                new ActionParameterChoicesFacetFromParentedCollection(
                        scalarOrCollectionParam, otma,
                        getDeploymentCategory(), specificationLoader,
                        authenticationSessionProvider, adapterManager ));
    }

    private static boolean existsAndIsDoOp(final Facet facet) {
        return facet != null && !facet.isNoop();
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
        adapterManager = servicesInjector.getPersistenceSessionServiceInternal();
    }

}
