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

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

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
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class ColumnSort implements Serializable {
        private static final long serialVersionUID = 1L;
        final int columnIndex;
        final MmSortUtils.SortDirection sortDirection;
        public Optional<Comparator<ManagedObject>> asComparator(final Can<? extends DataColumn> columns) {
            var columnToSort = columns.get(columnIndex).orElse(null);
            var sortProperty = columnToSort.getAssociationMetaModel().getSpecialization().leftIfAny();
            return Optional.ofNullable(sortProperty)
                    .map(prop->MmSortUtils.orderingBy(sortProperty, sortDirection));
        }
    }

    // -- TITLE, ROWS AND COLUMNS

    Observable<String> getTitle();
    Observable<Can<DataColumn>> getDataColumns();
    Observable<Can<ManagedObject>> getDataElements();
    Observable<Can<DataRow>> getDataRowsFilteredAndSorted();

    // -- META DATA

    ObjectMember getMetaModel();
    Optional<TableDecorator> getTableDecoratorIfAny();

    // -- ASSOCIATED ACTION

    ActionInteraction startAssociatedActionInteraction(final String actionId, final Where where);

    // -- ROW COUNT

    /**
     * Counts number of rows in {@link #getDataRowsFilteredAndSorted()}.
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

    Bindable<ColumnSort> getColumnSort();

    // -- SELECTION

    Bindable<Boolean> getSelectAllToggle();
    void doProgrammaticToggle(Runnable runnable);
    Set<Integer> getSelectedRowIndexes();
    Observable<Can<DataRow>> getDataRowsSelected();
    void selectRangeOfRowsByIndex(IntStream range, boolean select);

    // -- EXPORTING

    DataTable export();

    // -- SERIALIZATION

    DataTableMemento createMemento();

    // -- FILTER SUPPORT

    Bindable<String> getSearchArgument();
    boolean isSearchSupported();
    /**
     * @apiNote never called when not {@link #isSearchSupported()}
     */
    String getSearchPromptPlaceholderText();

}
