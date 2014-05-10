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

import org.apache.isis.applib.annotation.PostsCollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.ServicesProviderAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.event.PostsCollectionAddedToEventFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;

public class PostsCollectionAddedToEventAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesProviderAware {

    private ServicesProvider servicesProvider;

    public PostsCollectionAddedToEventAnnotationFacetFactory() {
        super(FeatureType.COLLECTIONS_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        FacetUtil.addFacet(create(method, processMethodContext.getFacetHolder()));
    }

    private PostsCollectionAddedToEventFacet create(Method method, final FacetHolder holder) {
        final PostsCollectionAddedToEvent annotation = Annotations.getAnnotation(method, PostsCollectionAddedToEvent.class);
        if(annotation == null) {
            return null;
        }
        
        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return null;
        } 
        final CollectionAddToFacet collectionAddToFacet = holder.getFacet(CollectionAddToFacet.class);
        if(collectionAddToFacet == null) {
            return null;
        }
        // the collectionAddToFacet will end up as the underlying facet of the PostsCollectionAddedToEventFacetAnnotation

        final Class<? extends CollectionAddedToEvent<?,?>> changedEventType = annotation.value();
        return new PostsCollectionAddedToEventFacetAnnotation(changedEventType, getterFacet, collectionAddToFacet, servicesProvider, holder);
    }

    @Override
    public void setServicesProvider(ServicesProvider servicesProvider) {
        this.servicesProvider = servicesProvider;
    }

}
