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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;

@FunctionalInterface
interface HasObjectActionContainer extends ObjectActionContainer {

	ObjectActionContainer objectActionContainer();

	@Override
	default Optional<ObjectAction> getAction(
			final String id,
			final ImmutableEnumSet<ActionScope> actionScopes,
			final MixedIn mixedIn) {
		return objectActionContainer().getAction(id, actionScopes, mixedIn);
	}

	@Override
	default Optional<ObjectAction> getDeclaredAction(
			final String id,
			final ImmutableEnumSet<ActionScope> actionScopes,
			final MixedIn mixedIn) {
		return objectActionContainer().getDeclaredAction(id, actionScopes, mixedIn);
	}

	@Override
	default  Stream<ObjectAction> streamActions(
			final ImmutableEnumSet<ActionScope> actionTypes,
			final MixedIn mixedIn,
			final Consumer<ObjectAction> onActionOverloaded) {
		return objectActionContainer().streamActions(actionTypes, mixedIn, onActionOverloaded);
	}

	@Override
	default Stream<ObjectAction> streamRuntimeActions(final MixedIn mixedIn) {
		return objectActionContainer().streamRuntimeActions(mixedIn);
	}

	@Override
	default Stream<ObjectAction> streamActionsForColumnRendering(final Where where) {
		return objectActionContainer().streamActionsForColumnRendering(where);
	}

	@Override
	default Stream<ObjectAction> streamDeclaredActions(
			final ImmutableEnumSet<ActionScope> actionScopes,
			final MixedIn mixedIn) {
		return objectActionContainer().streamDeclaredActions(actionScopes, mixedIn);
	}

}
