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
package org.apache.isis.core.metamodel.specloader.specimpl.dflt;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.tablecol.TableColumnOrderService;
import org.apache.isis.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.functions._Predicates;
import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.WhereValueFacet;
import org.apache.isis.core.metamodel.facets.all.hide.HiddenFacet;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.MixedIn;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
class _PropertiesAsColumns implements HasMetaModelContext {

    @Getter(onMethod_ = {@Override})
    private final MetaModelContext metaModelContext;

    public final Stream<OneToOneAssociation> streamPropertiesForColumnRendering(
            final ObjectSpecification elementType,
            final Identifier memberIdentifier,
            final ManagedObject parentObject) {

        // the type that has the properties that make up this table's columns
        val elementClass = elementType.getCorrespondingClass();

        val parentSpecIfAny = parentObject.getSpecification();

        val whereContext = whereContextFor(memberIdentifier);

        val propertyById = _Maps.<String, OneToOneAssociation>newLinkedHashMap();

        elementType.streamProperties(MixedIn.INCLUDED)
        .filter(property->property.streamFacets()
                .filter(facet -> facet instanceof HiddenFacet)
                .map(WhereValueFacet.class::cast)
                .map(WhereValueFacet::where)
                .noneMatch(where -> where.includes(whereContext)))
        .filter(associationDoesNotReferenceParent(parentSpecIfAny))
        .filter(property->filterColumnsUsingSpi(property, elementClass)) // optional SPI to filter columns;
        .forEach(property->propertyById.put(property.getId(), property));

        val propertyIdsInOrder = _Lists.<String>newArrayList(propertyById.keySet());

        // sort by order of occurrence within associated layout, if any
        propertyIdComparator(elementType)
        .ifPresent(propertyIdsInOrder::sort);

        // optional SPI to reorder columns
        sortColumnsUsingSpi(memberIdentifier, parentObject, propertyIdsInOrder, elementClass);

        // add all ordered columns to the table
        return propertyIdsInOrder.stream()
                .map(propertyById::get)
                .filter(_NullSafe::isPresent);

    }

    // -- HELPER

    private boolean filterColumnsUsingSpi(
            final ObjectAssociation property,
            final Class<?> elementType) {
        return getServiceRegistry()
                .select(TableColumnVisibilityService.class)
                .stream()
                .noneMatch(x -> x.hides(elementType, property.getId()));
    }

    // comparator based on grid facet, that is by order of occurrence within associated layout
    private Optional<Comparator<String>> propertyIdComparator(
            final @NonNull ObjectSpecification elementTypeSpec) {

        // same code also appears in EntityPage.
        // we need to do this here otherwise any tables will render the columns in the wrong order until at least
        // one object of that type has been rendered via EntityPage.
        val elementTypeGridFacet = elementTypeSpec.getFacet(GridFacet.class);

        if(elementTypeGridFacet == null) {
            return Optional.empty();
        }

        // the facet should always exist, in fact
        // just enough to ask for the metadata.

        // don't pass in any object, just need the meta-data
        val elementTypeGrid = elementTypeGridFacet.getGrid(null);

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

        val tableColumnOrderServices = getServiceRegistry().select(TableColumnOrderService.class);
        if(tableColumnOrderServices.isEmpty()) {
            return;
        }

        val whereContext = whereContextFor(memberIdentifier);

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

    static Predicate<ObjectAssociation> associationDoesNotReferenceParent(
            final @Nullable ObjectSpecification parentSpec) {
        if(parentSpec == null) {
            return _Predicates.alwaysTrue();
        }
        return (final ObjectAssociation property) -> {
                val hiddenFacet = property.getFacet(HiddenFacet.class);
                if(hiddenFacet == null) {
                    return true;
                }
                if (hiddenFacet.where() != Where.REFERENCES_PARENT) {
                    return true;
                }
                val propertySpec = property.getElementType();
                final boolean propertySpecIsOfParentSpec = parentSpec.isOfType(propertySpec);
                final boolean isVisible = !propertySpecIsOfParentSpec;
                return isVisible;
        };
    }

    static Where whereContextFor(final Identifier memberIdentifier) {
        return memberIdentifier.getType().isAction()
                ? Where.STANDALONE_TABLES
                : Where.PARENTED_TABLES;
    }

}
