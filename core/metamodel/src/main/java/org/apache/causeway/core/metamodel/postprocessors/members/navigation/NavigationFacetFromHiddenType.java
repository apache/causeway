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
package org.apache.causeway.core.metamodel.postprocessors.members.navigation;

import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.members.navigation.NavigationFacet;
import org.apache.causeway.core.metamodel.facets.object.hidden.HiddenTypeFacet;
import org.apache.causeway.core.metamodel.interactions.vis.ObjectVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

public record NavigationFacetFromHiddenType(
		ObjectSpecification navigatedType,
		FacetHolder facetHolder
		) implements NavigationFacet {

	public static Optional<NavigationFacet> create(final ObjectSpecification navigatedType, final FacetHolder holder) {
		return navigatedType.isValue()
				? Optional.empty() // don't create for value types (optimization, not strictly required)
						: Optional.of(new NavigationFacetFromHiddenType(navigatedType, holder));
	}

	@Override public Class<? extends Facet> facetType() { return NavigationFacet.class; }
	@Override public Precedence precedence() { return Precedence.DEFAULT; }

    public NavigationFacetFromHiddenType {
        _Assert.assertTrue(navigatedType.isSingular(), ()->String.format(
                "framework bug: elementType must not match any supported plural (collection) types, "
                + "nevertheless got %s", navigatedType));
    }

    @Override
    public String hides(final VisibilityContext ic) {
        var facet = navigatedType.getFacet(HiddenTypeFacet.class);
        if(facet == null) {
            // not expected to happen; this facet should only be installed for object members
            // that navigate to a class that has the HiddenTypeFacet
            return null;
        }
        var objVisibilityContext = new ObjectVisibilityContext(
                ic.head(),
                Identifier.classIdentifier(navigatedType.logicalType()),
                ic.initiatedBy(),
                ic.where(),
                ic.renderPolicy());
        final String hides = facet.hides(objVisibilityContext);
        return hides;
    }

    @Override
    public void visitAttributes(final BiConsumer<String, Object> visitor) {
    	NavigationFacet.super.visitAttributes(visitor);
        visitor.accept("navigatedType", navigatedType.logicalTypeName());
        visitor.accept("navigatedTypeFqcn", navigatedType.getCorrespondingClass().getName());
    }

}
