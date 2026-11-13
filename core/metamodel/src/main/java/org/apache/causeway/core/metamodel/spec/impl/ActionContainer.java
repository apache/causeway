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
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;

record ActionContainer(
		/**
		 * scopes as available at runtime
		 */
		ImmutableEnumSet<ActionScope> actionScopesAtRuntime,
		List<ObjectAction> productionActions,
		List<ObjectAction> prototypeActions,
		ObjectActionContainer superContainer)
implements ObjectActionContainer {

	// useful for types that have no mixin support e.g. value types
	static ActionContainer EMPTY = new ActionContainer(
			ImmutableEnumSet.noneOf(ActionScope.class),
			List.of(), List.of(), //Can.empty(),
			null);

	ActionContainer(
			final List<ObjectAction> actionsInOrder,
			/**
			 * scopes as available at runtime
			 */
			final ImmutableEnumSet<ActionScope> actionScopes,
			final ObjectActionContainer superContainer) {
		this(actionScopes,
				catalogue(actionsInOrder, ActionScope.PRODUCTION),
				catalogue(actionsInOrder, ActionScope.PROTOTYPE),
				superContainer);
	}

	@Override
	public Optional<ObjectAction> getAction(
			final String id,
			final ImmutableEnumSet<ActionScope> actionScopes,
			final MixedIn mixedIn) {
        var declaredAction = getDeclaredAction(id, mixedIn); // no inheritance nor type considered

        if(declaredAction.isPresent()) {
            // action found but if its not the right type, stop searching
            if(!actionScopes.contains(declaredAction.get().getScope()))
				return Optional.empty();
            return declaredAction;
        }

        return isTypeHierarchyRoot()
                ? Optional.empty() // stop searching
                : superContainer.getAction(id, actionScopes, mixedIn);
	}

	@Override
	public Optional<ObjectAction> getDeclaredAction(
			final String id,
			final ImmutableEnumSet<ActionScope> actionScopes,
			final MixedIn mixedIn) {
        return _Strings.isEmpty(id)
            ? Optional.empty()
            : streamDeclaredActions(actionScopes, mixedIn)
                .filter(action->
                    id.equals(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString())
                            || id.equals(action.getFeatureIdentifier().memberLogicalName())
                )
                .findFirst();
	}

	@Override
	public Stream<ObjectAction> streamActions(
			final ImmutableEnumSet<ActionScope> actionTypes,
			final MixedIn mixedIn,
			final Consumer<ObjectAction> onActionOverloaded) {

		var actionStream = isTypeHierarchyRoot()
                ? streamDeclaredActions(actionTypes, mixedIn) // stop going deeper
                : Stream.concat(
                        streamDeclaredActions(actionTypes, mixedIn),
                        superContainer.streamActions(actionTypes, mixedIn));

        var actionSignatures = new HashSet<String>();
        var actionIds = new HashSet<String>();

        return actionStream

            // as of contributing super-classes same actions might appear more than once (overriding)
            .filter(action->{
                if(action.isMixedIn())
					return true; // do not filter mixedIn actions based on signature
                var isUnique = actionSignatures
                        .add(action.getFeatureIdentifier().getMemberNameAndParameterClassNamesIdentityString());
                return isUnique;
            })

            // ensure we don't emit duplicates
            .filter(action->{
                var isUnique = actionIds.add(action.getId());
                if(!isUnique) {
                    onActionOverloaded.accept(action);
                }
                return isUnique;
            });
	}

	@Override
	public Stream<ObjectAction> streamRuntimeActions(final MixedIn mixedIn) {
       return streamActions(actionScopesAtRuntime, mixedIn);
	}

	@Override
	public Stream<ObjectAction> streamActionsForColumnRendering(final Where where) {
		return streamRuntimeActions(MixedIn.INCLUDED)
	            .filter(ObjectAction.Predicates.visibleAccordingToHiddenFacet(where))
	            .sorted((a, b)->a.getCanonicalFriendlyName().compareTo(b.getCanonicalFriendlyName()));
	}

	@Override
	public Stream<ObjectAction> streamDeclaredActions(
			final ImmutableEnumSet<ActionScope> actionScopes,
			final MixedIn mixedIn) {
        return actionScopes.stream()
            .flatMap(actionScope->list(actionScope).stream())
            .filter(mixedIn.toFilter());
	}

	// -- HELPER

	private boolean isTypeHierarchyRoot() {
        return superContainer==null;
    }

	private List<ObjectAction> list(final ActionScope actionScope) {
		return switch (actionScope) {
			case PRODUCTION ->  productionActions;
			case PROTOTYPE -> prototypeActions;
		};
	}

	private static List<ObjectAction> catalogue(
			final List<ObjectAction> actionsInOrder,
			final ActionScope actionScope) {
		return actionsInOrder.stream()
			.filter(ObjectAction.Predicates.ofActionType(actionScope))
			.toList();
	}
}
