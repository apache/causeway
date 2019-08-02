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

package org.apache.isis.metamodel.facets.collections.layout;

import java.util.Comparator;
import java.util.List;

import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.metamodel.facets.collections.sortedby.SortedByFacetAbstract;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

public class SortedByFacetForCollectionLayoutAnnotation extends SortedByFacetAbstract {

    public static SortedByFacet create(
            final List<CollectionLayout> collectionLayouts,
            final FacetHolder holder) {

        return collectionLayouts.stream()
                .map(CollectionLayout::sortedBy)
                .filter(sortedBy -> sortedBy != Comparator.class)
                .filter(Comparator.class::isAssignableFrom)
                .findFirst()
                .map(sortedBy -> {
                    Class<? extends Comparator<?>> sortedByForceGenerics = uncheckedCast(sortedBy);
                    return new SortedByFacetForCollectionLayoutAnnotation(sortedByForceGenerics, holder);
                })
                .orElse(null);
    }

    private SortedByFacetForCollectionLayoutAnnotation(Class<? extends Comparator<?>> sortedBy, FacetHolder holder) {
        super(sortedBy, holder);
    }

}
