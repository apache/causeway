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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.metamodel.services.deadlock.DeadlockRecognizer;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.extensions.commandlog.applib.spi.RunBackgroundCommandsJobListener;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    final static int RETRY_COUNT = 3;
    final static long RETRY_INTERVAL_MILLIS = 1000;

    @Inject InteractionService interactionService;
    @Inject TransactionService transactionService;
    @Inject ClockService clockService;
    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject CommandExecutorService commandExecutorService;
    @Inject BackgroundCommandsJobControl backgroundCommandsJobControl;
    @Inject DeadlockRecognizer deadlockRecognizer;

    @Inject List<RunBackgroundCommandsJobListener> listeners;
    @Autowired private CausewayConfiguration causewayConfiguration;

    @Override
    public void execute(final JobExecutionContext quartzContext) {

        if (backgroundCommandsJobControl.isPaused()) {
            log.debug("currently paused");
            return;
        }

        var userMemento = UserMemento.ofNameAndRoleNames("scheduler_user", "admin_role");
        var interactionContext = InteractionContext.builder().user(userMemento).build();

        // we obtain the list of Commands first; we use their CommandDto as it is serializable across transactions
        final Optional<List<CommandDto>> commandDtosIfAny = pendingCommandDtos(interactionContext);

        // for each command, we execute within its own transaction.  Failure of one should not impact the next.
        commandDtosIfAny.ifPresent(commandDtos -> {
            List<CommandAndResult> commandResults = new ArrayList<>();
            for (CommandDto dto : commandDtos) {
                Try<?> attempt = executeCommandWithinTransaction(dto, interactionContext);
                if(attempt.isFailure()) {
                    var onFailurePolicy = causewayConfiguration.getExtensions().getCommandLog().getRunBackgroundCommands().getOnFailurePolicy();
                    if (onFailurePolicy == CausewayConfiguration.Extensions.CommandLog.RunBackgroundCommands.OnFailurePolicy.STOP_THE_LINE) {
                        break;
                    }
                }
                CommandAndResult apply = CommandAndResult.of(dto, attempt);
                commandResults.add(apply);
            }

            // an enhancement for the listener interface would be to say whether each interaction succeeded or not
            // whether his is relevant depends on the onFailurePolicy (if it's set to STOP_THE_LINE, then everything passed on will have succeeded)
            var interactionIds = commandResults.stream()
                    .filter(commandAndResult -> commandAndResult.getExecutionResult().isSuccess())  // only the successes
                    .map(CommandAndResult::getCommandDto)
                    .map(CommandDto::getInteractionId)
                    .collect(Collectors.toList());
            listeners.forEach(listener -> {
                invokeListenerCallbackWithinTransaction(listener, interactionIds, interactionContext);
            });
        });
    }

    @Getter
    @RequiredArgsConstructor(staticName = "of")
    static class CommandAndResult {
        private final CommandDto commandDto;
        private final Try<?> executionResult;
    }

    private Optional<List<CommandDto>> pendingCommandDtos(final InteractionContext interactionContext) {
        return interactionService.callAndCatch(interactionContext, () ->
            transactionService.callTransactional(Propagation.REQUIRES_NEW, () ->
                commandLogEntryRepository.findBackgroundAndNotYetStarted()
                        .stream()
                        .map(CommandLogEntry::getCommandDto)
                        .limit(causewayConfiguration.getExtensions().getCommandLog().getRunBackgroundCommands().getBatchSize())
                        .collect(Collectors.toList())
                )
                .ifFailureFail()
                .valueAsNonNullElseFail()
            )
            .ifFailureFail()    // we give up if unable to find these
            .getValue();
    }

    @Inject TransactionServiceSpring transactionServiceSpring;

    private Try<?> executeCommandWithinTransaction(
            final CommandDto commandDto,
            final InteractionContext interactionContext
    ) {
        int remainingAttempts = RETRY_COUNT;
        Try<?> result;
        while(true) {
            result = interactionService.call(interactionContext, () -> {

                // previously we were creating a new transaction here with REQUIRES_NEW, but this isn't necessary
                // (and massively complicates things) since each interaction will implictly creates its own transaction
                var commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));
                if(commandLogEntryIfAny.isEmpty()) {
                    return Try.empty();
                }

                var commandLogEntry = commandLogEntryIfAny.get();
                return commandExecutorService.executeCommand(
                            CommandExecutorService.InteractionContextPolicy.NO_SWITCH, commandDto)
                        .ifSuccess(
                            bookmarkToResultIfAny ->
                                commandLogEntry.setCompletedAt(clockService.getClock().nowAsJavaSqlTimestamp())
                        )
                        .mapFailure(throwable -> new ThrowableWithDetailsOfAttempt(throwable, commandLogEntry.getStartedAt()));
            });
            if(result.isSuccess()) {
                return result;
            }
            if (! isEncounteredDeadlock(result)) {
                break;
            }
            if (--remainingAttempts <= 0) {
                log.debug("Deadlock occurred too many times, giving up on command: " + CommandDtoUtils.dtoMapper().toString(commandDto));
                break;
            }
            log.debug("Deadlock occurred, retrying command: " + CommandDtoUtils.dtoMapper().toString(commandDto));
            sleep(RETRY_INTERVAL_MILLIS);
        }

        // a failure has occurred
        var onFailurePolicy = causewayConfiguration.getExtensions().getCommandLog().getRunBackgroundCommands().getOnFailurePolicy();
        switch (onFailurePolicy) {
            case CONTINUE_WITH_NEXT:
                // the result _will_ contain a failure
                result.ifFailure(throwable -> captureFailure(throwable, commandDto, interactionContext));
                break;
            case STOP_THE_LINE:
                break;
        }
        return result;
    }

    /**
     * Wrap the original throwable so that we can tunnel information from the original failed attempt, to make
     * it available for subsequent processing.
     */
    @Getter
    @RequiredArgsConstructor(staticName = "of")
    static class ThrowableWithDetailsOfAttempt extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final Throwable original;
        private final java.sql.Timestamp startedAt;
    }

    /**
     * Update this command as having failed.
     *
     * <p>
     * If this in itself fails, we will just ignore, which will be the same as if the
     * {@link CausewayConfiguration.Extensions.CommandLog.RunBackgroundCommands#getOnFailurePolicy() onFailurePolicy}
     * policy was set to
     * {@link org.apache.causeway.core.config.CausewayConfiguration.Extensions.CommandLog.RunBackgroundCommands.OnFailurePolicy#STOP_THE_LINE}.
     * </p>
     *
     * @param throwable
     * @param commandDto
     * @param interactionContext
     */
    private void captureFailure(final Throwable throwable, final CommandDto commandDto, final InteractionContext interactionContext) {
        log.error("Failed to execute command.  As per onFailurePolicy, updating CommandLogEntry with result then continuing; command: " + CommandDtoUtils.dtoMapper().toString(commandDto), throwable);

        interactionService.run(interactionContext, () -> {
            // look up the CommandLogEntry again because we are within a new transaction.
            var commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

            // capture the error
            commandLogEntryIfAny.ifPresent(
                commandLogEntry -> {
                    // use tunnelled info if available
                    if (throwable instanceof ThrowableWithDetailsOfAttempt) {
                        var throwableWithDetailsOfAttempt = (ThrowableWithDetailsOfAttempt) throwable;
                        commandLogEntry.setStartedAt(throwableWithDetailsOfAttempt.getStartedAt());
                        commandLogEntry.setException(throwableWithDetailsOfAttempt.getOriginal());
                    } else {
                        commandLogEntry.setException(throwable);
                    }
                    commandLogEntry.setCompletedAt(clockService.getClock().nowAsJavaSqlTimestamp());
                }
            );
        });
    }

    private void invokeListenerCallbackWithinTransaction(
            final RunBackgroundCommandsJobListener listener,
            final List<String> interactionIds,
            final InteractionContext interactionContext) {
        interactionService.runAndCatch(interactionContext, () -> {
            transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
                listener.executed(interactionIds);
            });
        })
        .ifFailureFail();
    }

    private boolean isEncounteredDeadlock(final Try<?> result) {
        if (!result.isFailure()) {
            return false;
        }
        return result.getFailure()
                .map(throwable -> deadlockRecognizer.isDeadlock(throwable))
                .orElse(false);
    }

    private static void sleep(final long retryIntervalMs) {
        try {
            Thread.sleep(retryIntervalMs);
        } catch (InterruptedException e) {
            // do nothing - continue
        }
    }
}
