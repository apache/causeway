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
package org.apache.causeway.core.metamodel.object;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.collections.CollectionFacet;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmVisibilityUtil {

    public static Predicate<? super ManagedObject> filterOn(final InteractionInitiatedBy interactionInitiatedBy) {
        return $->MmVisibilityUtil.isVisible($, interactionInitiatedBy);
    }

    /**
     * Filters a collection (an adapter around either a Collection or an Object[]) and returns a stream of
     * {@link ManagedObject}s of those that are visible (as per any facet(s) installed on the element class
     * of the collection).
     * @param collectionAdapter - an adapter around a collection (as returned by a getter of a collection, or of an autoCompleteNXxx() or choicesNXxx() method, etc
     * @param interactionInitiatedBy
     */
    public static Stream<ManagedObject> streamVisibleAdapters(
            final ManagedObject collectionAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return CollectionFacet.streamAdapters(collectionAdapter)
                .filter(MmVisibilityUtil.filterOn(interactionInitiatedBy));
    }

    private static Stream<Object> streamVisiblePojos(
            final ManagedObject collectionAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return CollectionFacet.streamAdapters(collectionAdapter)
                .filter(MmVisibilityUtil.filterOn(interactionInitiatedBy))
                .map(MmUnwrapUtil::single);
    }

    public static Object[] visiblePojosAsArray(
            final ManagedObject collectionAdapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        return streamVisiblePojos(collectionAdapter, interactionInitiatedBy)
                .collect(_Arrays.toArray(Object.class));
    }

    public static Object visiblePojosAutofit(
            final ManagedObject collectionAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Class<?> requiredContainerType) {

        val visiblePojoStream = streamVisiblePojos(collectionAdapter, interactionInitiatedBy);
        val autofittedObjectContainer = CollectionFacet.AutofitUtils
                .collect(visiblePojoStream, requiredContainerType);
        return autofittedObjectContainer;
    }


    /**
     * @param adapter - wrapper of domain object whose visibility is being checked,
     *      must not be a mixin
     * @param interactionInitiatedBy
     */
    public static boolean isVisible(
            final ManagedObject adapter,
            final InteractionInitiatedBy interactionInitiatedBy) {

        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(adapter)) {
            // a choices list could include a null (eg example in ToDoItems#choices1Categorized()); want to show as "visible"
            return true;
        }
        val spec = adapter.getSpecification();
        if(spec.isEntity()) {
            if(MmEntityUtil.isDetachedCannotReattach(adapter)) {
                return false;
            }
        }
        if(!interactionInitiatedBy.isUser()) {
            return true;
        }
        val visibilityContext = createVisibleInteractionContext(
                adapter,
                InteractionInitiatedBy.USER,
                Where.OBJECT_FORMS);

        return InteractionUtils.isVisibleResult(spec, visibilityContext)
                .isNotVetoing();
    }

    private static VisibilityContext createVisibleInteractionContext(
            final ManagedObject objectAdapter,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Where where) {

        return new ObjectVisibilityContext(
                InteractionHead.regular(objectAdapter),
                objectAdapter.getSpecification().getFeatureIdentifier(),
                interactionInitiatedBy,
                where);
    }

}