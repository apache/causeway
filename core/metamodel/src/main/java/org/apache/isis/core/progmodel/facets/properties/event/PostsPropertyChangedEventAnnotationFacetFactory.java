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

package org.apache.isis.core.progmodel.facets.properties.event;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.annotation.PostsPropertyChangedEvent;
import org.apache.isis.applib.services.eventbus.PropertyChangedEvent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.ServicesProvider;
import org.apache.isis.core.metamodel.adapter.ServicesProviderAware;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MetaModelValidatorRefiner;
import org.apache.isis.core.metamodel.facets.Annotations;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.accessor.PropertyOrCollectionAccessorFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.properties.event.PostsPropertyChangedEventFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertyClearFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorComposite;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorVisiting.Visitor;
import org.apache.isis.core.metamodel.specloader.validator.ValidationFailures;

public class PostsPropertyChangedEventAnnotationFacetFactory extends FacetFactoryAbstract implements ServicesProviderAware, MetaModelValidatorRefiner {

    private ServicesProvider servicesProvider;

    public PostsPropertyChangedEventAnnotationFacetFactory() {
        super(FeatureType.PROPERTIES_ONLY);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {
        final Method method = processMethodContext.getMethod();
        FacetUtil.addFacet(create(method, processMethodContext.getFacetHolder()));
    }

    private PostsPropertyChangedEventFacet create(Method method, final FacetHolder holder) {
        final PostsPropertyChangedEvent annotation = Annotations.getAnnotation(method, PostsPropertyChangedEvent.class);
        if(annotation == null) {
            return null;
        }
        final PropertyOrCollectionAccessorFacet getterFacet = holder.getFacet(PropertyOrCollectionAccessorFacet.class);
        if(getterFacet == null) {
            return null;
        } 
        final PropertyClearFacet clearFacet = holder.getFacet(PropertyClearFacet.class);
        final PropertySetterFacet setterFacet = holder.getFacet(PropertySetterFacet.class);
        if (clearFacet == null && setterFacet == null) {
            return null;
        }
        
        
        // REVIEW: I'm a bit uncertain about this; this facet is multi-valued, but the setUnderlying(...) stuff only
        // works for single valued types.
        // the wrapperFactory stuff looks for underlying to find the imperative method, I think this only works in this
        // case because (by accident rather than design) there is also the PropertyInitializationFacet wrapping the setter.
        if(setterFacet != null) {
            holder.removeFacet(setterFacet);
        }
        if(clearFacet != null) {
            holder.removeFacet(clearFacet);
        }
        
        final Class<? extends PropertyChangedEvent<?, ?>> changedEventType = annotation.value();
        return new PostsPropertyChangedEventFacetAnnotation(changedEventType, getterFacet, setterFacet, clearFacet, servicesProvider, holder);
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
