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
package org.apache.causeway.core.metamodel.postprocessors.members;

import java.lang.reflect.Method;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.Property;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.commons.internal.reflection._Annotations;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.DomainEventFacetAbstract.EventTypeOrigin;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.actions.action.invocation.ActionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.collections.collection.CollectionAnnotationFacetFactory;
import org.apache.causeway.core.metamodel.facets.collections.collection.modify.CollectionDomainEventFacet;
import org.apache.causeway.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.causeway.core.metamodel.facets.properties.property.PropertyAnnotationFacetFactory;
import org.apache.causeway.core.metamodel.facets.properties.property.modify.PropertyDomainEventFacet;
import org.apache.causeway.core.metamodel.postprocessors.ObjectSpecificationPostProcessorAbstract;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToManyAssociationMixedIn;
import org.apache.causeway.core.metamodel.specloader.specimpl.OneToOneAssociationMixedIn;

public class TweakDomainEventsForMixinPostProcessor
extends ObjectSpecificationPostProcessorAbstract {

    @Inject
    public TweakDomainEventsForMixinPostProcessor(final MetaModelContext metaModelContext) {
        super(metaModelContext);
    }

    @Override
    public void postProcessAction(final ObjectSpecification objectSpecification, final ObjectAction objectAction) {
        if(objectAction.isMixedIn()) {
            // unlike collection and property mixins, there is no need to create the DomainEventFacet, it will
            // have been created in the ActionAnnotationFacetFactory
            objectAction
                .lookupFacet(ActionDomainEventFacet.class)
                .ifPresent(actionDomainEventFacet->
                    actionDomainEventFacet.initWithMixee(objectSpecification));
        }
    }

    @Override
    public void postProcessProperty(final ObjectSpecification objectSpecification, final OneToOneAssociation property) {

        if(property instanceof OneToOneAssociationMixedIn) {
            final OneToOneAssociationMixedIn propertyMixin = (OneToOneAssociationMixedIn) property;
            final FacetedMethod facetedMethod = propertyMixin.getFacetedMethod();
            final Method method = facetedMethod.getMethod().asMethodElseFail(); // no-arg method, should have a regular facade

            {
                // this is basically a subset of the code that is in CollectionAnnotationFacetFactory,
                // ignoring stuff which is deprecated for Causeway v2

                final Property propertyAnnot =
                        _Annotations.synthesize(method, Property.class)
                        .orElse(null);

                if(propertyAnnot != null) {
                    final Class<? extends PropertyDomainEvent<?, ?>> propertyDomainEventType =
                            PropertyAnnotationFacetFactory.defaultFromDomainObjectIfRequired(
                                    objectSpecification, propertyAnnot.domainEvent());
                    final PropertyOrCollectionAccessorFacet getterFacetIfAny = null;
                    FacetUtil.addFacet(
                            new PropertyDomainEventFacet(
                                    propertyDomainEventType, EventTypeOrigin.ANNOTATED_MEMBER, getterFacetIfAny, property));
                }
            }

            property
                .lookupFacet(PropertyDomainEventFacet.class)
                .ifPresent(propertyDomainEventFacet->
                    propertyDomainEventFacet.initWithMixee(objectSpecification));
        }
    }

    @Override
    public void postProcessCollection(final ObjectSpecification objectSpecification, final OneToManyAssociation collection) {

        if(collection instanceof OneToManyAssociationMixedIn) {
            final OneToManyAssociationMixedIn collectionMixin = (OneToManyAssociationMixedIn) collection;
            final FacetedMethod facetedMethod = collectionMixin.getFacetedMethod();
            final Method method = facetedMethod.getMethod().asMethodElseFail(); // no-arg method, should have a regular facade

            {
                // this is basically a subset of the code that is in CollectionAnnotationFacetFactory,
                // ignoring stuff which is deprecated for Causeway v2

                final Collection collectionAnnot =
                        _Annotations.synthesize(method, Collection.class)
                                .orElse(null);

                if(collectionAnnot != null) {
                    final Class<? extends CollectionDomainEvent<?, ?>> collectionDomainEventType =
                            CollectionAnnotationFacetFactory.defaultFromDomainObjectIfRequired(
                                    objectSpecification, collectionAnnot.domainEvent());
                    FacetUtil.addFacet(
                            new CollectionDomainEventFacet(
                                    collectionDomainEventType, EventTypeOrigin.ANNOTATED_MEMBER, collection));
                }
            }

            collection
                .lookupFacet(CollectionDomainEventFacet.class)
                .ifPresent(collectionDomainEventFacet->
                    collectionDomainEventFacet.initWithMixee(objectSpecification));

        }
    }

}
