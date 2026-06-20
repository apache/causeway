package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.apache.causeway.applib.annotation.*;

import org.jspecify.annotations.NonNull;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_replayOrRetrySelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary",
        describedAs = "Executes the oldest command.")
@RequiredArgsConstructor
public class CommandManager_replayOrRetryNext {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_replayOrRetrySelected> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act() {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
            return commandManager;
        }

        var nextIfAny = commandManager.streamPendingOrFailed().findFirst();
        commandInSequence(nextIfAny)
                .filter(ReplayableCommand::isKnownParticipants)
                .ifPresent(ReplayableCommand::tryReplayOrRetry);
        return commandManager;
    }

    /**
     * We lookup the command found in the context of the commands for export,
     * because we need to check isKnownParticipants
     */
    private @NonNull Optional<ReplayableCommand> commandInSequence(Optional<ReplayableCommand> nextIfAny) {
        final var allCommands = commandManager.getCommandsInSequence();
        return nextIfAny.stream()
                .flatMap(next -> allCommands.stream().filter(command -> command.getInteractionId().equals(next.getInteractionId())))
                .findFirst();
    }

    @MemberSupport
    public String disableAct() {
        final var pendingBackgroundCommandsReason = ReplayPendingBackgroundCommands.disableReason(commandManager.replayContext());
        if (pendingBackgroundCommandsReason != null) {
            return pendingBackgroundCommandsReason;
        }
        final var nextIfAny = commandManager.streamPendingOrFailed().findFirst();
        if(nextIfAny.isEmpty()) {
            return "No commands to execute";
        }
        final var commandInSequenceIfAny = commandInSequence(nextIfAny);
        if(commandInSequenceIfAny.isEmpty()) {
            // shouldn't happen
            return "Unable to find command in sequence (in order to check its known participants)";
        }
        if (commandInSequenceIfAny
                .filter(ReplayableCommand::isKnownParticipants)
                .isEmpty()) {
            return "Unknown participants (target and/or action args)";
        }
        return null;
    }
}
