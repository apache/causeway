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
package org.apache.causeway.core.metamodel.tabular;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.MultiselectChoices;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmSortUtils;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.internal.DataTableInternal;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

public interface DataTableInteractive extends MultiselectChoices {

    // -- FACTORIES

    public static DataTableInteractive empty(final ManagedMember managedMember, final Where where) {
        return DataTableInternal.empty(managedMember, where);
    }

    public static DataTableInteractive forCollection(final ManagedCollection managedCollection) {
        return DataTableInternal.forCollection(managedCollection);
    }

    public static DataTableInteractive forAction(
            final ManagedAction managedAction,
            final ManagedObject actionResult) {
        return DataTableInternal.forAction(managedAction, actionResult);
    }

    // --

    /**
     * Sorting helper class, that has the column index to be sorted by and the sort direction.
     */
    public record ColumnSort(
        int columnIndex,
        MmSortUtils.SortDirection sortDirection) implements Serializable {

        public Optional<Comparator<ManagedObject>> asComparator(final Can<? extends DataColumn> columns) {
            var columnToSort = columns.get(columnIndex).orElse(null);
            var sortProperty = columnToSort.associationMetaModel().getSpecialization().leftIfAny();
            return Optional.ofNullable(sortProperty)
                    .map(prop->MmSortUtils.orderingBy(sortProperty, sortDirection));
        }
    }

    // -- TITLE, ROWS AND COLUMNS

    Observable<String> titleObservable();
    Observable<Can<DataColumn>> dataColumnsObservable();
    Observable<Can<ManagedObject>> dataElementsObservable();
    Observable<Can<DataRow>> dataRowsFilteredAndSortedObservable();

    // -- META DATA

    ObjectMember getMetaModel();
    Optional<TableDecorator> getTableDecoratorIfAny();

    // -- ASSOCIATED ACTION

    ActionInteraction startAssociatedActionInteraction(final String actionId, final Where where);

    // -- ROW COUNT

    /**
     * Counts number of rows in {@link #dataRowsFilteredAndSortedObservable()}.
     */
    int getFilteredElementCount();

    // -- ROW LOOKUP

    /**
     * Lookup {@link DataRow} by its immutable zero-based index.
     */
    Optional<DataRow> lookupDataRow(int rowIndex);

    // -- PAGING

    int getPageSize(int pageSizeDefault);

    // -- SORTING

    Bindable<ColumnSort> columnSortBindable();

    // -- SELECTION

    void doProgrammaticToggle(Runnable runnable);
    Set<Integer> getSelectedRowIndexes();
    Observable<Can<DataRow>> dataRowsSelectedObservable();
    void selectRangeOfRowsByIndex(IntStream range, boolean select);
    void selectAllFiltered(boolean select);
    void selectAll(boolean select);

    // -- EXPORTING

    DataTable export();

    // -- SERIALIZATION

    DataTableMemento createMemento();

    // -- FILTER SUPPORT

    Bindable<String> searchArgumentBindable();
    boolean isSearchSupported();
    /**
     * @apiNote never called when not {@link #isSearchSupported()}
     */
    String getSearchPromptPlaceholderText();

}
