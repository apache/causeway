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
package org.apache.causeway.core.metamodel.facets.actions.synthetic;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.UtilityClass;
import lombok.val;

@UtilityClass
class ParentedCollectionNavigationMatchingUtil {

    MatchResult match(
            final @NonNull OneToManyAssociation collection,
            final @NonNull Can<ObjectAssociation> filterProperties,
            final Can<ManagedObject> argumentAdapters,
            final @NonNull InteractionInitiatedBy interactionInitiatedBy) {

        if(argumentAdapters == null
                || argumentAdapters.isEmpty()) {
            return MatchResult.missingParent();
        }

        val parentAdapter = argumentAdapters.getElseFail(0);
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(parentAdapter)) {
            return MatchResult.missingParent();
        }

        val collectionAdapter = collection.get(parentAdapter, interactionInitiatedBy);
        val matches = CollectionFacet.streamAdapters(collectionAdapter)
                .filter(childAdapter -> matches(childAdapter, filterProperties, argumentAdapters, interactionInitiatedBy))
                .collect(Collectors.toList());

        return MatchResult.of(matches);
    }

    String validationMessage(
            final @NonNull String actionId,
            final @NonNull MatchResult matchResult) {
        switch (matchResult.getStatus()) {
        case MISSING_PARENT:
            return String.format("missing parent argument for synthetic navigation %s", actionId);
        case NO_MATCH:
        case MULTIPLE_MATCHES:
            return String.format("%d items match. Use parameters to match just one item.", matchResult.getMatches().size());
        case EXACTLY_ONE:
        default:
            return null;
        }
    }

    private boolean matches(
            final ManagedObject childAdapter,
            final Can<ObjectAssociation> filterProperties,
            final Can<ManagedObject> argumentAdapters,
            final InteractionInitiatedBy interactionInitiatedBy) {
        for(int i = 0; i < filterProperties.size(); i++) {
            if(argumentAdapters.size() <= i + 1) {
                continue;
            }
            val argumentAdapter = argumentAdapters.getElseFail(i + 1);
            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(argumentAdapter)) {
                continue;
            }
            val property = filterProperties.getElseFail(i);
            val propertyValue = property.get(childAdapter, interactionInitiatedBy);
            if(!matches(MmUnwrapUtils.single(propertyValue), MmUnwrapUtils.single(argumentAdapter))) {
                return false;
            }
        }
        return true;
    }

    private boolean matches(final Object propertyValue, final Object argumentValue) {
        if(propertyValue instanceof String
                && argumentValue instanceof String) {
            return ((String) propertyValue).contains((String) argumentValue);
        }
        return Objects.equals(propertyValue, argumentValue);
    }

    @Value(staticConstructor = "of")
    static class MatchResult {
        @NonNull Status status;
        @NonNull List<ManagedObject> matches;

        static MatchResult missingParent() {
            return of(Status.MISSING_PARENT, List.of());
        }

        static MatchResult of(final @NonNull List<ManagedObject> matches) {
            if(matches.isEmpty()) {
                return of(Status.NO_MATCH, matches);
            }
            if(matches.size() == 1) {
                return of(Status.EXACTLY_ONE, matches);
            }
            return of(Status.MULTIPLE_MATCHES, matches);
        }

        ManagedObject singleMatch() {
            return matches.get(0);
        }
    }

    enum Status {
        MISSING_PARENT,
        NO_MATCH,
        MULTIPLE_MATCHES,
        EXACTLY_ONE
    }

}
