package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        choicesFrom = "commands",
        commandPublishing = Publishing.DISABLED,
        semantics = SemanticsOf.IDEMPOTENT,
        domainEvent = CommandExportManager_makeSelectedExportable.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        associateWith = "commands", sequence = "2.1",
        describedAs = "Makes selected Commands exportable (again)"
)
@RequiredArgsConstructor
public class CommandExportManager_makeSelectedExportable {

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_makeSelectedExportable> { }

    private final CommandExportManager commandExportManager;

    @MemberSupport
    public CommandExportManager act(final List<ReplayableCommand> selected) {
        selected.forEach(ReplayableCommand::makeExportable); // filtered on its own responsibility
        return commandExportManager;
    }

    @MemberSupport
    public String disableAct() {
        return commandExportManager.getCommands().isEmpty() ? "No commands in collection" : null;
    }

    @MemberSupport
    public String validateSelected(final List<ReplayableCommand> selected) {
        return selected != null && selected.isEmpty() ? "Select at least one command" : null;
    }

    // TODO: shouldn't be required because of 'choicesFrom', but in v2 there seems to be a MM validation error due to a missing choicesFacet
    @MemberSupport
    public List<ReplayableCommand> choicesSelected() {
        return commandExportManager.getCommands();
    }
}
