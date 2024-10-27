/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.audittrail.applib.dom;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.commons.collections.Can;

/**
 * Provides supporting functionality for querying {@link AuditTrailEntry audit trail entry} entities.
 *
 * @since 2.0 {@index}
 */
public interface AuditTrailEntryRepository {

    AuditTrailEntry createFor(final EntityPropertyChange change);

    default Can<AuditTrailEntry> createFor(final Can<EntityPropertyChange> entityPropertyChanges) {
        return Can.ofCollection(entityPropertyChanges.map(this::createFor).toList());
    }

    Optional<AuditTrailEntry> findFirstByTarget(final Bookmark target);

    List<AuditTrailEntry> findRecentByTarget(final Bookmark target);

    List<AuditTrailEntry> findRecentByTargetAndPropertyId(
            final Bookmark target,
            final String propertyId);

    List<AuditTrailEntry> findByInteractionId(final UUID interactionId);

    List<AuditTrailEntry> findByTargetAndFromAndTo(
            final Bookmark target,
            final LocalDate from,
            final LocalDate to);

    List<AuditTrailEntry> findByFromAndTo(
            final LocalDate from,
            final LocalDate to);

    List<AuditTrailEntry> findMostRecent();

    List<AuditTrailEntry> findMostRecent(final int limit);

    List<AuditTrailEntry> findByUsernameAndFromAndTo(
            final String username,
            final LocalDate from,
            final LocalDate to);

    List<AuditTrailEntry> findByUsernameAndTargetAndFromAndTo(
            final String username,
            final Bookmark target,
            final LocalDate from,
            final LocalDate to);

    List<AuditTrailEntry> findRecentByUsername(final String username);

    /**
     * intended for testing only
     */
    List<AuditTrailEntry> findAll();

    /**
     * intended for testing only
     */
    void removeAll();

}
