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
package org.apache.isis.core.metamodel.spec.feature;

import java.util.Comparator;
import java.util.OptionalInt;
import java.util.stream.Stream;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.compare._Comparators;
import org.apache.isis.commons.internal.factory._InstanceUtil;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.core.metamodel.facets.collections.sortedby.SortedByFacet;
import org.apache.isis.core.metamodel.facets.members.layout.order.LayoutOrderFacet;
import org.apache.isis.core.metamodel.facets.object.paged.PagedFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.memento.CollectionMemento;

import lombok.val;

public interface OneToManyAssociation
extends ObjectAssociation, OneToManyFeature {

    /**
     * Returns a serializable representation of this collection.
     */
    default CollectionMemento getMemento() {
        return CollectionMemento.forCollection(this);
    }

    // -- PAGE SIZE

    default OptionalInt getPageSize() {
        return Stream.of(this, getElementType())
            .map(facetHolder->facetHolder.getFacet(PagedFacet.class))
            .filter(_NullSafe::isPresent)
            .mapToInt(PagedFacet::value)
            .findFirst();
    }

    // -- SORTING

    /**
     * An element comparator corresponding to associated {@link SortedByFacet}.
     * The comparator operates on elements of type {@link ManagedObject}.
     * @return non-null
     */
    default Comparator<ManagedObject> getElementComparator(){

        var sortedBy = Stream.of(this, getElementType())
            .map(facetHolder->facetHolder.getFacet(SortedByFacet.class))
            .filter(_NullSafe::isPresent)
            .findFirst()
            .map(SortedByFacet::value)
            .orElse(null);

        if(sortedBy == null) {
            return (a, b) -> 0; // no-op comparator, works with Stream#sort
        }

        val pojoComparator = _Casts.<Comparator<Object>>uncheckedCast(
                _InstanceUtil.createInstance(sortedBy));
        getMetaModelContext().getServiceInjector().injectServicesInto(pojoComparator);

        return (a, b) -> pojoComparator.compare(a.getPojo(), b.getPojo());
    }

    // -- ASSOCIATED ACTIONS

    /**
     * Returns all actions that are associated with this collection,
     * and hence should be rendered close to this collection's UI representation.
     * Typically at the top bar of the UI collection panel.
     * <p>
     * Order matters, that is the order of returned actions corresponds to the order of
     * rendered (action) buttons.
     */
    default Stream<ObjectAction> streamAssociatedActions() {
        return getDeclaringType()
                .streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction.Predicates.isSameLayoutGroupAs(this))
                .sorted(this::deweyOrderCompare);
    }

    /**
     * Returns all actions that are targets for the multi-select UI feature.
     * That typically means, their first parameter is a non-scalar type with an
     * element type that corresponds to the element type of this collection.
     * <p>
     * Order does not matter, as currently only used to detect whether there are any
     */
    private Stream<ObjectAction> streamAssociatedActionsWithChoicesFromThisCollection() {
        return getDeclaringType()
                .streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction.Predicates.choicesFromAndHavingCollectionParameterFor(this));
    }

    default boolean hasAssociatedActionsWithChoicesFromThisCollection() {
        return streamAssociatedActionsWithChoicesFromThisCollection()
                .anyMatch(_Predicates.alwaysTrue());
    }

    // -- ASSOCIATED ACTION ORDER

    private int deweyOrderCompare(final ObjectAction a, final ObjectAction b) {
        val seqA = a.lookupFacet(LayoutOrderFacet.class)
            .map(LayoutOrderFacet::getSequence)
            .orElse("1");
        val seqB = b.lookupFacet(LayoutOrderFacet.class)
            .map(LayoutOrderFacet::getSequence)
            .orElse("1");
        return _Comparators.deweyOrderCompare(seqA, seqB);
    }

}
