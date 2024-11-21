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

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.filter.CollectionFilterService;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.IndexedFunction;
import org.apache.causeway.commons.internal.binding._BindableAbstract;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Observables;
import org.apache.causeway.commons.internal.binding._Observables.LazyObservable;
import org.apache.causeway.commons.internal.collections._Streams;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.DataColumn;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.core.metamodel.tabular.DataTableMemento;
import org.apache.causeway.core.metamodel.tabular.internal._FilterUtils.FilterHandler;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

public class DataTableInternal
implements DataTableInteractive {

    // -- FACTORIES

    public static DataTableInternal empty(final ManagedMember managedMember, final Where where) {
        return new DataTableInternal(managedMember, where, Can.empty());
    }

    public static DataTableInternal forCollection(
            final ManagedCollection managedCollection) {
        return new DataTableInternal(managedCollection, managedCollection.getWhere(),
            managedCollection
            .streamElements()
            .collect(Can.toCan()));
    }

    public static DataTableInternal forAction(
            final ManagedAction managedAction,
            final ManagedObject actionResult) {

        if(actionResult==null) {
            new DataTableInternal(managedAction, managedAction.getWhere(), Can.empty());
        }
        if(!(actionResult instanceof PackedManagedObject)) {
            throw _Exceptions.unexpectedCodeReach();
        }

        var elements = ((PackedManagedObject)actionResult).unpack();
        elements.forEach(ManagedObject::getBookmark);

        return new DataTableInternal(managedAction, managedAction.getWhere(), elements);
    }

    // -- CONSTRUCTION

    // as this is a layer of abstraction, don't expose via getter
    final @NonNull ManagedMember managedMember;
    final @NonNull Where where;

    @Accessors(fluent=true)
    @Getter private final @NonNull LazyObservable<Can<ManagedObject>> dataElementsObservable;

    @Accessors(fluent=true)
    @Getter private final @NonNull _BindableAbstract<String> searchArgumentBindable; // filter the data rows

    @Accessors(fluent=true)
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsObservable;

    @Accessors(fluent=true)
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsFilteredAndSortedObservable;

    @Accessors(fluent=true)
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsSelectedObservable;

    @Accessors(fluent=true)
    @Getter private final _BindableAbstract<ColumnSort> columnSortBinable;

    @Accessors(fluent=true)
    @Getter private final @NonNull LazyObservable<Can<DataColumn>> dataColumnsObservable;

    @Accessors(fluent=true)
    @Getter private final @NonNull LazyObservable<String> titleObservable;

    private final Optional<FilterHandler> filterHandler;

    /**
     * On data row selection changes (originating from UI),
     * the framework updates this {@link Bindable},
     * such that any listeners do get notified.
     */
    private final _BindableAbstract<Boolean> selectionChanges;

    private DataTableInternal(
            // we need access to the owner in support of imperative title and referenced column detection
            final ManagedMember managedMember,
            final Where where,
            final Can<ManagedObject> elements) {

        var elementType = managedMember.getElementType();
        //var mmc = elementType.getMetaModelContext();

        this.managedMember = managedMember;
        this.where = where;
        this.filterHandler = _FilterUtils.createFilterHandler(elementType);

        this.searchArgumentBindable = _Bindables.forValue("");
        this.columnSortBinable = _Bindables.forValue(null);

        this.dataElementsObservable = _Observables.lazy(()->elements
                //.map(mmc::injectServicesInto) // I believe is redundant, has major performance impact
                //.filter(this::ignoreHidden) // I believe is redundant, has major performance impact
                );

        this.dataRowsObservable = _Observables.lazy(()->
            dataElementsObservable.getValue().stream()
                .map(IndexedFunction.zeroBased((rowIndex, element)->new DataRowInternal(rowIndex, this, element, tokens(element))))
                .collect(Can.toCan()));

        this.dataRowsFilteredAndSortedObservable = _Observables.lazy(()->
            _Streams.sortConditionally(
                dataRowsObservable.getValue().stream().filter(adaptSearchPredicate()),
                    sortingComparator().orElse(null))
                .collect(Can.toCan()));

        this.dataRowsSelectedObservable = _Observables.lazy(()->
            dataRowsObservable.getValue().stream()
                .filter(dataRow->dataRow.selectToggleBindable().getValue().booleanValue())
                .collect(Can.toCan()));

        this.selectionChanges = _Bindables.forValue(Boolean.FALSE);

        this.searchArgumentBindable.addListener((e,o,n)->{
            dataRowsFilteredAndSortedObservable.invalidate();
        });

        this.columnSortBinable.addListener((e,o,n)->{
            dataRowsFilteredAndSortedObservable.invalidate();
        });

        this.dataColumnsObservable = _Observables.lazy(()->
            managedMember.getElementType()
            .streamAssociationsForColumnRendering(managedMember.getIdentifier(), managedMember.getOwner())
            .map(assoc->new DataColumnInternal(this, assoc))
            .collect(Can.toCan()));

        //XXX future extension: the title could dynamically reflect the number of elements selected
        //eg... 5 Orders selected
        this.titleObservable = _Observables.lazy(()->
            managedMember
            .getFriendlyName());
    }

    @Override
    public boolean isSearchSupported() {
        return filterHandler.isPresent();
    }

    @Override
    public int getPageSize(final int pageSizeDefault) {
        return getMetaModel().getPageSize().orElse(pageSizeDefault);
    }

    @Override
    public Optional<TableDecorator> getTableDecoratorIfAny() {
        return getMetaModel().getTableDecorator();
    }

    /**
     * Count all data rows (the user is allowed to see).
     */
    public int getVisibleElementCount() {
        return dataElementsObservable.getValue().size();
    }

    /**
     * Count filtered data rows.
     */
    @Override
    public int getFilteredElementCount() {
        return dataRowsFilteredAndSortedObservable.getValue().size();
    }

    @Override
    public ObjectMember getMetaModel() {
        return managedMember.getMetaModel();
    }

    public ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    @Override
    public Optional<DataRow> lookupDataRow(final int rowIndex) {
        return dataRowsObservable().getValue().get(rowIndex)
                .map(DataRow.class::cast);
    }

    // -- FILTER

    @Override
    public String getSearchPromptPlaceholderText() {
        return filterHandler.map(handler->handler.searchPromptPlaceholderText)
                .orElse("");
    }

    private Predicate<DataRow> adaptSearchPredicate() {
        return filterHandler.isEmpty()
                ? dataRow->true
                : dataRow->filterHandler.get().getDataRowFilter()
                    .test(dataRow, searchArgumentBindable.getValue());
    }

    @Nullable
    private CollectionFilterService.Tokens tokens(final ManagedObject element){
        return filterHandler.isEmpty()
                ? null
                : filterHandler.get().tokenizer.apply(element.getPojo());
    }

    // -- SORTING

    private Optional<Comparator<DataRow>> sortingComparator() {
        return Optional.ofNullable(columnSortBinable.getValue())
                .flatMap(sort->sort.asComparator(dataColumnsObservable.getValue()))
                .or(()->managedMember.getMetaModel().getElementComparator())
                .map(elementComparator->(rowA, rowB)->elementComparator.compare(rowA.rowElement(), rowB.rowElement()));
    }

    // -- TOGGLE ALL

    private final AtomicBoolean isProgrammaticToggle = new AtomicBoolean();

    @Override
    public void doProgrammaticToggle(final @NonNull Runnable runnable) {
        try {
            isProgrammaticToggle.set(true);
            runnable.run();
        } finally {
            isProgrammaticToggle.set(false);
            invalidateSelectionThenNotifyListeners();
        }
    }

    // -- DATA ROW TOGGLE

    void handleRowSelectToggle() {
        if(isProgrammaticToggle.get()) return;
        invalidateSelectionThenNotifyListeners();
    }

    private void invalidateSelectionThenNotifyListeners() {
        dataRowsSelectedObservable.invalidate();
        // simply toggles the boolean value, to trigger any listeners
        selectionChanges.setValue(!selectionChanges.getValue());
    }

    @Override
    public void selectAll(final boolean select) {
        doProgrammaticToggle(()->{
            dataRowsObservable.getValue()
                .forEach(dataRow->{
                    dataRow.selectToggleBindable().setValue(select);
                });
        });
    }

    @Override
    public void selectAllFiltered(final boolean select) {
        doProgrammaticToggle(()->{
            dataRowsFilteredAndSortedObservable.getValue()
                .forEach(dataRow->{
                    dataRow.selectToggleBindable().setValue(select);
                });
        });
    }

    @Override
    public void selectRangeOfRowsByIndex(final IntStream range, final boolean select) {
        doProgrammaticToggle(()->{
            dataRowsFilteredAndSortedObservable.getValue()
                .pickByIndex(range)
                .forEach(dataRow->{
                    dataRow.selectToggleBindable().setValue(select);
                });
        });
    }

//    // -- DATA ROW VISIBILITY
//
//    private boolean ignoreHidden(final ManagedObject adapter) {
//        final InteractionResult visibleResult =
//                InteractionUtils.isVisibleResult(
//                        adapter.getSpecification(),
//                        createVisibleInteractionContext(adapter));
//        return visibleResult.isNotVetoing();
//    }
//
//    private VisibilityContext createVisibleInteractionContext(final ManagedObject objectAdapter) {
//        return ObjectVisibilityContext
//                .createForRegular(objectAdapter, InteractionInitiatedBy.USER, Where.ALL_TABLES);
//    }

    // -- ASSOCIATED ACTION WITH MULTI SELECT

    @Override
    public Can<ManagedObject> getSelected() {
        return dataRowsSelectedObservable.getValue()
            .map(DataRow::rowElement);
    }

    @Override
    public Set<Integer> getSelectedRowIndexes() {
        return dataRowsSelectedObservable.getValue()
            .stream()
            .map(DataRow::rowIndex)
            .collect(Collectors.toSet());
    }

    @Override
    public ActionInteraction startAssociatedActionInteraction(final String actionId, final Where where) {
        var featureId = managedMember.getIdentifier();
        if(!featureId.getType().isPropertyOrCollection()) {
            return ActionInteraction.empty(String.format("[no such collection %s; instead got %s;"
                    + "(while searching for an associated action %s)]",
                    featureId,
                    featureId.getType(),
                    actionId));
        }
        return ActionInteraction.startWithMultiselect(managedMember.getOwner(), actionId, where, this);
    }

    // -- EXPORT

    @Override
    public DataTable export() {
        return new DataTable(
                getElementType(),
                titleObservable().getValue(),
                dataColumnsObservable().getValue()
                    .map(DataColumn::associationMetaModel),
                dataRowsFilteredAndSortedObservable().getValue()
                    .stream()
                    .map(dr->dr.rowElement())
                    .collect(Can.toCan()));
    }

    // used internally for serialization
    private DataTable exportAll() {
        return new DataTable(
                getElementType(),
                titleObservable().getValue(),
                dataColumnsObservable().getValue()
                    .map(DataColumn::associationMetaModel),
                dataElementsObservable().getValue());
    }

    // -- MEMENTO

    @Override
    public Memento createMemento() {
        return Memento.create(this);
    }

    /**
     * Recreation from given 'bookmarkable' {@link ManagedObject} (owner),
     * without triggering domain events.
     * Either originates from a <i>Collection</i> or an <i>Action</i>'s
     * non-scalar result.
     * <p>
     * Responsibility for recreation of the owner is with the caller
     * to allow for simpler object graph reconstruction (shared owner).
     */
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Memento implements DataTableMemento {
        private static final long serialVersionUID = 1L;

        static Memento create(
                final @NonNull DataTableInternal tableInteractive) {

            return new Memento(
                    tableInteractive.managedMember.getIdentifier(),
                    tableInteractive.where,
                    tableInteractive.exportAll(),
                    tableInteractive.searchArgumentBindable.getValue(),
                    tableInteractive.getSelectedRowIndexes(),
                    tableInteractive.columnSortBinable().getValue());
        }

        private final @NonNull Identifier featureId;
        private final @NonNull Where where;
        private final @NonNull DataTable dataTable;

        private @Nullable String searchArgument;
        private @NonNull Set<Integer> selectedRowIndexes;
        private @Nullable DataTableInteractive.ColumnSort columnSort;

        @Override
        public DataTableInternal getDataTableModel(final ManagedObject owner) {

            if(owner.getPojo()==null) {
                // owner (if entity) might have been deleted
                throw _Exceptions.illegalArgument("cannot recreate from memento for deleted object");
            }

            var memberId = featureId.getMemberLogicalName();

            final ManagedMember managedMember = featureId.getType().isPropertyOrCollection()
                    ? CollectionInteraction.start(owner, memberId, where)
                        .getManagedCollection().orElseThrow()
                    : ActionInteraction.start(owner, memberId, where)
                        .getManagedActionElseFail();

            var dataTableInteractive = new DataTableInternal(managedMember, where,
                    dataTable.streamDataElements()
                    .peek(obj->{
                        if(obj.getSpecialization().isViewmodel()) {
                            // make sure any referenced entities are made live if currently hollow
                            ManagedObjects.refreshViewmodel(obj, /*bookmark supplier*/ null);
                        }
                    })
                    .collect(Can.toCan()));

            if(columnSort!=null)  {
                dataTableInteractive.columnSortBinable.setValue(columnSort);
            }
            dataTableInteractive.searchArgumentBindable.setValue(searchArgument);
            dataTableInteractive.doProgrammaticToggle(()->{
                dataTableInteractive.dataRowsObservable.getValue().stream()
                    .filter(dataRow->selectedRowIndexes.contains(dataRow.rowIndex()))
                    .forEach(dataRow->dataRow.selectToggleBindable().setValue(true));
            });
            return dataTableInteractive;
        }

        @Override
        public void setupBindings(final DataTableInteractive tableInteractive) {
            tableInteractive.searchArgumentBindable().addListener((e, o, searchArg)->{
                this.searchArgument = searchArg;
            });
            ((DataTableInternal)tableInteractive).selectionChanges.addListener((e, o, n)->{
                this.selectedRowIndexes = tableInteractive.getSelectedRowIndexes();
            });
        }

    }

}
