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

import javax.inject.Inject;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facets.FacetFactoryAbstract;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacetFromCollectionLayoutAnnotation;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelValidatorForAmbiguousMixinAnnotations;

import lombok.val;

public class CollectionLayoutFacetFactory
extends FacetFactoryAbstract {

    @Inject
    public CollectionLayoutFacetFactory(final MetaModelContext mmc) {
        super(mmc, FeatureType.COLLECTIONS_AND_ACTIONS);
    }

    @Override
    public void process(final ProcessMethodContext processMethodContext) {

        val facetHolder = processMethodContext.getFacetHolder();
        val collectionLayoutIfAny = processMethodContext
                .synthesizeOnMethodOrMixinType(
                        CollectionLayout.class,
                        () -> MetaModelValidatorForAmbiguousMixinAnnotations
                        .addValidationFailure(processMethodContext.getFacetHolder(), CollectionLayout.class));

        addFacetIfPresent(
                CssClassFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacet(
                DefaultViewFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, getConfiguration(), facetHolder));

        addFacetIfPresent(
                MemberDescribedFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
                CanonicalDescribedFacetForCollectionLayoutAnnotation
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
                CanonicalNamedFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
                PagedFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));

        addFacetIfPresent(
                SortedByFacetForCollectionLayoutAnnotation
                .create(collectionLayoutIfAny, facetHolder));
    }

}
