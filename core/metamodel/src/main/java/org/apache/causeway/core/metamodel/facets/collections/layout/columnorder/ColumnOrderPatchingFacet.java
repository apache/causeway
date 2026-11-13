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
package org.apache.causeway.core.metamodel.facets.collections.layout.columnorder;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.facetapi.FacetAbstract;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.jspecify.annotations.Nullable;

import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Holder of a {@link Map} that is mutable during runtime and collects column order patching information.
 *
 * <p>To be installed on collection's element types.
 *
 * @apiNote Only used when PROTOTYPING.
 *
 * @since 4.0
 */
public final class ColumnOrderPatchingFacet extends FacetAbstract {

	@Getter @Accessors(fluent = true)
	private final Map<Identifier, Can<String>> columnOrder;

	public ColumnOrderPatchingFacet(final FacetHolder facetHolder) {
		super(ColumnOrderPatchingFacet.class, facetHolder);
		this.columnOrder = new ConcurrentHashMap<>();
	}

	/**
	 * Clears the map entry.
	 * @param identifier if null, acts as a no-op
	 */
	public void clearColumnOrder(final @Nullable Identifier identifier) {
		columnOrder.remove(identifier);
	}

	/**
	 * @param identifier if null, acts as a no-op
	 * @param columnListing if null, clears the map entry
	 */
	public void putColumnOrder(final @Nullable Identifier identifier, @Nullable final Can<String> columnListing) {
		if(identifier==null)
			return;
		if(columnListing==null) {
			clearColumnOrder(identifier);
			return;
		}
		columnOrder.put(identifier, columnListing);
	}

	public Optional<Can<String>> lookupColumnOrder(final @Nullable Identifier identifier) {
		return identifier!=null
			? Optional.ofNullable(columnOrder.get(identifier))
			: Optional.empty();
	}

	@Override
	public void visitAttributes(final BiConsumer<String, Object> visitor) {
	    super.visitAttributes(visitor);
	    columnOrder.forEach((identifier, order)->{
	        visitor.accept(identifier.toString(), order.join(","));
	    });
	}

}
