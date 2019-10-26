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

package org.apache.isis.metamodel.postprocessors.param;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.MetaModelContextAware;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facetapi.FacetUtil;
import org.apache.isis.metamodel.facets.Annotations;
import org.apache.isis.metamodel.facets.FacetedMethod;
import org.apache.isis.metamodel.facets.TypedHolder;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetAbstract;
import org.apache.isis.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.metamodel.facets.collections.disabled.fromimmutable.DisabledFacetOnCollectionDerivedFromImmutable;
import org.apache.isis.metamodel.facets.collections.disabled.fromimmutable.DisabledFacetOnCollectionDerivedFromImmutableFactory;
import org.apache.isis.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberDerivedFromType;
import org.apache.isis.metamodel.facets.members.describedas.annotprop.DescribedAsFacetOnMemberFactory;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.metamodel.facets.object.projection.ProjectionFacetFromProjectingProperty;
import org.apache.isis.metamodel.facets.object.projection.ident.IconFacetDerivedFromProjectionFacet;
import org.apache.isis.metamodel.facets.object.projection.ident.TitleFacetDerivedFromProjectionFacet;
import org.apache.isis.metamodel.facets.object.recreatable.DisabledFacetOnCollectionDerivedFromRecreatableObject;
import org.apache.isis.metamodel.facets.object.recreatable.DisabledFacetOnCollectionDerivedFromViewModelFacetFactory;
import org.apache.isis.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObject;
import org.apache.isis.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFacets;
import org.apache.isis.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterAnnotationElseDerivedFromTypeFactory;
import org.apache.isis.metamodel.facets.param.describedas.annotderived.DescribedAsFacetOnParameterDerivedFromType;
import org.apache.isis.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromType;
import org.apache.isis.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromDefaultedFacet;
import org.apache.isis.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutable;
import org.apache.isis.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutableFactory;
import org.apache.isis.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromType;
import org.apache.isis.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.metamodel.spec.ActionType;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.feature.Contributed;
import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.spec.feature.ObjectMember;
import org.apache.isis.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.metamodel.specloader.specimpl.ObjectActionParameterAbstract;
import org.apache.isis.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.metamodel.specloader.specimpl.OneToManyAssociationMixedIn;
import org.apache.isis.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

import lombok.Setter;
import lombok.val;

/**
 * Sets up all the {@link Facet}s for an action in a single shot.
 */
