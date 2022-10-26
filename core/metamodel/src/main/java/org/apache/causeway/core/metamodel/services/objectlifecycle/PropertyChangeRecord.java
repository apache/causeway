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

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.xactn.TransactionId;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmUnwrapUtil;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id"})
public final class PropertyChangeRecord {

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
            final @NonNull PropertyChangeRecordId pcrId) {
        return new PropertyChangeRecord(pcrId)
                        .withPreValueSetToCurrent();
    }

    public static PropertyChangeRecord ofCurrent(
            final @NonNull PropertyChangeRecordId pcrId,
            final Object currentValue) {
        return new PropertyChangeRecord(pcrId)
                        .withPreValueSetTo(currentValue);
    }

    public static PropertyChangeRecord ofDeleting(
            final @NonNull PropertyChangeRecordId id) {
        return new PropertyChangeRecord(id)
                        .withPreValueSetToCurrent()
                        .withPostValueSetToDeleted();
    }

    private PropertyChangeRecord(final @NonNull PropertyChangeRecordId id) {
        this.id = id;
    }

    public String getLogicalMemberIdentifier() {
        val target = getBookmark();
        val propertyId = getPropertyId();
        return target.getLogicalTypeName() + "#" + propertyId;
    }

    public PropertyChangeRecord withPreValueSetToNew() {
        return withPreValueSetTo(PropertyValuePlaceholder.NEW);
    }

    public PropertyChangeRecord withPreValueSetToCurrent() {
        return withPreValueSetTo(getPropertyValue());
    }

    public PropertyChangeRecord withPostValueSetToCurrent() {
        return withPostValueSetTo(getPropertyValue());
    }

    public PropertyChangeRecord withPostValueSetToDeleted() {
        return withPostValueSetTo(PropertyValuePlaceholder.DELETED);
    }

    private PropertyChangeRecord withPreValueSetTo(Object preValue) {
        this.preAndPostValue = PreAndPostValue.pre(preValue);
        return this;
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

        val target = getBookmark();
        val propertyId = getPropertyId();
        val preValue = getPreAndPostValue().getPreString();
        val postValue = getPreAndPostValue().getPostString();
        val interactionId = txId.getInteractionId();
        val sequence = txId.getSequence();

        String logicalMemberId = getLogicalMemberIdentifier();
        return EntityPropertyChange.of(
                interactionId, sequence,
                target, logicalMemberId, propertyId,
                preValue, postValue,
                username, timestamp);
    }

    // -- HELPER

    private Object getPropertyValue() {
        val referencedAdapter = getProperty().get(getEntity(), InteractionInitiatedBy.FRAMEWORK);
        return MmUnwrapUtil.single(referencedAdapter);
    }


}

