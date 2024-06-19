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
import java.util.UUID;

import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction.MementoForArgs;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.MultiselectChoices;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmSortUtils;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.internal.DataTableInternal;
import org.apache.causeway.core.metamodel.tabular.optimistic.DataTableO;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public interface DataTableInteractive extends MultiselectChoices {

    public enum TableImplementation {
        DEFAULT,
        OPTIMISTIC;

        public boolean isOptimistic() { return this == OPTIMISTIC;}

        private final static String PROPERTY_NAME_MODEL_SELECT = "causeway.metamodel.dataTableModelSelect";
        private static boolean isOptimisticSelected() {
            return "OPTIMISTIC".equalsIgnoreCase(System.getenv(PROPERTY_NAME_MODEL_SELECT))
                    || "OPTIMISTIC".equalsIgnoreCase(System.getProperty(PROPERTY_NAME_MODEL_SELECT));
        }

        @Getter(lazy=true)
        private final static TableImplementation selected = isOptimisticSelected() ? OPTIMISTIC : DEFAULT;
    }

    // -- FACTORIES

    public static DataTableInteractive empty(final ManagedMember managedMember, final Where where) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            return DataTableO.empty(managedMember, where);
        case DEFAULT:
        default:
            return DataTableInternal.empty(managedMember, where);
        }
    }

    public static DataTableInteractive forCollection(final ManagedCollection managedCollection) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            return DataTableO.forCollection(managedCollection);
        case DEFAULT:
        default:
            return DataTableInternal.forCollection(managedCollection);
        }
    }

    public static DataTableInteractive forAction(
            final ManagedAction managedAction,
            final ManagedObject actionResult) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            throw _Exceptions.unexpectedCodeReach();
        case DEFAULT:
        default:
            return DataTableInternal.forAction(managedAction, actionResult);
        }
    }

    @Deprecated // args only required for TableImplementation.OPTIMISTIC
    public static DataTableInteractive forAction(
            final ManagedAction managedAction,
            final @NonNull Can<ManagedObject> args,
            final ManagedObject actionResult) {
        switch (TableImplementation.getSelected()) {
        case OPTIMISTIC:
            return DataTableO.forAction(managedAction, args, actionResult);
        case DEFAULT:
        default:
            return DataTableInternal.forAction(managedAction, actionResult);
        }
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
            val columnToSort = columns.get(columnIndex).orElse(null);
            val sortProperty = columnToSort.getAssociationMetaModel().getSpecialization().leftIfAny();
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
    default ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

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

    @Deprecated // used by OPTIMISTIC data table
    default Optional<? extends DataRow> lookupDataRow(final @NonNull UUID uuid) {
        throw _Exceptions.unsupportedOperation();
    }

    // -- PAGING

    int getPageSize(int pageSizeDefault);

    // -- SORTING

    Bindable<ColumnSort> getColumnSort();

    // -- SELECTION

    Bindable<Boolean> getSelectAllToggle();
    void doProgrammaticToggle(Runnable runnable);
    Set<Integer> getSelectedRowIndexes();
    Observable<Can<DataRow>> getDataRowsSelected();

    // -- EXPORTING

    DataTable export();

    // -- SERIALIZATION

    DataTableMemento createMemento();

    @Deprecated // used by OPTIMISTIC data table
    default DataTableMemento createMemento(final MementoForArgs mementoForArgs) {
        throw _Exceptions.unsupportedOperation();
    }

    // -- FILTER SUPPORT

    Bindable<String> getSearchArgument();
    boolean isSearchSupported();
    /**
     * @apiNote never called when not {@link #isSearchSupported()}
     */
    String getSearchPromptPlaceholderText();

}
