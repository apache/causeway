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

import org.apache.causeway.applib.annotation.Where;

import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.services.filter.CollectionFilterService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Bindables.BooleanBindable;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.tabular.DataColumn;
import org.apache.causeway.core.metamodel.tabular.DataRow;

import lombok.Getter;
import lombok.NonNull;

class DataRowInternal
implements DataRow {

    @Getter private final int rowIndex;
    private final ManagedObject rowElement;
    @Getter private final BooleanBindable selectToggle;
    @Getter private final DataTableInternal parentTable;
    @Getter final Optional<CollectionFilterService.Tokens> filterTokens;

    DataRowInternal(
            final int rowIndex,
            final @NonNull DataTableInternal parentTable,
            final @NonNull ManagedObject rowElement,
            final @Nullable CollectionFilterService.Tokens filterTokens) {
        this.rowIndex = rowIndex;
        this.parentTable = parentTable;
        this.rowElement = rowElement;

        selectToggle = _Bindables.forBoolean(false);
        selectToggle.addListener((e,o,n)->{
            //_ToggleDebug.onSelectRowToggle(rowElement, o, n, parentTable.isToggleAllEvent.get());
            parentTable.handleRowSelectToggle();
        });
        this.filterTokens = Optional.ofNullable(filterTokens);
    }

    @Override
    public ManagedObject getRowElement() {
        return rowElement;
    }

    @Override
    public Optional<DataColumn> lookupColumnById(final @NonNull String columnId) {
        return parentTable.getDataColumns().getValue().stream()
                .filter(dataColumn->dataColumn.getColumnId().equals(columnId))
                .findFirst();
    }

    /**
     * Can be none, one or many per table cell.
     */
    @Override
    public Can<ManagedObject> getCellElementsForColumn(final @NonNull DataColumn column) {
        final ObjectAssociation assoc = column.getAssociationMetaModel();
        var interactionInitiatedBy = InteractionInitiatedBy.PASS_THROUGH;
        return assoc.getSpecialization().fold(
                property-> Can.of(
                        // similar to ManagedProperty#reassessPropertyValue
                        property.isVisible(getRowElement(), interactionInitiatedBy, Where.ALL_TABLES).isAllowed()
                                ? property.get(getRowElement(), interactionInitiatedBy)
                                : ManagedObject.empty(property.getElementType())),
                collection-> ManagedObjects.unpack(
                        collection.isVisible(getRowElement(), interactionInitiatedBy, Where.ALL_TABLES).isAllowed()
                                ? collection.get(getRowElement(), interactionInitiatedBy)
                                : null
                ));
    }

    /**
     * Can be none, one or many per table cell. (returns empty Can if column not found)
     */
    @Override
    public Can<ManagedObject> getCellElementsForColumn(final @NonNull String columnId) {
        return lookupColumnById(columnId)
                .map(this::getCellElementsForColumn)
                .orElseGet(Can::empty);
    }

}
