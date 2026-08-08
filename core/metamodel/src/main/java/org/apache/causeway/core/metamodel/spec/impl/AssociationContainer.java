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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;
import org.jspecify.annotations.Nullable;

record AssociationContainer(
		Can<ObjectAssociation> associationsInOrder,
		ObjectAssociationContainer superContainer,
		/** used for column rendering, null in the EMPTY case */
		@Nullable ObjectSpecification correspondingSpec)
implements ObjectAssociationContainer {

	// e.g. used for value types
	static AssociationContainer EMPTY = new AssociationContainer(
			Can.empty(),
			null,
			null);

	AssociationContainer(
			final List<ObjectAssociation> associationsInOrder,
			final ObjectAssociationContainer superContainer,
			final ObjectSpecification correspondingSpec) {
		this(Can.ofCollection(associationsInOrder), superContainer, correspondingSpec);
	}

    @Override
    public Optional<ObjectAssociation> getAssociation(final String id, final MixedIn mixedIn) {

        var declaredAssociation = getDeclaredAssociation(id, mixedIn); // no inheritance considered

        if(declaredAssociation.isPresent())
        	return declaredAssociation;

        return isTypeHierarchyRoot()
               ? Optional.empty() // stop searching
               : superContainer.getAssociation(id, mixedIn);
    }

    @Override
    public Stream<ObjectAssociation> streamAssociations(final MixedIn mixedIn) {
        if(isTypeHierarchyRoot())
        	return streamDeclaredAssociations(mixedIn); // stop going deeper

        var ids = new HashSet<String>();

        return Stream.concat(
                streamDeclaredAssociations(mixedIn),
                superContainer.streamAssociations(mixedIn)
            )
            .filter(association->ids.add(association.getId())); // ensure we don't emit duplicates
    }

	@Override
	public Optional<ObjectAssociation> getDeclaredAssociation(final String id, final MixedIn mixedIn) {
        if(_Strings.isEmpty(id))
			return Optional.empty();

        return streamDeclaredAssociations(mixedIn)
                .filter(objectAssociation->objectAssociation.getId().equals(id))
                .findFirst();
	}

	@Override
	public Stream<ObjectAssociation> streamAssociationsForColumnRendering(final ColumnQuery columnQuery) {
		if(correspondingSpec==null)
			return Stream.empty();
		return new _MembersAsColumns(correspondingSpec.getMetaModelContext())
			.streamAssociationsForColumnRendering(correspondingSpec, columnQuery);
	}

	@Override
	public Stream<ObjectAssociation> streamDeclaredAssociations(final MixedIn mixedIn) {
		return associationsInOrder.stream()
            .filter(mixedIn.toFilter());
	}

	// -- HELPER

	private boolean isTypeHierarchyRoot() {
        return superContainer==null;
    }

}
