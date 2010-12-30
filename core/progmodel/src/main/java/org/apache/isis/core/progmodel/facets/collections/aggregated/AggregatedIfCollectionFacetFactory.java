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


package org.apache.isis.core.progmodel.facets.collections.aggregated;

import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetUtil;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.MethodRemover;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.aggregated.AggregatedFacet;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModel;
import org.apache.isis.core.metamodel.spec.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.specloader.collectiontyperegistry.CollectionTypeRegistry;
import org.apache.isis.core.progmodel.facets.collections.CollectionFacetFactory;


/**
 * All {@link CollectionTypeRegistry collection types} are intrinsically {@link AggregatedFacet aggregeted}.
 * 
 * 
 * Must be registered in the  {@link ProgrammingModel} after {@link CollectionFacetFactory}.
 */
public class AggregatedIfCollectionFacetFactory extends FacetFactoryAbstract {

    public AggregatedIfCollectionFacetFactory() {
        super(FeatureType.OBJECTS_ONLY);
    }

    @Override
    public boolean process(final Class<?> cls, final MethodRemover methodRemover, final FacetHolder holder) {
        if (!holder.containsFacet(CollectionFacet.class)) {
            return false;
        }
        return FacetUtil.addFacet(new AggregatedSinceCollection(holder));
    }

}
