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

package org.apache.isis.core.metamodel.postprocessors;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.Property;
import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.reflection._Annotations;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facets.FacetedMethod;
import org.apache.isis.core.metamodel.facets.TypedHolder;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.actions.defaults.ActionDefaultsFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacet;
import org.apache.isis.core.metamodel.facets.members.disabled.DisabledFacetAbstract;
import org.apache.isis.core.metamodel.facets.object.defaults.DefaultedFacet;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.ActionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.CollectionDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.domainevents.PropertyDomainEventDefaultFacetForDomainObjectAnnotation;
import org.apache.isis.core.metamodel.facets.object.domainobject.editing.ImmutableFacetFromConfiguration;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.EditingEnabledFacet;
import org.apache.isis.core.metamodel.facets.object.immutable.ImmutableFacet;
import org.apache.isis.core.metamodel.facets.object.projection.ProjectionFacetFromProjectingProperty;
import org.apache.isis.core.metamodel.facets.object.projection.ident.IconFacetDerivedFromProjectionFacet;
import org.apache.isis.core.metamodel.facets.object.projection.ident.TitleFacetDerivedFromProjectionFacet;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObject;
import org.apache.isis.core.metamodel.facets.object.recreatable.DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory;
import org.apache.isis.core.metamodel.facets.object.title.TitleFacet;
import org.apache.isis.core.metamodel.facets.object.viewmodel.ViewModelFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.choices.ChoicesFacet;
import org.apache.isis.core.metamodel.facets.objectvalue.typicallen.TypicalLengthFacet;
import org.apache.isis.core.metamodel.facets.param.autocomplete.ActionParameterAutoCompleteFacet;
import org.apache.isis.core.metamodel.facets.param.choices.ActionParameterChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.core.metamodel.facets.param.choices.enums.ActionParameterChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.param.defaults.ActionParameterDefaultsFacet;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFacets;
import org.apache.isis.core.metamodel.facets.param.defaults.fromtype.ActionParameterDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromType;
import org.apache.isis.core.metamodel.facets.param.typicallen.fromtype.TypicalLengthFacetOnParameterDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.PropertyChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacet;
import org.apache.isis.core.metamodel.facets.properties.choices.enums.PropertyChoicesFacetDerivedFromChoicesFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.defaults.PropertyDefaultFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromDefaultedFacet;
import org.apache.isis.core.metamodel.facets.properties.defaults.fromtype.PropertyDefaultFacetDerivedFromTypeFactory;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutable;
import org.apache.isis.core.metamodel.facets.properties.disabled.fromimmutable.DisabledFacetOnPropertyDerivedFromImmutableFactory;
import org.apache.isis.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetAbstract;
import org.apache.isis.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacetForPropertyAnnotation;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromType;
import org.apache.isis.core.metamodel.facets.properties.typicallen.fromtype.TypicalLengthFacetOnPropertyDerivedFromTypeFacetFactory;
import org.apache.isis.core.metamodel.progmodel.ObjectSpecificationPostProcessor;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectActionParameterAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.ObjectMemberAbstract;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToManyAssociationMixedIn;
import org.apache.isis.core.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

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

        //XXX in principle it would be sufficient to just process declared members; can optimize if worth the effort

        // all the actions of this type
        val actionTypes = inferActionTypes();
        final Stream<ObjectAction> objectActions = objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED);


        // for each action, ...
        objectActions.forEach(objectAction -> {

            // previously was also copying ImmutableFacet from spec onto Action (as for properties and collections ...
            // corresponds to CopyImmutableFacetOntoMembersFactory.  However, ImmutableFacet only ever disables for
            // properties and collections, so no point in copying over.

            tweakActionDomainEventForMixin(objectSpecification, objectAction);

        });

        objectSpecification.streamProperties(MixedIn.INCLUDED).forEach(property -> {
            derivePropertyDisabledFromViewModel(property);
            derivePropertyDisabledFromImmutable(property);
            tweakPropertyMixinDomainEvent(objectSpecification, property);
        });

        objectSpecification.streamCollections(MixedIn.INCLUDED).forEach(collection->{

            // ... see if any of its actions has a collection parameter of the same type
            //
            // eg Order#getItems() and Order#removeItems(List<OrderItem>)
            //
            final ObjectSpecification specification = collection.getSpecification();

            final ObjectActionParameter.Predicates.CollectionParameter whetherCollectionParamOfType =
                    new ObjectActionParameter.Predicates.CollectionParameter(specification);

            final ObjectActionParameter.Predicates.ScalarParameter whetherScalarParamOfType =
                    new ObjectActionParameter.Predicates.ScalarParameter(specification);

            objectSpecification.streamActions(actionTypes, MixedIn.INCLUDED)
            .filter(ObjectAction.Predicates.associatedWith(collection))
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
            deriveCollectionDomainEventForMixins(objectSpecification, collection);
        });
        deriveProjectionFacets(objectSpecification);
    }

    private static void deriveProjectionFacets(final ObjectSpecification objectSpecification) {
        val projectionFacet = ProjectionFacetFromProjectingProperty.create(objectSpecification);
        if (projectionFacet == null) {
            return;
        }
        FacetUtil.addFacet(projectionFacet);
        val titleFacet = objectSpecification.getFacet(TitleFacet.class);
        if(canOverwrite(titleFacet)) {
            FacetUtil.addFacet(new TitleFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
        val iconFacet = objectSpecification.getFacet(IconFacet.class);
        if(canOverwrite(iconFacet)) {
            FacetUtil.addFacet(new IconFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
        val cssClassFacet = objectSpecification.getFacet(CssClassFacet.class);
        if(canOverwrite(cssClassFacet)) {
            FacetUtil.addFacet(new IconFacetDerivedFromProjectionFacet(projectionFacet, objectSpecification));
        }
    }

    private static void tweakActionDomainEventForMixin(
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

    private static void deriveCollectionDomainEventForMixins(
            final ObjectSpecification objectSpecification,
            final OneToManyAssociation collection) {

        if(collection instanceof OneToManyAssociationMixedIn) {
            final OneToManyAssociationMixedIn collectionMixin = (OneToManyAssociationMixedIn) collection;
            final FacetedMethod facetedMethod = collectionMixin.getFacetedMethod();
            final Method method = facetedMethod != null ? facetedMethod.getMethod() : null;

            if(method != null) {
                // this is basically a subset of the code that is in CollectionAnnotationFacetFactory,
                // ignoring stuff which is deprecated for Isis v2

                final Collection collectionAnnot =
                        _Annotations.synthesizeInherited(method, Collection.class)
                        .orElse(null);

//                _Assert.assertEquals("expected same", collectionAnnot,
//                        Annotations.getAnnotation(method, Collection.class));

                if(collectionAnnot != null) {
                    final Class<? extends CollectionDomainEvent<?, ?>> collectionDomainEventType =
                            CollectionAnnotationFacetFactory.defaultFromDomainObjectIfRequired(
                                    objectSpecification, collectionAnnot.domainEvent());
                    final CollectionDomainEventFacetForCollectionAnnotation collectionDomainEventFacet =
                            new CollectionDomainEventFacetForCollectionAnnotation(
                                    collectionDomainEventType, collection);
                    FacetUtil.addFacet(collectionDomainEventFacet);
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

    private static void tweakPropertyMixinDomainEvent(
            final ObjectSpecification objectSpecification,
            final OneToOneAssociation property) {

        if(property instanceof OneToOneAssociationMixedIn) {
            final OneToOneAssociationMixedIn propertyMixin = (OneToOneAssociationMixedIn) property;
            final FacetedMethod facetedMethod = propertyMixin.getFacetedMethod();
            final Method method = facetedMethod != null ? facetedMethod.getMethod() : null;

            if(method != null) {
                // this is basically a subset of the code that is in CollectionAnnotationFacetFactory,
                // ignoring stuff which is deprecated for Isis v2

                final Property propertyAnnot =
                        _Annotations.synthesizeInherited(method, Property.class)
                        .orElse(null);

//                _Assert.assertEquals("expected same", propertyAnnot,
//                        Annotations.getAnnotation(method, Property.class));

                if(propertyAnnot != null) {
                    final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType =
                            PropertyAnnotationFacetFactory.defaultFromDomainObjectIfRequired(
                                    objectSpecification, propertyAnnot.domainEvent());
                    final PropertyOrCollectionAccessorFacet getterFacetIfAny = null;
                    final PropertyDomainEventFacetForPropertyAnnotation propertyDomainEventFacet =
                            new PropertyDomainEventFacetForPropertyAnnotation(
                                    propertyDomainEventType, getterFacetIfAny, property);
                    FacetUtil.addFacet(propertyDomainEventFacet);
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


    /**
     * Replaces {@link DisabledFacetOnPropertyDerivedFromRecreatableObjectFacetFactory}
     * @param property
     */
    private static void derivePropertyDisabledFromViewModel(final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(DisabledFacet.class)){
            return;
        }
        property.getOnType()
        .lookupNonFallbackFacet(ViewModelFacet.class)
        .ifPresent(specFacet -> FacetUtil.addFacet(new DisabledFacetOnPropertyDerivedFromRecreatableObject(
                                    facetedMethodFor(property), inferSemanticsFrom(specFacet))));
    }


    /**
     * Replaces {@link DisabledFacetOnPropertyDerivedFromImmutableFactory}
     */
    private static void derivePropertyDisabledFromImmutable(final OneToOneAssociation property) {
        if(property.containsNonFallbackFacet(DisabledFacet.class)) {
            return;
        }

        val typeSpec = property.getOnType();

        typeSpec
        .lookupNonFallbackFacet(ImmutableFacet.class)
        .ifPresent(immutableFacet->{

            if(immutableFacet instanceof ImmutableFacetFromConfiguration) {

                val isEditingEnabledOnType = typeSpec.lookupNonFallbackFacet(EditingEnabledFacet.class)
                        .isPresent();

                if(isEditingEnabledOnType) {
                    // @DomainObject(editing=ENABLED)
                    return;
                }

            }

            FacetUtil.addFacet(DisabledFacetOnPropertyDerivedFromImmutable
                            .forImmutable(facetedMethodFor(property), immutableFacet));
        });
    }


    private static void addCollectionParamDefaultsFacetIfNoneAlready(final ObjectActionParameter collectionParam) {
        if(collectionParam.getNumber()!=0) {
            return; // with current programming model this can only be the first parameter of an action dialog
        }
        if(collectionParam.containsNonFallbackFacet(ActionParameterDefaultsFacet.class)) {
            return;
        }
        FacetUtil.addFacet(new ActionParameterDefaultsFacetFromAssociatedCollection(collectionParam));
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

    private static boolean canOverwrite(final Facet facet) {
        return facet == null || facet.isFallback() || facet.isDerived();
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
