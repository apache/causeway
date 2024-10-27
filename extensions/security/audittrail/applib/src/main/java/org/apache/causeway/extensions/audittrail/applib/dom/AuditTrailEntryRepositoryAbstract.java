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
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

/**
 * Provides supporting functionality for querying {@link AuditTrailEntry audit trail entry} entities.
 *
 * @since 2.0 {@index}
 */
public abstract class AuditTrailEntryRepositoryAbstract<E extends AuditTrailEntry>
        implements AuditTrailEntryRepository  {

    @Inject RepositoryService repositoryService;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    private final Class<E> auditTrailEntryClass;

    protected AuditTrailEntryRepositoryAbstract(final Class<E> auditTrailEntryClass) {
        this.auditTrailEntryClass = auditTrailEntryClass;
    }

    public Class<E> getEntityClass() {
        return auditTrailEntryClass;
    }

    @Override
    public AuditTrailEntry createFor(final EntityPropertyChange change) {
        E entry = factoryService.detachedEntity(auditTrailEntryClass);
        entry.init(change);
        return repositoryService.persistAndFlush(entry);
    }

    @Override
    public Can<AuditTrailEntry> createFor(Can<EntityPropertyChange> entityPropertyChanges) {
        return Can.ofCollection(repositoryService.execInBulk(() -> entityPropertyChanges.map(this::createFor).toList()));
    }

    public Optional<AuditTrailEntry> findFirstByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService.firstMatch(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_FIRST_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(2)
                )
        );
    }

    public List<AuditTrailEntry> findRecentByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(100)
                )
        );
    }

    public List<AuditTrailEntry> findRecentByTargetAndPropertyId(
            final Bookmark target,
            final String propertyId) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_RECENT_BY_TARGET_AND_PROPERTY_ID)
                        .withParameter("target", target)
                        .withParameter("propertyId", propertyId)
                        .withLimit(30)
                )
        );
    }

    @Override
    public List<AuditTrailEntry> findByInteractionId(final UUID interactionId) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId)
                )
        );
    }

    public List<AuditTrailEntry> findByTargetAndFromAndTo(
            final Bookmark target,
            final LocalDate from,
            final LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

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
        return _Casts.uncheckedCast(repositoryService.allMatches(query));
    }

    @Override
    public List<AuditTrailEntry> findByFromAndTo(
            final LocalDate from,
            final LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

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
        return _Casts.uncheckedCast(repositoryService.allMatches(query));
    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt!=null
                ? Timestamp.valueOf(dt.atStartOfDay().plusDays(daysOffset))
                :null;
    }

    @Override
    public List<AuditTrailEntry> findMostRecent() {
        return findMostRecent(100);
    }

    @Override
    public List<AuditTrailEntry> findMostRecent(final int limit) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_MOST_RECENT).withLimit(limit)
                )
        );
    }

    @Override
    public List<AuditTrailEntry> findByUsernameAndFromAndTo(
            final String username,
            final LocalDate from,
            final LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN)
                        .withParameter("username", username)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_AFTER)
                        .withParameter("username", username)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE)
                        .withParameter("username", username)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME)
                        .withParameter("username", username)
                ;
            }
        }
        return _Casts.uncheckedCast(repositoryService.allMatches(query));
    }

    public List<AuditTrailEntry> findByUsernameAndTargetAndFromAndTo(
            final String username,
            final Bookmark target,
            final LocalDate from,
            final LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("username", username)
                        .withParameter("target", target)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("username", username)
                        .withParameter("target", target)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("username", username)
                        .withParameter("target", target)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_BY_USERNAME_AND_TARGET)
                        .withParameter("username", username)
                        .withParameter("target", target)
                ;
            }
        }
        return _Casts.uncheckedCast(repositoryService.allMatches(query));
    }

    @Override
    public List<AuditTrailEntry> findRecentByUsername(final String username) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
                        .withLimit(100)
                )
        );
    }

    /**
     * intended for testing only
     */
    @Override
    public List<AuditTrailEntry> findAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'findAll' in production systems");
        }
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(auditTrailEntryClass, AuditTrailEntry.Nq.FIND)
                )
        );
    }

    /**
     * intended for testing only
     */
    @Override
    public void removeAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'removeAll' in production systems");
        }
        repositoryService.removeAll(auditTrailEntryClass);
    }

}
