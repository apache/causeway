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
package org.apache.causeway.core.metamodel.facets.object.hidden.method;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jspecify.annotations.Nullable;

import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.HasImperativeAspect;
import org.apache.causeway.core.metamodel.facets.ImperativeAspect;
import org.apache.causeway.core.metamodel.facets.object.hidden.HiddenFacetForObject;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;

public record HiddenFacetForObjectViaMethod(
		ImperativeAspect imperativeAspect,
		FacetHolder facetHolder
		) implements HiddenFacetForObject, HasImperativeAspect {

	public static Optional<HiddenFacetForObject> create(
			final @Nullable ResolvedMethod methodIfAny,
			final FacetHolder holder) {
		
		return Optional.ofNullable(methodIfAny)
				.map(method->ImperativeAspect.singleRegularMethod(method, Intent.CHECK_IF_HIDDEN))
				.map(imperativeAspect->new HiddenFacetForObjectViaMethod(imperativeAspect, holder));
	}
	
	@Override public Class<? extends Facet> facetType() { return HiddenFacetForObject.class; }
	@Override public Precedence precedence() { return Precedence.DEFAULT;}

    @Override
    public String hides(final VisibilityContext ic) {
        final ManagedObject toValidate = ic.target();
        return toValidate != null ? hiddenReason(toValidate) : null;
    }

    @Override
    public HiddenFacetForObjectViaMethod copyTo(final FacetHolder holder) {
        return new HiddenFacetForObjectViaMethod(imperativeAspect, holder);
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	HiddenFacetForObject.super.visitAttributes(visitor);
        imperativeAspect.visitAttributes(visitor);
    }
    
    // -- HELPER
    
    private String hiddenReason(final ManagedObject target) {
    	final boolean isHidden = imperativeAspect.eval(target, false);
    	return isHidden ? "Hidden" : null;
    }

}
