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
package org.apache.causeway.core.metamodel.tabular.optimistic;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.Identifier;
import org.apache.causeway.applib.annotation.TableDecorator;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.binding.Bindable;
import org.apache.causeway.commons.binding.Observable;
import org.apache.causeway.commons.collections.Can;
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
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction.MementoForArgs;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.PackedManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.tabular.DataColumn;
import org.apache.causeway.core.metamodel.tabular.DataRow;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.core.metamodel.tabular.DataTableMemento;
import org.apache.causeway.core.metamodel.tabular.simple.DataTable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class DataTableO
implements DataTableInteractive {

    // -- FACTORIES

    public static DataTableO empty(final ManagedMember managedMember, final Where where) {
        return new DataTableO(managedMember, where, Can::empty);
    }

    public static DataTableO forCollection(
            final ManagedCollection managedCollection) {
        return new DataTableO(managedCollection, managedCollection.getWhere(), ()->
            managedCollection
            .streamElements()
            .collect(Can.toCan()));
    }

    public static DataTableO forAction(
            final ManagedAction managedAction,
            final Can<ManagedObject> args,
            final ManagedObject actionResult) {

        if(actionResult==null) {
            new DataTableO(managedAction, managedAction.getWhere(), Can::empty);
        }
        if(!(actionResult instanceof PackedManagedObject)) {
            throw _Exceptions.unexpectedCodeReach();
        }

        val elements = ((PackedManagedObject)actionResult).unpack();
        elements.forEach(ManagedObject::getBookmark);

        return new DataTableO(managedAction, managedAction.getWhere(),
                ()->elements);
    }

    // -- CONSTRUCTION

    // as this is a layer of abstraction, don't expose via getter
    final @NonNull ManagedMember managedMember;
    final @NonNull Where where;

    @Getter private final @NonNull LazyObservable<Can<ManagedObject>> dataElements;
    @Getter private final @NonNull _BindableAbstract<String> searchArgument; // filter the data rows
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsFiltered;
    @Getter private final @NonNull LazyObservable<Can<DataRow>> dataRowsSelected;
    @Getter private final _BindableAbstract<Boolean> selectAllToggle;

    @Getter private final @NonNull LazyObservable<Can<DataColumn>> dataColumns;
    @Getter private final @NonNull LazyObservable<String> title;

    private DataTableO(
            // we need access to the owner in support of imperative title and referenced column detection
            final ManagedMember managedMember,
            final Where where,
            final Supplier<Can<ManagedObject>> elementSupplier) {

        this.managedMember = managedMember;
        this.where = where;

        //dataElements = _Observables.lazy(elementSupplier);
        dataElements = _Observables.lazy(()->elementSupplier.get().map(
            MetaModelContext.instanceElseFail()::injectServicesInto));

        searchArgument = _Bindables.forValue(null);

        dataRowsFiltered = _Observables.lazy(()->
            dataElements.getValue().stream()
                //XXX future extension: filter by searchArgument
                .filter(this::ignoreHidden)
                .sorted(managedMember.getMetaModel().getElementComparator()
                        .orElseGet(()->(a, b)->0)) // else don't sort (no-op comparator for streams)
                .map(domainObject->new DataRowO(this, domainObject))
                .collect(Can.toCan()));

        dataRowsSelected = _Observables.lazy(()->
            dataRowsFiltered.getValue().stream()
            .filter(dataRow->dataRow.getSelectToggle().getValue().booleanValue())
            .collect(Can.toCan()));

        selectAllToggle = _Bindables.forValue(Boolean.FALSE);
        selectAllToggle.addListener((e,o,isAllOn)->{
            //_Debug.onClearToggleAll(o, isAllOn, isClearToggleAllEvent.get());
            if(isClearToggleAllEvent.get()) {
                return;
            }
            dataRowsSelected.invalidate();
            try {
                isToggleAllEvent.set(true);
                dataRowsFiltered.getValue().forEach(dataRow->dataRow.getSelectToggle().setValue(isAllOn));
            } finally {
                isToggleAllEvent.set(false);
            }
        });

        searchArgument.addListener((e,o,n)->{
            dataRowsFiltered.invalidate();
            dataRowsSelected.invalidate();
        });

        dataColumns = _Observables.lazy(()->
            managedMember.getElementType()
            .streamAssociationsForColumnRendering(managedMember.getIdentifier(), managedMember.getOwner())
            .map(assoc->new DataColumnO(this, assoc))
            .collect(Can.toCan()));

        //XXX future extension: the title could dynamically reflect the number of elements selected
        //eg... 5 Orders selected
        title = _Observables.lazy(()->
            managedMember
            .getFriendlyName());
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
     * Count filtered data rows.
     */
    public int getElementCount() {
        return dataRowsFiltered.getValue().size();
    }

    @Override
    public ObjectMember getMetaModel() {
        return managedMember.getMetaModel();
    }

    private final Map<UUID, Optional<DataRowO>> dataRowByUuidLookupCache = _Maps.newConcurrentHashMap();
    @Override
    public Optional<DataRowO> lookupDataRow(final @NonNull UUID uuid) {
        // lookup can be safely cached
        return dataRowByUuidLookupCache.computeIfAbsent(uuid, __->getDataRowsFiltered().getValue().stream()
                .map(DataRowO.class::cast)
                .filter(dr->dr.getUuid().equals(uuid))
                .findFirst());
    }

    // -- TOGGLE ALL

    final AtomicBoolean isToggleAllEvent = new AtomicBoolean();
    private final AtomicBoolean isClearToggleAllEvent = new AtomicBoolean();
    public void clearToggleAll() {
        try {
            isClearToggleAllEvent.set(true);
            selectAllToggle.setValue(Boolean.FALSE);
        } finally {
            isClearToggleAllEvent.set(false);
        }
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
                Where.ALL_TABLES,
                InteractionUtils.renderPolicy(objectAdapter));
    }

    // -- ASSOCIATED ACTION WITH MULTI SELECT

    @Override
    public Can<ManagedObject> getSelected() {
      return getDataRowsSelected()
                .getValue()
                .map(DataRow::getRowElement);
    }

    @Override
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

    @Override
    public DataTable export() {
        return new DataTable(getElementType(),
                getTitle().getValue(),
                getDataColumns().getValue()
                    .map(DataColumn::getAssociationMetaModel),
                getDataRowsFiltered().getValue()
                    .stream()
                    .map(dr->dr.getRowElement())
                    .collect(Can.toCan()));
    }

    // -- MEMENTO

    @Override
    public Memento createMemento(final @Nullable ManagedAction.MementoForArgs argsMemento) {
        return Memento.create(this, argsMemento);
    }

    /**
     * Recreation from given 'bookmarkable' {@link ManagedObject} (owner),
     * without triggering domain events.
     * Either originates from a <i>Collection</i> or an <i>Action</i>'s
     * non-scalar result.
     * <p>
     * In the <i>Action</i> case, requires the <i>Action</i>'s arguments
     * for reconstruction.
     * <p>
     * Responsibility for recreation of the owner is with the caller
     * to allow for simpler object graph reconstruction (shared owner).
     * <p>
     * However, we keep track of the argument list here.
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Memento implements DataTableMemento {
        private static final long serialVersionUID = 1L;

        static Memento create(
                final @Nullable DataTableO table,
                final @Nullable MementoForArgs argsMemento) {
            val managedMember = table.managedMember;

            return new Memento(
                    managedMember.getIdentifier(),
                    table.where,
                    argsMemento);
        }

        private final Identifier featureId;
        private final Where where;
        private final MementoForArgs argsMemento;

        @Override
        public DataTableO getDataTableModel(final ManagedObject owner) {

            if(owner.getPojo()==null) {
                // owner (if entity) might have been deleted
                throw _Exceptions.illegalArgument("cannot recreate from memento for deleted object");
            }

            val memberId = featureId.getMemberLogicalName();

            if(featureId.getType().isPropertyOrCollection()) {
                // bypass domain events
                val collInteraction = CollectionInteraction.start(owner, memberId, where);
                val managedColl = collInteraction.getManagedCollection().orElseThrow();
                // invocation bypassing domain events (pass-through)
                return new DataTableO(managedColl, where, ()->
                    managedColl.streamElements(InteractionInitiatedBy.PASS_THROUGH).collect(Can.toCan()));
            }
            val actionInteraction = ActionInteraction.start(owner, memberId, where);
            val managedAction = actionInteraction.getManagedActionElseFail();
            val args = argsMemento.getArgumentList(managedAction.getMetaModel());
            // invocation bypassing domain events (pass-through)
            val actionResult = managedAction.invoke(args, InteractionInitiatedBy.PASS_THROUGH)
                    .getSuccessElseFail();
            return forAction(managedAction, args, actionResult);
        }

        @Override
        public void setupBindings(final DataTableInteractive tableInteractive) {
            throw _Exceptions.unsupportedOperation();
        }
    }

    @Override
    public Observable<Can<DataRow>> getDataRowsFilteredAndSorted() {
        return getDataRowsFiltered();
    }

    @Override
    public int getFilteredElementCount() {
        return getElementCount();
    }

    @Override
    public Optional<DataRow> lookupDataRow(final int rowIndex) {
        throw _Exceptions.unsupportedOperation("this impl. does lookup by UUID");
    }

    @Override
    public Bindable<ColumnSort> getColumnSort() {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public void doProgrammaticToggle(final Runnable runnable) {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public Set<Integer> getSelectedRowIndexes() {
        throw _Exceptions.unsupportedOperation();
    }

    @Override
    public DataTableMemento createMemento() {
        throw _Exceptions.unsupportedOperation("this impl. takes an nullable arg");
    }

    @Override
    public boolean isSearchSupported() {
        return false;
    }

    @Override
    public String getSearchPromptPlaceholderText() {
        return "";
    }

}
