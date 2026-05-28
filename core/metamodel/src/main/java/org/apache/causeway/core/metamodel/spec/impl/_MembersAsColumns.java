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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.services.tablecol.TableColumnOrderService;
import org.apache.causeway.applib.services.tablecol.TableColumnVisibilityService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facets.collections.layout.columnorder.ColumnOrderPatchingFacet;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAction;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociationContainer.ColumnQuery;

record _MembersAsColumns(
		boolean isColumnOrderPatchingEnabled,
		Can<TableColumnVisibilityService> tableColumnVisibilityServices,
		Can<TableColumnOrderService> tableColumnOrderServices) {

	_MembersAsColumns(final MetaModelContext mmc) {
		this(
				mmc.getSystemEnvironment().isPrototyping(),
				mmc.getServiceRegistry().select(TableColumnVisibilityService.class),
				mmc.getServiceRegistry().select(TableColumnOrderService.class));
	}

    public Stream<ObjectAction> streamActionsForColumnRendering(
            final ObjectSpecification elementType,
            final Where where) {
        if(elementType.isValue())
			return Stream.empty();

        return elementType.streamRuntimeActions(MixedIn.INCLUDED)
            .filter(ObjectAction.Predicates.visibleAccordingToHiddenFacet(where))
            .sorted((a, b)->a.getCanonicalFriendlyName().compareTo(b.getCanonicalFriendlyName()));
    }

    /**
     * @param parentObject not used for standalone tables and allowed to be empty for parented ones
     */
	public Stream<ObjectAssociation> streamAssociationsForColumnRendering(
			// the type that has the properties and collections that make up this table's columns
			final ObjectSpecification elementType,
			final ColumnQuery columnQuery) {

        var assocById = assembleAvailableColumns(elementType, columnQuery);
        var assocIdsInOrder = new ArrayList<>(assocById.keySet());

        // sort by order of occurrence within associated layout, if any
        propertyIdComparator(elementType)
            .ifPresent(assocIdsInOrder::sort);

        // when querying for AVAILABLE columns, we skip the column sorting SPI and also the column-patching (PROTOTYPING feature)
        if(columnQuery.mode().isEnabled()) {
        	if(!sortColumnsUsingPatch(columnQuery, assocIdsInOrder, elementType)) {
				// SPI to reorder columns, where TableColumnOrderServiceUsingTxtFile is a built-in one
        		// apply only, if not patched
		        sortColumnsUsingSpi(columnQuery, assocIdsInOrder, elementType);
			}
        }

        // stream columns in final order
        return assocIdsInOrder.stream()
            .map(assocById::get)
            .filter(Objects::nonNull);
    }

    // -- HELPER

	private Map<String, ObjectAssociation> assembleAvailableColumns(
			final ObjectSpecification elementType,
			final ColumnQuery columnQuery) {

        final var assocById = new LinkedHashMap<String, ObjectAssociation>();

		elementType.streamAssociations(MixedIn.INCLUDED)
            .filter(ObjectAssociation.Predicates.visibleAccordingToHiddenFacet(columnQuery.where()))
            .filter(columnQuery.isStandalone()
				? _Predicates.alwaysTrue()
				: ObjectAssociation.Predicates.referencesParent(columnQuery.parentObject().objSpec()).negate())
            .filter(assoc->hideColumnUsingSpi(assoc, elementType.getCorrespondingClass()))
            .forEach(assoc->assocById.put(assoc.getId(), assoc));

		return assocById;
	}

    private boolean hideColumnUsingSpi(
            final ObjectAssociation assoc,
            final Class<?> elementType) {
        return tableColumnVisibilityServices
            .stream()
            .noneMatch(it -> it.hides(elementType, assoc.getId()));
    }

    // comparator based on grid facet, that is by order of occurrence within associated layout
    private Optional<Comparator<String>> propertyIdComparator(
            final @NonNull ObjectSpecification elementTypeSpec) {

        // same code also appears in DomainObjectPage.
        // we need to do this here otherwise any tables will render the columns in the wrong order until at least
        // one object of that type has been rendered via DomainObjectPage.
        var elementTypeGridFacet = elementTypeSpec.lookupFacet(GridFacet.class).orElse(null);

        if(elementTypeGridFacet == null)
			return Optional.empty();

        // the facet should always exist, in fact
        // just enough to ask for the metadata.

        // don't pass in any object, just need the meta-data
        var elementTypeGrid = elementTypeGridFacet.getGrid(null);
        if(elementTypeGrid ==null)
			return Optional.empty();

        final Map<String, Integer> propertyIdOrderWithinGrid = new HashMap<>();
        elementTypeGrid.streamPropertyLayoutData()
            .map(PropertyLayoutData::getId)
            .forEach(propertyId->{
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


    /**
     * @return whether a column-order patch was found and applied
     */
    private boolean sortColumnsUsingPatch(
    		final ColumnQuery columnQuery,
            final List<String> assocIdsInOrder, //mutable
            final ObjectSpecification elementType) {

    	if(!isColumnOrderPatchingEnabled)
    		return false;

    	var identifier = columnQuery.isStandalone()
    			? elementType.getFeatureIdentifier()
    			: columnQuery.memberIdentifier();
    	Objects.requireNonNull(identifier, ()->"framework bug");

    	var patchedColumnOrder = elementType
			.lookupFacet(ColumnOrderPatchingFacet.class)
			.flatMap(it->it.lookupColumnOrder(identifier))
			.orElse(null);

    	if(patchedColumnOrder==null) {
    	    // for the standalone case we are done
    	    if(columnQuery.isStandalone())
    	        return false;
            // if the parented case is not patched, but we find a patch for the corresponding element-type,
            // then use that as a fallback
    	    return sortColumnsUsingPatch(columnQuery.toStandalone(), assocIdsInOrder, elementType);
    	}

    	// intersect 'assocIdsInOrder' with 'patchedColumnOrder' while preserving order as given by the latter
    	var available = new HashSet<>(assocIdsInOrder);
    	assocIdsInOrder.clear();
    	patchedColumnOrder.stream()
			.filter(available::contains)
			.forEach(assocIdsInOrder::add);

		return true;
    }

    private void sortColumnsUsingSpi(
            final ColumnQuery columnQuery,
            final List<String> assocIdsInOrder, //mutable
            final ObjectSpecification elementType) {

        if(tableColumnOrderServices.isEmpty())
			return;

        tableColumnOrderServices.stream()
            .map(tableColumnOrderService->
                columnQuery.isStandalone()
                ? tableColumnOrderService.orderStandalone(
                        elementType.getCorrespondingClass(),
                        assocIdsInOrder)
                : tableColumnOrderService.orderParented(
                		columnQuery.parentObject().getPojo(),
                        columnQuery.memberIdentifier().memberLogicalName(),
                        elementType.getCorrespondingClass(),
                        assocIdsInOrder))
            .filter(Objects::nonNull)
            .findFirst()
            .filter(assocReorderedIds->assocReorderedIds!=assocIdsInOrder) // skip if its the same object
            .ifPresent(assocReorderedIds->{
                assocIdsInOrder.clear();
                assocIdsInOrder.addAll(assocReorderedIds);
            });
    }

}
