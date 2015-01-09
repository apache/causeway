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

package org.apache.isis.core.metamodel.facets.collections.interaction;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionInteraction;
import org.apache.isis.applib.annotation.PostsCollectionAddedToEvent;
import org.apache.isis.applib.annotation.PostsCollectionRemovedFromEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.ContributeeMemberFacetFactory;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.collections.collection.CollectionInteractionFacetForCollectionAnnotation;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.facets.propcoll.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

public class CollectionInteractionFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware, ContributeeMemberFacetFactory {

    private ServicesInjector servicesInjector;

    public CollectionInteractionFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();


        //
        // Set up CollectionInteractionFacet, which will act as the hiding/disabling/validating advisor
        //
        final Collection collection = Annotations.getAnnotation(method, Collection.class);
        final CollectionInteraction collectionInteraction = Annotations.getAnnotation(method, CollectionInteraction.class);
        final Class<? extends CollectionDomainEvent<?, ?>> collectionInteractionEventType;

        final CollectionInteractionFacetAbstract collectionInteractionFacet;
        if(collection != null && collection.domainEvent() != null) {
            collectionInteractionEventType = collection.domainEvent();
            collectionInteractionFacet = new CollectionInteractionFacetForCollectionAnnotation(
                    collectionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);

        } else if(collectionInteraction != null) {
            collectionInteractionEventType = collectionInteraction.value();
            collectionInteractionFacet = new CollectionInteractionFacetAnnotation(
                    collectionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
        } else {
            collectionInteractionEventType = CollectionDomainEvent.Default.class;
            collectionInteractionFacet = new CollectionInteractionFacetDefault(
                    collectionInteractionEventType, servicesInjector, getSpecificationLoader(), holder);
        }
        FacetUtil.addFacet(collectionInteractionFacet);


        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if (getterFacet == null) {
            return;
        }

        //
        // if the collection is mutable, then replace the existing addTo and removeFrom facets with equivalents that
        // also post to the event bus.
        //
        // here we support the deprecated annotations
        //
        final PostsCollectionAddedToEvent postsCollectionAddedToEvent = Annotations.getAnnotation(method, PostsCollectionAddedToEvent.class);
        final PostsCollectionRemovedFromEvent postsCollectionRemovedFromEvent = Annotations.getAnnotation(method, PostsCollectionRemovedFromEvent.class);

        final CollectionAddToFacet collectionAddToFacet = holder.getFacet(CollectionAddToFacet.class);
        if (collectionAddToFacet != null) {
            // the current collectionAddToFacet will end up as the underlying facet of one of these facets to be created.
            final CollectionAddToFacetForInteractionAbstract replacementFacet;
            if(collectionInteraction != null) {
                replacementFacet = new CollectionAddToFacetForCollectionInteractionAnnotation(
                        collectionInteractionEventType, getterFacet, collectionAddToFacet, collectionInteractionFacet, holder, servicesInjector);
            } else if (postsCollectionAddedToEvent != null) {
                replacementFacet = new CollectionAddToFacetForPostsCollectionAddedToEventAnnotation(
                        postsCollectionAddedToEvent.value(), getterFacet, collectionAddToFacet, collectionInteractionFacet, holder, servicesInjector);
            } else {
                replacementFacet = new CollectionAddToFacetForCollectionInteractionDefault(
                        collectionInteractionEventType, getterFacet, collectionAddToFacet, collectionInteractionFacet, holder, servicesInjector);
            }
            FacetUtil.addFacet(replacementFacet);
        }

        final CollectionRemoveFromFacet collectionRemoveFromFacet = holder.getFacet(CollectionRemoveFromFacet.class);
        if (collectionRemoveFromFacet != null) {
            // the current collectionRemoveFromFacet will end up as the underlying facet of the PostsCollectionRemovedFromEventFacetAnnotation
            final CollectionRemoveFromFacetForInteractionAbstract replacementFacet;
            if(collectionInteraction != null) {
                replacementFacet = new CollectionRemoveFromFacetForCollectionInteractionAnnotation(collectionInteractionEventType, getterFacet, collectionRemoveFromFacet, collectionInteractionFacet, servicesInjector, holder);
            } else if (postsCollectionRemovedFromEvent != null) {
                replacementFacet = new CollectionRemoveFromFacetForPostsCollectionRemovedFromEventAnnotation(postsCollectionRemovedFromEvent.value(), getterFacet, collectionRemoveFromFacet, collectionInteractionFacet, servicesInjector, holder);
            } else {
                replacementFacet = new CollectionRemoveFromFacetForCollectionInteractionDefault(collectionInteractionEventType, getterFacet, collectionRemoveFromFacet, collectionInteractionFacet, servicesInjector, holder);
            }
            FacetUtil.addFacet(replacementFacet);
        }
    }


    @Override
    public void process(ProcessContributeeMemberContext processMemberContext) {

        final ObjectMember objectMember = processMemberContext.getFacetHolder();

        //
        // an enhancement would be to pick up a custom event, however the contributed collection ultimately maps
        // to an action on a service, and would therefore require a @CollectionInteraction(...) annotated on an action;
        // would look rather odd
        //

        FacetUtil.addFacet(new CollectionInteractionFacetDefault(CollectionDomainEvent.Default.class, servicesInjector, getSpecificationLoader(), objectMember));

    }

    // //////////////////////////////////////

    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
