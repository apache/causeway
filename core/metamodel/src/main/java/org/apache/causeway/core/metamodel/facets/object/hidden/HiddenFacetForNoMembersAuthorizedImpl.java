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
package org.apache.causeway.core.metamodel.facets.object.hidden;

import org.apache.causeway.core.metamodel.facetapi.Facet;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.interactions.vis.VisibilityContext;
import org.apache.causeway.core.metamodel.postprocessors.allbutparam.authorization.AuthorizationFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;

public record HiddenFacetForNoMembersAuthorizedImpl(
		FacetHolder facetHolder
		) implements HiddenFacetForNoMembersAuthorized {
	
	@Override public Class<? extends Facet> facetType() { return HiddenFacetForNoMembersAuthorized.class; }
	@Override public Precedence precedence() { return Precedence.HIGH; }
	
    @Override
    public String hides(final VisibilityContext vc) {
        var spec = (ObjectSpecification) facetHolder();

        if(!spec.isEntityOrViewModelOrAbstract()) return null;

        /*[CAUSEWAY-3657] Don't hide members based on their element type having no visible actions,
         * properties or collections in case the element type is an interface.
         *
         * I encountered a case, where an interface type was visible during PROTOTYPING, but hidden in production.
         * This is because the check for visibility also considers mixed in members,
         * and it is the case that any interface when PROTOTYPING has some Object_ actions mixed in,
         * but not necessarily in production.
         */
        if(spec.getCorrespondingClass().isInterface()) return null;

        var hasVisibleProperty = spec.streamProperties(MixedIn.INCLUDED)
                .anyMatch(prop -> !AuthorizationFacet.hidesProperty(prop, vc));
        if (hasVisibleProperty) return null;

        var hasVisibleCollection = spec.streamCollections(MixedIn.INCLUDED)
                .anyMatch(coll -> !AuthorizationFacet.hidesCollection(coll, vc));
        if (hasVisibleCollection) return null;

        var hasVisibleAction = spec.streamRuntimeActions(MixedIn.INCLUDED)
                .anyMatch(act -> !AuthorizationFacet.hidesAction(act, vc));
        if (hasVisibleAction) return null;

        return "All members (actions, properties and collections) are hidden for logical-type %s"
        		.formatted(spec.logicalTypeName());
    }

}
