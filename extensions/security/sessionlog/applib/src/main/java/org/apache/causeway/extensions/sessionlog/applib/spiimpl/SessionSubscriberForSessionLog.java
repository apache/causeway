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

package org.apache.causeway.extensions.sessionlog.applib.spiimpl;

import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.session.SessionSubscriber;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.extensions.sessionlog.applib.CausewayModuleExtSessionLogApplib;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Implementation of the {@link SessionSubscriber} SPI, which persists a log entry to the database (the
 * {@link SessionLogEntry} entity) each time a user either logs on or logs out, or if their session expires.
 *
 * @since 2.0 {@index}
 */
@Service
@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Named(SessionSubscriberForSessionLog.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.LATE)
@Qualifier("sessionlog")
//@Log4j2
public class SessionSubscriberForSessionLog implements SessionSubscriber {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtSessionLogApplib.NAMESPACE + ".SessionLoggingServiceDefault";

    final SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;
    final TransactionService transactionService;
    final InteractionService interactionService;
    final ClockService clockService;

    @Override
    public void log(final Type type, final String username, final Date date, final CausedBy causedBy, final UUID sessionGuid, final String httpSessionId) {
        interactionService.runAnonymous(() -> {
            transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
                if (type == Type.LOGIN) {
                    sessionLogEntryRepository.create(username, sessionGuid, httpSessionId, causedBy, Timestamp.from(date.toInstant()));
                } else {

                    val sessionLogEntryIfAny = sessionLogEntryRepository.findBySessionGuid(sessionGuid);
                    sessionLogEntryIfAny
                            .ifPresent(entry -> {
                                        entry.setLogoutTimestamp(Timestamp.from(date.toInstant()));
                                        entry.setCausedBy(causedBy);
                                        transactionService.flushTransaction();
                                    }
                            );
                }
            })
            .ifFailureFail(); // throw if rolled back
        });
    }
}
