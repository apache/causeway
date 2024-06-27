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
package org.apache.causeway.applib.services.publishing.spi;

import java.sql.Timestamp;
import java.util.Comparator;
import java.util.UUID;

import org.apache.causeway.applib.services.bookmark.Bookmark;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Immutable data record for {@link EntityPropertyChangeSubscriber}s.
 *
 * @since 2.0 {@index}
 */

@EqualsAndHashCode(of = {"interactionId", "sequence", "targetStr", "propertyId"})
public final class EntityPropertyChange implements Comparable<EntityPropertyChange> {

    @Getter private final UUID interactionId;
    @Getter private final int sequence;
    @Getter private final Bookmark target;
    @Getter private final String logicalMemberIdentifier;
    @Getter private final String propertyId;
    @Getter private final String preValue;
    @Getter private final String postValue;
    @Getter private final String username;
    @Getter private final Timestamp timestamp;

    @Getter(AccessLevel.PRIVATE) private final String targetStr;

    public static EntityPropertyChange of(UUID interactionId, int sequence, Bookmark target, String logicalMemberIdentifier, String propertyId, String preValue, String postValue, String username, Timestamp timestamp) {
        return new EntityPropertyChange(interactionId, sequence, target, logicalMemberIdentifier, propertyId, preValue, postValue, username, timestamp);
    }

    private EntityPropertyChange(
            final UUID interactionId,
            final int sequence,
            final Bookmark target,
            final String logicalMemberIdentifier,
            final String propertyId,
            final String preValue,
            final String postValue,
            final String username,
            final Timestamp timestamp) {
        this.interactionId = interactionId;
        this.sequence = sequence;
        this.target = target;
        this.logicalMemberIdentifier = logicalMemberIdentifier;
        this.propertyId = propertyId;
        this.preValue = preValue;
        this.postValue = postValue;
        this.username = username;
        this.timestamp = timestamp;

        this.targetStr = target.toString();
    }

    @Override
    public String toString() {
        return String.format("%s,%d: %s by %s, %s: %s -> %s",
                getInteractionId(),
                getSequence(),
                getTargetStr(),
                getUsername(),
                getPropertyId(),
                getPreValue(),
                getPostValue());
    }

    @Override
    public int compareTo(EntityPropertyChange o) {
        return Comparator
                .comparing(EntityPropertyChange::getInteractionId)
                .thenComparing(EntityPropertyChange::getSequence)
                .thenComparing(EntityPropertyChange::getTargetStr)
                .thenComparing(EntityPropertyChange::getPropertyId)
                .compare(this, o);
    }
}
