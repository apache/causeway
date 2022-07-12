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
package org.apache.isis.extensions.commandlog.applib.integtest;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.eclipse.persistence.logging.SessionLogEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;

import org.apache.isis.applib.annotation.Value;
import org.apache.isis.applib.services.session.SessionLogService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.extensions.commandlog.applib.app.CommandLogMenu;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.isis.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public abstract class CommandLogIntegTestAbstract extends IsisIntegrationTestAbstract {

    @BeforeEach
    void setUp() {
    }

    @Test
    void invoke_mixin() {

        List<CommandLogEntry> notYetReplayed = commandLogEntryRepository.findNotYetReplayed();

        wrapperFactory.wrapMixin(CommandLogMenu.truncateLog.class, commandLogMenu).act();

        List<CommandLogEntry> notYetReplayedAfter = commandLogEntryRepository.findNotYetReplayed();

    }

    @Inject CommandLogMenu commandLogMenu;
    @Inject CommandLogEntryRepository<CommandLogEntry> commandLogEntryRepository;
    @Inject WrapperFactory wrapperFactory;

}
