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

package org.apache.isis.audittrail.applib.dom;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.repository.RepositoryService;

import lombok.NonNull;
import lombok.val;

/**
 * Provides supporting functionality for querying {@link AuditTrailEntry audit trail entry} entities.
 */
public abstract class AuditTrailEntryRepository<E extends AuditTrailEntry> {

    @Inject RepositoryService repositoryService;
    @Inject FactoryService factoryService;

    private final Class<E> auditTrailEntryClass;

    protected AuditTrailEntryRepository(@NonNull Class<E> auditTrailEntryClass) {
        this.auditTrailEntryClass = auditTrailEntryClass;
    }


    public AuditTrailEntry create() {
        E entry = factoryService.detachedEntity(auditTrailEntryClass);
        // TODO: set the state
        return repositoryService.persistAndFlush(entry);
    }

    public Optional<E> findFirstByTarget(final Bookmark target) {
        return repositoryService.firstMatch(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_FIRST_BY_TARGET)
                        .withParameter("target", target)
        );
    }

    public List<E> findRecentByTarget(final Bookmark target) {
        return repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
        );
    }

    public List<E> findRecentByTargetAndPropertyId(
            final Bookmark target,
            final String propertyId) {
        final String targetStr = target.toString();
        return repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_RECENT_BY_TARGET_AND_PROPERTY_ID)
                        .withParameter("target", target)
                        .withParameter("propertyId", propertyId)
        );
    }

    public List<E> findByInteractionId(final UUID interactionId) {
        return repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId)
        );
    }

    public List<E> findByTargetAndFromAndTo(
            final Bookmark target,
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TARGET)
                        .withParameter("target", target)
                ;
            }
        }
        return repositoryService.allMatches(query);
    }

    public List<E> findByFromAndTo(
            final LocalDate from,
            final LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND);
            }
        }
        return repositoryService.allMatches(query);
    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt!=null
                ? Timestamp.valueOf(dt.atStartOfDay().plusDays(daysOffset))
                :null;
    }


}
