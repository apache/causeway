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

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.specloader.SpecificationLoader;

public record ObjectTreeAdapter(SpecificationLoader specLoader) 
implements 
    TreeAdapter<Object> {

    @Override
    public int childCountOf(final Object node) {
        return treeNodeFacet(node)
            .map(treeNodeFacet->treeNodeFacet.childCountOf(node))
            .orElse(0);
    }
    @Override
    public Stream<Object> childrenOf(final Object node) {
        return treeNodeFacet(node)
            .map(treeNodeFacet->treeNodeFacet.childrenOf(node))
            .orElseGet(Stream::empty);
    }

    // -- HELPER

    private <T> Optional<NavigableSubtreeFacet> treeNodeFacet(final T node) {
        return specLoader().loadSpecification(node.getClass())
                .lookupFacet(NavigableSubtreeFacet.class)
                .map(treeNodeFacet->_Casts.<NavigableSubtreeFacet>uncheckedCast(treeNodeFacet));
    }

}