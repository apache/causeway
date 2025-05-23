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
package org.apache.causeway.core.metamodel.facets.collections.layout;

import jakarta.inject.Inject;

import org.springframework.util.StringUtils;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;
import org.apache.causeway.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.causeway.core.metamodel.facets.collections.layout.tabledec.TableDecoratorFacetForCollectionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromCollectionLayoutAnnotation;
import org.apache.causeway.core.metamodel.facets.object.navchild.NavigableSubtreeSequenceFacet;
import org.apache.causeway.core.metamodel.specloader.validator.ValidationFailureUtils;

public class CollectionLayoutFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public CollectionLayoutFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        var facetHolder = processMethodContext.getFacetHolder();
        var collectionLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        CollectionLayout.class,
                        () -> ValidationFailureUtils
                        .raiseAmbiguousMixinAnnotations(processMethodContext.getFacetHolder(), CollectionLayout.class));

        addFacetIfPresent(
            CssClassFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacet(
            DefaultViewFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder)
                .orElseGet(()->DefaultViewFacetAsConfigured.create(facetHolder)));

        addFacetIfPresent(
            MemberDescribedFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            HiddenFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            LayoutOrderFacetFromCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            MemberNamedFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            TableDecoratorFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            PagedFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            SortedByFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
            collectionLayoutIfAny
                .map(CollectionLayout::navigableSubtree)
                .filter(StringUtils::hasLength)
                .flatMap(sequence->NavigableSubtreeSequenceFacet.create("CollectionLayout annotation",
                    processMethodContext.getCls(), processMethodContext.getMethod().asMethod(), sequence, facetHolder)));
    }

}
