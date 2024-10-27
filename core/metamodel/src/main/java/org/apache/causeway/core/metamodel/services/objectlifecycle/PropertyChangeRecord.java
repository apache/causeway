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
package org.apache.causeway.core.metamodel.services.objectlifecycle;

import java.sql.Timestamp;
import java.util.Comparator;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtils;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id"})
public final class PropertyChangeRecord implements Comparable<PropertyChangeRecord> {

    @Getter private final PropertyChangeRecordId id;
    @Getter private PreAndPostValue preAndPostValue;

    public ManagedObject getEntity() {return id.getEntity();}
    public OneToOneAssociation getProperty() {return id.getProperty();}
    public Bookmark getBookmark() {return id.getBookmark();}
    public String getPropertyId() {return id.getPropertyId();}

    public static PropertyChangeRecord ofNew(
            final @NonNull PropertyChangeRecordId pcrId) {
        return new PropertyChangeRecord(pcrId)
                        .withPreValueSetToNew();
    }

    public static PropertyChangeRecord ofCurrent(
            final @NonNull PropertyChangeRecordId pcrId,
            final DeadlockRecognizer deadlockRecognizer) {
        return new PropertyChangeRecord(pcrId)
                        .withPreValueSetToCurrentElseUnknown(deadlockRecognizer);
    }

    public static PropertyChangeRecord ofCurrent(
            final @NonNull PropertyChangeRecordId pcrId,
            final Object currentValue) {
        return new PropertyChangeRecord(pcrId)
                        .withPreValueSetTo(currentValue);
    }

    public static PropertyChangeRecord ofDeleting(
            final @NonNull PropertyChangeRecordId id,
            final DeadlockRecognizer deadlockRecognizer) {
        return new PropertyChangeRecord(id)
                .withPreValueSetToCurrentElseUnknown(deadlockRecognizer)
                .withPostValueSetToDeleted();
    }

    private PropertyChangeRecord(final @NonNull PropertyChangeRecordId id) {
        this.id = id;
    }

    public String getLogicalMemberIdentifier() {
        var target = getBookmark();
        var propertyId = getPropertyId();
        return target.getLogicalTypeName() + "#" + propertyId;
    }

    public PropertyChangeRecord withPreValueSetToCurrentElseUnknown(DeadlockRecognizer deadlockRecognizer) {
        try {
            return withPreValueSetToCurrent();
        } catch (Exception ex) {
            deadlockRecognizer.rethrowIfDeadlock(ex);
            return withPreValueSetToUnknown();
        }
    }

    private PropertyChangeRecord withPreValueSetToCurrent() {
        return withPreValueSetTo(getPropertyValue());
    }

    private PropertyChangeRecord withPreValueSetToUnknown() {
        return withPreValueSetTo(PropertyValuePlaceholder.UNKNOWN);
    }

    private PropertyChangeRecord withPreValueSetToNew() {
        return withPreValueSetTo(PropertyValuePlaceholder.NEW);
    }

    private PropertyChangeRecord withPreValueSetTo(Object preValue) {
        this.preAndPostValue = PreAndPostValue.pre(preValue);
        return this;
    }

    public PropertyChangeRecord withPostValueSetToCurrentElseUnknown(DeadlockRecognizer deadlockRecognizer) {
        try {
            return withPostValueSetToCurrent();
        } catch (Exception ex) {
            deadlockRecognizer.rethrowIfDeadlock(ex);
            return withPostValueSetToUnknown();
        }
    }

    public PropertyChangeRecord withPostValueSetToDeleted() {
        return withPostValueSetTo(PropertyValuePlaceholder.DELETED);
    }

    public PropertyChangeRecord withPostValueSetToCurrent() {
        return withPostValueSetTo(getPropertyValue());
    }

    public PropertyChangeRecord withPostValueSetToUnknown() {
        return withPostValueSetTo(PropertyValuePlaceholder.UNKNOWN);
    }

    private PropertyChangeRecord withPostValueSetTo(Object postValue) {
        this.preAndPostValue = preAndPostValue.withPost(postValue);
        return this;
    }

    // -- UTILITY

    public EntityPropertyChange toEntityPropertyChange(
            final Timestamp timestamp,
            final String username,
            final TransactionId txId) {

        var target = getBookmark();
        var propertyId = getPropertyId();
        var preValue = getPreAndPostValue().getPreString();
        var postValue = getPreAndPostValue().getPostString();
        var interactionId = txId.getInteractionId();
        var sequence = txId.getSequence();

        String logicalMemberId = getLogicalMemberIdentifier();
        return EntityPropertyChange.of(
                interactionId, sequence,
                target, logicalMemberId, propertyId,
                preValue, postValue,
                username, timestamp);
    }

    // -- HELPER

    private Object getPropertyValue() {
        var referencedAdapter = getProperty().get(getEntity(), InteractionInitiatedBy.PASS_THROUGH);
        return MmUnwrapUtils.single(referencedAdapter);
    }

    @Override
    public int compareTo(PropertyChangeRecord o) {
        return Comparator
                .comparing(PropertyChangeRecord::getId)
                .compare(this, o);
    }
}
