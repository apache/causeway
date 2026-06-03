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
        domainEvent = CommandReplayManager_replayOrRetrySelected.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.2",
        cssClass = "btn-secondary",
        cssClassFa = "solid forward",
        describedAs = "Executes the list of commands in sequence, after having sorted them by their timestamp. "
                + "If any of the given commands fails, "
                + "its surrounding transaction is rolled back, but any successful commands so far are marked OK). "
                + "The command, that caused the failure, gets marked FAILED.")
@RequiredArgsConstructor
public class CommandReplayManager_replayOrRetrySelected {

    public static class DomainEvent extends CommandReplayManager.ActionDomainEvent<CommandReplayManager_replayOrRetrySelected> { }

    private final CommandReplayManager commandReplayManager;

    @MemberSupport
    public CommandReplayManager act(final List<ReplayableCommand> selected) {
        var replayables = selected.stream()
                .sorted()
                .collect(Collectors.toList());
        for (var replayableCommand : replayables) {
            var tryReplayOrRetry = replayableCommand.tryReplayOrRetry(); // filtered on its own responsibility
            if (tryReplayOrRetry.isFailure()) {
                return commandReplayManager; // stop further execution
            }
        }
        return commandReplayManager;
    }


    @MemberSupport
    public String disableAct() {
        return commandReplayManager.getPendingOrFailed().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected != null && selected.isEmpty() ? "Select at least one command" : null;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandReplayManager.getPendingOrFailed();
    }
}
