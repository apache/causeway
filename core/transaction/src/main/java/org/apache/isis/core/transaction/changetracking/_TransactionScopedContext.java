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
package org.apache.isis.core.transaction.changetracking;

import java.util.Map;
import java.util.Set;

import javax.inject.Provider;

import org.apache.isis.applib.annotation.EntityChangeKind;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.iactn.InteractionProvider;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
final class _TransactionScopedContext {

    private final Provider<InteractionProvider> interactionProviderProvider;

    /**
     * Contains initial change records having set the pre-values of every property of every object that was enlisted.
     */
    @Getter(AccessLevel.PACKAGE)
    @Accessors(fluent = true)
    private final Set<_PropertyChangeRecord> entityPropertyChangeRecords = _Sets.newLinkedHashSet();

    /**
     * Contains pre- and post- values of every property of every object that actually changed. A lazy snapshot,
     * triggered by internal call to {@link #snapshotPropertyChangeRecords()}.
     */
    private final _Lazy<Can<_PropertyChangeRecord>> entityPropertyChangeRecordsForPublishing
        = _Lazy.threadSafe(this::capturePostValuesAndDrain);

    @Getter(AccessLevel.PACKAGE)
    @Accessors(fluent = true)
    private final Map<Bookmark, EntityChangeKind> changeKindByEnlistedAdapter = _Maps.newLinkedHashMap();

    /**
     * For any enlisted Object Properties collects those, that are meant for publishing,
     * then clears enlisted objects.
     * @param interactionContextProvider
     * @param authenticationContextProvider
     */
    Can<_PropertyChangeRecord> capturePostValuesAndDrain() {

        _Xray.capturePostValuesAndDrain(
                entityPropertyChangeRecords,
                interactionProviderProvider);

        debug("BEFORE CLEAR");

        val records = entityPropertyChangeRecords.stream()
        .peek(managedProperty->managedProperty.updatePostValue()) // set post values, which have been left empty up to now
        .filter(managedProperty->managedProperty.getPreAndPostValue().shouldPublish())
        .collect(Can.toCan());

        debug("CLEAR");

        entityPropertyChangeRecords.clear();

        return records;

    }

    Can<_PropertyChangeRecord> snapshotPropertyChangeRecords() {
        // this code path has side-effects, it locks the result for this transaction,
        // such that cannot enlist on top of it
        return entityPropertyChangeRecordsForPublishing.get();
    }

    boolean isAlreadyPreparedForPublishing() {
        return entityPropertyChangeRecordsForPublishing.isMemoized();
    }

    // side-effect free, used by XRay
    long countPotentialPropertyChangeRecords() {
        return entityPropertyChangeRecords.stream().count();
    }

    int numberEntitiesDirtied() {
        return changeKindByEnlistedAdapter.size();
    }

    /**
     * @return <code>true</code> if successfully enlisted, <code>false</code> if was already enlisted
     */
    boolean enlistForChangeKindPublishing(
            final @NonNull ManagedObject entity,
            final @NonNull EntityChangeKind changeKind) {

        val bookmark = ManagedObjects.bookmarkElseFail(entity);

        val previousChangeKind = changeKindByEnlistedAdapter.get(bookmark);
        if(previousChangeKind == null) {
            changeKindByEnlistedAdapter.put(bookmark, changeKind);
            return true;
        }
        switch (previousChangeKind) {
        case CREATE:
            switch (changeKind) {
            case DELETE:
                changeKindByEnlistedAdapter.remove(bookmark);
            case CREATE:
            case UPDATE:
                return false;
            }
            break;
        case UPDATE:
            switch (changeKind) {
            case DELETE:
                changeKindByEnlistedAdapter.put(bookmark, changeKind);
                return true;
            case CREATE:
            case UPDATE:
                return false;
            }
            break;
        case DELETE:
            return false;
        }
        return previousChangeKind == null;
    }

    private void debug(String label) {
        _Probe.errOut("!!! %s %d",
                label,
                entityPropertyChangeRecords().size());
    }

}
