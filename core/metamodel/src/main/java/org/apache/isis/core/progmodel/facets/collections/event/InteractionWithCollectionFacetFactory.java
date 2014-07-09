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

package org.apache.isis.core.progmodel.facets.collections.event;

import java.lang.reflect.Method;
import org.apache.isis.applib.annotation.InteractionWithCollection;
import org.apache.isis.applib.annotation.PostsCollectionAddedToEvent;
import org.apache.isis.applib.annotation.PostsCollectionRemovedFromEvent;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionInteractionEvent;
import org.apache.isis.applib.services.eventbus.CollectionRemovedFromEvent;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionRemoveFromFacet;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjectorAware;

public class InteractionWithCollectionFacetFactory extends FacetFactoryAbstract implements ServicesInjectorAware {

    private ServicesInjector servicesInjector;

    public InteractionWithCollectionFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        final FacetHolder holder = processMethodContext.getFacetHolder();

        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if (getterFacet == null) {
            return;
        }

        final InteractionWithCollection interactionWithCollection = Annotations.getAnnotation(method, InteractionWithCollection.class);
        final PostsCollectionAddedToEvent postsCollectionAddedToEvent = Annotations.getAnnotation(method, PostsCollectionAddedToEvent.class);
        final PostsCollectionRemovedFromEvent postsCollectionRemovedFromEvent = Annotations.getAnnotation(method, PostsCollectionRemovedFromEvent.class);

        final CollectionAddToFacet collectionAddToFacet = holder.getFacet(CollectionAddToFacet.class);
        if (collectionAddToFacet != null) {
            // the collectionAddToFacet will end up as the underlying facet of the PostsCollectionAddedToEventFacetAnnotation
            if(interactionWithCollection != null) {
                final Class<? extends CollectionInteractionEvent<?,?>> eventType = interactionWithCollection.value();
                FacetUtil.addFacet(new InteractionWithCollectionFacetAddForAnnotation(eventType, getterFacet, collectionAddToFacet, servicesInjector, holder));
            } else if (postsCollectionAddedToEvent != null) {
                final Class<? extends CollectionAddedToEvent<?,?>> eventType = postsCollectionAddedToEvent.value();
                FacetUtil.addFacet(new InteractionWithCollectionFacetAddPostsCollectionAddedToEventAnnotation(eventType, getterFacet, collectionAddToFacet, servicesInjector, holder));
            } else {
                final Class<? extends CollectionInteractionEvent<?,?>> eventType = CollectionInteractionEvent.Default.class;
                FacetUtil.addFacet(new InteractionWithCollectionFacetAddDefault(eventType, getterFacet, collectionAddToFacet, servicesInjector, holder));
            }
        }

        final CollectionRemoveFromFacet collectionRemoveFromFacet = holder.getFacet(CollectionRemoveFromFacet.class);
        if (collectionRemoveFromFacet != null) {
            // the collectionRemoveFromFacet will end up as the underlying facet of the PostsCollectionRemovedFromEventFacetAnnotation
            if(interactionWithCollection != null) {
                final Class<? extends CollectionInteractionEvent<?,?>> eventType = interactionWithCollection.value();
                FacetUtil.addFacet(new InteractionWithCollectionFacetRemoveForAnnotation(eventType, getterFacet, collectionRemoveFromFacet, servicesInjector, holder));
            } else if (postsCollectionRemovedFromEvent != null) {
                final Class<? extends CollectionRemovedFromEvent<?,?>> eventType = postsCollectionRemovedFromEvent.value();
                FacetUtil.addFacet(new InteractionWithCollectionFacetRemovePostsCollectionRemovedFromEventAnnotation(eventType, getterFacet, collectionRemoveFromFacet, servicesInjector, holder));
            } else {
                final Class<? extends CollectionInteractionEvent<?,?>> eventType = CollectionInteractionEvent.Default.class;
                FacetUtil.addFacet(new InteractionWithCollectionFacetRemoveDefault(eventType, getterFacet, collectionRemoveFromFacet, servicesInjector, holder));
            }
        }
    }


    // //////////////////////////////////////

    @Override
    public void setServicesInjector(ServicesInjector servicesInjector) {
        this.servicesInjector = servicesInjector;
    }

}
