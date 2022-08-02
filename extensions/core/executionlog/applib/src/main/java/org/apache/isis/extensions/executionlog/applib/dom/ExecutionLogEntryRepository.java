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
package org.apache.isis.extensions.executionlog.applib.dom;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.isis.applib.exceptions.RecoverableException;
import org.apache.isis.applib.query.Query;
import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.iactn.Execution;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.core.config.environment.IsisSystemEnvironment;

import lombok.Getter;

/**
 * Provides supporting functionality for querying and persisting
 * {@link ExecutionLogEntry command} entities.
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
    @Inject IsisSystemEnvironment isisSystemEnvironment;

    protected ExecutionLogEntryRepository(Class<E> executionLogEntryClass) {
        this.executionLogEntryClass = executionLogEntryClass;
    }

    public Class<E> getEntityClass() {
        return executionLogEntryClass;
    }


    /**
     * for testing only.
     */
    protected ExecutionLogEntryRepository(Class<E> executionLogEntryClass, Provider<RepositoryService> repositoryServiceProvider, FactoryService factoryService) {
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

    public List<E> findByInteractionId(UUID interactionId) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID)
                        .withParameter("interactionId", interactionId));
    }

    public Optional<E> findByInteractionIdAndSequence(UUID interactionId, int sequence) {
        return repositoryService().firstMatch(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_INTERACTION_ID_AND_SEQUENCE)
                        .withParameter("interactionId", interactionId)
                        .withParameter("sequence", sequence)
        );
    }

    public List<E> findMostRecent() {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_MOST_RECENT));
    }

    public List<E> findMostRecent(final int limit) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_MOST_RECENT).withLimit(limit));
    }

    public List<E> findByTarget(Bookmark target) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET)
                        .withParameter("target", target));
    }

    public List<E> findByTargetAndTimestampAfter(Bookmark target, Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_AFTER)
                        .withParameter("target", target)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTargetAndTimestampBefore(Bookmark target, Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BEFORE)
                        .withParameter("target", target)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTargetAndTimestampBetween(Bookmark target, Timestamp timestampFrom, Timestamp timestampTo) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TARGET_AND_TIMESTAMP_BETWEEN)
                        .withParameter("target", target)
                        .withParameter("timestampFrom", timestampFrom)
                        .withParameter("timestampTo", timestampTo)
        );
    }

    public List<E> findByTimestampAfter(Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTimestampBefore(Timestamp timestamp) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("timestamp", timestamp)
        );
    }

    public List<E> findByTimestampBetween(Timestamp timestampFrom, Timestamp timestampTo) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("timestampFrom", timestampFrom)
                        .withParameter("timestampTo", timestampTo)
        );
    }

    public List<E> findRecentByUsername(String username) {
        return repositoryService().allMatches(
                Query.named(executionLogEntryClass,  ExecutionLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                        .withParameter("username", username)
                        .withLimit(30)
        );
    }

    public List<E> findRecentByTarget(Bookmark target) {
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
        return repositoryService().allInstances(executionLogEntryClass);
    }


    /**
     * intended for testing purposes only
     */
    public void removeAll() {
        if (isisSystemEnvironment.getDeploymentType().isProduction()) {
            throw new IllegalStateException("Cannot removeAll in production systems");
        }
        repositoryService().removeAll(executionLogEntryClass);
    }


}
