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
package org.apache.causeway.core.metamodel.tabular.internal;

import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.filter.CollectionFilterService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Bindables.BooleanBindable;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.interactions.VisibilityConstraint;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.tabular.DataColumn;
import org.apache.causeway.core.metamodel.tabular.DataRow;

record DataRowInternal(
    int rowIndex,
    ManagedObject rowElement,
    BooleanBindable selectToggleBindable,
    DataTableInternal parentTable,
    Optional<CollectionFilterService.Tokens> filterTokens
    ) implements DataRow {

    DataRowInternal(
            final int rowIndex,
            final @NonNull DataTableInternal parentTable,
            final @NonNull ManagedObject rowElement,
            final CollectionFilterService.@Nullable Tokens filterTokens) {
        this(rowIndex, rowElement, _Bindables.forBoolean(false), parentTable, Optional.ofNullable(filterTokens));
        selectToggleBindable.addListener((event, old, neW)->parentTable.handleRowSelectToggle());
    }

    @Override
    public Optional<DataColumn> lookupColumnById(final @NonNull String columnId) {
        return parentTable.dataColumnsObservable().getValue().stream()
                .filter(dataColumn->dataColumn.columnId().equals(columnId))
                .findFirst();
    }

    @Override
    public Can<ManagedObject> getCellElementsForColumn(final @NonNull DataColumn column) {
        final ObjectAssociation assoc = column.associationMetaModel();
        return assoc.getSpecialization().fold(
                property-> Can.of(
                        // similar to ManagedProperty#reassessPropertyValue
                        property.isVisible(rowElement(), InteractionInitiatedBy.USER, VISIBILITY_CONSTRAINT).isAllowed()
                                ? property.get(rowElement(), InteractionInitiatedBy.USER)
                                : ManagedObject.empty(property.getElementType())),
                collection-> ManagedObjects.unpack(
                        collection.isVisible(rowElement(), InteractionInitiatedBy.USER, VISIBILITY_CONSTRAINT).isAllowed()
                                ? collection.get(rowElement(), InteractionInitiatedBy.USER)
                                : null
                ));
    }

    @Override
    public Can<ManagedObject> getCellElementsForColumn(final @NonNull String columnId) {
        return lookupColumnById(columnId)
                .map(this::getCellElementsForColumn)
                .orElseGet(Can::empty);
    }

    // we are always checking whether a property is visible, constraint by Where.ALL_TABLES (but not by WhatViewer)
    private final static VisibilityConstraint VISIBILITY_CONSTRAINT = VisibilityConstraint.noViewer(Where.ALL_TABLES);

}
