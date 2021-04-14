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
package org.apache.isis.extensions.commandreplay.secondary.jobcallables;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdoRepository;
import org.apache.isis.extensions.commandlog.impl.jdo.ReplayState;
import org.apache.isis.extensions.commandreplay.secondary.SecondaryStatus;
import org.apache.isis.extensions.commandreplay.secondary.StatusException;
import org.apache.isis.extensions.commandreplay.secondary.analysis.CommandReplayAnalysisService;
import org.apache.isis.extensions.commandreplay.secondary.fetch.CommandFetcher;
import org.apache.isis.extensions.commandreplay.secondary.spi.ReplayCommandExecutionController;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Encodes the algorithm for fetching commands from the primary, and
 * replaying on the secondary.
 *
 * <p>
 *     This class is instantiated each time the Quartz job
 *     (<code>{@link org.apache.isis.extensions.commandreplay.secondary.job.ReplicateAndReplayJob}</code>)
 *     files.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class ReplicateAndRunCommands implements Callable<SecondaryStatus> {

    @Inject CommandExecutorService commandExecutorService;
    @Inject TransactionService transactionService;
    @Inject CommandFetcher commandFetcher;
    @Inject CommandJdoRepository commandJdoRepository;
    @Inject CommandReplayAnalysisService analysisService;
    @Inject Optional<ReplayCommandExecutionController> controller;

    @Override
    public SecondaryStatus call() {
        try {
            doCall();
            return SecondaryStatus.OK;
        } catch (StatusException e) {
            return e.secondaryStatus;
        }
    }

    private void doCall() throws  StatusException  {

        if(!isRunning()) {
            log.debug("ReplicateAndRunCommands is paused");
            return;
        }

        List<CommandJdo> commandsToReplay;

        while(isRunning()) {

            // is there a pending command already?
            // (we fetch several at a time, so we may not have processed them all yet)
            commandsToReplay = commandJdoRepository.findNotYetReplayed();

            if(commandsToReplay.isEmpty()) {

                // look for previously replayed on secondary
                CommandJdo hwm = commandJdoRepository.findMostRecentReplayed().orElse(null);

                if (hwm != null) {
                    // give up if there was a failure; admin will need to fix issue and retry
                    if (hwm.getReplayState() != null &&
                            hwm.getReplayState().isFailed()) {
                        log.info("Command {} hit replay error", hwm.getInteractionId());
                        return;
                    }
                } else {
                    // after a DB restore from primary to secondary, there won't be
                    // any that have been replayed.  So instead we simply use
                    // latest completed (on primary) as the HWM.
                    hwm = commandJdoRepository.findMostRecentCompleted().orElse(null);
                }

                // fetch next command(s) from primary (if any)
                val commandDtos = commandFetcher.fetchCommand(hwm);
                commandsToReplay = commandDtos.stream()
                        .map(dto ->
                                transactionService.callWithinCurrentTransactionElseCreateNew(
                                    () -> commandJdoRepository.saveForReplay(dto))
                                .optionalElseFail()
                                .orElse(null)
                        ).collect(Collectors.toList());

                if(commandsToReplay.isEmpty()) {
                    return; // nothing more to do for now.
                }
            }

            replay(commandsToReplay);
        }
    }

    /**
     *
     * @param commandsToReplay
     * @apiNote could return, whether there was a command to process (and so continue)
     */
    private void replay(List<CommandJdo> commandsToReplay) {

        commandsToReplay.forEach(commandJdo -> {

            log.info("replaying {}", commandJdo.getInteractionId());

            //
            // run command
            //
            val replayState = executeCommandInTranAndAnalyse(commandJdo);
            if(replayState.isFailed()) {
                // will effectively block the running of any further commands
                // until the issue is fixed.
                return;
            }

            //
            // find child commands, and run them
            //
            val parent = commandJdo;
            val childCommands =
                    transactionService.callWithinCurrentTransactionElseCreateNew(
                            () -> commandJdoRepository.findByParent(parent))
                    .optionalElseFail()
                    .orElse(null);
            for (val childCommand : childCommands) {
                val childReplayState = executeCommandInTranAndAnalyse(childCommand);
                if(childReplayState.isFailed()) {
                    // give up
                    return;
                }
            }

        });


    }

    private ReplayState executeCommandInTranAndAnalyse(final CommandJdo commandJdo) {
        transactionService.runWithinCurrentTransactionElseCreateNew(
                () -> {
                    commandExecutorService.executeCommand(
                        CommandExecutorService.SudoPolicy.SWITCH, commandJdo.getCommandDto(), commandJdo.outcomeHandler());
                });

        transactionService.runWithinCurrentTransactionElseCreateNew(() -> {
            analysisService.analyse(commandJdo);
        });

        return commandJdo.getReplayState();

    }

    private boolean isRunning() {
        return controller
                .map( control -> transactionService
                        .callWithinCurrentTransactionElseCreateNew(control::getState)
                        .optionalElseFail()
                        .orElse(null))
                .map(state -> state == ReplayCommandExecutionController.State.RUNNING)
            // if no controller implementation provided, then just continue
            .orElse(true);

    }

}
