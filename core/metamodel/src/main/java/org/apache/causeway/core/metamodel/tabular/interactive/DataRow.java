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
package org.apache.causeway.core.metamodel.tabular.interactive;

import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Bindables.BooleanBindable;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.Getter;
import lombok.NonNull;

public class DataRow {

    @Getter private final UUID uuid = UUID.randomUUID(); // in support of client side sorting
    private final ManagedObject rowElement;
    @Getter private final BooleanBindable selectToggle;
    @Getter private final DataTableInteractive parentTable;

    public DataRow(
            final @NonNull DataTableInteractive parentTable,
            final @NonNull ManagedObject rowElement) {
        this.parentTable = parentTable;
        this.rowElement = rowElement;

        selectToggle = _Bindables.forBoolean(false);
        selectToggle.addListener((e,o,n)->{

            //_ToggleDebug.onSelectRowToggle(rowElement, o, n, parentTable.isToggleAllEvent.get());

            if(parentTable.isToggleAllEvent.get()) {
                return;
            }
            parentTable.getDataRowsSelected().invalidate();
            // in any case, if we have a toggle state change, clear the toggle all bindable
            parentTable.clearToggleAll();
        });

    }

    public ManagedObject getRowElement() {
        return rowElement;
    }

    public Optional<DataColumn> lookupColumnById(final @NonNull String columnId) {
        return parentTable.getDataColumns().getValue().stream()
                .filter(dataColumn->dataColumn.getColumnId().equals(columnId))
                .findFirst();
    }

    /**
     * Can be none, one or many per table cell.
     */
    public Can<ManagedObject> getCellElementsForColumn(final @NonNull DataColumn column) {
        final ObjectAssociation assoc = column.getAssociationMetaModel();
        return assoc.getSpecialization().fold(
                property->Can.of(property.get(getRowElement())),
                collection->ManagedObjects.unpack(collection.get(getRowElement())));
    }

    /**
     * Can be none, one or many per table cell. (returns empty Can if column not found)
     */
    public Can<ManagedObject> getCellElementsForColumn(final @NonNull String columnId) {
        return lookupColumnById(columnId)
                .map(this::getCellElementsForColumn)
                .orElseGet(Can::empty);
    }

}
