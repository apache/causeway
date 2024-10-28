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
package org.apache.causeway.core.metamodel.tabular.simple;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.tabular.TabularModel;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.experimental.UtilityClass;

@UtilityClass
class TabularUtil {

    /**
     * Converts a {@link DataTable} to a {@link TabularModel.TabularSheet}.
     */
    TabularModel.TabularSheet toTabularSheet(
            final DataTable dataTable,
            final DataTable.AccessMode accessMode) {
        var interactionInitiatedBy = switch(accessMode) {
            case PASS_THROUGH -> InteractionInitiatedBy.PASS_THROUGH;
            default -> InteractionInitiatedBy.USER;
        };

        var columns = dataTable.getDataColumns()
                .map(IndexedFunction.zeroBased(TabularUtil::tabularColumn));
        var rows = dataTable.getDataRows()
                .map(dr->tabularRow(dataTable.getDataColumns(), dr, interactionInitiatedBy));
        return new TabularModel.TabularSheet(dataTable.getTableFriendlyName(), columns, rows);
    }

    // -- HELPER

    private TabularModel.TabularColumn tabularColumn(final int index, final DataColumn dc) {
        return new TabularModel.TabularColumn(
                index,
                dc.getColumnFriendlyName(),
                dc.getColumnDescription().orElse(""));
    }

    private TabularModel.TabularRow tabularRow(
            final Can<DataColumn> dataColumns,
            final DataRow dataRow,
            final InteractionInitiatedBy interactionInitiatedBy) {
        var cells = dataColumns.map(dataColumn->{
            var cellElements = dataRow.getCellElements(dataColumn, interactionInitiatedBy);
            final int cardinality = cellElements.size();
            final boolean forceUseTitle = !dataColumn.getMetamodel().getElementType().isValue();

            var tabularCell = cardinality==1
                    && !forceUseTitle
                ? TabularModel.TabularCell.single(cellElements.getFirstElseFail().getPojo())
                : TabularModel.TabularCell.labeled(cardinality, ()->cellElements.map(ManagedObject::getTitle).stream());
            return tabularCell;
        })
        .toList();

        return new TabularModel.TabularRow(Can.ofCollection(cells));
    }

}
