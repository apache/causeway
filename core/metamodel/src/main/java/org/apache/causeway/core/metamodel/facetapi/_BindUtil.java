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

	/**
     * @deprecated Use for debugging only! Breaks the contract, that every facet is contained by its holder.
     * @throws {@link IllegalArgumentException} when facet is not found, or facet is of EVENT precedence.
     */
	@Deprecated
	void unbind(@Nullable final Facet facet) {
		resolveInternalElseFail(facet.facetHolder())
			.removeFacet(facet);
	}

    // -- DYNAMIC UPDATE SUPPORT

    /**
     * Removes any {@link Facet} from its {@link FacetHolder}, that matches the exactFacetClass
     * and has no higher precedence than the given one.
     *
     * @apiNote {@link Facet}(s) by contract, have a contained-by relation with their {@link FacetHolder}.
     * 	However, this method breaks this contract, as removed {@link Facet}(s)
     * 	still reference their former holder, but the holder no longer contains the facet.
     *
     * @see ReloadableFacet
     */
    void purgeExactFacetClassHonoringPrecedence(
    		final Class<? extends Facet> exactFacetClass,
    		final QualifiedFacet.Key qualifierKey,
    		final FacetHolder facetHolder,
    		final Facet.@Nullable Precedence precedence,
    		final @Nullable String qualifier) {

    	Objects.requireNonNull(exactFacetClass);
    	Objects.requireNonNull(qualifierKey);
    	Objects.requireNonNull(facetHolder);
    	Objects.requireNonNull(precedence);
    	Assert.isTrue(precedence.ordinal() < Facet.Precedence.EVENT.ordinal(), "Purge not supported for facets with EVENT precedence");
    	Assert.isTrue(qualifierKey.facetType().isAssignableFrom(exactFacetClass), "Exact facet-class %s is not an instace of facet-type %s"
    			.formatted(exactFacetClass.getName(), qualifierKey.facetType().getName()));

        facetHolder.lookupFacetRanking(qualifierKey.facetType())
            .ifPresent(ranking->ranking.purgeIf(qualifierKey.facetType(),
                    qualifierKey, // discriminate by qualifier
                    exactFacetClass::isInstance, // facet filter
                    prec->qualifierKey.isQualified()
                        ? true // if qualified, purge all ranks
                        : prec.ordinal()<=precedence.ordinal() // don't change ranks of higher precedence
                ));
    }

}
