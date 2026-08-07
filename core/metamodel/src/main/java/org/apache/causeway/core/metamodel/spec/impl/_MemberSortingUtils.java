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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.causeway.applib.exceptions.unrecoverable.UnknownTypeException;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.core.metamodel.layout.DeweyOrderSet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/** package private utility */
final class _MemberSortingUtils {

	// -- ASSOCIATION SORTING

    static List<ObjectAssociation> associationsInOrder(
    		final ObjectSpecification objSpec,
    		final List<? extends ObjectAssociation> regularAssociations,
            final List<? extends ObjectAssociation> mixedInAssociations) {
    	_MemberIdClashReporting.flagAnyMemberIdClashes(objSpec, regularAssociations, mixedInAssociations); // do before sorting
        return sortAssociationsIntoList(Stream.concat(
                regularAssociations.stream(),
                mixedInAssociations.stream()));
    }

    // -- ACTION SORTING

    static  List<ObjectAction> actionsInOrder(
    		final ObjectSpecification objSpec,
    		final List<? extends ObjectAction> regularActions,
            final List<? extends ObjectAction> mixedInActions,
            final List<? extends ObjectAction> syntheticActions) {
    	_MemberIdClashReporting.flagAnyMemberIdClashes(objSpec, regularActions, mixedInActions); // do before sorting
        return sortActionsIntoList(_Streams.concat(
        		regularActions.stream(),
        		mixedInActions.stream(),
        		syntheticActions.stream()));
    }

    // -- HELPER

    private static List<ObjectAssociation> sortAssociationsIntoList(final Stream<ObjectAssociation> associations) {
    	var deweyOrderSet = DeweyOrderSet.createOrderSet(associations);
    	var orderedAssociations = new ArrayList<ObjectAssociation>();
    	sortAssociations(deweyOrderSet, orderedAssociations);
    	return orderedAssociations;
    }

    private static List<ObjectAction> sortActionsIntoList(final Stream<ObjectAction> actions) {
    	var deweyOrderSet = DeweyOrderSet.createOrderSet(actions);
    	var orderedActions = new ArrayList<ObjectAction>();
    	sortActions(deweyOrderSet, orderedActions);
    	return orderedActions;
    }

    private static void sortAssociations(final DeweyOrderSet orderSet, final List<ObjectAssociation> associationsToAppendTo) {
        for (final Object element : orderSet) {
            if (element instanceof OneToManyAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof OneToOneAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof DeweyOrderSet childOrderSet) {
                // just flatten.
                sortAssociations(childOrderSet, associationsToAppendTo);
            } else
				throw new UnknownTypeException(element);
        }
    }

    private static void sortActions(final DeweyOrderSet orderSet, final List<ObjectAction> actionsToAppendTo) {
        for (var element : orderSet) {
            if(element instanceof ObjectAction objectAction) {
                actionsToAppendTo.add(objectAction);
            }
            else if (element instanceof DeweyOrderSet deweyOrderSet) {
                var actions = _Lists.<ObjectAction>newArrayList();
                sortActions(deweyOrderSet, actions);
                actionsToAppendTo.addAll(actions);
            } else
				throw new UnknownTypeException(element);
        }
    }

}
