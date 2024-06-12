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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.search.CollectionSearchService;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.binding._BindableAbstract;
import org.apache.causeway.commons.internal.binding._Bindables;
import org.apache.causeway.commons.internal.binding._Observables;
import org.apache.causeway.commons.internal.binding._Observables.LazyObservable;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.consent.InteractionResult;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.interactions.InteractionHead;
import org.apache.causeway.core.metamodel.interactions.InteractionUtils;
import org.apache.causeway.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.causeway.core.metamodel.interactions.VisibilityContext;
import org.apache.causeway.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.MultiselectChoices;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmSortUtils;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

public class DataTableInteractive
implements MultiselectChoices {

    // -- FACTORIES

    public static DataTableInteractive empty(final ManagedMember managedMember, final Where where) {
        return new DataTableInteractive(managedMember, where, Can.empty());
    }

    public static DataTableInteractive forCollection(
            final ManagedCollection managedCollection) {
        return new DataTableInteractive(managedCollection, managedCollection.getWhere(),
            managedCollection
            .streamElements()
            .collect(Can.toCan()));
    }

    public static DataTableInteractive forAction(
            final ManagedAction managedAction,
            final ManagedObject actionResult) {

        if(actionResult==null) {
            new DataTableInteractive(managedAction, managedAction.getWhere(), Can.empty());
        }
        if(!(actionResult instanceof PackedManagedObject)) {
            throw _Exceptions.unexpectedCodeReach();
        }

        val elements = ((PackedManagedObject)actionResult).unpack();
        elements.forEach(ManagedObject::getBookmark);

        return new DataTableInteractive(managedAction, managedAction.getWhere(), elements);
    }

    // -- CONSTRUCTION

    // as this is a layer of abstraction, don't expose via getter
    final @NonNull ManagedMember managedMember;
    final @NonNull Where where;

    @Getter private final @NonNull LazyObservable<Can<ManagedObject>> dataElements;
    @Getter private final @NonNull _BindableAbstract<String> searchArgument; // filter the data rows

    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsVisible;
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsFilteredAndSorted;
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsSelected;
    @Getter private final _BindableAbstract<Boolean> selectAllToggle;
    @Getter private final _BindableAbstract<ColumnSort> columnSort;

    @Getter private final @NonNull LazyObservable<Can<DataColumn>> dataColumns;
    @Getter private final @NonNull LazyObservable<String> title;

    private final @Nullable BiPredicate<Object, String> searchPredicate;
    @Getter private final String searchPromptPlaceholderText;

    /**
     * On data row selection changes (originating from UI),
     * the framework updates this {@link Bindable},
     * such that any listeners do get notified.
     */
    @Getter private final _BindableAbstract<Boolean> selectionChanges;

    private DataTableInteractive(
            // we need access to the owner in support of imperative title and referenced column detection
            final ManagedMember managedMember,
            final Where where,
            final Can<ManagedObject> elements) {

        val mmc = MetaModelContext.instanceElseFail();

        this.managedMember = managedMember;
        this.where = where;

        { // search stuff
            var collectionSearchServiceOpt = mmc.lookupService(CollectionSearchService.class);
            var elementType = managedMember.getElementType().getCorrespondingClass();
            this.searchPredicate = _Casts.uncheckedCast(
                collectionSearchServiceOpt
                    .flatMap(collectionSearchService->collectionSearchService.searchPredicate(elementType))
                    .orElse(null));
            this.searchPromptPlaceholderText = _Strings.nullToEmpty(
                collectionSearchServiceOpt
                    .map(collectionSearchService->collectionSearchService.searchPromptPlaceholderText(elementType))
                    .orElse(null));
        }

        dataElements = _Observables.lazy(()->elements.map(
            mmc::injectServicesInto));

        searchArgument = _Bindables.forValue("");
        columnSort = _Bindables.forValue(null);

        dataRowsVisible = _Observables.lazy(()->
            dataElements.getValue().stream()
                .filter(this::ignoreHidden)
                .map(domainObject->new DataRow(this, domainObject))
                .collect(Can.toCan()));

        dataRowsFilteredAndSorted = _Observables.lazy(()->
            dataRowsVisible.getValue().stream()
                .filter(adaptSearchPredicate())
                .sorted(sortingComparator()
                        .orElseGet(()->(a, b)->0)) // else don't sort (no-op comparator for streams)
                .collect(Can.toCan()));

        dataRowsSelected = _Observables.lazy(()->
            dataRowsVisible.getValue().stream()
                .filter(dataRow->dataRow.getSelectToggle().getValue().booleanValue())
                .collect(Can.toCan()));

        selectionChanges = _Bindables.forValue(Boolean.FALSE);
        selectAllToggle = _Bindables.forValue(Boolean.FALSE);
        selectAllToggle.addListener((e,o,isAllOn)->{
            //_Debug.onClearToggleAll(o, isAllOn, isClearToggleAllEvent.get());
            if(isClearToggleAllEvent.get()) {
                return;
            }
            dataRowsSelected.invalidate(); //TODO[CAUSEWAY-3772] could this be moved last in this lambda?
            try {
                isToggleAllEvent.set(true);
                dataRowsVisible.getValue().forEach(dataRow->dataRow.getSelectToggle().setValue(isAllOn));
            } finally {
                isToggleAllEvent.set(false);
            }
            selectionChanges.setValue(!selectionChanges.getValue()); // triggers selectionChange listeners
        });

        searchArgument.addListener((e,o,n)->{
            System.err.printf("search: %s->%s%n", o, n); //TODO[CAUSEWAY-3772] remove debug line
            dataRowsFilteredAndSorted.invalidate();
        });

        columnSort.addListener((e,o,n)->{
            dataRowsFilteredAndSorted.invalidate();
        });

        dataColumns = _Observables.lazy(()->
            managedMember.getElementType()
            .streamAssociationsForColumnRendering(managedMember.getIdentifier(), managedMember.getOwner())
            .map(assoc->new DataColumn(this, assoc))
            .collect(Can.toCan()));

        //XXX future extension: the title could dynamically reflect the number of elements selected
        //eg... 5 Orders selected
        title = _Observables.lazy(()->
            managedMember
            .getFriendlyName());
    }

    public boolean isSearchSupported() {
        return searchPredicate!=null;
    }

    public int getPageSize(final int pageSizeDefault) {
        return getMetaModel().getPageSize().orElse(pageSizeDefault);
    }

    public Optional<TableDecorator> getTableDecoratorIfAny() {
        return getMetaModel().getTableDecorator();
    }

    /**
     * Count all data rows (the user is allowed to see).
     */
    public int getVisibleElementCount() {
        return dataRowsVisible.getValue().size();
    }

    /**
     * Count filtered data rows.
     */
    public int getFilteredElementCount() {
        return dataRowsFilteredAndSorted.getValue().size();
    }

    public ObjectMember getMetaModel() {
        return managedMember.getMetaModel();
    }

    public ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    //TODO[CAUSEWAY-3772] use Bookmarks instead of UUID? But that will break lists; only works with sets so to speak
    private final Map<UUID, Optional<DataRow>> dataRowByUuidLookupCache = _Maps.newConcurrentHashMap();
    public Optional<DataRow> lookupDataRow(final @NonNull UUID uuid) {
        // lookup can be safely cached
        return dataRowByUuidLookupCache.computeIfAbsent(uuid, __->getDataRowsVisible().getValue().stream()
                .filter(dr->dr.getUuid().equals(uuid))
                .findFirst());
    }

    // -- SEARCH

    private Predicate<DataRow> adaptSearchPredicate() {
        System.err.printf("adaptSearchPredicate (execute search)%n"); //TODO[CAUSEWAY-3772] remove debug line
        return searchPredicate==null
                ? dataRow->true
                : dataRow->searchPredicate
                    .test(dataRow.getRowElement().getPojo(), searchArgument.getValue());
    }

    // -- SORTING

    /**
     * Sorting helper class, that has the column index to be sorted by and the sort direction.
     */
    @RequiredArgsConstructor
    @EqualsAndHashCode
    public static class ColumnSort implements Serializable {
        private static final long serialVersionUID = 1L;
        final int columnIndex;
        final MmSortUtils.SortDirection sortDirection;
        Optional<Comparator<ManagedObject>> asComparator(final Can<DataColumn> columns) {
            val columnToSort = columns.get(columnIndex).orElse(null);
            val sortProperty = columnToSort.getAssociationMetaModel().getSpecialization().leftIfAny();
            return Optional.ofNullable(sortProperty)
                    .map(prop->MmSortUtils.orderingBy(sortProperty, sortDirection));
        }
    }

    private Optional<Comparator<DataRow>> sortingComparator() {
        return Optional.ofNullable(columnSort.getValue())
                .flatMap(sort->sort.asComparator(dataColumns.getValue()))
                .or(()->managedMember.getMetaModel().getElementComparator())
                .map(elementComparator->(rowA, rowB)->elementComparator.compare(rowA.getRowElement(), rowB.getRowElement()));
    }

    // -- TOGGLE ALL

    private final AtomicBoolean isToggleAllEvent = new AtomicBoolean();
    private final AtomicBoolean isClearToggleAllEvent = new AtomicBoolean();
    public void clearToggleAll() {
        try {
            isClearToggleAllEvent.set(true);
            selectAllToggle.setValue(Boolean.FALSE);
        } finally {
            isClearToggleAllEvent.set(false);
        }
    }

    // -- DATA ROW TOGGLE

    void handleRowSelectToggle() {
        if(isToggleAllEvent.get()) {
            return;
        }
        getDataRowsSelected().invalidate();
        // in any case, if we have a toggle state change, clear the toggle all bindable
        clearToggleAll();
        notifySelectionChangeListeners();
    }

    private void notifySelectionChangeListeners() {
        // simply toggles the boolean value, to trigger any listeners
        selectionChanges.setValue(!selectionChanges.getValue());
    }

    // -- DATA ROW VISIBILITY

    private boolean ignoreHidden(final ManagedObject adapter) {
        final InteractionResult visibleResult =
                InteractionUtils.isVisibleResult(
                        adapter.getSpecification(),
                        createVisibleInteractionContext(adapter));
        return visibleResult.isNotVetoing();
    }

    private VisibilityContext createVisibleInteractionContext(final ManagedObject objectAdapter) {
        return new ObjectVisibilityContext(
                InteractionHead.regular(objectAdapter),
                objectAdapter.getSpecification().getFeatureIdentifier(),
                InteractionInitiatedBy.USER,
                Where.ALL_TABLES);
    }

    // -- ASSOCIATED ACTION WITH MULTI SELECT

    @Override
    public Can<ManagedObject> getSelected() {
        return getDataRowsSelected()
            .getValue()
            .map(DataRow::getRowElement);
    }

    public Set<Bookmark> getSelectedRowsAsBookmarks() {
        return getDataRowsSelected()
                .getValue()
                .stream()
                .map(row->row.getRowElement().getBookmarkElseFail())
                .collect(Collectors.toSet());
    }

    public ActionInteraction startAssociatedActionInteraction(final String actionId, final Where where) {
        val featureId = managedMember.getIdentifier();
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

    public DataTable export() {
        return new DataTable(
                getElementType(),
                getTitle().getValue(),
                getDataColumns().getValue()
                    .map(DataColumn::getAssociationMetaModel),
                getDataRowsFilteredAndSorted().getValue()
                    .stream()
                    .map(dr->dr.getRowElement())
                    .collect(Can.toCan()));
    }

    // used internally for serialization
    private DataTable exportAll() {
        return new DataTable(
                getElementType(),
                getTitle().getValue(),
                getDataColumns().getValue()
                    .map(DataColumn::getAssociationMetaModel),
                getDataElements().getValue());
    }

    // -- MEMENTO

    public Memento getMemento() {
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
    public static class Memento implements Serializable {
        private static final long serialVersionUID = 1L;

        static Memento create(
                final @NonNull DataTableInteractive tableInteractive) {

            return new Memento(
                    tableInteractive.managedMember.getIdentifier(),
                    tableInteractive.where,
                    tableInteractive.exportAll(),
                    tableInteractive.searchArgument.getValue(),
                    tableInteractive.getSelectedRowsAsBookmarks());
        }

        private final @NonNull Identifier featureId;
        private final @NonNull Where where;
        private final @NonNull DataTable dataTable;

        /**
         * Exposed as setter,
         * such that we don't need to recreate the entire memento, just because the searchArgument has changed.
         */
        @Setter private @Nullable String searchArgument;
        @Setter private @NonNull Set<Bookmark> selectedRowsAsBookmarks;

        public DataTableInteractive getDataTableModel(final ManagedObject owner) {

            if(owner.getPojo()==null) {
                // owner (if entity) might have been deleted
                throw _Exceptions.illegalArgument("cannot recreate from memento for deleted object");
            }

            val memberId = featureId.getMemberLogicalName();

            final ManagedMember managedMember = featureId.getType().isPropertyOrCollection()
                    ? CollectionInteraction.start(owner, memberId, where)
                        .getManagedCollection().orElseThrow()
                    : ActionInteraction.start(owner, memberId, where)
                        .getManagedActionElseFail();

            var dataTableInteractive = new DataTableInteractive(managedMember, where,
                    dataTable.streamDataElements().collect(Can.toCan()));

            dataTableInteractive.searchArgument.setValue(searchArgument);
            dataTableInteractive.dataRowsVisible.getValue().stream()
                .filter(dataRow->selectedRowsAsBookmarks.contains(dataRow.getRowElement().getBookmarkElseFail()))
                .forEach(dataRow->{
                    dataRow.getSelectToggle().setValue(true);
                });
            return dataTableInteractive;
        }

    }

}
