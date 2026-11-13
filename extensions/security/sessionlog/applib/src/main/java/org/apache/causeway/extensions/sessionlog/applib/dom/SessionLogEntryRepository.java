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

import org.apache.causeway.applib.services.session.SessionSubscriber;

/**
 * Provides supporting functionality for querying {@link SessionLogEntry session log entry} entities.
 *
 * @since 2.0 {@index}
 */
public interface SessionLogEntryRepository {

    void logoutAllSessions(final Timestamp logoutTimestamp);

    SessionLogEntry create(
            final String username,
            final UUID sessionGuid,
            final String httpSessionId,
            final SessionSubscriber.CausedBy causedBy,
            final Timestamp timestamp);

    Optional<SessionLogEntry> findBySessionGuid(final UUID sessionGuid);

    Optional<SessionLogEntry> findByHttpSessionId(final String httpSessionId);

    List<SessionLogEntry> findByUsername(final String username);

    List<SessionLogEntry> findByUsernameAndFromAndTo(
            final String username,
            final LocalDate from,
            final LocalDate to);

    List<SessionLogEntry> findByFromAndTo(
            final LocalDate from,
            final LocalDate to);

    List<SessionLogEntry> findByUsernameAndStrictlyBefore(
            final String username,
            final Timestamp from);

    List<SessionLogEntry> findByUsernameAndStrictlyAfter(
            final String username,
            final Timestamp from);

    List<SessionLogEntry> findActiveSessions();

    List<SessionLogEntry> findRecentByUsername(final String username);

    /**
     * for testing purposes only
     */
    List<SessionLogEntry> findAll();

    /**
     * for testing purposes only
     */
    void removeAll();

}
