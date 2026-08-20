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
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.graph.tree.TreeAdapter;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.util.DeweyOrderComparator;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides the parent/child relationship information between pojos
 * to derive a tree-structure.
 *
 * @since 3.2
 */
@Slf4j
public final class NavigableSubtreeFacet
extends FacetAbstract
implements TreeAdapter<Object>{

    // -- FACTORY

    static <T> Optional<NavigableSubtreeFacet> create(
        final Can<NavigableSubtreeSequenceFacet> navigableSubtreeSequenceFacets,
        final FacetHolder facetHolder) {
        if(navigableSubtreeSequenceFacets.isEmpty()) return Optional.empty();

        var comparator = new DeweyOrderComparator();
        Can<MethodHandle> subNodesMethodHandles = navigableSubtreeSequenceFacets
            .sorted((a, b)->comparator.compare(a.sequence(), b.sequence()))
            .map(NavigableSubtreeSequenceFacet::methodHandle);

        return Optional.of(new NavigableSubtreeFacet(subNodesMethodHandles, facetHolder));
    }


	private final Can<MethodHandle> subNodesMethodHandles;

	NavigableSubtreeFacet(
			final Can<MethodHandle> subNodesMethodHandles,
		    final FacetHolder facetHolder) {
    	super(NavigableSubtreeFacet.class, facetHolder);
    	this.subNodesMethodHandles = subNodesMethodHandles;
    }

    @Override
    public final int childCountOf(final Object node) {
        return subNodesMethodHandles.stream()
            .mapToInt(mh->{
                try {
                    return _NullSafe.sizeAutodetect(mh.invoke(node));
                } catch (Throwable e) {
                    log.error("failed to invoke subNodesMethodHandle {}",
                            mh.toString(), e);
                    return 0;
                }
            })
            .sum();
    }

    @Override
    public final Stream<Object> childrenOf(final Object node) {
        return subNodesMethodHandles.stream()
            .flatMap(mh->{
                try {
                    return _NullSafe.streamAutodetect(mh.invoke(node));
                } catch (Throwable e) {
                    throw _Exceptions.unrecoverable(e);
                }
            });
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	NavigableSubtreeFacet.super.visitAttributes(visitor);
        visitor.accept("subNodesMethodHandles", subNodesMethodHandles.map(MethodHandle::toString));
    }

}