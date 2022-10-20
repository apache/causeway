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

import java.util.Optional;

import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.collections.layout.tabledec.CollectionLayoutTableDecorationFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.causeway.core.metamodel.facets.object.paged.PagedFacetAbstract;

import lombok.val;

public class PagedFacetForCollectionLayoutAnnotation extends PagedFacetAbstract {

    public static Optional<PagedFacet> create(
            final Optional<CollectionLayout> collectionLayoutIfAny,
            final FacetHolder holder) {

        val tableDecorationFacet = holder.getFacet(CollectionLayoutTableDecorationFacet.class);
        if (tableDecorationFacet.value().isDataTablesNet()) {
            return Optional.of(new PagedFacetOverriddenByDataTablesDecoration(holder));
        }

        return collectionLayoutIfAny
                .map(CollectionLayout::paged)
                .filter(paged -> paged != -1)
                .map(paged -> new PagedFacetForCollectionLayoutAnnotation(paged, holder));
    }

    private PagedFacetForCollectionLayoutAnnotation(final int paged, final FacetHolder holder) {
        super(paged, holder);
    }

}
