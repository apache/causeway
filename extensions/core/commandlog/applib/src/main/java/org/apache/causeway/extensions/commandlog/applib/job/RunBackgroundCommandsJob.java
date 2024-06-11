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

import org.apache.causeway.commons.functional.ThrowingConsumer;
import org.apache.causeway.commons.functional.ThrowingFunction;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.core.interaction.session.CausewayInteraction;
import org.apache.causeway.core.runtimeservices.transaction.TransactionServiceSpring;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import org.apache.causeway.extensions.commandlog.applib.spi.RunBackgroundCommandsJobListener;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    final static int RETRY_COUNT = 3;
    final static long RETRY_INTERVAL_MILLIS = 1000;

    @Inject InteractionService interactionService;
    @Inject TransactionService transactionService;
    @Inject ClockService clockService;
    @Inject CommandLogEntryRepository commandLogEntryRepository;
    @Inject CommandExecutorService commandExecutorService;
    @Inject BackgroundCommandsJobControl backgroundCommandsJobControl;

    @Inject List<RunBackgroundCommandsJobListener> listeners;
    @Autowired private CausewayConfiguration causewayConfiguration;

    @Override
    public void execute(final JobExecutionContext quartzContext) {

        if (backgroundCommandsJobControl.isPaused()) {
            log.debug("currently paused");
            return;
        }

        val userMemento = UserMemento.ofNameAndRoleNames("scheduler_user", "admin_role");
        val interactionContext = InteractionContext.builder().user(userMemento).build();

        // we obtain the list of Commands first; we use their CommandDto as it is serializable across transactions
        final Optional<List<CommandDto>> commandDtosIfAny = pendingCommandDtos(interactionContext);

        // for each command, we execute within its own transaction.  Failure of one should not impact the next.
        commandDtosIfAny.ifPresent(commandDtos -> {
            List<CommandAndResult> commandResults = commandDtos
                    .stream()
                    .map(commandDto -> {
                        Try<Void> voidTry = executeCommandWithinTransaction(commandDto, interactionContext);
                        return CommandAndResult.of(commandDto, voidTry);
                    })
                    .collect(Collectors.toList());

            val interactionIds = commandResults.stream()
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
        private final Try<Void> executionResult;
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

    private Try<Void> executeCommandWithinTransaction(
            final CommandDto commandDto,
            final InteractionContext interactionContext
    ) {
        int remainingAttempts = RETRY_COUNT;
        Try<Void> result;
        while(true) {
            result = interactionService.call(interactionContext, () -> {
                // at this point there is already a transaction.
                // we suspend it while we attempt to execute the command (with retry) in a different transaction
                Try<Void> voidTry = executeCommandWithinOwnTransactionElseFail(commandDto);
                // as we executed the commands in their own transaction, if they failed then they will have rolled back
                // but they  won't have set the must_abort on the transaction created implicitly for _this_ initial
                // interaction; we therefore cascade the failure to initial implicit transaction.
                voidTry.ifFailure(throwable ->
                        interactionService.currentInteraction()
                            .filter(CausewayInteraction.class::isInstance)
                            .map(CausewayInteraction.class::cast)
                            .ifPresent(currentInteraction -> transactionServiceSpring.requestRollback(currentInteraction)));
                return voidTry;
            });
            if(result.isSuccess()) {
                return result;
            }
            if (!isEncounteredDeadlock(result)) {
                // some other sort of failure
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
        val onFailurePolicy = causewayConfiguration.getExtensions().getCommandLog().getRunBackgroundCommands().getOnFailurePolicy();
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
        private final Throwable original;
        private final java.sql.Timestamp startedAt;
    }

    private Try<Void> executeCommandWithinOwnTransactionElseFail(CommandDto commandDto) {
        Try<Void> commandTry = transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
            // look up the CommandLogEntry again because we are within a new transaction.
            val commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

            // finally, we execute
            commandLogEntryIfAny.ifPresent(
                commandLogEntry ->
                    commandExecutorService.executeCommand(
                        CommandExecutorService.InteractionContextPolicy.NO_SWITCH, commandDto)
                    .ifSuccess(
                        bookmarkToResultIfAny ->
                            commandLogEntry.setCompletedAt(clockService.getClock().nowAsJavaSqlTimestamp())
                    )
                    .mapFailure(throwable -> new ThrowableWithDetailsOfAttempt(throwable, commandLogEntry.getStartedAt()))
                    // this will result in the transcation being aborted
                    .ifFailureFail()
            );
        });

        // because we used REQUIRES_NEW, we _don't_ propagate any failures; they will already have resulted in the
        // transaction being completed. however, we do return the Try, so that our caller can find out what happened.

        // at this point the original transaction will have resumed
        return commandTry;
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
    private void captureFailure(Throwable throwable, CommandDto commandDto, InteractionContext interactionContext) {
        log.error("Failed to execute command.  As per onFailurePolicy, updating CommandLogEntry with result then continuing; command: " + CommandDtoUtils.dtoMapper().toString(commandDto), throwable);

        interactionService.run(interactionContext, () -> {
            transactionService.runTransactional(Propagation.REQUIRES_NEW, () -> {
                // look up the CommandLogEntry again because we are within a new transaction.
                val commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

                // capture the error
                commandLogEntryIfAny.ifPresent(
                    commandLogEntry -> {
                        // use tunnelled info if available
                        if (throwable instanceof ThrowableWithDetailsOfAttempt) {
                            val throwableWithDetailsOfAttempt = (ThrowableWithDetailsOfAttempt) throwable;
                            commandLogEntry.setStartedAt(throwableWithDetailsOfAttempt.getStartedAt());
                            commandLogEntry.setException(throwableWithDetailsOfAttempt.getOriginal());
                        } else {
                            commandLogEntry.setException(throwable);
                        }
                        commandLogEntry.setCompletedAt(clockService.getClock().nowAsJavaSqlTimestamp());
                    }
                );
            });
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

    private static boolean isEncounteredDeadlock(Try<?> result) {
        if (!result.isFailure()) {
            return false;
        }
        return result.getFailure()
                .map(throwable -> throwable instanceof DeadlockLoserDataAccessException)
                .orElse(false);
    }

    private static void sleep(long retryIntervalMs) {
        try {
            Thread.sleep(retryIntervalMs);
        } catch (InterruptedException e) {
            // do nothing - continue
        }
    }

}
