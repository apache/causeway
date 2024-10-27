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
package org.apache.causeway.extensions.commandlog.applib.fakescheduler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.causeway.commons.internal.concurrent._ConcurrentTask;
import org.apache.causeway.extensions.commandlog.applib.spi.RunBackgroundCommandsJobListener;

import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.concurrent._ConcurrentContext;
import org.apache.causeway.commons.internal.concurrent._ConcurrentTaskList;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.Builder;
import lombok.experimental.Accessors;

/**
 * Intended to support integration testing which uses the
 * {@link org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService} to create background
 * {@link CommandLogEntry command}s, that the integration test then needs to be executed.
 *
 * <p>
 *     In effect, emulates the work performed by
 *     {@link org.apache.causeway.extensions.commandlog.applib.job.RunBackgroundCommandsJob}.
 * </p>
 *
 * @see org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService
 * @since 2.0 {@index}
 */
@Service
public class FakeScheduler {

    public enum NoCommandsPolicy {
        /**
         * If no commands are found, simply return
         */
        RELAXED,
        /**
         * If no commands are found, then throw an exception.  The idea here is that this is a utility to support integration testing; in some tests we might want to fail if no background commands were found.
         */
        STRICT;
    }

    /** record candidate */
    @lombok.Value @Builder @Accessors(fluent=true)
    public static class CommandBulkExecutionResult {
        static CommandBulkExecutionResult happyCase() {
            return CommandBulkExecutionResult.builder()
                    .build();
        }
        private final @Nullable Throwable failure;
        private final boolean hasTimedOut;
        /** Number of commands still to be processed.
         * This will generally be 0 (in the happy case),
         * but could be non-zero if not enough time was provided to wait. */
        private final int remainingCommandsToProcessCount;
    }

    /**
     * @param waitForMillis how long to wait for the background commands to execute.
     *      The commands themselves run in a background thread to this.
     * @param noCommandsPolicy what to do if there are no commands found to be executed.
     * @return {@link CommandBulkExecutionResult} optionally containing information on what went wrong.
     * @throws InterruptedException
     */
    public CommandBulkExecutionResult runBackgroundCommands(
            final long waitForMillis,
            final NoCommandsPolicy noCommandsPolicy) throws InterruptedException {

        // we obtain the list of Commands first; we use their CommandDto as it is serializable across transactions
        List<CommandDto> commandDtos = pendingCommandDtos();

        if(commandDtos.isEmpty()) {
            switch (noCommandsPolicy) {
                case STRICT:
                    return CommandBulkExecutionResult.builder()
                            .failure(new IllegalStateException(
                                    "There are no background commands to be started"))
                            .build();
                case RELAXED:
                default:
                    return CommandBulkExecutionResult.happyCase();
            }
        }

        transactionService.flushTransaction();

        final _ConcurrentTaskList tasks = _ConcurrentTaskList.named("Execute Command DTOs");
        tasks.addRunnable("Bulk run all pending CommandDtos then call listeners", () ->{
            for (var commandDto : commandDtos) {
                executeCommandWithinTransaction(commandDto);
            }

            var interactionIds = commandDtos.stream().map(CommandDto::getInteractionId).collect(Collectors.toList());
            listeners.forEach(listener -> {
                invokeListenerCallbackWithinTransaction(listener, interactionIds);
            });

        });

        tasks.submit(_ConcurrentContext.singleThreaded());
        var hasTimedOut = !tasks.await(waitForMillis, TimeUnit.MILLISECONDS);

        return CommandBulkExecutionResult.builder()
                .hasTimedOut(hasTimedOut)

                .failure(tasks.getTasks().stream()
                        .map(_ConcurrentTask::getFailedWith)
                        .filter(_NullSafe::isPresent)
                        .findAny()
                        .orElse(null))
                .remainingCommandsToProcessCount(
                        commandLogEntryRepository.findBackgroundAndNotYetStarted()
                        .size())
                .build();
    }

    private List<CommandDto> pendingCommandDtos() {
        return commandLogEntryRepository.findBackgroundAndNotYetStarted()
                .stream()
                .map(CommandLogEntry::getCommandDto)
                .collect(Collectors.toList());
    }

    private void executeCommandWithinTransaction(final CommandDto commandDto) {
        interactionService.runAnonymous(() -> {
            transactionService.runTransactional(Propagation.REQUIRED, () -> {
                    // look up the CommandLogEntry again because we are within a new transaction.
                    var commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

                    commandLogEntryIfAny.ifPresent(commandLogEntry ->
                            commandExecutorService.executeCommand(
                                    CommandExecutorService.InteractionContextPolicy.NO_SWITCH, commandDto));
                })
                .ifFailureFail();
            }
        );
    }

    private void invokeListenerCallbackWithinTransaction(RunBackgroundCommandsJobListener listener, List<String> interactionIds) {
        interactionService.runAnonymous(() -> {
            transactionService.runTransactional(Propagation.REQUIRED, () -> {
                listener.executed(interactionIds);
            });
        });
    }

    @Inject List<RunBackgroundCommandsJobListener> listeners;
    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject CommandExecutorService commandExecutorService;
    @Inject TransactionService transactionService;
    @Inject InteractionService interactionService;

}
