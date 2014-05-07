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
import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.annotation.PostsCollectionAddedToEvent;
import org.apache.isis.applib.services.eventbus.CollectionAddedToEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.ServicesProviderAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionAddToFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.collections.event.PostsAddedToCollectionEventFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class PostsCollectionAddToEventAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesProviderAware, MetaModelValidatorRefiner {

    private ServicesProvider servicesProvider;

    public PostsCollectionAddToEventAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        FacetUtil.addFacet(create(method, processMethodContext.getFacetHolder()));
    }

    private PostsAddedToCollectionEventFacet create(Method method, final FacetHolder holder) {
        final PostsCollectionAddedToEvent annotation = Annotations.getAnnotation(method, PostsCollectionAddedToEvent.class);
        if(annotation == null) {
            return null;
        }
        final PostsAddedToCollectionEventFacet postsAddedToCollectionEventFacet = holder.getFacet(PostsAddedToCollectionEventFacet.class);
        if(postsAddedToCollectionEventFacet == null) {
            return null;
        } 
        final CollectionAddToFacet collectionAddToFacet = holder.getFacet(CollectionAddToFacet.class);
        final CollectionFacet collectionFacet = holder.getFacet(CollectionFacet.class);
        if(postsAddedToCollectionEventFacet != null) {
            holder.removeFacet(postsAddedToCollectionEventFacet);
        }
        if(collectionAddToFacet != null) {
            holder.removeFacet(collectionAddToFacet);
        }
        if(collectionFacet != null) {
            holder.removeFacet(collectionFacet);
        }
        final Class<? extends CollectionAddedToEvent<?,?>> changedEventType = annotation.value();
        return new PostsCollectionAddToEventFacetAnnotation(changedEventType, postsAddedToCollectionEventFacet, collectionAddToFacet, collectionFacet, servicesProvider, holder);
    }

    @Override
    public void setServicesProvider(ServicesProvider servicesProvider) {
        this.servicesProvider = servicesProvider;
    }

    @Override
    public void refineMetaModelValidator(MetaModelValidatorComposite metaModelValidator, IsisConfiguration configuration) {
        metaModelValidator.add(new MetaModelValidatorVisiting(newValidatorVisitor()));
    }

    protected Visitor newValidatorVisitor() {
        return new MetaModelValidatorVisiting.Visitor() {

            @Override
            public boolean visit(ObjectSpecification objectSpec, ValidationFailures validationFailures) {
                List<OneToManyAssociation> objectCollections = objectSpec.getCollections(Contributed.EXCLUDED);
                for (OneToManyAssociation objectCollection : objectCollections) {
                    final SortedByFacet facet = objectCollection.getFacet(SortedByFacet.class);
                    if(facet != null) {
                        final Class<? extends Comparator<?>> cls = facet.value();
                        if(!Comparator.class.isAssignableFrom(cls)) {
                            validationFailures.add("%s#%s is annotated with @SortedBy, but the class specified '%s' is not a Comparator", objectSpec.getIdentifier().getClassName(), objectCollection.getId(), facet.value().getName());
                        }
                    }
                }
                return true;
            }
        };
    }

}
