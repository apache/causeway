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
package org.apache.causeway.core.metamodel.facets.collections.layout.tabledec;

import java.util.Optional;

import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.TableDecoration;
import org.apache.causeway.core.config.metamodel.facets.CollectionLayoutConfigOptions;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;

public class CollectionLayoutTableDecorationFacetForCollectionLayoutXml
extends CollectionLayoutTableDecorationFacetAbstract {

    public static final Class<CollectionLayoutTableDecorationFacet> type() {
        return CollectionLayoutTableDecorationFacet.class;
    }

    public static Optional<CollectionLayoutTableDecorationFacet> create(
            final CollectionLayoutData collectionLayout,
            final FacetHolder holder) {
        if (collectionLayout == null) {
            return Optional.empty();
        }

        final TableDecoration tableDecoration = collectionLayout.getTableDecoration();
        return tableDecoration == TableDecoration.DATATABLES_NET
                ? Optional.of(new CollectionLayoutTableDecorationFacetForCollectionLayoutXml(holder))
                : Optional.empty();
    }

    private CollectionLayoutTableDecorationFacetForCollectionLayoutXml(final FacetHolder holder) {
        super(CollectionLayoutConfigOptions.TableDecoration.DATATABLES_NET, holder);
    }

}
