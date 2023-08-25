package org.apache.causeway.extensions.commandlog.applib.fakescheduler;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.command.CommandExecutorService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;
import org.apache.causeway.schema.cmd.v2.CommandDto;

import lombok.val;

/**
 * Intended to support integration testing which uses the
 * {@link org.apache.causeway.extensions.commandlog.applib.dom.BackgroundService} to create background
 * {@link CommandLogEntry command}s, that the integration test then needs to be executed.
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

    /**
     *
     * @param waitForMillis how long to wait for the background commands to execute.  The commands themselves run in a background thread to this.
     * @param noCommandsPolicy what to do if there are no commands found to be executed.
     * @return the number of commands still to be processed.  This will generally be 0, but could be non-zero if not enough time was provided to wait.
     * @throws InterruptedException
     */
    public int runBackgroundCommands(
            final Integer waitForMillis,
            final NoCommandsPolicy noCommandsPolicy) throws InterruptedException {

        // we obtain the list of Commands first; we use their CommandDto as it is serializable across transactions
        List<CommandDto> commandDtos = commandLogEntryRepository.findBackgroundAndNotYetStarted()
                .stream()
                .map(CommandLogEntry::getCommandDto)
                .collect(Collectors.toList());

        if(commandDtos.isEmpty()) {
            switch (noCommandsPolicy) {
                case STRICT:
                    throw new IllegalStateException("There are no background commands to be started");
                case RELAXED:
                default:
                    return 0;
            }
        }

        long startAt = nowInMillis();

        transactionService.flushTransaction();

        Thread thread = new Thread(() -> execute(commandDtos));
        thread.start();

        boolean hitTimeout = false;
        List<CommandLogEntry> commandsToProcess;
        while(!(commandsToProcess = commandLogEntryRepository.findBackgroundAndNotYetStarted()).isEmpty() && !hitTimeout) {
            Thread.sleep(100L);
            hitTimeout = nowInMillis() > startAt + waitForMillis;
        }

        return commandsToProcess.size();
    }

    private long nowInMillis() {
        return clockService.getClock().nowAsEpochMilli();
    }

    private void execute(List<CommandDto> commandDtos) {

        for (val commandDto : commandDtos) {
            interactionService.runAnonymous(() -> {
                transactionService.runTransactional(Propagation.REQUIRED, () -> {
                        // look up the CommandLogEntry again because we are within a new transaction.
                        val commandLogEntryIfAny = commandLogEntryRepository.findByInteractionId(UUID.fromString(commandDto.getInteractionId()));

                        commandLogEntryIfAny.ifPresent(commandLogEntry ->
                                commandExecutorService.executeCommand(
                                        CommandExecutorService.InteractionContextPolicy.NO_SWITCH, commandDto));
                    })
                    .ifFailureFail();
                }
            );
        }
    }

    @Inject CommandLogEntryRepository<CommandLogEntry> commandLogEntryRepository;
    @Inject CommandExecutorService commandExecutorService;
    @Inject TransactionService transactionService;
    @Inject InteractionService interactionService;
    @Inject ClockService clockService;

}
