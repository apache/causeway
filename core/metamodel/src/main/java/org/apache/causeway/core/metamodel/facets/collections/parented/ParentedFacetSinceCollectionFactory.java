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
package org.apache.causeway.core.metamodel.facets.collections.parented;

import jakarta.inject.Inject;

import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.facets.collections.javautilcollection.CollectionFacetFactory;
import org.apache.causeway.core.metamodel.facets.object.parented.ParentedCollectionFacet;
import org.apache.causeway.core.metamodel.progmodel.ProgrammingModel;

/**
 * All collection types are intrinsically {@link ParentedCollectionFacet parented}.
 *
 * <p>
 * Must be registered in the {@link ProgrammingModel} after
 * {@link CollectionFacetFactory}.
 */
public class ParentedFacetSinceCollectionFactory
extends FacetFactoryAbstract {

    @Inject
    public ParentedFacetSinceCollectionFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.OBJECTS_ONLY);
    }

    @Override
    public void process(final ProcessClassContext processClassContaxt) {
        if (!processClassContaxt.getFacetHolder().containsFacet(CollectionFacet.class)) {
            return;
        }
        FacetUtil.addFacet(new ParentedCollectionFacetDefault(processClassContaxt.getFacetHolder()));
    }

}
