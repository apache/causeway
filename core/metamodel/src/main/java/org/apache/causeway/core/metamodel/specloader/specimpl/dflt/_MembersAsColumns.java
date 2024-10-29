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
package org.apache.causeway.core.metamodel.specloader.specimpl.dflt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderService;
import org.apache.causeway.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.util.WhereContexts;

import static org.apache.causeway.applib.annotation.Where.PARENTED_TABLES;
import static org.apache.causeway.applib.annotation.Where.STANDALONE_TABLES;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class _MembersAsColumns implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    public final Stream<ObjectAction> streamActionsForColumnRendering(
            final ObjectSpecification elementType,
            final Identifier memberIdentifier) {
        if(elementType.isValue()) return Stream.empty();

        return elementType.streamRuntimeActions(MixedIn.INCLUDED)
                .filter(ObjectAction.Predicates.visibleAccordingToHiddenFacet(WhereContexts.collectionVariant(memberIdentifier)))
                .sorted((a, b)->a.getCanonicalFriendlyName().compareTo(b.getCanonicalFriendlyName()));
    }

    public final Stream<ObjectAssociation> streamAssociationsForColumnRendering(
            final ObjectSpecification elementType,
            final Identifier memberIdentifier,
            final ManagedObject parentObject) {

        // the type that has the properties and collections that make up this table's columns
        var elementClass = elementType.getCorrespondingClass();

        var parentSpecIfAny = parentObject.getSpecification();

        var assocById = _Maps.<String, ObjectAssociation>newLinkedHashMap();

        elementType.streamAssociations(MixedIn.INCLUDED)
        .filter(ObjectAssociation.Predicates.visibleAccordingToHiddenFacet(WhereContexts.collectionVariant(memberIdentifier)))
        .filter(ObjectAssociation.Predicates.referencesParent(parentSpecIfAny).negate())
        .filter(assoc->filterColumnsUsingSpi(assoc, elementClass)) // optional SPI to filter columns;
        .forEach(assoc->assocById.put(assoc.getId(), assoc));

        var assocIdsInOrder = _Lists.<String>newArrayList(assocById.keySet());

        // sort by order of occurrence within associated layout, if any
        propertyIdComparator(elementType)
        .ifPresent(assocIdsInOrder::sort);

        // optional SPI to reorder columns
        sortColumnsUsingSpi(memberIdentifier, parentObject, assocIdsInOrder, elementClass);

        // add all ordered columns to the table
        return assocIdsInOrder.stream()
                .map(assocById::get)
                .filter(_NullSafe::isPresent);
    }

    // -- HELPER

    private boolean filterColumnsUsingSpi(
            final ObjectAssociation assoc,
            final Class<?> elementType) {
        return getServiceRegistry()
                .select(TableColumnVisibilityService.class)
                .stream()
                .noneMatch(x -> x.hides(elementType, assoc.getId()));
    }

    // comparator based on grid facet, that is by order of occurrence within associated layout
    private Optional<Comparator<String>> propertyIdComparator(
            final @NonNull ObjectSpecification elementTypeSpec) {

        // same code also appears in EntityPage.
        // we need to do this here otherwise any tables will render the columns in the wrong order until at least
        // one object of that type has been rendered via EntityPage.
        var elementTypeGridFacet = elementTypeSpec.getFacet(GridFacet.class);

        if(elementTypeGridFacet == null) {
            return Optional.empty();
        }

        // the facet should always exist, in fact
        // just enough to ask for the metadata.

        // don't pass in any object, just need the meta-data
        var elementTypeGrid = elementTypeGridFacet.getGrid(null);

        final Map<String, Integer> propertyIdOrderWithinGrid = new HashMap<>();
        elementTypeGrid.getAllPropertiesById().forEach((propertyId, __)->{
            propertyIdOrderWithinGrid.put(propertyId, propertyIdOrderWithinGrid.size());
        });

        // if propertyId is mentioned within grid, put into first 'half' ordered by
        // occurrence within grid
        // if propertyId is not mentioned within grid, put into second 'half' ordered by
        // propertyId (String) in natural order
        return Optional.of(Comparator
                .<String>comparingInt(propertyId->
                propertyIdOrderWithinGrid.getOrDefault(propertyId, Integer.MAX_VALUE))
                .thenComparing(Comparator.naturalOrder()));
    }

    private void sortColumnsUsingSpi(
            final Identifier memberIdentifier,
            final ManagedObject parentObject,
            final List<String> propertyIdsInOrder,
            final Class<?> elementType) {

        var tableColumnOrderServices = getServiceRegistry().select(TableColumnOrderService.class);
        if(tableColumnOrderServices.isEmpty()) {
            return;
        }

        var whereContext = whereContextFor(memberIdentifier);

        tableColumnOrderServices.stream()
        .map(tableColumnOrderService->
            whereContext.inStandaloneTable()
            ? tableColumnOrderService.orderStandalone(
                    elementType,
                    propertyIdsInOrder)
            : tableColumnOrderService.orderParented(
                        parentObject.getPojo(),
                        memberIdentifier.getMemberLogicalName(),
                        elementType,
                        propertyIdsInOrder))
        .filter(_NullSafe::isPresent)
        .findFirst()
        .filter(propertyReorderedIds->propertyReorderedIds!=propertyIdsInOrder) // skip if its the same object
        .ifPresent(propertyReorderedIds->{
            propertyIdsInOrder.clear();
            propertyIdsInOrder.addAll(propertyReorderedIds);
        });

    }

    static Where whereContextFor(final Identifier memberIdentifier) {
        return memberIdentifier.getType().isAction()
                ? STANDALONE_TABLES
                : PARENTED_TABLES;
    }

}
