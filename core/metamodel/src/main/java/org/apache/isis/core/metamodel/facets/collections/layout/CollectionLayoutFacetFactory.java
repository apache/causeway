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
package org.apache.isis.core.metamodel.facets.collections.layout;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.collection.defaultview.DefaultViewFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.members.cssclass.CssClassFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;

import lombok.val;

public class CollectionLayoutFacetFactory 
extends FacetFactoryAbstract {

    public CollectionLayoutFacetFactory() {
        super(FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();

        val collectionLayoutIfAny = processMethodContext.synthesizeOnMethodOrMixinType(CollectionLayout.class);

        // cssClass
        CssClassFacet cssClassFacet = CssClassFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, facetHolder);
        super.addFacet(cssClassFacet);


        // describedAs
        DescribedAsFacet describedAsFacet =
                DescribedAsFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, facetHolder);
        super.addFacet(describedAsFacet);


        // hidden
        HiddenFacet hiddenFacet = HiddenFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, facetHolder);
        super.addFacet(hiddenFacet);


        // defaultView
        DefaultViewFacet defaultViewFacet =
                DefaultViewFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, getConfiguration(), facetHolder);
        super.addFacet(defaultViewFacet);


        // named
        NamedFacet namedFacet = NamedFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, facetHolder);
        super.addFacet(namedFacet);


        // paged
        PagedFacet pagedFacet = PagedFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, facetHolder);
        super.addFacet(pagedFacet);


        // sortedBy
        SortedByFacet sortedByFacet = SortedByFacetForCollectionLayoutAnnotation.create(collectionLayoutIfAny, facetHolder);
        super.addFacet(sortedByFacet);

    }



}
