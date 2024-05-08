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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * An implementation of a Quartz {@link Job} that queries for {@link CommandLogEntry}s that have been persisted by
 * the {@link org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService} but not yet started; and then
 * executes them.
 *
 * <p>
 *     Note that although this is a component, a new instance is created for each run.  It is for this reason that
 *     the control is managed through the injected {@link BackgroundCommandsJobControl}
 * </p>
 *
 * @see BackgroundCommandsJobControl
 *
 * @since 2.0 {@index}
 */
@Component
@DisallowConcurrentExecution
@PersistJobDataAfterExecution
@Log4j2
public class RunBackgroundCommandsJob implements Job {

    @Inject InteractionService interactionService;
    @Inject TransactionService transactionService;
    @Inject ClockService clockService;
    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject CommandExecutorService commandExecutorService;
    @Inject BackgroundCommandsJobControl backgroundCommandsJobControl;


    @Override
    public void execute(final JobExecutionContext quartzContext) {

        if (backgroundCommandsJobControl.isPaused()) {
            log.debug("currently paused");
            return;
        }

        UserMemento user = UserMemento.ofNameAndRoleNames("scheduler_user", "admin_role");
        InteractionContext interactionContext = InteractionContext.builder().user(user).build();

        // we obtain the list of Commands first; we use their CommandDto as it is serializable across transactions
        final Optional<List<CommandDto>> commandDtosIfAny = pendingCommandDtos(interactionContext);

        // for each command, we execute within its own transaction.  Failure of one should not impact the next.
        commandDtosIfAny.ifPresent(commandDtos -> {
            for (val commandDto : commandDtos) {
                executeWithinOwnTransaction(commandDto, interactionContext);
            }
        });
    }

    private Optional<List<CommandDto>> pendingCommandDtos(InteractionContext interactionContext) {
        final Optional<List<CommandDto>> commandDtosIfAny =
                interactionService.callAndCatch(interactionContext, () ->
                    transactionService.callTransactional(Propagation.REQUIRES_NEW, () ->
                        commandLogEntryRepository.findBackgroundAndNotYetStarted()
                                .stream()
                                .map(CommandLogEntry::getCommandDto)
                                .collect(Collectors.toList())
                        )
                        .ifFailureFail()
                        .valueAsNonNullElseFail()
                    )
                    .ifFailureFail()    // we give up if unable to find these
                    .getValue();
        return commandDtosIfAny;
    }

    private void executeWithinOwnTransaction(CommandDto commandDto, InteractionContext interactionContext) {
        interactionService.runAndCatch(interactionContext, () -> {
                    executeCommandWithinOwnTransactionElseFail(commandDto);
                })
                .ifFailure(throwable -> {
                    logAndCaptureFailure(throwable, commandDto, interactionContext);
                });
    }

    private void executeCommandWithinOwnTransactionElseFail(CommandDto commandDto) {
        transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
                // look up the CommandLogEntry again because we are within a new transaction.
                val commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

                // finally, we execute
                commandLogEntryIfAny.ifPresent(commandLogEntry ->
                {
                    commandExecutorService.executeCommand(
                            CommandExecutorService.InteractionContextPolicy.NO_SWITCH, commandDto);
                    commandLogEntry.setCompletedAt(clockService.getClock().nowAsJavaSqlTimestamp());
                });
            })
            .ifFailureFail();
    }

    private void logAndCaptureFailure(Throwable throwable, CommandDto commandDto, InteractionContext interactionContext) {
        log.error("Failed to execute command: " + CommandDtoUtils.dtoMapper().toString(commandDto), throwable);
        // update this command as having failed.
        interactionService.runAndCatch(interactionContext, () -> {
            transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
                // look up the CommandLogEntry again because we are within a new transaction.
                val commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

                // capture the error
                commandLogEntryIfAny.ifPresent(commandLogEntry ->
                {
                    commandLogEntry.setException(throwable);
                    commandLogEntry.setCompletedAt(clockService.getClock().nowAsJavaSqlTimestamp());
                });
            });
        });
    }

}
