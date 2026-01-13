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
package org.apache.causeway.core.metamodel.facets.object.objectvalidprops.impl;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facetapi.FacetUtil;
import org.apache.causeway.core.metamodel.facets.object.objectvalidprops.ObjectValidPropertiesFacet;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;
import org.apache.causeway.core.metamodel.interactions.val.ObjectValidityContext;
import org.apache.causeway.core.metamodel.interactions.val.ValidityContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

public record ObjectValidPropertiesFacetImpl(
		FacetHolder facetHolder)
implements ObjectValidPropertiesFacet {

    @Override public Class<? extends Facet> facetType() { return ObjectValidPropertiesFacet.class; }
    @Override public Precedence precedence() { return Precedence.DEFAULT; }

    @Override
    public String toString() {
        return FacetUtil.toString(this);
    }

    @Override
    public String invalidates(final ValidityContext ic) {
        return (ic instanceof final ObjectValidityContext validityContext)
    		? invalidReason(validityContext)
			: null;
    }

    @Override
    public String invalidReason(final ObjectValidityContext context) {
        final ManagedObject mo = context.target();
        var sb = new StringBuilder();

        mo.objSpec().streamProperties(MixedIn.EXCLUDED)
	        .filter(property->property.isVisible(mo, context.initiatedBy(), VISIBILITY_CONSTRAINT).isVetoed()) // ignore hidden properties
	        .filter(property->property.isUsable(mo, context.initiatedBy(), VISIBILITY_CONSTRAINT).isVetoed())  // ignore disabled properties
	        .forEach(property->{
	            final ManagedObject value = property.get(mo, context.initiatedBy());
	            if (property.isAssociationValid(mo, value, context.initiatedBy()).isVetoed()) {
	                if (sb.length() > 0) {
	                    sb.append(", ");
	                }
	                sb.append(property.getFriendlyName(context::target));
	            }
	        });
        return (sb.length() > 0)
			? "Invalid properties: " + sb.toString()
        	: null;
    }

    // REVIEW: should provide the rendering context, rather than hardcoding.
    // The net effect currently is that class members annotated with
    // @Hidden(where=Where.ANYWHERE) or @Disabled(where=Where.ANYWHERE) will indeed
    // be hidden/disabled, but will be visible/enabled (perhaps incorrectly)
    // for any other value for Where.
    // However, ultimately we do check, whether the object is valid prior to persisting,
    // I think, this should not be constraint by WhatViewer or Where.
    private final static VisibilityConstraint VISIBILITY_CONSTRAINT = VisibilityConstraint.noViewer(Where.ANYWHERE);

}
