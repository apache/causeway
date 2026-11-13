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

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer;

@FunctionalInterface
interface HasObjectAssociationContainer extends ObjectAssociationContainer {

	ObjectAssociationContainer objectAssociationContainer();

    @Override
    default Optional<ObjectAssociation> getAssociation(final String id, final MixedIn mixedIn) {
    	return objectAssociationContainer().getAssociation(id, mixedIn);
    }

    @Override
    default Stream<ObjectAssociation> streamAssociations(final MixedIn mixedIn) {
    	return objectAssociationContainer().streamAssociations(mixedIn);
    }

	@Override
	default Optional<ObjectAssociation> getDeclaredAssociation(final String id, final MixedIn mixedIn) {
		return objectAssociationContainer().getDeclaredAssociation(id, mixedIn);
	}

	@Override
	default Stream<ObjectAssociation> streamDeclaredAssociations(final MixedIn mixedIn) {
		return objectAssociationContainer().streamDeclaredAssociations(mixedIn);
	}

	@Override
	default Stream<ObjectAssociation> streamAssociationsForColumnRendering(final ColumnQuery columnQuery) {
		return objectAssociationContainer().streamAssociationsForColumnRendering(columnQuery);
	}

}
