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

import java.util.Comparator;
import java.util.Optional;

import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.core.metamodel.commons.ClassUtil;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacetAbstract;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;

public class SortedByFacetForCollectionXml
extends SortedByFacetAbstract {

    public static Optional<SortedByFacet> create(
            final CollectionLayoutData collectionLayout,
            final FacetHolder holder) {
        if(collectionLayout == null) {
            return Optional.empty();
        }
        final String sortedBy = collectionLayout.getSortedBy();
        if (sortedBy == null) {
            return Optional.empty();
        }
        final Class<?> sortedByClass = ClassUtil.forNameElseFail(sortedBy);
        if(sortedByClass == Comparator.class) {
            return Optional.empty();
        }

        return sortedByClass != null
                ? Optional.of(new SortedByFacetForCollectionXml(uncheckedCast(sortedByClass), holder))
                : Optional.empty();
    }

    private SortedByFacetForCollectionXml(final Class<? extends Comparator<?>> sortedBy, final FacetHolder holder) {
        super(sortedBy, holder);
    }

}
