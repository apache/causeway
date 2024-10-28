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

import java.util.ArrayList;
import java.util.function.Function;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.tabular.TabularModel;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;

import lombok.experimental.UtilityClass;

/**
 * Converts a {@link DataTable} to a {@link TabularModel}.
 */
@UtilityClass
class TabularUtil {

    TabularModel toTabularModel(
            final DataTable dataTable,
            final CollectionContentsExporter.AccessMode accessMode) {
        var interactionInitiatedBy = switch(accessMode) {
            case PASS_THROUGH -> InteractionInitiatedBy.PASS_THROUGH;
            default -> InteractionInitiatedBy.USER;
        };

        var columns = dataTable.getDataColumns()
                .map(IndexedFunction.zeroBased(TabularUtil::tabularColumn));
        var rows = dataTable.getDataRows()
                .map(dr->tabularRow(dataTable.getDataColumns(), col->dr.getCellElements(col, interactionInitiatedBy)));
        var sheet = new TabularModel.TabularSheet(dataTable.getTableFriendlyName(), columns, rows);
        return new TabularModel(Can.of(sheet));
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
            final Function<DataColumn, Can<ManagedObject>> cellElementProvider) {
        var cells = new ArrayList<TabularModel.TabularCell>(dataColumns.size());
        //dataColumns.forEach(null); //TODO[CAUSEWAY-3825] implement!
        return new TabularModel.TabularRow(Can.ofCollection(cells));
    }

}
