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
package org.apache.isis.sessionlog.applib;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.sessionlog.applib.dom.SessionLogEntryRepository;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class SessionLogIntegTestAbstract extends IsisIntegrationTestAbstract {


    @Value
    @RequiredArgsConstructor
    static class Session {
        private static AtomicInteger counter = new AtomicInteger();
        @Getter final String username;
        final Instant instant;
        public Date getDate() { return Date.from(instant); }
        UUID sessionGuid = UUID.randomUUID();
        final String httpSessionId = "http-" + counter.incrementAndGet();
    }
    @BeforeEach
    void setUp() {
    }

    @Test
    void login_and_logout() {

        List<? extends SessionLogEntry> sessions;
        SessionLogEntry session;
        Optional<SessionLogEntry> sessionIfAny;

        // given
        sessions = sessionLogEntryRepository.findActiveSessions();
        Assertions.assertThat(sessions).isEmpty();

        // when
        Session session1 = new Session("fred",  Instant.now().minus(Duration.ofDays(2)));

        sessionLogService.log(SessionLogService.Type.LOGIN, session1.username, session1.getDate(), SessionLogService.CausedBy.USER, session1.sessionGuid, session1.httpSessionId);

        // then
        sessions = sessionLogEntryRepository.findActiveSessions();
        Assertions.assertThat(sessions).hasSize(1);
        session = sessions.get(0);
        Assertions.assertThat(session.getSessionGuid()).isEqualTo(session1.sessionGuid);

        // when
        Session session2 = new Session("mary", Instant.now().minus(Duration.ofDays(1)));

        sessionLogService.log(SessionLogService.Type.LOGIN, session2.username, session2.getDate(), SessionLogService.CausedBy.USER, session2.sessionGuid, session2.httpSessionId);

        // then
        sessions = sessionLogEntryRepository.findActiveSessions();
        Assertions.assertThat(sessions).hasSize(2);

        // then
        sessions = sessionLogEntryRepository.findByUsername(session1.username);
        Assertions.assertThat(sessions).hasSize(1);
        Assertions.assertThat(sessions.get(0)).extracting(SessionLogEntry::getUsername).isEqualTo(session1.username);

        sessionIfAny = sessionLogEntryRepository.findBySessionGuid(session2.sessionGuid);
        Assertions.assertThat(sessionIfAny).isPresent();
        Assertions.assertThat(sessionIfAny).get().extracting(SessionLogEntry::getUsername).isEqualTo(session2.username);

        sessionIfAny = sessionLogEntryRepository.findBySessionGuidStr(session1.sessionGuid.toString());
        Assertions.assertThat(sessionIfAny).isPresent();
        Assertions.assertThat(sessionIfAny).get().extracting(SessionLogEntry::getUsername).isEqualTo(session1.username);

        sessions = sessionLogEntryRepository.findByUsernameAndStrictlyAfter(session1.username, Timestamp.from(session1.instant.plus(Duration.ofMillis(10))));
        Assertions.assertThat(sessions).isEmpty();

        sessions = sessionLogEntryRepository.findByUsernameAndStrictlyAfter(session1.username, Timestamp.from(session1.instant.minus(Duration.ofMillis(10))));
        Assertions.assertThat(sessions).hasSize(1);

        sessions = sessionLogEntryRepository.findByUsernameAndStrictlyBefore(session1.username, Timestamp.from(session1.instant.minus(Duration.ofMillis(10))));
        Assertions.assertThat(sessions).isEmpty();

        sessions = sessionLogEntryRepository.findByUsernameAndStrictlyBefore(session1.username, Timestamp.from(session1.instant.plus(Duration.ofMillis(10))));
        Assertions.assertThat(sessions).hasSize(1);

        sessions = sessionLogEntryRepository.findRecentByUsername(session1.username);
        Assertions.assertThat(sessions).hasSize(1);

        // when
        sessionLogService.log(SessionLogService.Type.LOGOUT, null, session1.getDate(), SessionLogService.CausedBy.USER, session1.sessionGuid, null);

        // then
        sessions = sessionLogEntryRepository.findActiveSessions();
        Assertions.assertThat(sessions).hasSize(1);
        Assertions.assertThat(sessions.get(0)).extracting(SessionLogEntry::getUsername).isEqualTo(session2.username);

        sessionLogService.log(SessionLogService.Type.LOGOUT, null, session2.getDate(), SessionLogService.CausedBy.USER, session2.sessionGuid, null);

        // then
        sessions = sessionLogEntryRepository.findActiveSessions();
        Assertions.assertThat(sessions).isEmpty();


    }

    @Inject @Qualifier("default") SessionLogService sessionLogService;
    @Inject SessionLogEntryRepository sessionLogEntryRepository;

}
