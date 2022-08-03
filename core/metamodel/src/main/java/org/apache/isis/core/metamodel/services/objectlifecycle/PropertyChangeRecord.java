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
package org.apache.isis.core.metamodel.services.objectlifecycle;

import java.sql.Timestamp;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

@EqualsAndHashCode(of = {"id"})
@ToString(of = {"id"})
public final class PropertyChangeRecord {

    @Getter
    private final PropertyChangeRecordId id;

    public ManagedObject getEntity() {return id.getEntity();}
    public OneToOneAssociation getProperty() {return id.getProperty();}
    public Bookmark getBookmark() {return id.getBookmark();}
    public String getPropertyId() {return id.getPropertyId();}

    @Getter private PreAndPostValue preAndPostValue;


    public static @NonNull PropertyChangeRecord of(
            final @NonNull PropertyChangeRecordId id) {
        return new PropertyChangeRecord(id, PreAndPostValue.pre(PropertyValuePlaceholder.NEW));
    }

    public static PropertyChangeRecord of(
            final @NonNull PropertyChangeRecordId id,
            final @NonNull PreAndPostValue preAndPostValue) {
        return new PropertyChangeRecord(id, preAndPostValue);
    }

    private PropertyChangeRecord(
            final @NonNull PropertyChangeRecordId id,
            final PreAndPostValue preAndPostValue) {

        this.id = id;
        this.preAndPostValue = preAndPostValue;
    }

    public String getLogicalMemberIdentifier() {
        val target = getBookmark();
        val propertyId = getPropertyId();
        return target.getLogicalTypeName() + "#" + propertyId;
    }

    public void updatePreValueAsNew() {
        preAndPostValue = PreAndPostValue.pre(PropertyValuePlaceholder.NEW);
    }

    public void updatePreValueWithCurrent() {
        preAndPostValue = PreAndPostValue.pre(getPropertyValue());
    }

    public void updatePostValueWithCurrent() {
        preAndPostValue = preAndPostValue.withPost(getPropertyValue());
    }

    public void updatePostValueAsDeleted() {
        preAndPostValue = preAndPostValue.withPost(PropertyValuePlaceholder.DELETED);
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
        return ManagedObjects.UnwrapUtil.single(referencedAdapter);
    }


}

