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

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.transaction.changetracking.events.IsisTransactionPlaceholder;

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
                ? preAndPostValue.withPost(IsisTransactionPlaceholder.DELETED)
                : preAndPostValue.withPost(getPropertyValue());
    }

    // -- HELPER

    private Object getPropertyValue() {
        val referencedAdapter = property.get(entity, InteractionInitiatedBy.FRAMEWORK);
        return ManagedObjects.UnwrapUtil.single(referencedAdapter);
    }



}

