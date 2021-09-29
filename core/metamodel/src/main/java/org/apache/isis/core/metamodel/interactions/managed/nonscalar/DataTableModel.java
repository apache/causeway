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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.binding._BindableAbstract;
import org.apache.isis.commons.internal.binding._Bindables;
import org.apache.isis.commons.internal.binding._Observables;
import org.apache.isis.commons.internal.binding._Observables.LazyObservable;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.consent.InteractionResult;
import org.apache.isis.core.metamodel.facets.all.named.MemberNamedFacet;
import org.apache.isis.core.metamodel.interactions.InteractionHead;
import org.apache.isis.core.metamodel.interactions.InteractionUtils;
import org.apache.isis.core.metamodel.interactions.ObjectVisibilityContext;
import org.apache.isis.core.metamodel.interactions.VisibilityContext;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

public class DataTableModel {

    // -- FACTORIES

    public static DataTableModel forCollection(
            final ManagedCollection managedCollection) {
        return new DataTableModel(managedCollection, ()->
            managedCollection
            .streamElements()
            .collect(Can.toCan()));
    }

    public static DataTableModel forActionResult(
            final ManagedAction managedAction,
            final ManagedObject actionResult) {

        val objectManager = managedAction.getMetaModel().getMetaModelContext().getObjectManager();

        return new DataTableModel(managedAction, ()->
            ManagedObjects.isNullOrUnspecifiedOrEmpty(actionResult)
                ? Can.empty()
                : _NullSafe.streamAutodetect(actionResult.getPojo())
                        .map(objectManager::adapt)
                        .collect(Can.toCan()));
    }

    // -- CONSTRUCTION

    // as this is a layer of abstraction, don't expose via getter
    final @NonNull ManagedMember managedMember;

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
            final Supplier<Can<ManagedObject>> elementSupplier) {

        this.managedMember = managedMember;

        dataElements = _Observables.forFactory(elementSupplier);

        searchArgument = _Bindables.forValue(null);

        dataRowsFiltered = _Observables.forFactory(()->
            dataElements.getValue().stream()
                //TODO filter by searchArgument
                .filter(this::ignoreHidden)
                .sorted(managedMember.getMetaModel().getElementComparator())
                //TODO apply projection conversion (if any)
                .map(domainObject->new DataRow(this, domainObject))
                .collect(Can.toCan()));

        dataRowsSelected = _Observables.forFactory(()->
            dataRowsFiltered.getValue().stream()
            .filter(dataRow->dataRow.getSelectToggle().getValue().booleanValue())
            .collect(Can.toCan()));

        selectAllToggle = _Bindables.forValue(Boolean.FALSE);
        selectAllToggle.addListener((e,o,isAllOn)->{
            if(isClearToggleAllEvent.get()) return;
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

        dataColumns = _Observables.forFactory(()->
            managedMember.getElementType()
            .streamPropertiesForColumnRendering(managedMember.getIdentifier(), Optional.of(managedMember.getOwner()))
            .map(property->new DataColumn(this, property))
            .collect(Can.toCan()));

        //TODO the tile could dynamically reflect the number of elements selected
        //eg... 5 Orders selected
        title = _Observables.forFactory(()->
            managedMember.getElementType()
            .lookupFacet(MemberNamedFacet.class)
            .map(MemberNamedFacet::getSpecialization)
            .map(specialization->specialization
                    .fold(namedFacet->namedFacet.translated(),
                            namedFacet->namedFacet.textElseNull(managedMember.getOwner())))
            .orElse(managedMember.getIdentifier().getMemberLogicalName()));
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

}
