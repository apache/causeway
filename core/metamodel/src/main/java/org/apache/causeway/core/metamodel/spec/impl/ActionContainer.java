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

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Multimaps;
import org.apache.causeway.commons.internal.collections._Multimaps.ListMultimap;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.core.metamodel.spec.ActionScope;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionContainer;

record ActionContainer(
		/**
		 * scopes as available at runtime
		 */
		ImmutableEnumSet<ActionScope> actionScopes,
		// partitions and caches objectActions by type; updated in sortCacheAndUpdateActions()
		ListMultimap<ActionScope, ObjectAction> objectActionsByType,
		Can<ObjectAction> actionsInOrder,
		ObjectActionContainer superContainer)
implements ObjectActionContainer {

	// e.g. used for value types
	static ActionContainer EMPTY = new ActionContainer(
			ImmutableEnumSet.noneOf(ActionScope.class),
			_Multimaps.newListMultimap(Map::of, List::of),
			Can.empty(),
			null);

	ActionContainer(
			final List<ObjectAction> actionsInOrder,
			/**
			 * scopes as available at runtime
			 */
			final ImmutableEnumSet<ActionScope> actionScopes,
			final ObjectActionContainer superContainer) {
		this(actionScopes, _Multimaps.newListMultimap(),
				build(actionsInOrder),
				superContainer);
		buildMap();
	}

	private static Can<ObjectAction> build(final List<ObjectAction> actionsInOrder) {
		return Can.ofCollection(actionsInOrder);
	}

	@Override
	public Optional<ObjectAction> getAction(final String id,
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
	public Optional<ObjectAction> getDeclaredAction(final String id, final ImmutableEnumSet<ActionScope> actionScopes,
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
	public Stream<ObjectAction> streamActions(final ImmutableEnumSet<ActionScope> actionTypes, final MixedIn mixedIn,
			final Consumer<ObjectAction> onActionOverloaded) {

		var actionStream = isTypeHierarchyRoot()
                ? streamDeclaredActions(actionTypes, mixedIn) // stop going deeper
                : Stream.concat(
                        streamDeclaredActions(actionTypes, mixedIn),
                        superContainer.streamActions(actionTypes, mixedIn));

        var actionSignatures = _Sets.<String>newHashSet();
        var actionIds = _Sets.<String>newHashSet();

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
       return streamActions(actionScopes, mixedIn);
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
            .flatMap(actionScope->stream(objectActionsByType.get(actionScope)))
            .filter(mixedIn.toFilter());
	}

	// -- HELPER

	private boolean isTypeHierarchyRoot() {
        return superContainer==null;
    }

	private void buildMap() {
        // rebuild objectActionsByType multi-map
        for (var actionType : ActionScope.values()) {
            var objectActionForType = objectActionsByType.getOrElseNew(actionType);
            objectActionForType.clear();
            actionsInOrder.stream()
	            .filter(ObjectAction.Predicates.ofActionType(actionType))
	            .forEach(objectActionForType::add);
        }
	}

}
