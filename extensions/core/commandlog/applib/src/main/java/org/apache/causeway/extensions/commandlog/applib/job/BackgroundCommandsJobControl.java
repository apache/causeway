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
package org.apache.causeway.extensions.commandlog.applib.job;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import lombok.val;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Programmatic;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

/**
 * Is checked by {@link RunBackgroundCommandsJob} each time it is executed.
 *
 * <p>
 *     A typical way to leverage this service would be to implement 'pause' and 'resume' actions on an admin-only
 *     dashboard.
 * </p>
 *
 * @see RunBackgroundCommandsJob
 */
@Service
@Log4j2
public class BackgroundCommandsJobControl {

    public enum State {
        RUNNING,
        PAUSED
    }

    @Getter
    private State state = State.RUNNING;

    @Programmatic
    public void pause() {
        log.info("Paused");
        state = State.PAUSED;
    }
    @Programmatic
    public void resume() {
        log.info("Running");
        state = State.RUNNING;
    }

    @Programmatic
    public boolean isPaused() {
        return state == State.PAUSED;
    }
    @Programmatic
    public boolean isRunning() {
        return state == State.RUNNING;
    }

}