public class DeriveFacetsPostProcessor 
implements ObjectSpecificationPostProcessor, MetaModelContextAware {
    
    @Setter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    @Override
    public void postProcess(final ObjectSpecification objectSpecification) {

        // all the actions of this type
        final List<ActionType> actionTypes = inferActionTypes();
        final Stream<ObjectAction> objectActions = objectSpecification.streamObjectActions(actionTypes, Contributed.INCLUDED);

        // and all the collections of this type
        final Stream<OneToManyAssociation> collections = objectSpecification.streamCollections(Contributed.INCLUDED);

        // and all the properties of this type
        final Stream<OneToOneAssociation> properties = objectSpecification.streamProperties(Contributed.INCLUDED);

        // for each action, ...
        objectActions.forEach(objectAction -> {

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

            tweakActionDomainEventForMixin(objectSpecification, objectAction);

        });

        // for each property, ...
        properties.forEach(property -> {
            derivePropertyChoicesFromExistingChoices(property);
            derivePropertyDefaultsFromType(property);
            derivePropertyTypicalLengthFromType(property);
            derivePropertyOrCollectionDescribedAsFromType(property);
            derivePropertyDisabledFromViewModel(property);
            derivePropertyDisabledFromImmutable(property);
            tweakPropertyMixinDomainEvent(objectSpecification, property);
        });


        // for each collection, ...
        collections.forEach(collection->{

            derivePropertyOrCollectionDescribedAsFromType(collection);
            deriveCollectionDisabledFromViewModel(collection);
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

            objectSpecification.streamObjectActions(actionTypes, Contributed.INCLUDED)
            .filter(ObjectAction.Predicates.associatedWith(collection))
            .forEach(action->{

                final List<ObjectActionParameter> parameters = action.getParameters();

                final List<ObjectActionParameter> compatibleCollectionParams =
                        Collections.unmodifiableList(
                                _Lists.filter(parameters, whetherCollectionParamOfType));

                final List<ObjectActionParameter> compatibleScalarParams = 
                        Collections.unmodifiableList(
                                _Lists.filter(parameters, whetherScalarParamOfType));

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
            deriveCollectionDomainEventForMixins(objectSpecification, collection);
        });
        deriveProjectionFacets(objectSpecification);
    }

    private void deriveProjectionFacets(final ObjectSpecification objectSpecification) {
        val projectionFacet = ProjectionFacetFromProjectingProperty.create(objectSpecification);
        if (projectionFacet == null) {
            return;
        }
        this.addFacet(projectionFacet);
        val titleFacet = objectSpecification.getFacet(TitleFacet.class);
        if(canOverwrite(titleFacet)) {
            this.addFacet(new TitleFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
        val iconFacet = objectSpecification.getFacet(IconFacet.class);
        if(canOverwrite(iconFacet)) {
            this.addFacet(new IconFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
        val cssClassFacet = objectSpecification.getFacet(CssClassFacet.class);
        if(canOverwrite(cssClassFacet)) {
            this.addFacet(new IconFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
    }

    private static boolean canOverwrite(final Facet facet) {
        return facet == null || facet.isFallback() || facet.isDerived();
    }

    private void tweakActionDomainEventForMixin(
            final ObjectSpecification objectSpecification,
            final ObjectAction objectAction) {
        if(objectAction instanceof ObjectActionMixedIn) {
            // unlike collection and property mixins, there is no need to create the DomainEventFacet, it will
            // have been created in the ActionAnnotationFacetFactory
            final ActionDomainEventDefaultFacetForDomainObjectAnnotation actionDomainEventDefaultFacet =
                    objectSpecification.getFacet(ActionDomainEventDefaultFacetForDomainObjectAnnotation.class);

            if(actionDomainEventDefaultFacet != null) {
                final ObjectActionMixedIn actionMixedIn = (ObjectActionMixedIn) objectAction;
                final ActionDomainEventFacet actionFacet = actionMixedIn.getFacet(ActionDomainEventFacet.class);
                if (actionFacet instanceof ActionDomainEventFacetAbstract) {
                    final ActionDomainEventFacetAbstract facetAbstract = (ActionDomainEventFacetAbstract) actionFacet;
                    if (facetAbstract.getEventType() == ActionDomainEvent.Default.class) {
                        final ActionDomainEventFacetAbstract existing = (ActionDomainEventFacetAbstract) actionFacet;
                        existing.setEventType(actionDomainEventDefaultFacet.getEventType());
                    }
                }
            }
        }
    }

    private void deriveCollectionDomainEventForMixins(
            final ObjectSpecification objectSpecification,
            final OneToManyAssociation collection) {

        if(collection instanceof OneToManyAssociationMixedIn) {
            final OneToManyAssociationMixedIn collectionMixin = (OneToManyAssociationMixedIn) collection;
            final FacetedMethod facetedMethod = collectionMixin.getFacetedMethod();
            final Method method = facetedMethod != null ? facetedMethod.getMethod() : null;

            if(method != null) {
                // this is basically a subset of the code that is in CollectionAnnotationFacetFactory,
                // ignoring stuff which is deprecated for Isis v2
                final Collection collectionAnnot = Annotations.getAnnotation(method, Collection.class);
                if(collectionAnnot != null) {
                    final Class<? extends CollectionDomainEvent<?, ?>> collectionDomainEventType =
                            CollectionAnnotationFacetFactory.defaultFromDomainObjectIfRequired(
                                    objectSpecification, collectionAnnot.domainEvent());
                    final CollectionDomainEventFacetForCollectionAnnotation collectionDomainEventFacet = 
                            new CollectionDomainEventFacetForCollectionAnnotation(
                                    collectionDomainEventType, collection);
                    this.addFacet(collectionDomainEventFacet);
                }

                final CollectionDomainEventDefaultFacetForDomainObjectAnnotation collectionDomainEventDefaultFacet =
                        objectSpecification.getFacet(CollectionDomainEventDefaultFacetForDomainObjectAnnotation.class);
                if(collectionDomainEventDefaultFacet != null) {
                    final CollectionDomainEventFacet collectionFacet = collection.getFacet(CollectionDomainEventFacet.class);
                    if (collectionFacet instanceof CollectionDomainEventFacetAbstract) {
                        final CollectionDomainEventFacetAbstract facetAbstract = (CollectionDomainEventFacetAbstract) collectionFacet;
                        if (facetAbstract.getEventType() == CollectionDomainEvent.Default.class) {
                            final CollectionDomainEventFacetAbstract existing = (CollectionDomainEventFacetAbstract) collectionFacet;
                            existing.setEventType(collectionDomainEventDefaultFacet.getEventType());
                        }
                    }
                }
            }
        }
    }

    private void tweakPropertyMixinDomainEvent(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation property) {

        if(property instanceof OneToOneAssociationMixedIn) {
            final OneToOneAssociationMixedIn propertyMixin = (OneToOneAssociationMixedIn) property;
            final FacetedMethod facetedMethod = propertyMixin.getFacetedMethod();
            final Method method = facetedMethod != null ? facetedMethod.getMethod() : null;

            if(method != null) {
                // this is basically a subset of the code that is in CollectionAnnotationFacetFactory,
                // ignoring stuff which is deprecated for Isis v2
                final Property propertyAnnot = Annotations.getAnnotation(method, Property.class);
                if(propertyAnnot != null) {
                    final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType =
                            PropertyAnnotationFacetFactory.defaultFromDomainObjectIfRequired(
                                    objectSpecification, propertyAnnot.domainEvent());
                    final PropertyOrCollectionAccessorFacet getterFacetIfAny = null;
                    final PropertyDomainEventFacetForPropertyAnnotation propertyDomainEventFacet =
                            new PropertyDomainEventFacetForPropertyAnnotation(
                                    propertyDomainEventType, getterFacetIfAny, property);
                    this.addFacet(propertyDomainEventFacet);
                }
            }
            final PropertyDomainEventDefaultFacetForDomainObjectAnnotation propertyDomainEventDefaultFacet =
                    objectSpecification.getFacet(PropertyDomainEventDefaultFacetForDomainObjectAnnotation.class);
            if(propertyDomainEventDefaultFacet != null) {
                final PropertyDomainEventFacet propertyFacet = property.getFacet(PropertyDomainEventFacet.class);
                if (propertyFacet instanceof PropertyDomainEventFacetAbstract) {
                    final PropertyDomainEventFacetAbstract facetAbstract = (PropertyDomainEventFacetAbstract) propertyFacet;
                    if (facetAbstract.getEventType() == PropertyDomainEvent.Default.class) {
                        final PropertyDomainEventFacetAbstract existing = (PropertyDomainEventFacetAbstract) propertyFacet;
                        existing.setEventType(propertyDomainEventDefaultFacet.getEventType());
                    }
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
        if(existsAndIsDoOp(specFacet)) {
            this.addFacet(new DescribedAsFacetOnMemberDerivedFromType(specFacet, facetedMethodFor(objectAction)));
        }
    }

    /**
     * Replaces {@link org.apache.isis.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory}
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
            this.addFacet(
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
            this.addFacet(
                    new ActionParameterChoicesFacetDerivedFromChoicesFacet(
                            peerFor(parameter)));
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
            this.addFacet(new DescribedAsFacetOnParameterDerivedFromType(specFacet, peerFor(parameter)));
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
            this.addFacet(new TypicalLengthFacetOnParameterDerivedFromType(specFacet, peerFor(parameter)));
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
            this.addFacet(
                    new PropertyChoicesFacetDerivedFromChoicesFacet(facetedMethodFor(property)));
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
            this.addFacet(
                    new PropertyDefaultFacetDerivedFromDefaultedFacet(
                            specFacet, facetedMethodFor(property)));
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
            this.addFacet(
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
        if(existsAndIsDoOp(specFacet)) {
            this.addFacet(
                    new DescribedAsFacetOnMemberDerivedFromType(specFacet, facetedMethodFor(objectAssociation)));
        }
    }

    /**
     * Replaces {@link org.apache.isis.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory}
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
            this.addFacet(new DisabledFacetOnPropertyDerivedFromRecreatableObject(facetedMethodFor(property), semantics));
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
            this.addFacet(
                    new DisabledFacetOnPropertyDerivedFromImmutable(facetedMethodFor(property)));
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
            this.addFacet(new DisabledFacetOnCollectionDerivedFromRecreatableObject(facetedMethodFor(collection), semantics));
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
            this.addFacet(
                    new DisabledFacetOnCollectionDerivedFromImmutable(specFacet, facetedMethodFor(collection)));
        }
    }



    private void addCollectionParamDefaultsFacetIfNoneAlready(final ObjectActionParameter collectionParam) {
        if(collectionParam.containsDoOpFacet(ActionParameterDefaultsFacet.class)) {
            return;
        }
        this.addFacet(new ActionParameterDefaultsFacetFromAssociatedCollection(collectionParam));
    }

    private void addCollectionParamChoicesFacetIfNoneAlready(
            final OneToManyAssociation otma,
            final ObjectActionParameter scalarOrCollectionParam) {
        if (scalarOrCollectionParam.containsDoOpFacet(ActionParameterChoicesFacet.class) ||
                scalarOrCollectionParam.containsDoOpFacet(ActionParameterAutoCompleteFacet.class)) {
            return;
        }

        this.addFacet(
                new ActionParameterChoicesFacetFromParentedCollection(
                        scalarOrCollectionParam, otma));
    }

    private static boolean existsAndIsDoOp(final Facet facet) {
        return facet != null && !facet.isFallback();
    }

    private List<ActionType> inferActionTypes() {
        final List<ActionType> actionTypes = _Lists.newArrayList();
        actionTypes.add(ActionType.USER);
        if (metaModelContext.getSystemEnvironment().isPrototyping()) {
            actionTypes.add(ActionType.PROTOTYPE);
        }
        return actionTypes;
    }
    
    private void addFacet(Facet facet) {
        FacetUtil.addFacet(facet);
    }

}
