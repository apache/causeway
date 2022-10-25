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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.publishing.spi.EntityPropertyChange;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Provides supporting functionality for querying {@link AuditTrailEntry audit trail entry} entities.
 *
 * @since 2.0 {@index}
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AuditTrailEntryRepository<E extends AuditTrailEntry> {


    private final Class<E> auditTrailEntryClass;

    @Inject RepositoryService repositoryService;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    public Class<E> getEntityClass() {
        return auditTrailEntryClass;
    }

    public AuditTrailEntry createFor(final EntityPropertyChange change) {
        E entry = factoryService.detachedEntity(auditTrailEntryClass);
        entry.init(change);
        return repositoryService.persistAndFlush(entry);
    }

    public Optional<E> findFirstByTarget(final Bookmark target) {
        return repositoryService.firstMatch(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_FIRST_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(2)
        );
    }

    public List<E> findRecentByTarget(final Bookmark target) {
        return repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(100)
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
                        .withLimit(30)
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


    public List<E> findMostRecent() {
        return findMostRecent(100);
    }

    public List<E> findMostRecent(final int limit) {
        return repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_MOST_RECENT).withLimit(limit));
    }



    /**
     * intended for testing only
     */
    public List<? extends AuditTrailEntry> findAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'findAll' in production systems");
        }
        return repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND)
        );
    }

    /**
     * intended for testing only
     */
    public void removeAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'removeAll' in production systems");
        }
        repositoryService.removeAll(auditTrailEntryClass);
    }


}
