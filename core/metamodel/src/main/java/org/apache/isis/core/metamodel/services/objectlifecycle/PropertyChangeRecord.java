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
import java.util.UUID;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.isis.applib.services.xactn.TransactionId;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.actions.action.invocation.IdentifierUtil;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.val;

@EqualsAndHashCode(of = {"bookmarkStr", "propertyId"})
@ToString(of = {"bookmarkStr", "propertyId"})
public final class PropertyChangeRecord {

    @Getter private final ManagedObject entity;
    @Getter private final ObjectAssociation property;
    @Getter private final Bookmark bookmark;
    @Getter private final String propertyId;
    @Getter private PreAndPostValue preAndPostValue;

    private final String bookmarkStr;

    public static PropertyChangeRecord of(
            final @NonNull ManagedObject entity,
            final @NonNull ObjectAssociation property) {
        return new PropertyChangeRecord(entity, property, null);
    }

    public static PropertyChangeRecord of(
            final @NonNull ManagedObject entity,
            final @NonNull ObjectAssociation property,
            final @NonNull PreAndPostValue preAndPostValue) {
        return new PropertyChangeRecord(entity, property, preAndPostValue);
    }

    private PropertyChangeRecord(
            final ManagedObject entity,
            final ObjectAssociation property,
            final PreAndPostValue preAndPostValue) {
        this.entity = entity;
        this.property = property;
        this.propertyId = property.getId();

        this.bookmark = ManagedObjects.bookmarkElseFail(entity);
        this.bookmarkStr = bookmark.toString();

        this.preAndPostValue = preAndPostValue;
    }

    public String getMemberId() {
        return property.getFeatureIdentifier().getFullIdentityString();
    }

    public void setPreValue(final Object pre) {
        preAndPostValue = PreAndPostValue.pre(pre);
    }

    public void updatePreValue() {
        setPreValue(getPropertyValue());
    }

    @Deprecated // unreliable logic, instead the caller should know if this originates from a delete event
    public void updatePostValue() {
        preAndPostValue = EntityUtil.isDetachedOrRemoved(entity) //TODO[ISIS-2573] when detached, logic is wrong
                ? preAndPostValue.withPost(PropertyValuePlaceholder.DELETED)
                : preAndPostValue.withPost(getPropertyValue());
    }

    // -- UTILITY

    public EntityPropertyChange toEntityPropertyChange(
            final Timestamp timestamp,
            final String user,
            final TransactionId txId) {

        val spec = getEntity().getSpecification();
        val property = this.getProperty();

        final Bookmark target = ManagedObjects.bookmarkElseFail(getEntity());
        final String propertyId = property.getId();
        final String memberId = property.getFeatureIdentifier().getFullIdentityString();
        final String preValueStr = getPreAndPostValue().getPreString();
        final String postValueStr = getPreAndPostValue().getPostString();
        final String targetClass = IdentifierUtil.targetClassNameFor(spec);

        final UUID transactionId = txId.getInteractionId();
        final int sequence = txId.getSequence();


        return EntityPropertyChange.of(
                transactionId, sequence, targetClass, target,
                memberId, propertyId, preValueStr, postValueStr, user, timestamp);
    }

    // -- HELPER

    private Object getPropertyValue() {
        val referencedAdapter = property.get(entity, InteractionInitiatedBy.FRAMEWORK);
        return ManagedObjects.UnwrapUtil.single(referencedAdapter);
    }


}

