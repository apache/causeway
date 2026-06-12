package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "succeededOrExcluded",
        semantics = SemanticsOf.IDEMPOTENT,
        domainEvent = CommandReplayManager_deleteSelectedSucceededOrExcluded.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "succeededOrExcluded",
        named = "Delete Selected",
        describedAs = "Deletes selected Commands (cannot be undone)"
)
@RequiredArgsConstructor
public class CommandReplayManager_deleteSelectedSucceededOrExcluded {

    public static class DomainEvent extends CommandReplayManager.ActionDomainEvent<CommandReplayManager_deleteSelectedSucceededOrExcluded> { }

    private final CommandReplayManager commandReplayManager;

    public CommandReplayManager act(final List<ReplayableCommand> selected) {
        selected.stream()
                .forEach(ReplayableCommand::deleteObj); // filtered on its own responsibility
        return commandReplayManager;
    }

    @MemberSupport
    public String disableAct() {
        return commandReplayManager.getSucceededOrExcluded().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected != null && selected.isEmpty() ? "Select at least one command" : null;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandReplayManager.getSucceededOrExcluded();
    }
}
