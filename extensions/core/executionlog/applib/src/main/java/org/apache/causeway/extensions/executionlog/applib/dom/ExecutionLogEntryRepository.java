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

import org.apache.causeway.applib.exceptions.RecoverableException;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactn.Execution;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

import lombok.Getter;
import lombok.val;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionLogEntry command} entities.
 *
 * @since 2.0 {@index}
 */
public abstract class ExecutionLogEntryRepository<E extends ExecutionLogEntry> {

    public static class NotFoundException extends RecoverableException {
        private static final long serialVersionUID = 1L;
        @Getter
        private final UUID interactionId;
        public NotFoundException(final UUID interactionId) {
            super("Execution log entry not found");
            this.interactionId = interactionId;
        }
    }

    private final Class<E> executionLogEntryClass;

    @Inject Provider<RepositoryService> repositoryServiceProvider;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    protected ExecutionLogEntryRepository(final Class<E> executionLogEntryClass) {
        this.executionLogEntryClass = executionLogEntryClass;
    }

    public Class<E> getEntityClass() {
        return executionLogEntryClass;
    }


    /**
     * for testing only.
     */
    protected ExecutionLogEntryRepository(final Class<E> executionLogEntryClass, final Provider<RepositoryService> repositoryServiceProvider, final FactoryService factoryService) {
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

    public List<E> findByInteractionId(final UUID interactionId) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId));
    }

    public Optional<E> findByInteractionIdAndSequence(final UUID interactionId, final int sequence) {
        return repositoryService().firstMatch(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE)
                        .withParameter("interactionId", interactionId)
                        .withParameter("sequence", sequence)
        );
    }

    public List<E> findByFromAndTo(
            final @Nullable LocalDate from,
            final @Nullable LocalDate to) {
        val fromTs = toTimestampStartOfDayWithOffset(from, 0);
        val toTs = toTimestampStartOfDayWithOffset(to, 1);

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
        return repositoryService().allMatches(query);
    }

    public List<E> findMostRecent() {
        return findMostRecent(100);
    }

    public List<E> findMostRecent(final int limit) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_MOST_RECENT).withLimit(limit));
    }

    public List<E> findByTarget(final Bookmark target) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET)
                        .withParameter("target", target));
    }

    public List<E> findByTargetAndTimestampAfter(final Bookmark target, final Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTargetAndTimestampBefore(final Bookmark target, final Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTargetAndTimestampBetween(final Bookmark target, final Timestamp timestampFrom, final Timestamp timestampTo) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("target", target)
                        .withParameter("timestampFrom", timestampFrom)
                        .withParameter("timestampTo", timestampTo)
        );
    }

    public List<E> findByTimestampAfter(final Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", timestamp)
        );
    }

    public List<E> findByTimestampBefore(final Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", timestamp)
        );
    }

    public List<E> findByTimestampBetween(final Timestamp timestampFrom, final Timestamp timestampTo) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", timestampFrom)
                        .withParameter("to", timestampTo)
        );
    }

    public List<E> findRecentByUsername(final String username) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
                        .withLimit(30)
        );
    }

    public List<E> findRecentByTarget(final Bookmark target) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_TARGET)
                        .withParameter("target", target)
                        .withLimit(30)
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
    public List<E> findAll() {
        if (causewaySystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot call 'findAll' in production systems");
        }
        return repositoryService().allInstances(executionLogEntryClass);
    }


    /**
     * intended for testing purposes only
     */
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
