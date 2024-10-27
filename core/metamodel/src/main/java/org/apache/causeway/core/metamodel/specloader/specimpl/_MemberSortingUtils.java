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
package org.apache.causeway.core.metamodel.specloader.specimpl;

import java.util.List;
import java.util.stream.Stream;

import org.apache.causeway.applib.exceptions.unrecoverable.UnknownTypeException;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.metamodel.layout.DeweyOrderSet;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

/** package private utility */
final class _MemberSortingUtils {

    // -- ASSOCIATION SORTING

    static List<ObjectAssociation> sortAssociationsIntoList(final Stream<ObjectAssociation> associations) {
        var deweyOrderSet = DeweyOrderSet.createOrderSet(associations);
        var orderedAssociations = _Lists.<ObjectAssociation> newArrayList();
        sortAssociations(deweyOrderSet, orderedAssociations);
        return orderedAssociations;
    }

    // -- ACTION SORTING

    static List<ObjectAction> sortActionsIntoList(final Stream<ObjectAction> actions) {
        var deweyOrderSet = DeweyOrderSet.createOrderSet(actions);
        var orderedActions = _Lists.<ObjectAction>newArrayList();
        sortActions(deweyOrderSet, orderedActions);
        return orderedActions;
    }

    // -- HELPER

    private static void sortAssociations(final DeweyOrderSet orderSet, final List<ObjectAssociation> associationsToAppendTo) {
        for (final Object element : orderSet) {
            if (element instanceof OneToManyAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof OneToOneAssociation) {
                associationsToAppendTo.add((ObjectAssociation) element);
            } else if (element instanceof DeweyOrderSet) {
                // just flatten.
                DeweyOrderSet childOrderSet = (DeweyOrderSet) element;
                sortAssociations(childOrderSet, associationsToAppendTo);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

    private static void sortActions(final DeweyOrderSet orderSet, final List<ObjectAction> actionsToAppendTo) {
        for (var element : orderSet) {
            if(element instanceof ObjectAction) {
                var objectAction = (ObjectAction) element;
                actionsToAppendTo.add(objectAction);
            }
            else if (element instanceof DeweyOrderSet) {
                var deweyOrderSet = ((DeweyOrderSet) element);
                var actions = _Lists.<ObjectAction>newArrayList();
                sortActions(deweyOrderSet, actions);
                actionsToAppendTo.addAll(actions);
            } else {
                throw new UnknownTypeException(element);
            }
        }
    }

}
