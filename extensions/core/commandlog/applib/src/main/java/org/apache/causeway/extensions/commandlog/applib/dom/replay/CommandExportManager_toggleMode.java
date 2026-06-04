package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

import static org.apache.causeway.extensions.commandlog.applib.dom.replay.CommandExportManager.Direction.NEXT;

@Action(
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = CommandExportManager_toggleMode.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "1.1",
        cssClass = "btn-secondary",
        describedAs = "Toggle between exporting and un-exporting."
)
@RequiredArgsConstructor
public class CommandExportManager_toggleMode {

    public static class DomainEvent extends CommandExportManager.ActionDomainEvent<CommandExportManager_toggleMode> { }

    private final CommandExportManager commandExportManager;

    @MemberSupport
    public CommandExportManager act() {
        return commandExportManager.withMode(commandExportManager.getMode().toggle());
    }

}
