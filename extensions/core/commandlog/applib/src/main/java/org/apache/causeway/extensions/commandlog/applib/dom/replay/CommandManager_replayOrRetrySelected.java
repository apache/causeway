package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandManager_replayOrRetrySelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.2",
        cssClass = "btn-secondary",
        cssClassFa = "solid forward",
        describedAs = "Executes the list of commands in sequence, each in their own transaction.  Note that there is no checking of the 'knownParticipants', so review first."
)
@RequiredArgsConstructor
public class CommandManager_replayOrRetrySelected {

    public static class DomainEvent extends CommandManager.ActionDomainEvent<CommandManager_replayOrRetrySelected> { }

    private final CommandManager commandManager;

    @MemberSupport
    public CommandManager act(final List<ReplayableCommand> selected) {
        if (ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
            return commandManager;
        }

        var replayables = selected.stream()
                .sorted()
                .collect(Collectors.toList());
        for (var replayableCommand : replayables) {
            var tryReplayOrRetry = replayableCommand.tryReplayOrRetry(); // filtered on its own responsibility
            if (tryReplayOrRetry.isFailure()
                    || ReplayPendingBackgroundCommands.hasPendingBackgroundCommands(commandManager.replayContext())) {
                return commandManager; // stop further execution
            }
        }
        return commandManager;
    }


    @MemberSupport
    public String disableAct() {
        var pendingBackgroundCommandsReason = ReplayPendingBackgroundCommands.disableReason(commandManager.replayContext());
        if (pendingBackgroundCommandsReason != null) {
            return pendingBackgroundCommandsReason;
        }
        return commandManager.getPendingOrFailed().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected != null && selected.isEmpty() ? "Select at least one command" : null;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandManager.getPendingOrFailed();
    }
}
