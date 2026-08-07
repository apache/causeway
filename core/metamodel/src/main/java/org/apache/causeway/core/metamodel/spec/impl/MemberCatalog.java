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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.facets.ImperativeFacet;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;

record MemberCatalog(
		Map<ResolvedMethod, ObjectMember> membersByMethod) {

	static MemberCatalog EMPTY = new MemberCatalog(Map.of());

    MemberCatalog(final ObjectSpecificationBuilder spec) {
    	this(catalogMembersByMethod(Objects.requireNonNull(spec)));
	}

	Optional<? extends ObjectMember> lookupMember(final ResolvedMethod method) {
		if(this==EMPTY) // the EMPTY case
			throw new UnsupportedOperationException("members are only available after introspection, lookupMember was probably called too early");
        return Optional.ofNullable(membersByMethod.get(method));
    }

	// -- HELPER

	private static Map<ResolvedMethod, ObjectMember> catalogMembersByMethod(final ObjectSpecificationBuilder spec) {
		var membersByMethod = new HashMap<ResolvedMethod, ObjectMember>();
		cataloguePropertiesAndCollections(spec, membersByMethod::put);
		catalogueActions(spec, membersByMethod::put);
		return Collections.unmodifiableMap(membersByMethod);
	}

    private static void cataloguePropertiesAndCollections(final ObjectSpecificationBuilder spec, final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
        spec.streamDeclaredAssociations(MixedIn.EXCLUDED)
	        .forEach(field->
	            field.streamFacets(ImperativeFacet.class)
	                .map(ImperativeFacet::getMethods)
	                .flatMap(Can::stream)
	                .map(MethodFacade::asMethodElseFail) // expected regular
	                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
	                .forEach(imperativeFacetMethod->onMember.accept(imperativeFacetMethod, field)));
    }

    private static void catalogueActions(final ObjectSpecificationBuilder spec, final BiConsumer<ResolvedMethod, ObjectMember> onMember) {
    	spec.streamDeclaredActions(MixedIn.INCLUDED)
	        .forEach(userAction->
	            userAction.streamFacets(ImperativeFacet.class)
	                .map(ImperativeFacet::getMethods)
	                .flatMap(Can::stream)
	                .map(MethodFacade::asMethodForIntrospection)
	                .peek(method->_Reflect.guardAgainstSynthetic(method.method())) // expected non-synthetic
	                .forEach(imperativeFacetMethod->onMember.accept(imperativeFacetMethod, userAction)));
    }

}
