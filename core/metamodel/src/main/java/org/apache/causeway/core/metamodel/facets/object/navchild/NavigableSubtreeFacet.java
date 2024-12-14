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
package org.apache.causeway.core.metamodel.facets.object.navchild;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.util.DeweyOrderComparator;

/**
 * Provides the parent/child relationship information between pojos
 * to derive a tree-structure.
 *
 * @since 3.2
 */
public sealed interface NavigableSubtreeFacet 
extends Facet, TreeAdapter<Object>
permits NavigableSubtreeFacetRecord {
    
    // -- FACTORY
    
    static <T> Optional<NavigableSubtreeFacet> create(
        final Can<NavigableSubtreeSequenceFacet> navigableSubtreeSequenceFacets,
        final FacetHolder facetHolder) {
        if(navigableSubtreeSequenceFacets.isEmpty()) return Optional.empty();
        
        var comparator = new DeweyOrderComparator();
        Can<MethodHandle> subNodesMethodHandles = navigableSubtreeSequenceFacets
            .sorted((a, b)->comparator.compare(a.sequence(), b.sequence()))
            .map(NavigableSubtreeSequenceFacet::methodHandle);
        
        return Optional.of(new NavigableSubtreeFacetRecord(subNodesMethodHandles, facetHolder));
    }
    
}
