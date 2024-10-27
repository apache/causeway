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
package org.apache.causeway.extensions.executionlog.applib.dom;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionLogEntry command} entities.
 *
 * @since 2.0 {@index}
 */
public abstract class ExecutionLogEntryRepositoryAbstract<E extends ExecutionLogEntry> implements ExecutionLogEntryRepository {

    private final Class<E> executionLogEntryClass;

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    protected ExecutionLogEntryRepositoryAbstract(final Class<E> executionLogEntryClass) {
        this.executionLogEntryClass = executionLogEntryClass;
    }

    public Class<E> getEntityClass() {
        return executionLogEntryClass;
    }

    /**
     * for testing only.
     */
    protected ExecutionLogEntryRepositoryAbstract(final Class<E> executionLogEntryClass, final Provider<RepositoryService> repositoryServiceProvider, final FactoryService factoryService) {
        this.executionLogEntryClass = executionLogEntryClass;
        this.repositoryServiceProvider = repositoryServiceProvider;
        this.factoryService = factoryService;
    }

    public E createEntryAndPersist(final Execution execution) {
        E e = factoryService.detachedEntity(executionLogEntryClass);
        e.init(execution);
        persist(e);
        return e;
    }

    @Override
    public List<ExecutionLogEntry> findByInteractionId(final UUID interactionId) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId))
        );
    }

    @Override
    public Optional<ExecutionLogEntry> findByInteractionIdAndSequence(final UUID interactionId, final int sequence) {
        return _Casts.uncheckedCast(
                repositoryService().firstMatch(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE)
                            .withParameter("interactionId", interactionId)
                            .withParameter("sequence", sequence)
                    )
        );
    }

    @Override
    public List<ExecutionLogEntry> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(executionLogEntryClass, ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(executionLogEntryClass, ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(executionLogEntryClass, ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(executionLogEntryClass, ExecutionLogEntry.Nq.FIND);
            }
        }
        return _Casts.uncheckedCast(repositoryService().allMatches(query));
    }

    @Override
    public List<ExecutionLogEntry> findMostRecent() {
        return findMostRecent(100);
    }

    @Override
    public List<ExecutionLogEntry> findMostRecent(final int limit) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_MOST_RECENT).withLimit(limit))
        );
    }

    public List<ExecutionLogEntry> findByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET)
                            .withParameter("target", target))
        );
    }

    public List<ExecutionLogEntry> findByTargetAndTimestampAfter(final Bookmark target, final Timestamp timestamp) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                            .withParameter("target", target)
                            .withParameter("timestamp", timestamp)
                    )
        );
    }

    public List<ExecutionLogEntry> findByTargetAndTimestampBefore(final Bookmark target, final Timestamp timestamp) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                            .withParameter("target", target)
                            .withParameter("timestamp", timestamp)
                   )
        );
    }

    public List<ExecutionLogEntry> findByTargetAndTimestampBetween(final Bookmark target, final Timestamp timestampFrom, final Timestamp timestampTo) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                            .withParameter("target", target)
                            .withParameter("timestampFrom", timestampFrom)
                            .withParameter("timestampTo", timestampTo)
                   )
        );
    }

    @Override
    public List<ExecutionLogEntry> findByTimestampAfter(final Timestamp timestamp) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                            .withParameter("from", timestamp)
               )
        );
    }

    @Override
    public List<ExecutionLogEntry> findByTimestampBefore(final Timestamp timestamp) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                            .withParameter("to", timestamp)
                )
        );
    }

    @Override
    public List<ExecutionLogEntry> findByTimestampBetween(final Timestamp timestampFrom, final Timestamp timestampTo) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                            .withParameter("from", timestampFrom)
                            .withParameter("to", timestampTo)
                )
        );
    }

    @Override
    public List<ExecutionLogEntry> findRecentByUsername(final String username) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                            .withParameter("username", username)
                            .withLimit(30)
                )
        );
    }

    public List<ExecutionLogEntry> findRecentByTarget(final Bookmark target) {
        return _Casts.uncheckedCast(
                repositoryService().allMatches(
                    Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_TARGET)
                            .withParameter("target", target)
                            .withLimit(30)
            )
        );
    }

    private void persist(final E commandLogEntry) {
        repositoryService().persist(commandLogEntry);
    }

    private RepositoryService repositoryService() {
        return repositoryServiceProvider.get();
    }

    /**
     * intended for testing purposes only
     */
    @Override
    public List<ExecutionLogEntry> findAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'findAll' in production systems");
        }
        return _Casts.uncheckedCast(repositoryService().allInstances(executionLogEntryClass));
    }

    /**
     * intended for testing purposes only
     */
    @Override
    public void removeAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'removeAll' in production systems");
        }
        repositoryService().removeAll(executionLogEntryClass);
    }

    private static Timestamp toTimestampStartOfDayWithOffset(
            final @Nullable LocalDate dt,
            final int daysOffset) {

        return dt!=null
                ? new java.sql.Timestamp(
                Instant.from(dt.atStartOfDay().plusDays(daysOffset).atZone(ZoneId.systemDefault()))
                        .toEpochMilli())
                : null;
    }

}
