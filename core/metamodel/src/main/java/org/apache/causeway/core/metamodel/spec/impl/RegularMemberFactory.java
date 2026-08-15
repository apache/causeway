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
package org.apache.causeway.core.metamodel.spec.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.metamodel.facets.FacetedMethod;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacet;
import org.apache.causeway.core.metamodel.facets.object.mixin.MixinFacetImpl;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

record RegularMemberFactory(
		Optional<MixinFacet> mixinFacetOpt,
		FacetedMethodsFactory factory) {

	Stream<ObjectAssociation> createAssociations() {
        return factory.createAssociationFacetedMethods()
    		.stream()
            .map(this::createAssociation)
            .filter(Objects::nonNull);
    }

    Stream<ObjectAction> createActions() {
    	return factory.createActionFacetedMethods()
			.stream()
			.map(this::createAction)
			.filter(Objects::nonNull);
    }

    // -- HELPER

    private ObjectAssociation createAssociation(final FacetedMethod facetMethod) {
        if (facetMethod.featureType().isCollection())
			return OneToManyAssociationDefault.forMethod(facetMethod);
		else if (facetMethod.featureType().isProperty())
			return OneToOneAssociationDefault.forMethod(facetMethod);
		else
			return null;
    }

    private ObjectAction createAction(final FacetedMethod facetedMethod) {
        if (facetedMethod.featureType().isAction()) {
            /* Assuming, that facetedMethod was already populated with ContributingFacet,
             * we copy the mixin-sort information from the FacetedMethod to the MixinFacet
             * that is held by the mixin's type spec. */
        	mixinFacetOpt
	            .flatMap(mixinFacet->_Casts.castTo(MixinFacetImpl.class, mixinFacet))
	            .ifPresent(mixinFacetAbstract->
	                mixinFacetAbstract.initMixinSortFrom(facetedMethod));

            return mixinFacetOpt.isPresent()
                    ? ObjectActionDefault.forMixinMain(facetedMethod)
                    : ObjectActionDefault.forMethod(facetedMethod);
        } else
			return null;
    }

}
