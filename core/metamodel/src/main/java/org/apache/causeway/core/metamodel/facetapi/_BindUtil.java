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
package org.apache.causeway.core.metamodel.facetapi;

import java.util.Objects;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import lombok.experimental.UtilityClass;

@UtilityClass
class _BindUtil {

	@Nullable
	<F extends Facet> F bind(final @Nullable F facet) {
		if(facet==null)
			return facet;
		Assert.isTrue(facet instanceof FacetAbstract, ()->"not a Facet that inherits FacetAbstract %s".formatted(facet.getClass().getName()));
		resolveInternalElseFail(facet.facetHolder())
			.addFacet(facet);
		return facet;
    }

	FacetHolderInternal resolveInternalElseFail(final FacetHolder facetHolder) {
		return resolveInternal(Objects.requireNonNull(facetHolder))
			.orElseThrow(()->
				new UnsupportedOperationException("FacetHolder is not internal %s".formatted(facetHolder.getClass().getName())));
	}

	Optional<FacetHolderInternal> resolveInternal(final @Nullable FacetHolder facetHolder) {
		if(facetHolder == null)
    		return Optional.empty();
		if(facetHolder instanceof FacetHolderInternal facetHolderInternal)
			return Optional.of(facetHolderInternal);
		if(facetHolder instanceof HasFacetHolder hasFacetHolder)
			return resolveInternal(hasFacetHolder.facetHolder());
		return Optional.empty();
	}

	void unbind(@Nullable final Facet facet) {
		resolveInternalElseFail(facet.facetHolder())
			.removeFacet(facet);
	}

}
