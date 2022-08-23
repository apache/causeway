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

package org.apache.isis.extensions.sessionlog.applib.spiimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.session.SessionSubscriber;
import org.apache.isis.extensions.sessionlog.applib.IsisModuleExtSessionLogApplib;
import org.apache.isis.extensions.sessionlog.applib.dom.SessionLogEntry;
import org.apache.isis.extensions.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of the Isis {@link SessionSubscriber} creates a log
 * entry to the database (the {@link SessionLogEntry} entity) each time a
 * user either logs on or logs out, or if their session expires.
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Named(SessionSubscriberForSessionLog.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("sessionlog")
//@Log4j2
public class SessionSubscriberForSessionLog implements SessionSubscriber {

    static final String LOGICAL_TYPE_NAME = IsisModuleExtSessionLogApplib.NAMESPACE + ".SessionLoggingServiceDefault";

    final SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;
    final ClockService clockService;

    @Override
    public void log(final Type type, final String username, final Date date, final CausedBy causedBy, final UUID sessionGuid, final String httpSessionId) {
        if (type == Type.LOGIN) {
            sessionLogEntryRepository.create(username, sessionGuid, httpSessionId, causedBy, Timestamp.from(date.toInstant()));
        } else {
            val sessionLogEntryIfAny = sessionLogEntryRepository.findBySessionGuid(sessionGuid);
            sessionLogEntryIfAny
                    .ifPresent(entry -> {
                        entry.setLogoutTimestamp(Timestamp.from(date.toInstant()));
                        entry.setCausedBy(causedBy);
                    }
            );
        }
    }

}
