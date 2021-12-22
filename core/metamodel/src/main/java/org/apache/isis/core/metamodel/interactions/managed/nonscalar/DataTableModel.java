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
package org.apache.isis.core.metamodel.interactions.managed.nonscalar;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.binding._BindableAbstract;
import org.apache.isis.commons.internal.binding._Bindables;
import org.apache.isis.commons.internal.binding._Observables;
import org.apache.isis.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.interactions.managed.ActionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.CollectionInteraction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction.MementoForArgs;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.core.metamodel.interactions.managed.MultiselectChoices;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.PackedManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class DataTableModel
implements MultiselectChoices {

    // -- FACTORIES

    public static DataTableModel forCollection(
            final ManagedCollection managedCollection) {
        return new DataTableModel(managedCollection, managedCollection.getWhere(), ()->
            managedCollection
            .streamElements()
            .collect(Can.toCan()));
    }

    public static DataTableModel forAction(
            final ManagedAction managedAction,
            final Can<ManagedObject> args,
            final ManagedObject actionResult) {

        if(actionResult==null) {
            new DataTableModel(managedAction, managedAction.getWhere(), Can::empty);
        }
        if(!(actionResult instanceof PackedManagedObject)) {
            throw _Exceptions.unexpectedCodeReach();
        }
        return new DataTableModel(managedAction, managedAction.getWhere(),
                ()->((PackedManagedObject)actionResult).unpack());
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

    private DataTableModel(
            // we need access to the owner in support of imperative title and referenced column detection
            final ManagedMember managedMember,
            final Where where,
            final Supplier<Can<ManagedObject>> elementSupplier) {

        this.managedMember = managedMember;
        this.where = where;

        //dataElements = _Observables.lazy(elementSupplier);
        dataElements = _Observables.lazy(()->elementSupplier.get().map(e->
            e.getMetaModelContext().getServiceInjector().injectServicesInto(e)));

        searchArgument = _Bindables.forValue(null);

        dataRowsFiltered = _Observables.lazy(()->
            dataElements.getValue().stream()
                //XXX future extension: filter by searchArgument
                .filter(this::ignoreHidden)
                .sorted(managedMember.getMetaModel().getElementComparator())
                .map(domainObject->new DataRow(this, domainObject))
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
            .streamPropertiesForColumnRendering(managedMember.getIdentifier(), managedMember.getOwner())
            .map(property->new DataColumn(this, property))
            .collect(Can.toCan()));

        //XXX future extension: the tile could dynamically reflect the number of elements selected
        //eg... 5 Orders selected
        title = _Observables.lazy(()->
            managedMember
            .getFriendlyName());
    }

    public int getElementCount() {
        return dataRowsFiltered.getValue().size();
    }

    public ObjectMember getMetaModel() {
        return managedMember.getMetaModel();
    }

    public ObjectSpecification getElementType() {
        return getMetaModel().getElementType();
    }

    public Optional<DataRow> lookupDataRow(final @NonNull UUID uuid) {
        //TODO can be safely cached
        return getDataRowsFiltered().getValue().stream()
                .filter(dr->dr.getUuid().equals(uuid))
                .findFirst();
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
                Where.ALL_TABLES);
    }

    // -- ASSOCIATED ACTION WITH MULTI SELECT

    @Override
    public Can<ManagedObject> getSelected() {
      return getDataRowsSelected()
                .getValue()
                .map(DataRow::getRowElement);
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

    // -- MEMENTO

    public Memento getMemento(final @Nullable ManagedAction.MementoForArgs argsMemento) {
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
    public static class Memento implements Serializable {
        private static final long serialVersionUID = 1L;

        static Memento create(
                final @Nullable DataTableModel table,
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

        public DataTableModel getDataTableModel(final ManagedObject owner) {

            val memberId = featureId.getMemberLogicalName();

            if(featureId.getType().isPropertyOrCollection()) {
                // bypass domain events
                val collInteraction = CollectionInteraction.start(owner, memberId, where);
                val managedColl = collInteraction.getManagedCollection().orElseThrow();
                // invocation bypassing domain events (pass-through)
                return new DataTableModel(managedColl, where, ()->
                    managedColl.streamElements(InteractionInitiatedBy.PASS_THROUGH).collect(Can.toCan()));
            }
            val actionInteraction = ActionInteraction.start(owner, memberId, where);
            val managedAction = actionInteraction.getManagedActionElseFail();
            val args = argsMemento.getArgumentList(managedAction.getMetaModel());
            // invocation bypassing domain events (pass-through)
            val actionResult = managedAction.invoke(args, InteractionInitiatedBy.PASS_THROUGH).left().orElseThrow();
            return forAction(managedAction, args, actionResult);
        }
    }

}
