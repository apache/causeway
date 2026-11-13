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

import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.jspecify.annotations.NonNull;

import lombok.Getter;
import lombok.experimental.Accessors;

public abstract class FacetAbstract
implements Facet, HasMetaModelContext {

	@Getter(onMethod_ = {@Override}) @Accessors(fluent = true, makeFinal = true)
    private final @NonNull Class<? extends Facet> facetType;

    @Getter(onMethod_ = {@Override}) @Accessors(fluent = true, makeFinal = true)
    private final @NonNull FacetHolder facetHolder;

    protected FacetAbstract(
            final Class<? extends Facet> facetType,
            final FacetHolder facetHolder) {
        this.facetType = Objects.requireNonNull(facetType);
        this.facetHolder = Objects.requireNonNull(facetHolder);
        prebind(facetHolder);
        // binding by contract
        _BindUtil.resolveInternalElseFail(facetHolder)
    			.addFacet(this);
    }

    @Override
    public Precedence precedence() {
    	return Precedence.DEFAULT;
    }

    @Override
    public String toString() {
        return FacetUtil.toString(this);
    }

    // -- HELPER

    /**
     * Some {@link Facet}(s) may need certain adjustments to the {@link FacetHolder} which they are about to be added to.
     * This method is called before binding this {@link Facet} to its holder. It is called inside the {@link Facet}'s constructor.
     */
    private final void prebind(final FacetHolder facetHolder) {
    	if(this instanceof ReloadableFacet reloadableFacet) {
    		reloadableFacet.onPrebind(facetHolder); // dynamic update support
    	}
    }

}
