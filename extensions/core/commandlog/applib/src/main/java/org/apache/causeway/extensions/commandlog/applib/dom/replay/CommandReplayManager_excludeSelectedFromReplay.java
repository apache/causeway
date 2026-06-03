package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "pendingOrFailed",
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandReplayManager_excludeSelectedFromReplay.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "pendingOrFailed", sequence = "1.3",
        cssClass = "btn-secondary",
        describedAs = "Marks selected Commands to be EXCLUDED from replay"
)
@RequiredArgsConstructor
public class CommandReplayManager_excludeSelectedFromReplay {

    public static class DomainEvent extends CommandReplayManager.ActionDomainEvent<CommandReplayManager_excludeSelectedFromReplay> { }

    private final CommandReplayManager commandReplayManager;

    @MemberSupport
    public CommandReplayManager act(final List<ReplayableCommand> selected) {
        selected.stream()
                .forEach(ReplayableCommand::excludeFromReplay); // filtered on its own responsibility
        return commandReplayManager;
    }

    @MemberSupport
    public String disableAct() {
        return commandReplayManager.sizePendingOrFailed() == 0 ? "No commands in collection" : null;
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
