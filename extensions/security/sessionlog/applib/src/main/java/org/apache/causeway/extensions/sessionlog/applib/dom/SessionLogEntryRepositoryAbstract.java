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

package org.apache.causeway.extensions.sessionlog.applib.dom;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.inject.Inject;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.core.config.environment.CausewaySystemEnvironment;

import org.jspecify.annotations.NonNull;

/**
 * Provides supporting functionality for querying {@link SessionLogEntry session log entry} entities.
 *
 * @since 2.0 {@index}
 */
public abstract class SessionLogEntryRepositoryAbstract<E extends SessionLogEntry> implements SessionLogEntryRepository {

    @Inject RepositoryService repositoryService;
    @Inject TransactionService transactionService;
    @Inject FactoryService factoryService;
    @Inject CausewaySystemEnvironment causewaySystemEnvironment;

    private final Class<E> sessionLogEntryClass;

    protected SessionLogEntryRepositoryAbstract(@NonNull final Class<E> sessionLogEntryClass) {
        this.sessionLogEntryClass = sessionLogEntryClass;
    }

    @Override
    public void logoutAllSessions(final Timestamp logoutTimestamp) {
        var allSessions = repositoryService.allMatches(
                Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS));
        for (var activeEntry : allSessions) {
            activeEntry.setCausedBy(SessionSubscriber.CausedBy.RESTART);
            activeEntry.setLogoutTimestamp(logoutTimestamp);
        }
        transactionService.flushTransaction();
    }

    @Override
    public SessionLogEntry create(
            final String username,
            final UUID sessionGuid,
            final String httpSessionId,
            final SessionSubscriber.CausedBy causedBy,
            final Timestamp timestamp) {
        E entry = factoryService.detachedEntity(sessionLogEntryClass);
        entry.setUsername(username);
        entry.setSessionGuid(sessionGuid);
        entry.setHttpSessionId(httpSessionId);
        entry.setCausedBy(causedBy);
        entry.setLoginTimestamp(timestamp);
        return repositoryService.persistAndFlush(entry);
    }

    @Override
    public Optional<SessionLogEntry> findBySessionGuid(final UUID sessionGuid) {
        return _Casts.uncheckedCast(
                repositoryService.firstMatch(
                    Query.named(sessionLogEntryClass,  SessionLogEntry.Nq.FIND_BY_SESSION_GUID)
                         .withParameter("sessionGuid", sessionGuid))
        );
    }

    @Override
    public Optional<SessionLogEntry> findByHttpSessionId(final String httpSessionId) {
        return _Casts.uncheckedCast(
                repositoryService.firstMatch(
                    Query.named(sessionLogEntryClass,  SessionLogEntry.Nq.FIND_BY_HTTP_SESSION_ID)
                         .withParameter("httpSessionId", httpSessionId))
        );
    }

    @Override
    public List<SessionLogEntry> findByUsername(final String username) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME)
                         .withParameter("username", username))
        );
    }

    @Override
    public List<SessionLogEntry> findByUsernameAndFromAndTo(
            final String username,
            final LocalDate from,
            final LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BETWEEN)
                        .withParameter("username", username)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_AFTER)
                        .withParameter("username", username)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_BEFORE)
                        .withParameter("username", username)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME)
                        .withParameter("username", username);
            }
        }
        return _Casts.uncheckedCast(repositoryService.allMatches(query));
    }

    @Override
    public List<SessionLogEntry> findByFromAndTo(
            final LocalDate from,
            final LocalDate to) {
        var fromTs = toTimestampStartOfDayWithOffset(from, 0);
        var toTs = toTimestampStartOfDayWithOffset(to, 1);

        final Query<E> query;
        if(from != null) {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BETWEEN)
                        .withParameter("from", fromTs)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_AFTER)
                        .withParameter("from", fromTs);
            }
        } else {
            if(to != null) {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_TIMESTAMP_BEFORE)
                        .withParameter("to", toTs);
            } else {
                query = Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND);
            }
        }
        return _Casts.uncheckedCast(repositoryService.allMatches(query));
    }

    @Override
    public List<SessionLogEntry> findByUsernameAndStrictlyBefore(
            final String username,
            final Timestamp from) {

        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_BEFORE)
                        .withParameter("username", username)
                        .withParameter("from", from))
        );
    }

    @Override
    public List<SessionLogEntry> findByUsernameAndStrictlyAfter(
            final String username,
            final Timestamp from) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_BY_USERNAME_AND_TIMESTAMP_STRICTLY_AFTER)
                        .withParameter("username", username)
                        .withParameter("from", from))
        );
    }

    @Override
    public List<SessionLogEntry> findActiveSessions() {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_ACTIVE_SESSIONS))
        );
    }

    @Override
    public List<SessionLogEntry> findRecentByUsername(final String username) {
        return _Casts.uncheckedCast(
                repositoryService.allMatches(
                    Query.named(sessionLogEntryClass, SessionLogEntry.Nq.FIND_RECENT_BY_USERNAME)
                            .withParameter("username", username)
                            .withLimit(10))
        );

    }

    private static Timestamp toTimestampStartOfDayWithOffset(final LocalDate dt, final int daysOffset) {
        return dt != null
                ? Timestamp.valueOf(dt.atStartOfDay().plusDays(daysOffset))
                : null;
    }

    /**
     * for testing purposes only
     */
    @Override
    public List<SessionLogEntry> findAll() {
        if (causewaySystemEnvironment.deploymentType().isProduction()) {
            throw new IllegalStateException("Cannot removeAll in production systems");
        }
        return _Casts.uncheckedCast(repositoryService.allInstances(sessionLogEntryClass));
    }

    /**
     * for testing purposes only
     */
    @Override
    public void removeAll() {
        if (causewaySystemEnvironment.deploymentType().isProduction()) {
            throw new IllegalStateException("Cannot removeAll in production systems");
        }
        repositoryService.removeAll(sessionLogEntryClass);
    }

}
